package com.nightmare.Mobs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

public final class Mobs {

    public static void spawnNightmareZombie(Entity entity, YamlConfiguration config) {
        new NightmareZombie(entity, config);
    }

    public static void spawnNightmareCreeper(Entity entity, YamlConfiguration config) {
        new NightmareCreeper(entity, config);
    }

    public static void spawnNightmareSpider(Entity entity, YamlConfiguration config) {
        new NightmareSpider(entity, config);
    }

    public static void spawnNightmareSkeleton(Entity entity, YamlConfiguration config) {
        new NightmareSkeleton(entity, config);
    }

    public static void spawnNightmareEnderman(Entity entity, YamlConfiguration config) {
        new NightmareEnderman(entity, config);
    }
    
}
