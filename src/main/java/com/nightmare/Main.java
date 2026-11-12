package com.nightmare;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nightmare.command.NightmareCommand;
import com.nightmare.core.DifficultyService;
import com.nightmare.core.InsomniaService;
import com.nightmare.core.NightmarePackService;
import com.nightmare.enchant.AbyssalCoreService;
import com.nightmare.enchant.EnchantAnvilService;
import com.nightmare.enchant.EnchantBookService;
import com.nightmare.enchant.EnchantCraftService;
import com.nightmare.enchant.EnchantService;
import com.nightmare.gui.GuiListener;
import com.nightmare.gui.StoryService;
import com.nightmare.item.ArmorTrimService;
import com.nightmare.lang.LangService;
import com.nightmare.mob.MobChunkService;
import com.nightmare.mob.MobLootService;
import com.nightmare.mob.MobProfileLoader;
import com.nightmare.mob.MobSpawnService;
import com.nightmare.mob.MobTargetingService;
import com.nightmare.mob.MobTaskService;
import com.nightmare.mob.NametagService;
import com.nightmare.mob.PhaseAssaultService;
import com.nightmare.player.BackpackService;
import com.nightmare.player.GraveService;
import com.nightmare.player.PermanentItemService;
import com.nightmare.relic.AbyssalTendrilsService;
import com.nightmare.relic.RelicCraftService;
import com.nightmare.relic.RelicRecipeService;
import com.nightmare.relic.SoulHeartService;
import com.nightmare.relic.TendrilsEffects;
import com.nightmare.relic.WhiteExplosionEffect;
import com.nightmare.world.GodDropService;
import com.nightmare.world.StructureService;

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

    LangService.init(instance);
    MobProfileLoader.load(instance);
    EnchantBookService.registerRecipes(instance);
    TendrilsEffects.init(instance);
    WhiteExplosionEffect.init(instance);
    NightmarePackService.register(instance);
    RelicRecipeService.register(instance);

    {
      getServer().getPluginManager().registerEvents(new Events(), instance);
      getServer().getPluginManager().registerEvents(new EnchantService(), instance);
      getServer().getPluginManager().registerEvents(new EnchantAnvilService(), instance);
      getServer().getPluginManager().registerEvents(new EnchantCraftService(), instance);
      getServer().getPluginManager().registerEvents(new MobSpawnService(), instance);
      getServer().getPluginManager().registerEvents(new MobLootService(), instance);
      getServer().getPluginManager().registerEvents(new MobChunkService(), instance);
      getServer().getPluginManager().registerEvents(new NametagService(), instance);
      getServer().getPluginManager().registerEvents(new MobTargetingService(), instance);
      getServer().getPluginManager().registerEvents(new AbyssalCoreService(), instance);
      getServer().getPluginManager().registerEvents(new AbyssalTendrilsService(), instance);
      getServer().getPluginManager().registerEvents(new SoulHeartService(), instance);
      getServer().getPluginManager().registerEvents(new RelicCraftService(), instance);
      
      SoulHeartService.start(instance);
      
      getServer().getPluginManager().registerEvents(new ArmorTrimService(), instance);
      getServer().getPluginManager().registerEvents(new GuiListener(), instance);
      getServer().getPluginManager().registerEvents(new StoryService(), instance);
      getServer().getPluginManager().registerEvents(new StructureService(), instance);

      StoryService.load(instance);
      StoryService.start(instance);
      StructureService.load(instance);
      StructureService.start(instance);
      GodDropService.start(instance);
      BackpackService.start(instance);
      PermanentItemService.start(instance);
      GraveService.start(instance);
      NightmareCommand nightmareCommand = new NightmareCommand();

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

    if (config.getBoolean("scoreboard.enable")) {
      NightmareScoreboard scoreboard = new NightmareScoreboard();
      scoreboard.startScoreboard();
    }

    if (config.getBoolean("tablist.enable")) {

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
      
      getServer().getScheduler().runTaskTimer(instance, MobTaskService::tick, 20L, 20L);
      getServer().getPluginManager().registerEvents(new DifficultyService(), instance);
      getServer().getScheduler().runTaskTimer(instance, () -> {
        for (World world : getServer().getWorlds()) {
          DifficultyService.apply(world);
        }
      }, 0L, 600L);
      getServer().getScheduler().runTaskTimer(instance, EnchantService::tick, 20L, 20L);

      PhaseAssaultService phaseAssault = new PhaseAssaultService();
      
      phaseAssault.start(instance);
    
      InsomniaService.start(instance);
    }

  }

  public void onDisable() {
    getServer().getScheduler().cancelTasks(instance);
    
    MobTaskService.ACTIVE.clear();
    MobTaskService.SUMMON_EXPIRY.clear();
    NametagService.VISIBLE_UNTIL.clear();
    PhaseAssaultService.shutdown();
    InsomniaService.shutdown();
    EnchantService.PROCESSING_HIT.clear();
    EnchantService.PROCESSING_DAMAGED.clear();
    AbyssalTendrilsService.shutdown();
    SoulHeartService.shutdown();
    TendrilsEffects.shutdown();
    WhiteExplosionEffect.shutdown();
    NightmarePackService.shutdown();
    StructureService.save();
    GodDropService.shutdown();
    BackpackService.shutdown();
    GraveService.shutdown();
    PlayerEffects.RANDOM_EFFECT_COOLDOWN.clear();

    DisplayStopMessage();
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

  public static Plugin getInstance() {
    return instance;
  }

  public static YamlConfiguration getSettings() {
    return settings;
  }

  public static YamlConfiguration getMessages() {
    return messages;
  }

}