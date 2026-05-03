package com.nightmare.Mobs;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.ConfigEvaluator;
import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

public class NightmareEnderman {
    NightmareEnderman(Entity entity, YamlConfiguration config) {

        try {
            ConfigEvaluator.evaluate("MobSpawning", config); 
        } catch (Exception e) {
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            e.printStackTrace();
        }  

        final RandomnessManagement randomness = new RandomnessManagement();

        final TimeManagement timeManagement = Main.getTimeManagement();
        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> worldTime = timeManagement.getSpecificWorldTime(currentWorldName);

        if (worldTime.isPresent() && randomness.is5percent()) {
            final WorldTime currentWorldTime = worldTime.get();

            if (currentWorldTime.isDayAbove50()) 
                createCtierNightmareEnderman(entity, config, randomness);

        }
        
        if (randomness.is15percent()) {
            createBtierNightmareEnderman(entity, config, randomness);
            return;
        }

        if (randomness.is70percent()) {
            createAtierNightmareEnderman(entity, config, randomness);
            return;
        }

    }

    private void createAtierNightmareEnderman(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Enderman mob = (Enderman) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_a.getValue()).replace("%mob%", Enderman.class.getSimpleName()));
        
        mob.setCustomName(name);
        
        {

            RandomnessManagement random = new RandomnessManagement();

            if (random.is10percent()) {
                mob.setCustomNameVisible(true);
            } else {
                mob.setCustomNameVisible(false);
            }

        }

        mob.setCanPickupItems(false);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));

    }

    private void createBtierNightmareEnderman(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Enderman mob = (Enderman) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", Enderman.class.getSimpleName()));

        mob.setCustomName(name);

        {

            RandomnessManagement random = new RandomnessManagement();

            if (random.is25percent()) {
                mob.setCustomNameVisible(true);
            } else {
                mob.setCustomNameVisible(false);
            }

        }

        mob.setCanPickupItems(false);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 3));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
    }

    private void createCtierNightmareEnderman(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Enderman mob = (Enderman) entity;

        final String c = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Enderman.class.getSimpleName()));

        mob.setCustomName(c);
        mob.setCustomNameVisible(true);

        mob.setCanPickupItems(false);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 4));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2));
    
    }

    public static void setCtierConstantEffects(Entity entity, World world) { 

        YamlConfiguration settings = Main.getSettings();

        final Enderman enderman = (Enderman) entity;
        final String name = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(settings.getString(Constants.Mobs.config_mobs_name_c.getValue())).replace("%mob%", Enderman.class.getSimpleName()));

        if (enderman.getCustomName() != null && enderman.getCustomName().equalsIgnoreCase(name) && !enderman.isDead()) {
            enderman.getWorld().spawnParticle(Particle.END_ROD, enderman.getLocation(), 60);
        } else if (enderman.getCustomName() != null && enderman.getCustomName().equalsIgnoreCase(name) && !enderman.isDead()) {
            enderman.getWorld().spawnParticle(Particle.PORTAL, enderman.getLocation(), 60);
        }

    
    }

}
