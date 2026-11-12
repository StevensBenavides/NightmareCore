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

    public static void apply(World world) {
        long day = world.getFullTime() / 24000L;
        int maxMonsters = Main.balance.getInt("caps.max-monsters", 90);

        world.setDifficulty(Difficulty.HARD);
        world.setPVP(day >= 9);

        int monsters;
        if (day < 9) {
            monsters = 50;
        } else if (day < 27) {
            monsters = 60;
        } else if (day < 54) {
            monsters = 70;
        } else if (day < 81) {
            monsters = 80;
        } else {
            monsters = maxMonsters;
        }

        world.setSpawnLimit(SpawnCategory.MONSTER, monsters);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        apply(event.getWorld());
    }

}
