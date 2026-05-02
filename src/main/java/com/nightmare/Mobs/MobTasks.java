package com.nightmare.Mobs;

import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.Plugin;

public final class MobTasks {

    public static void startCTierMobEffects(Plugin plugin) {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (World world : plugin.getServer().getWorlds()) {
                for (Entity mob : world.getEntitiesByClasses(Zombie.class, Creeper.class, Spider.class, Enderman.class)) {

                    if (mob instanceof Zombie) {
                        NightmareZombie.setCtierConstantEffects(mob, world);
                    } else if (mob instanceof Creeper) {
                        NightmareCreeper.setCtierConstantEffects(mob, world);
                    } else if (mob instanceof Spider) {
                        NightmareSpider.setCtierConstantEffects(mob, world);
                    } else if (mob instanceof Enderman) {
                        NightmareEnderman.setCtierConstantEffects(mob, world);
                    }

                }

            }
            
        }, 0, 30L);

    } 
    
}
