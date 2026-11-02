package com.nightmare;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class Main extends JavaPlugin {
  private static Main instance;

  public static YamlConfiguration settings;
  public static YamlConfiguration messages;
  public static YamlConfiguration enchants;
  public static YamlConfiguration balance;
  public static YamlConfiguration enchantLore;

  public void onEnable() {

    instance = this;

    DisplayStartMessage();

    IO.InitConfigFiles(instance);
    IO.LoadConfigFiles(instance);

    saveResource("enchants.yml", false);
    saveResource("balance.yml", false);
    saveResource("mobs.yml", false);
    saveResource("enchant-lore.yml", false);
    saveResource("story.yml", false);
    saveResource("structures.yml", false);

    enchants = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "enchants.yml"));
    balance = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "balance.yml"));
    enchantLore = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "enchant-lore.yml"));
    com.nightmare.mob.MobProfileLoader.load(instance);
    com.nightmare.enchant.EnchantBookService.registerRecipes(instance);
    com.nightmare.relic.TendrilsEffects.init(instance);
    com.nightmare.relic.WhiteExplosionEffect.init(instance);
    com.nightmare.core.NightmarePackService.register(instance);
    com.nightmare.relic.RelicRecipeService.register(instance);

    {
      getServer().getPluginManager().registerEvents(new Events(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.enchant.EnchantService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.enchant.EnchantAnvilService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.enchant.EnchantCraftService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.enchant.EnchantMigrationService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.mob.MobSpawnService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.mob.MobLootService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.mob.MobChunkService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.mob.NametagService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.mob.MobTargetingService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.enchant.AbyssalCoreService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.relic.AbyssalTendrilsService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.relic.SoulHeartService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.relic.RelicCraftService(), instance);
      com.nightmare.relic.SoulHeartService.start(instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.item.ArmorTrimService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.gui.GuiListener(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.gui.StoryService(), instance);
      getServer().getPluginManager().registerEvents(new com.nightmare.world.StructureService(), instance);
      com.nightmare.gui.StoryService.load(instance);
      com.nightmare.gui.StoryService.start(instance);
      com.nightmare.world.StructureService.load(instance);
      com.nightmare.world.StructureService.start(instance);
      com.nightmare.command.NightmareCommand nightmareCommand = new com.nightmare.command.NightmareCommand();

      getCommand("nightmare").setExecutor(nightmareCommand);

      getCommand("nightmare").setTabCompleter(nightmareCommand);
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
      NightmareScoreboard scoreboard = new NightmareScoreboard();
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
      getServer().getScheduler().runTaskTimer(instance, com.nightmare.mob.MobTaskService::tick, 20L, 20L);
      getServer().getPluginManager().registerEvents(new com.nightmare.core.DifficultyService(), instance);
      getServer().getScheduler().runTaskTimer(instance, () -> {
        for (org.bukkit.World world : getServer().getWorlds()) {
          com.nightmare.core.DifficultyService.apply(world);
        }
      }, 0L, 600L);
      getServer().getScheduler().runTaskTimer(instance, com.nightmare.enchant.EnchantService::tick, 20L, 20L);
      com.nightmare.mob.PhaseAssaultService phaseAssault = new com.nightmare.mob.PhaseAssaultService();
      phaseAssault.start(instance);
    }

  }

  public void onDisable() {
    getServer().getScheduler().cancelTasks(instance);
    com.nightmare.mob.MobTaskService.ACTIVE.clear();
    com.nightmare.mob.MobTaskService.SUMMON_EXPIRY.clear();
    com.nightmare.mob.NametagService.VISIBLE_UNTIL.clear();
    com.nightmare.mob.PhaseAssaultService.cleanup();
    com.nightmare.enchant.EnchantService.PROCESSING_HIT.clear();
    com.nightmare.enchant.EnchantService.PROCESSING_DAMAGED.clear();
    com.nightmare.relic.AbyssalTendrilsService.cleanup();
    com.nightmare.relic.SoulHeartService.cleanup();
    com.nightmare.relic.TendrilsEffects.dispose();
    com.nightmare.relic.WhiteExplosionEffect.dispose();
    com.nightmare.core.NightmarePackService.dispose();
    com.nightmare.world.StructureService.save();
    com.nightmare.PlayerEffects.RANDOM_EFFECT_COOLDOWN.clear();
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