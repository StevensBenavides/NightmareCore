package com.nightmare;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class TimeManagement {

    private static HashMap<String, WorldTime> worldTimes = new HashMap<String, WorldTime>();
    private static HashMap<UUID, PlayerTime> playerTimes = new HashMap<UUID, PlayerTime>();

    public static void init(Plugin plugin) {

        for (World world : plugin.getServer().getWorlds()) {

            WorldTime worldTime = new WorldTime(world);
            String worldName = world.getName();

            worldTimes.put(worldName, worldTime);
        }

        new BukkitRunnable() {
            public void run() {

                for (World world : plugin.getServer().getWorlds()) {
                    String worldName = world.getName();
                    Optional<WorldTime> worldTime = Optional.of(worldTimes.get(worldName));

                    if (worldTime.isPresent()) {
                        WorldTime previousWorldTime = worldTime.get();

                        previousWorldTime.update(world.getFullTime());
                    } else {
                        WorldTime worldTime_2 = new WorldTime(world);
                        String worldName_2 = world.getName();

                        worldTimes.put(worldName_2, worldTime_2);
                    }
                }

            }
        }.runTaskTimer(plugin, 0, 500L);

        new BukkitRunnable() {
            public void run() {

                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        UUID playerUUID = player.getUniqueId();
                        Optional<PlayerTime> currentPlayerTime = Optional.of(playerTimes.get(playerUUID));
                        
                        if (currentPlayerTime.isPresent()) {
                            PlayerTime playerTime = currentPlayerTime.get();

                            playerTime.update();

                            playerTimes.remove(playerUUID);
                            playerTimes.put(playerUUID, playerTime);

                        }
                    
                    }
                }

            }
        }.runTaskTimer(plugin, 0, 500L);
    }

    public static void updatePlayerTimes() {
        for (World world : Main.getInstance().getServer().getWorlds()) {
            for (Player player : world.getPlayers()) {
                UUID playerUUID = player.getUniqueId();
                Optional<PlayerTime> currentPlayerTime = Optional.of(playerTimes.get(playerUUID));
                
                if (currentPlayerTime.isPresent()) {
                    PlayerTime playerTime = currentPlayerTime.get();

                    playerTime.update();

                    playerTimes.remove(playerUUID);
                    playerTimes.put(playerUUID, playerTime);

                }
            
            }
        }
    }


    public static void updateWorldsTimes() {
        for (World world : Main.getInstance().getServer().getWorlds()) {
            String worldName = world.getName();
            Optional<WorldTime> worldTime = Optional.of(worldTimes.get(worldName));

            if (worldTime.isPresent()) {
                WorldTime previousWorldTime = worldTime.get();
                previousWorldTime.update(world.getFullTime());
            } else {
                WorldTime worldTime_2 = new WorldTime(world);
                String worldName_2 = world.getName();

                worldTimes.put(worldName_2, worldTime_2);
            }
        }
    }

    public static void addPlayerTime(UUID uuid) {
        playerTimes.put(uuid, new PlayerTime());
    }

    public static void removePlayerTime(UUID uuid) {
        playerTimes.remove(uuid);
    }

    public static Optional<PlayerTime> getSpecificPlayerTime(UUID uuid) {
        return Optional.ofNullable(playerTimes.get(uuid));
    }

    public static Optional<WorldTime> getSpecificWorldTime(String name) {
        return Optional.ofNullable(worldTimes.get(name));
    }

    public static HashMap<String, WorldTime> getWorldTimes() {
        return worldTimes;
    }
}
