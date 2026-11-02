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
                    WorldTime worldTime = worldTimes.get(worldName);

                    if (worldTime == null) {
                        worldTimes.put(worldName, new WorldTime(world));
                    } else {
                        worldTime.update(world.getFullTime());
                    }
                }

            }
        }.runTaskTimer(plugin, 0, 500L);

        new BukkitRunnable() {
            public void run() {

                for (World world : plugin.getServer().getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        UUID playerUUID = player.getUniqueId();
                        PlayerTime playerTime = playerTimes.get(playerUUID);

                        if (playerTime == null) {
                            playerTimes.put(playerUUID, new PlayerTime());
                        } else {
                            playerTime.update();
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
                PlayerTime playerTime = playerTimes.get(playerUUID);

                if (playerTime == null) {
                    playerTimes.put(playerUUID, new PlayerTime());
                } else {
                    playerTime.update();
                }
            }
        }
    }

    public static void updateWorldsTimes() {
        for (World world : Main.getInstance().getServer().getWorlds()) {
            String worldName = world.getName();
            WorldTime worldTime = worldTimes.get(worldName);

            if (worldTime == null) {
                worldTimes.put(worldName, new WorldTime(world));
            } else {
                worldTime.update(world.getFullTime());
            }
        }
    }

    public static void addPlayerTime(UUID uuid) {
        playerTimes.put(uuid, new PlayerTime());
    }

    public static void addPlayerTimeIfAbsent(UUID uuid) {
        playerTimes.putIfAbsent(uuid, new PlayerTime());
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
