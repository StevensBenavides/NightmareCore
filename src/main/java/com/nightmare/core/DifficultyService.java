package com.nightmare.core;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import com.nightmare.Main;

public final class DifficultyService implements Listener {

    public DifficultyService() {
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        apply(event.getWorld());
    }

    public static void apply(World world) {
        long day = world.getFullTime() / 24000L;
        int maxMonsters = Main.balance.getInt("caps.max-monsters", 300);

        world.setDifficulty(Difficulty.HARD);
        world.setPVP(day >= 9);

        try {
            World.class.getMethod("setGameRuleValue", String.class, String.class)
                    .invoke(world, "doDaylightCycle", String.valueOf(day < 81));
        } catch (ReflectiveOperationException ignored) {
        }

        if (day >= 81) {
            world.setTime(18000L);
        }

        if (day < 9) {
            world.setSpawnLimit(SpawnCategory.MONSTER, 50);
            return;
        }

        if (day < 27) {
            world.setSpawnLimit(SpawnCategory.MONSTER, 100);
            world.setSpawnLimit(SpawnCategory.ANIMAL, 100);
            world.setSpawnLimit(SpawnCategory.AMBIENT, 100);
            world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, 100);
            world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 100);
            world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, 100);

            return;
        }

        if (day < 54) {
            world.setSpawnLimit(SpawnCategory.MONSTER, 200);
            world.setSpawnLimit(SpawnCategory.ANIMAL, 60);
            world.setSpawnLimit(SpawnCategory.AMBIENT, 60);
            world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, 60);
            world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 60);
            world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, 60);

            return;
        }

        if (day < 81) {
            world.setSpawnLimit(SpawnCategory.MONSTER, 250);
            world.setSpawnLimit(SpawnCategory.ANIMAL, 40);
            world.setSpawnLimit(SpawnCategory.AMBIENT, 40);
            world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, 40);
            world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 40);
            world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, 40);
            return;
        }

        world.setSpawnLimit(SpawnCategory.MONSTER, maxMonsters);
        world.setSpawnLimit(SpawnCategory.ANIMAL, 20);
        world.setSpawnLimit(SpawnCategory.AMBIENT, 20);
        world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, 20);
        world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 20);
        world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, 20);
    }

}
