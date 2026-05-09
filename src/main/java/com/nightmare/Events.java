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
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

            if (player.getGameMode() == GameMode.SURVIVAL) {

                final TimeManagement timeManagement = Main.getTimeManagement();
                final Optional<WorldTime> optionalWorldTime = timeManagement.getSpecificWorldTime(player.getWorld().getName());

                if (optionalWorldTime.isPresent()) {
                    final WorldTime worldTime = optionalWorldTime.get();
                    final RandomnessManagement random = new RandomnessManagement();

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnEvent(EntitySpawnEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();
        
            if (event.getEntityType() == EntityType.ZOMBIE) 
                Mobs.spawnNightmareZombie(event.getEntity(), config);
            
            if (event.getEntityType() == EntityType.CREEPER) 
                Mobs.spawnNightmareCreeper(event.getEntity(), config);
            
            if (event.getEntityType() == EntityType.SPIDER) 
                Mobs.spawnNightmareSpider(event.getEntity(), config);

            if (event.getEntityType() == EntityType.SKELETON) 
                Mobs.spawnNightmareSkeleton(event.getEntity(), config);

            if (event.getEntityType() == EntityType.ENDERMAN) 
                Mobs.spawnNightmareEnderman(event.getEntity(), config);

            if (event.getEntityType() == EntityType.VILLAGER) 
                Mobs.spawnNightmareVillager(event.getEntity(), config);
        
        } catch (Exception e) {
        
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        
        }
        
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityShootArrow(EntityShootBowEvent event) {

        if (event.getBow().getItemMeta().getPersistentDataContainer() != null && event.getEntityType() == EntityType.SKELETON)  {

            final PersistentDataContainer pdc = event.getBow().getItemMeta().getPersistentDataContainer();

            if (pdc.get(new NamespacedKey(Main.getInstance(), "NightmareSkeletonCTierBow"), PersistentDataType.BOOLEAN) != null) {

                Skeleton mob = (Skeleton) event.getEntity();

                if (mob.getTarget() != null && mob.getTarget() instanceof Player) {
                    mob.getWorld().createExplosion(event.getProjectile().getLocation(), 1.5F);
                }
            }

        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();

            final String c_tier = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", event.getEntity().getClass().getSimpleName()));

            if (event.getEntity() != null) {
                if (event.getEntityType() == EntityType.ZOMBIE && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(c_tier))
                    event.getDrops().clear();
    
                else if (event.getEntityType() == EntityType.SKELETON && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(c_tier))
                    event.getDrops().clear();
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

        {

            try {
                
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    final YamlConfiguration config = Main.getSettings();    
                    final boolean isDeathMessageEnabled = config.getBoolean("message_death.enable");
    
                    if (isDeathMessageEnabled) {
                        String originalMessage = config.getString("message_death.message");
    
                        originalMessage = originalMessage.replace("%player_killed%", player.getName());
    
                        for (Player outSidePlayer : player.getServer().getOnlinePlayers()) {
                            if (outSidePlayer == null) {
                                continue;
                            }
    
                            Pattern pattern = Pattern.compile("%.+?%");
                            Matcher matcher = pattern.matcher(originalMessage);
    
                            if (matcher.find()) {
                                originalMessage = ChatColor.translateAlternateColorCodes('&', originalMessage.replace(matcher.group(), PlaceholderAPI.setPlaceholders(outSidePlayer, matcher.group())));
                            }
    
                            outSidePlayer.sendTitle(originalMessage, null, 10, 40, 10);
    
                            outSidePlayer.playSound(
                                outSidePlayer.getLocation(), 
                                Sound.ITEM_TRIDENT_RETURN, 
                                12.0f,   
                                1.0f   
                            );
    
                            outSidePlayer.playSound(
                                outSidePlayer.getLocation(), 
                                Sound.ITEM_TOTEM_USE, 
                                8.0f,   
                                1.0f   
                            );
                        
                        }
    
                    }

                    final TimeManagement timeManagement = Main.getTimeManagement();
                    final String worldName = player.getWorld().getName();
                    final Optional<WorldTime> worldTimeOpt = timeManagement.getSpecificWorldTime(worldName);

                    if (worldTimeOpt.isPresent()) {
                        WorldTime worldTime = worldTimeOpt.get();
                        Date expirationDate = null; 

                        if (worldTime.isDayBelow(50)) {
                            long hoursToBan = 24; 
                            expirationDate = new Date(System.currentTimeMillis() + (hoursToBan * 3600000));
                        } 
                    
                        player.ban("You fall into the nightmare.", expirationDate, "NightmareCore", true);
                    
                    }
                                    }

            } catch (Exception e) {
        
                e.printStackTrace();
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            
            }    



        }
        
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onToolBreaking(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        Main.getTimeManagement().getSpecificWorldTime(player.getWorld().getName()).ifPresent(worldTime -> {
            RandomnessManagement random = new RandomnessManagement();

            if (worldTime.isDayAbove(50) && worldTime.isDayBelow(101)) {
                if (random.is1percent()) { 
                    item.setAmount(0);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.5f);
                }
            } else if (worldTime.isDayAbove(100)) {
                if (random.is5percent()) {
                    item.setAmount(0);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.2f);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isSprinting()) return; 

        Main.getTimeManagement().getSpecificWorldTime(player.getWorld().getName()).ifPresent(worldTime -> {
            RandomnessManagement random = new RandomnessManagement();

            if (worldTime.isDayAbove(50)) {
                if (random.is1percent()) { 
                    ItemStack boots = player.getInventory().getBoots();

                    if (boots != null && boots.getItemMeta() instanceof Damageable meta) {
                        meta.setDamage(meta.getDamage() + 10);
                        boots.setItemMeta(meta);
                        player.damage(1.0);
                    }
                
                }
            }
        });
    }

}
