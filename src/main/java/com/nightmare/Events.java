package com.nightmare;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nightmare.FastBoard.FastBoard;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.nightmare.Mobs.Mobs;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public final class Events implements Listener {

    private final Plugin plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {

        final YamlConfiguration config = Main.getSettings();

        try {

            ConfigEvaluator.evaluate("onJoin", config);

            if (config.getBoolean("scoreboard.enable")) {

                FastBoard board = new FastBoard(event.getPlayer());

                board.updateTitle(ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.name")));

                NighmareScoreboard.boards.put(event.getPlayer().getUniqueId(), board);

            }

            if (config.getBoolean("join_and_leave.enable")) {

                String message = config.getString("prefix") + config.getString("join_and_leave.join");

                Pattern pattern = Pattern.compile("%.+?%");
                Matcher matcher = pattern.matcher(message);

                if (matcher.find()) 
                    message = message.replace(matcher.group(), PlaceholderAPI.setPlaceholders(event.getPlayer(), matcher.group()));

                event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', message));

            }

            if (config.getBoolean("message_join.enable")) {

                ArrayList<String> tempLines = new ArrayList<String>();

                for (String line : config.getStringList("message_join.lines")) {

                    Pattern pattern = Pattern.compile("%.+?%");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        tempLines.add(ChatColor.translateAlternateColorCodes('&', line.replace(matcher.group(), PlaceholderAPI.setPlaceholders(event.getPlayer(), matcher.group()))));
                        continue;
                    }    

                    tempLines.add(ChatColor.translateAlternateColorCodes('&', line));    

                }

                for (String line : tempLines) {
                    event.getPlayer().sendMessage(line);
                }

                event.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', config.getString("message_join.title")), null, 20, 20, 10);
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 80, 100);

            }

            if (config.getBoolean("config.player.display_health")) {

                ScoreboardManager manager = event.getPlayer().getServer().getScoreboardManager();
                Scoreboard score = manager.getNewScoreboard();

                Objective obj = score.registerNewObjective("player_health", Criteria.HEALTH, ChatColor.translateAlternateColorCodes('&', "&c♥"));

                obj.setDisplaySlot(DisplaySlot.BELOW_NAME);

                event.getPlayer().setScoreboard(score);
                event.getPlayer().damage(0.001F);

            } 

            {
                Player player = event.getPlayer();
                UUID playerUUID = player.getUniqueId();
    
                Main.getTimeManagement().addPlayerTime(playerUUID);
            }


        } catch (Exception e) {
            
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);

        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeave(PlayerQuitEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();

            ConfigEvaluator.evaluate("onLeave", config);

            if (config.getBoolean("scoreboard.enable")) {
                NighmareScoreboard.boards.remove(event.getPlayer().getUniqueId());
            }
            
            if (config.getBoolean("join_and_leave.enable")) {

                String message = config.getString("prefix") + config.getString("join_and_leave.leave");

                Pattern pattern = Pattern.compile("%.+?%");
                Matcher matcher = pattern.matcher(message);

                if (matcher.find())
                    message = message.replace(matcher.group(), PlaceholderAPI.setPlaceholders(event.getPlayer(), matcher.group()));

                event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', message));

            }

            {
                Player player = event.getPlayer();
                UUID playerUUID = player.getUniqueId();
    
                Main.getTimeManagement().removePlayerTime(playerUUID);
            }

        } catch (Exception e) {

            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);

        }    

    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {

        Player player = event.getEntity();

        player.setMetadata("thristy", new FixedMetadataValue(plugin, false));

        {
            UUID uuid = player.getUniqueId();
            Optional<PlayerTime> currentPlayerTime = Main.getTimeManagement().getSpecificPlayerTime(uuid);

            if (currentPlayerTime.isPresent()) {
                PlayerTime playerTime = currentPlayerTime.get();
                playerTime.resetThirstyMinutes();
            }
        }
        
    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemConsumeEvent(PlayerItemConsumeEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        {
            if (player.getGameMode() == GameMode.SURVIVAL) {

                if (item.getType() == Material.POTION || item.getType() == Material.GLASS_BOTTLE) {

                    PotionMeta meta = (PotionMeta) item.getItemMeta();
        
                    if (meta.getBasePotionType() == PotionType.WATER) {
    
                        if (player.hasMetadata("thristy")) {
    
                            boolean isThristy = false;
    
                            List<MetadataValue> metadataValueList = player.getMetadata("thristy");
                            Optional<MetadataValue> metadataValueOptionalWrapped = Optional.of(metadataValueList.get(0));
                    
                            if (metadataValueOptionalWrapped.isPresent()) {
                                MetadataValue metadataValue = metadataValueOptionalWrapped.get();
                                isThristy = metadataValue.asBoolean();
                            }
            
                            if (isThristy) {
                                UUID uuid = player.getUniqueId();
            
                                Optional<PlayerTime> currentPlayerTime = Main.getTimeManagement().getSpecificPlayerTime(uuid);
            
                                if (currentPlayerTime.isPresent()) {
                                    PlayerTime playerTime = currentPlayerTime.get();
                                    playerTime.resetThirstyMinutes();
                               
                                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                                    player.removePotionEffect(PotionEffectType.SLOWNESS);

                                    player.setMetadata("thristy", new FixedMetadataValue(plugin, false));   
                                }
                                
                            }
                        }
            
                    }
                    
                }

            }
        }

    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerPing(ServerListPingEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();

            ConfigEvaluator.evaluate("onServerPing", config);

            if (config.getBoolean("motd.enable")) {

                StringBuilder strMotd = new StringBuilder();

                for (String line : config.getStringList("motd.lines")) {
                    strMotd.append(ChatColor.translateAlternateColorCodes('&', line) + "\n");
                }

                event.setMotd(strMotd.toString());

            }

        } catch (Exception e) {

            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);

        }
        
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnEvent(EntitySpawnEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();
        
            if (event.getEntityType() == EntityType.ZOMBIE) {

                ConfigEvaluator.evaluate("onSpawnEvent", config);
                Mobs.spawnNightmareZombie(event.getEntity(), config);

            } else if (event.getEntityType() == EntityType.CREEPER) {

                ConfigEvaluator.evaluate("onSpawnEvent", config);
                Mobs.spawnNightmareCreeper(event.getEntity(), config);

            } else if (event.getEntityType() == EntityType.SPIDER) {

                ConfigEvaluator.evaluate("onSpawnEvent", config);
                Mobs.spawnNightmareSpider(event.getEntity(), config);

            } else if (event.getEntityType() == EntityType.SKELETON) {

                ConfigEvaluator.evaluate("onSpawnEvent", config);
                Mobs.spawnNightmareSkeleton(event.getEntity(), config);

            } else if (event.getEntityType() == EntityType.ENDERMAN) {

                ConfigEvaluator.evaluate("onSpawnEvent", config);

                Mobs.spawnNightmareEnderman(event.getEntity(), config);

            } 
        
        } catch (Exception e) {
        
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        
        }
        
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityShootArrow(EntityShootBowEvent event) {

        final String NightmareBow = ChatColor.translateAlternateColorCodes('&', "&c&lNightmare Bow &4☠");

        if (event.getBow() != null && event.getBow().getItemMeta().hasDisplayName() && event.getBow().getItemMeta().getDisplayName().equalsIgnoreCase(NightmareBow) && event.getEntityType() == EntityType.SKELETON) {

            Skeleton mob = (Skeleton) event.getEntity();

            if (mob.getTarget() != null && mob.getTarget() instanceof Player) {
                mob.getWorld().createExplosion(event.getProjectile().getLocation(), 1.5F);
            }

        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();

            ConfigEvaluator.evaluate("onSpawnEvent", config);

            final String c = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", event.getEntity().getClass().getSimpleName()));
            final String b = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", event.getEntity().getClass().getSimpleName()));

            if (event.getEntityType() == EntityType.ZOMBIE && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(c))

                event.getDrops().clear();

            else if (event.getEntityType() == EntityType.SKELETON && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(c))

                event.getDrops().clear();

            else if (event.getEntityType() == EntityType.SKELETON && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(b))

                event.getDrops().clear();

        } catch (Exception e) {
        
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        
        }    

    }

}
