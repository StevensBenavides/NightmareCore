package com.nightmare;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nightmare.Mobs.MobTasks;

import net.md_5.bungee.api.ChatColor;

public final class Main extends JavaPlugin {
  private static Main instance;

  public static YamlConfiguration settings;
  public static YamlConfiguration messages;

  public void onEnable() {

    instance = this;

    {
      Recipes.registerRecipes();
    }

    DisplayStartMessage();

    IO.InitConfigFiles(instance);
    IO.LoadConfigFiles(instance);

    {
      getServer().getPluginManager().registerEvents(new Events(), instance);
      getCommand("nightmare").setExecutor(new Commands());
    }

    TimeManagement.init(instance);
        
    File settings = new File(getDataFolder(), "settings.yml");

    if (!settings.exists()) {

      try {
        throw new Exception("settings.yml does not exist.");
      } catch (Exception e) {
        getServer().getPluginManager().disablePlugin(instance);
        e.printStackTrace();
      }
      
    }

    YamlConfiguration config = YamlConfiguration.loadConfiguration(settings);  

    if (config.get("scoreboard.enable") == null) {
      try {
        throw new Exception("scoreboard.enable not found in settings.yml.");
      } catch (Exception e) {
        getServer().getPluginManager().disablePlugin(instance);
        e.printStackTrace();
      }

    } else if (config.getBoolean("scoreboard.enable")) {
      NighmareScoreboard scoreboard = new NighmareScoreboard();
      scoreboard.startScoreboard();
    }

    if (config.get("tablist.enable") == null) {

      try {
        throw new Exception("tablist.enable not found in settings.yml.");
      } catch (Exception e) {
        getServer().getPluginManager().disablePlugin(instance);
        e.printStackTrace();
      }

    } else if (config.getBoolean("tablist.enable")) {

      Tablist tablist = new Tablist();

      try {
        tablist.startTablist();
      } catch (IOException e) {
        getServer().getPluginManager().disablePlugin(instance);
        e.printStackTrace();
      }

    }

    {
      PlayerEffects.startPlayersEffects(instance);
      WorldDifficulty.startWorldDifficulty(instance);
      MobTasks.startCTierMobEffects(instance);
      MobTasks.startIdleTasks(instance);
    }

  }

  public void onDisable() {
    getServer().getScheduler().cancelTasks(instance);
    DisplayStopMessage();
  }

  public static Plugin getInstance() {
    return instance;
  }

  public static YamlConfiguration getSettings() {
    return settings;
  }

  public static YamlConfiguration getMessages() {
    return messages;
  }

  public static void setSettings() throws IOException {

    File file = new File(instance.getDataFolder(), "settings.yml");

    if (!file.exists())
      throw new IOException("settings.yml does not exist.");

    settings = YamlConfiguration.loadConfiguration(file);

  }

  public static void setMessages() throws IOException {
    
    File file = new File(instance.getDataFolder(), "messages.yml");

    if (!file.exists())
      throw new IOException("messages.yml does not exist.");

    messages = YamlConfiguration.loadConfiguration(file);  

  }

  private void DisplayStartMessage() {
    Bukkit.getConsoleSender().sendMessage("");
    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lNighmare Core &7| &aON"));
    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&fCreated by &bStevens Benavides"));
    Bukkit.getConsoleSender().sendMessage("");
  }

  private void DisplayStopMessage() {
    Bukkit.getConsoleSender().sendMessage("");
    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lNighmare Core &7| &cOFF"));
    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&fCreated by &bStevens Benavides"));
    Bukkit.getConsoleSender().sendMessage("");
  }

}