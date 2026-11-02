package com.nightmare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    public static final Map<UUID, Long> RANDOM_EFFECT_COOLDOWN = new HashMap<>();

    public static void startPlayersEffects(Plugin plugin) {
        new PlayerEffects(plugin);
    }

    PlayerEffects(Plugin plugin) {

        new BukkitRunnable() {
            public void run() {

                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {

                        if (player.getGameMode() == GameMode.SURVIVAL) {

                            Optional<PlayerTime> currentPlayerTime = TimeManagement.getSpecificPlayerTime(player.getUniqueId());
                            Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(player.getWorld().getName());

                            Randomness random = new Randomness();

                            if (currentPlayerTime.isPresent() && currentWorldTime.isPresent()) {
                                
                                PlayerTime playerTime = currentPlayerTime.get();
                                WorldTime worldTime = currentWorldTime.get();

                                Long thristyMinutes = playerTime.getThirstyMinutes();
    
                                if (!player.hasMetadata("thristy")) {
                                    player.setMetadata("thristy", new FixedMetadataValue(plugin, false));
                                }
    
                                boolean isPreviousThristy = false;

                                for (MetadataValue metadataValueThristy : player.getMetadata("thristy")) {
                                    if (metadataValueThristy.getOwningPlugin() == plugin) {
                                        isPreviousThristy = metadataValueThristy.asBoolean();
                                        break;
                                    }
                                }

                                int thristyLimit = 15;

                                if (worldTime.isDayAbove50()) {
                                    thristyLimit = random.random(5, 15);
                                }  else  {
                                    thristyLimit = random.random(9, 15);
                                }
                                
                                if (thristyMinutes >= thristyLimit && !isPreviousThristy) {
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

                                for (MetadataValue metadataValueThristy : player.getMetadata("thristy")) {
                                    if (metadataValueThristy.getOwningPlugin() == plugin) {
                                        isThristy = metadataValueThristy.asBoolean();
                                        break;
                                    }
                                }
    
                                if (isThristy) {
                                    double currentHealth = player.getHealth();
                                    
                                    Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(world.getName());
                                    
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
                long now = System.currentTimeMillis();
                long globalCooldown = Main.balance.getLong("player-effects.global-cooldown-ms", 600000L);

                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        if (player.getGameMode() != GameMode.SURVIVAL) {
                            continue;
                        }

                        UUID uuid = player.getUniqueId();

                        if (RANDOM_EFFECT_COOLDOWN.getOrDefault(uuid, 0L) > now) {
                            continue;
                        }

                        Optional<PlayerTime> playerTimeOptional = TimeManagement.getSpecificPlayerTime(uuid);
                        Optional<WorldTime> worldTimeOptional = TimeManagement.getSpecificWorldTime(world.getName());

                        if (playerTimeOptional.isEmpty() || worldTimeOptional.isEmpty()) {
                            continue;
                        }

                        PlayerTime playerTime = playerTimeOptional.get();
                        WorldTime worldTime = worldTimeOptional.get();

                        long minutes = playerTime.getMinutes();
                        long day = worldTime.getDay();

                        List<String> pool = new ArrayList<>();

                        if (minutes >= 60) {
                            for (int index = 0; index < 20; index++) {
                                pool.add("SLOWNESS");
                            }

                            for (int index = 0; index < 20; index++) {
                                pool.add("MINING_FATIGUE");
                            }
                        }

                        if (minutes >= 120) {
                            for (int index = 0; index < 10; index++) {
                                pool.add("WEAKNESS_SHORT");
                            }
                        }

                        if (day > 20 && day <= 50) {
                            for (int index = 0; index < 15; index++) {
                                pool.add("DARKNESS");
                            }
                        }

                        if (day > 50) {
                            for (int index = 0; index < 10; index++) {
                                pool.add("BAD_OMEN");
                            }

                            for (int index = 0; index < 15; index++) {
                                pool.add("HUNGER");
                            }

                            for (int index = 0; index < 2; index++) {
                                pool.add("WITHER");
                            }
                        }

                        if (day > 100) {
                            for (int index = 0; index < 10; index++) {
                                pool.add("BLINDNESS");
                            }

                            for (int index = 0; index < 10; index++) {
                                pool.add("WEAKNESS_END");
                            }
                        }

                        if (pool.isEmpty()) {
                            continue;
                        }

                        double chance;

                        if (day <= 50) {
                            chance = Main.balance.getDouble("player-effects.chance-early", 0.20);
                        } else if (day <= 100) {
                            chance = Main.balance.getDouble("player-effects.chance-mid", 0.25);
                        } else {
                            chance = Main.balance.getDouble("player-effects.chance-late", 0.30);
                        }

                        if (ThreadLocalRandom.current().nextDouble() > chance) {
                            continue;
                        }

                        String effect = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));

                        switch (effect) {
                            case "SLOWNESS" -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                                    Main.balance.getInt("player-effects.slowness-duration-ticks", 1200), 0,
                                    true, true, true));
                            case "MINING_FATIGUE" -> player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE,
                                    Main.balance.getInt("player-effects.mining-fatigue-duration-ticks", 1200), 0,
                                    true, true, true));
                            case "WEAKNESS_SHORT" -> player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                                    Main.balance.getInt("player-effects.weakness-short-duration-ticks", 2400), 0,
                                    true, true, true));
                            case "DARKNESS" -> {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,
                                        Main.balance.getInt("player-effects.darkness-duration-ticks", 100), 0,
                                        true, true, true));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.5f, 0.5f);
                            }
                            case "BAD_OMEN" -> player.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN,
                                    Main.balance.getInt("player-effects.bad-omen-duration-ticks", 6000), 0,
                                    true, true, true));
                            case "HUNGER" -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER,
                                    Main.balance.getInt("player-effects.hunger-duration-ticks", 300), 0,
                                    true, true, true));
                            case "WITHER" -> {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,
                                        Main.balance.getInt("player-effects.wither-duration-ticks", 60), 0,
                                        true, true, true));
                                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.2f, 0.2f);
                            }
                            case "BLINDNESS" -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                                    Main.balance.getInt("player-effects.blindness-duration-ticks", 100), 0,
                                    true, true, true));
                            case "WEAKNESS_END" -> player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                                    Main.balance.getInt("player-effects.weakness-end-duration-ticks", 600), 0,
                                    true, true, true));
                            default -> {
                            }
                        }

                        RANDOM_EFFECT_COOLDOWN.put(uuid, now + globalCooldown);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, Main.balance.getLong("player-effects.interval-ticks", 12000L));

    }
    
}
