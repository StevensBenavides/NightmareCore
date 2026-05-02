package com.nightmare.Mobs;

import com.nightmare.RandomnessManagement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightmareCreeper {

    NightmareCreeper(Entity entity, YamlConfiguration config) {

        final RandomnessManagement randomness = new RandomnessManagement();

        if (randomness.is25percent())
            createBtierNightmareCreeper(entity, config, randomness);

        if (randomness.is5percent())
            createCtierNightmareCreeper(entity, config, randomness);

    }

    private void createBtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString("config.mobs.b").replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setExplosionRadius(randomness.random(15, 25));

        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));

    }

    private void createCtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString("config.mobs.c").replace("%mob%", Creeper.class.getSimpleName()));

        Creeper mob = (Creeper) entity;

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setExplosionRadius(randomness.random(25, 50));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 4));

        mob.setPersistent(true);

        if (mob.getLocation().getY() >= mob.getWorld().getHighestBlockAt((int) mob.getLocation().getX(), (int) mob.getLocation().getZ()).getLocation().getY())
            mob.getWorld().spawnEntity(mob.getLocation(), EntityType.LIGHTNING_BOLT);
    }
}
