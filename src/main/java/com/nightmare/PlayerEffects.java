package com.nightmare;

import java.util.List;
import java.util.Optional;

import org.bukkit.GameMode;
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
            public void run() {
                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {

                        if (player.getGameMode() == GameMode.SURVIVAL) {

                            Optional<PlayerTime> currentPlayerTime = Main.getTimeManagement().getSpecificPlayerTime(player.getUniqueId());
                            Optional<WorldTime> currentWorldTime = Main.getTimeManagement().getSpecificWorldTime(world.getName());

                            if (currentPlayerTime.isPresent() && currentWorldTime.isPresent()) {

                                WorldTime worldTime = currentWorldTime.get();
                                PlayerTime playerTime = currentPlayerTime.get();
                                Long Minutes = playerTime.getMinutes();

                                if (Minutes >= 60) {

                                    {
                                        RandomnessManagement random = new RandomnessManagement();
                                        
                                        if (random.is5percent()) {
                                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 60 * 2, random.random(1, 5)));
                                        }

                                    }


                                    if (worldTime.isDayAbove50()) {
                                        {
                                            RandomnessManagement random = new RandomnessManagement();
                                            
                                            if (random.is5percent()) {
                                                player.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, 20 * 60 * 2, random.random(1, 5)));
                                            }
    
                                        }
                                    }

                                    {
                                        RandomnessManagement random = new RandomnessManagement();
                                        
                                        if (random.is5percent()) {
                                            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 60 * 2, random.random(1, 5)));
                                        }

                                    }

                                }

                            }
                        }

                    }
                }
            }
        }.runTaskTimer(plugin, 0, 6000L);


    }
    
}
