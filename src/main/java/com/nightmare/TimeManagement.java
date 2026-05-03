package com.nightmare;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeManagement {

    public WorldTime worldTime;
    public HashMap<String, WorldTime> worldTimes = new HashMap<String, WorldTime>();
    public HashMap<UUID, PlayerTime> playerTimes = new HashMap<UUID, PlayerTime>();

    public static TimeManagement startTimeManagement(Plugin plugin) {
        return new TimeManagement(plugin);
    }

    TimeManagement(Plugin plugin) {

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
                        long currentWorldDay = world.getFullTime() / 24000L;

                        if (currentWorldDay > previousWorldTime.getDay()) {
                            previousWorldTime.update(world.getFullTime());
                        }
                    }
                }

            }
        }.runTaskTimerAsynchronously(plugin, 0, 200L);

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
        }.runTaskTimerAsynchronously(plugin, 0, 500L);
    }

    public void addPlayerTime(UUID uuid) {
        this.playerTimes.put(uuid, new PlayerTime());
    }

    public void removePlayerTime(UUID uuid) {
        this.playerTimes.remove(uuid);
    }

    public Optional<PlayerTime> getSpecificPlayerTime(UUID uuid) {
        return Optional.of(this.playerTimes.get(uuid));
    }

    public Optional<WorldTime> getSpecificWorldTime(String name) {
        return Optional.of(worldTimes.get(name));
    }

    public HashMap<String, WorldTime> getWorldTimes() {
        return this.worldTimes;
    }
}
