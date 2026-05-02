package com.nightmare.Mobs;

import com.nightmare.ConfigEvaluator;
import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import net.md_5.bungee.api.ChatColor;

import java.util.Objects;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightmareCreeper {

    NightmareCreeper(Entity entity, YamlConfiguration config) {

        try {
            ConfigEvaluator.evaluate("MobSpawning", config); 
        } catch (Exception e) {
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            e.printStackTrace();
        }  

        final RandomnessManagement randomness = new RandomnessManagement();

        if (randomness.is50percent()) 
            createAtierNightmareCreeper(entity, config, randomness);

        if (randomness.is25percent())
            createBtierNightmareCreeper(entity, config, randomness);

        if (randomness.is5percent())
            createCtierNightmareCreeper(entity, config, randomness);

    }

    private void createAtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {
        
        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_a.getValue()).replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));

    }

    private void createBtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setExplosionRadius(randomness.random(15, 25));

        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));

    }

    private void createCtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Creeper.class.getSimpleName()));

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


    public static void setCtierConstantEffects(Entity entity, World world) {

        YamlConfiguration settings = Main.getSettings();

        final Creeper creeper = (Creeper) entity;
        final String name = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(settings.getString(Constants.Mobs.config_mobs_name_c.getValue())).replace("%mob%", Creeper.class.getSimpleName()));

        if (creeper.getCustomName() != null && creeper.getCustomName().equalsIgnoreCase(name) && !creeper.isDead() && creeper.getTarget() == null) {

            for (Entity entity_2 : creeper.getNearbyEntities(15.0, 15.0, 15.0).stream().filter(entity_ -> entity_ instanceof Player).toList()) {
                Player player = (Player) entity_2;

                if (player.getGameMode() == GameMode.SURVIVAL) 
                    creeper.setTarget(player);
            }

        } else if (creeper.getCustomName() != null && creeper.getCustomName().equalsIgnoreCase(name) && !creeper.isDead() && creeper.getTarget() == null) {

            for (Entity entity_2 : creeper.getNearbyEntities(15.0, 15.0, 15.0).stream().filter(entity_ -> entity_ instanceof Player).toList()) {
                Player player = (Player) entity_2;

                if (player.getGameMode() == GameMode.SURVIVAL)
                    creeper.setTarget(player);
            }

        }

    }
}
