package com.nightmare;

import java.util.List;
import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerEffects {

    public static void startPlayersEffects(Plugin plugin) {
        new PlayerEffects(plugin);
    }

    PlayerEffects(Plugin plugin) {

        new BukkitRunnable() {
            public void run() {

                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {

                        if (player.getGameMode() == GameMode.SURVIVAL) {

                            Optional<PlayerTime> currentPlayerTime = Main.getTimeManagement().getSpecificPlayerTime(player.getUniqueId());

                            if (currentPlayerTime.isPresent()) {
    
                                PlayerTime playerTime = currentPlayerTime.get();
                                Long thristyMinutes = playerTime.getThirstyMinutes();
    
                                if (!player.hasMetadata("thristy")) {
                                    player.setMetadata("thristy", new FixedMetadataValue(plugin, false));
                                }
    
                                boolean isPreviousThristy = false;
    
                                List<MetadataValue> metadataValueList = player.getMetadata("thristy");
                                Optional<MetadataValue> metadataValueOptionalWrapped = Optional.of(metadataValueList.get(0));
    
                                if (metadataValueOptionalWrapped.isPresent()) {
    
                                    MetadataValue metadataValueThristy = metadataValueOptionalWrapped.get();
                                    isPreviousThristy = metadataValueThristy.asBoolean();
    
                                }
    
                                if (thristyMinutes >= 15 && !isPreviousThristy) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 1));
                                    player.setMetadata("thristy", new FixedMetadataValue(plugin, true));
                                }
    
                            }

                        }

                    }
                }


            }
        }.runTaskTimer(plugin, 0, 300L);

        new BukkitRunnable() {
            public void run() {
                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {

                        if (player.getGameMode() == GameMode.SURVIVAL) {

                            if (player.hasMetadata("thristy")) {

                                boolean isThristy = false;
    
                                List<MetadataValue> metadataValueList = player.getMetadata("thristy");
                                Optional<MetadataValue> metadataValueOptionalWrapped = Optional.of(metadataValueList.get(0));
    
                                if (metadataValueOptionalWrapped.isPresent()) {
    
                                    MetadataValue metadataValueThristy = metadataValueOptionalWrapped.get();
                                    isThristy = metadataValueThristy.asBoolean();
    
                                }
    
                                if (isThristy) {
                                    double currentHealth = player.getHealth();
                                    
                                    Optional<WorldTime> currentWorldTime = Main.getTimeManagement().getSpecificWorldTime(world.getName());
                                    
                                    if (currentWorldTime.isPresent()) {
                                        WorldTime worldTime = currentWorldTime.get();

                                        if (worldTime.isDayAbove50()) {
                                            if (currentHealth > 2.5) {
                                                player.setHealth(currentHealth - 3.5);  
                                            } 
                                        } else {
                                            if (currentHealth > 2.5) {
                                                player.setHealth(currentHealth - 2.0);  
                                            } 
                                        }

                                    } else {
                                        if (currentHealth > 2.5) {
                                            player.setHealth(currentHealth - 2.0);  
                                        } 
                                    }
                                }
    
                            }

                        }

                    }
                }
            }
        }.runTaskTimer(plugin, 0, 80L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        if (player.getGameMode() != GameMode.SURVIVAL) continue;
        
                        Main.getTimeManagement().getSpecificPlayerTime(player.getUniqueId()).ifPresent(playerTime -> {
                            Main.getTimeManagement().getSpecificWorldTime(world.getName()).ifPresent(worldTime -> {
                                
                                long minutes = playerTime.getMinutes();
                                RandomnessManagement random = new RandomnessManagement();
        
                                if (minutes >= 60) {
                                    if (random.is5percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 60 * 2, random.random(1, 3)));
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 60 * 2, random.random(1, 2)));
                                    }
                                }
        
                                if (minutes >= 120) { 
                                    if (random.is10percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 60 * 5, 1));
                                    }
                                }
        
                                if (worldTime.isDayAbove(20) && !worldTime.isDayAbove(50)) {
                                    if (random.is5percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20 * 15, 0));
                                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.5f, 0.5f);
                                    }
                                }
        
                                if (worldTime.isDayAbove(50)) {
                                    if (random.is5percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, 20 * 60 * 10, random.random(1, 5)));
                                    }
        
                                    if (random.is10percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 30, 2));
                                    }
        
                                    if (random.is1percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 5, 1));
                                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.2f, 0.2f);
                                    }
                                }
        
                                if (worldTime.isDayAbove(100)) {
                                    if (random.is5percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 0));
                                    }
                                    
                                    if (random.is5percent()) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 60, 3));
                                    }
                                }
                            });
                        });
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 6000L);

    }
    
}
