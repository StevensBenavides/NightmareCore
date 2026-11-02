package com.nightmare;

import java.util.ArrayList;
import java.util.Date;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;


import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public final class Events implements Listener {

    private final Plugin plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {

        final YamlConfiguration config = Main.getSettings();

        try {

            if (config.getBoolean("scoreboard.enable")) {

                FastBoard board = new FastBoard(event.getPlayer());

                board.updateTitle(ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.name")));

                NightmareScoreboard.boards.put(event.getPlayer().getUniqueId(), board);

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
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.8F, 1.0F);

            }

            if (config.getBoolean("config.player.display_health")) {

                ScoreboardManager manager = event.getPlayer().getServer().getScoreboardManager();
                Scoreboard score = manager.getNewScoreboard();

                Objective obj_health_below = score.registerNewObjective("player_health", Criteria.HEALTH, ChatColor.translateAlternateColorCodes('&', "&c♥"));

                obj_health_below.setDisplaySlot(DisplaySlot.BELOW_NAME);

                event.getPlayer().setScoreboard(score);
                event.getPlayer().damage(0.001F);

                Objective obj_health_tab = score.registerNewObjective("player_health_tab", Criteria.HEALTH, ChatColor.translateAlternateColorCodes('&', "&c♥"));

                obj_health_tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                obj_health_tab.setRenderType(RenderType.HEARTS);

                event.getPlayer().setScoreboard(score);
                event.getPlayer().damage(0.001F);

            } 

            {
                Player player = event.getPlayer();
                UUID playerUUID = player.getUniqueId();
    
                TimeManagement.addPlayerTimeIfAbsent(playerUUID);

                TimeManagement.getSpecificPlayerTime(playerUUID).ifPresent(playerTime -> {

                    if (player.getGameMode() == GameMode.SURVIVAL && playerTime.getThirstyMinutes() >= 15) {

                        player.setMetadata("thristy", new FixedMetadataValue(plugin, true));

                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
                    }
                });
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

            if (config.getBoolean("scoreboard.enable")) {
                NightmareScoreboard.boards.remove(event.getPlayer().getUniqueId());
            }
            
            if (config.getBoolean("join_and_leave.enable")) {

                String message = config.getString("prefix") + config.getString("join_and_leave.leave");

                Pattern pattern = Pattern.compile("%.+?%");
                Matcher matcher = pattern.matcher(message);

                if (matcher.find())
                    message = message.replace(matcher.group(), PlaceholderAPI.setPlaceholders(event.getPlayer(), matcher.group()));

                event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', message));

            }

        } catch (Exception e) {
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }    

    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemConsumeEvent(PlayerItemConsumeEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        {
            if (player.getGameMode() == GameMode.SURVIVAL) {

                final Optional<WorldTime> optionalWorldTime = TimeManagement.getSpecificWorldTime(player.getWorld().getName());

                if (optionalWorldTime.isPresent()) {
                    final WorldTime worldTime = optionalWorldTime.get();
                    final Randomness random = new Randomness();

                    if (worldTime.isDayBelow(20)) {
                        if (random.is5percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    } else if (worldTime.isDayBelow(40)) {
                        if (random.is15percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    }  else if (worldTime.isDayBelow50()) {
                        if (random.is15percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    }
                    
                    if (worldTime.isDayAbove(50)) {
                        if (random.is15percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    }

                }
                

            }

            if (event.isCancelled()) {
                return;
            }

            if (player.getGameMode() == GameMode.SURVIVAL) {

                if (item.getType() == Material.POTION || item.getType() == Material.GLASS_BOTTLE) {

                    if (item.getItemMeta() instanceof PotionMeta meta) {
    
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
            
                                Optional<PlayerTime> currentPlayerTime = TimeManagement.getSpecificPlayerTime(uuid);
            
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

    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerPing(ServerListPingEvent event) {

        try {
            final YamlConfiguration config = Main.getSettings();

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {

        Player player = event.getEntity();

        player.setMetadata("thristy", new FixedMetadataValue(plugin, false));

        {
            UUID uuid = player.getUniqueId();
            Optional<PlayerTime> currentPlayerTime = TimeManagement.getSpecificPlayerTime(uuid);

            if (currentPlayerTime.isPresent()) {
                PlayerTime playerTime = currentPlayerTime.get();
                playerTime.resetThirstyMinutes();
            }
        }

        {

            try {
                
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    final YamlConfiguration config = Main.getSettings();    
                    final boolean isDeathMessageEnabled = config.getBoolean("message_death.enable");
    
                    if (isDeathMessageEnabled) {
                        String titleMessage = config.getString("message_death.title");
                        String subtitleMessage = config.getString("message_death.subtitle");
                        
                        titleMessage = titleMessage.replace("%player_killed%", player.getName());
                        subtitleMessage = subtitleMessage.replace("%player_killed%", player.getName());
                        
                        for (Player outSidePlayer : player.getServer().getOnlinePlayers()) {
                            if (outSidePlayer == null) {
                                continue;
                            }

                            {
                                Pattern pattern = Pattern.compile("%.+?%");
                                Matcher matcher = pattern.matcher(titleMessage);
        
                                if (matcher.find()) {
                                    titleMessage = ChatColor.translateAlternateColorCodes('&', titleMessage.replace(matcher.group(), PlaceholderAPI.setPlaceholders(outSidePlayer, matcher.group())));
                                }
                    
                            }

                            {
                                Pattern pattern = Pattern.compile("%.+?%");
                                Matcher matcher = pattern.matcher(subtitleMessage);
        
                                if (matcher.find()) {
                                    subtitleMessage = ChatColor.translateAlternateColorCodes('&', subtitleMessage.replace(matcher.group(), PlaceholderAPI.setPlaceholders(outSidePlayer, matcher.group())));
                                }
                    
                            }
    
                            outSidePlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', titleMessage), ChatColor.translateAlternateColorCodes('&', subtitleMessage), 10, 40, 10);
    
                            outSidePlayer.playSound(
                                outSidePlayer.getLocation(), 
                                Sound.ITEM_TRIDENT_RETURN, 
                                0.9f,   
                                1.0f   
                            );
    
                            outSidePlayer.playSound(
                                outSidePlayer.getLocation(), 
                                Sound.ITEM_TOTEM_USE, 
                                0.8f,   
                                1.0f   
                            );
                        
                        }
    
                    }

                    final String worldName = player.getWorld().getName();
                    final Optional<WorldTime> worldTimeOpt = TimeManagement.getSpecificWorldTime(worldName);

                    if (worldTimeOpt.isPresent()) {
                        WorldTime worldTime = worldTimeOpt.get();
                        long day = worldTime.getDay();
                        int minutes;

                        if (day < 27) {
                            minutes = Main.balance.getInt("death-ban.days-0-26", 5);
                        } else if (day < 54) {
                            minutes = Main.balance.getInt("death-ban.days-27-53", 10);
                        } else if (day < 81) {
                            minutes = Main.balance.getInt("death-ban.days-54-80", 15);
                        } else {
                            minutes = Main.balance.getInt("death-ban.days-81-100", 20);
                        }

                        Date expirationDate = new Date(System.currentTimeMillis() + minutes * 60L * 1000L);

                        if (!player.hasPermission("nightmare.bypass.deathban")) {
                            player.ban("You fall into the nightmare.", expirationDate, "NightmareCore", true);
                        }
                    }
                                    }

            } catch (Exception e) {
                e.printStackTrace();
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }    
            
        }
        
    }

}
