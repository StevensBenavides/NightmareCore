package com.nightmare.Mobs;

import java.util.Objects;

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

        if (randomness.is50percent())
            createAtierNightmareEnderman(entity, config, randomness);

        if (randomness.is25percent())
            createBtierNightmareEnderman(entity, config, randomness);

        if (randomness.is5percent())
            createCtierNightmareEnderman(entity, config, randomness);

    }

    private void createAtierNightmareEnderman(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Enderman mob = (Enderman) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString("config.mobs.a").replace("%mob%", Enderman.class.getSimpleName()));
        
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setCanPickupItems(false);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));

    }

    private void createBtierNightmareEnderman(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Enderman mob = (Enderman) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString("config.mobs.b").replace("%mob%", Enderman.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

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
