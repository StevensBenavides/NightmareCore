package com.nightmare;

import java.util.Optional;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.plugin.Plugin;

public class WorldDifficulty {

    public static void startWorldDifficulty(Plugin plugin) {
        new WorldDifficulty(plugin);
    }

    WorldDifficulty(Plugin plugin) {

        for (World world : plugin.getServer().getWorlds()) {
            world.setDifficulty(Difficulty.HARD);

            Optional<WorldTime> currentWorldTime = Main.getTimeManagement().getSpecificWorldTime(world.getName());

            if (currentWorldTime.isPresent()) {
                
                WorldTime worldTime = currentWorldTime.get();

                if (worldTime.isDayAbove50()) {
                    world.setSpawnLimit(SpawnCategory.MONSTER, 1000);
                    world.setSpawnLimit(SpawnCategory.ANIMAL, 20);
                    world.setSpawnLimit(SpawnCategory.AMBIENT, 20);
                    world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, 20);
                    world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 20);
                    world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, 20);
                } 
                else if (worldTime.isDayBelow(10)) {
                    world.setSpawnLimit(SpawnCategory.MONSTER, 50);
                }
                else if (worldTime.isDayBelow(20)) {
                    world.setSpawnLimit(SpawnCategory.MONSTER, 100);
                }
                else if (worldTime.isDayBelow(40)) {
                    world.setSpawnLimit(SpawnCategory.MONSTER, 200);
                }
               
            }
        }
    }
    
}
