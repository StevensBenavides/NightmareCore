package com.nightmare.Mobs;

import java.util.Optional;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

public class NightmareEnderman {
    NightmareEnderman(Entity entity, YamlConfiguration config) {
        final RandomnessManagement randomness = new RandomnessManagement();
        final TimeManagement timeManagement = Main.getTimeManagement();
        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> currentWorldTime = timeManagement.getSpecificWorldTime(currentWorldName);
    
        if (currentWorldTime.isPresent()) {
            final WorldTime worldTime = currentWorldTime.get();
    
            if (worldTime.isDayBelow(20)) {
            
                if (randomness.is1percent()) {
                    createCtierNightmareEnderman(entity, config, randomness);
                    return;
                }
                if (randomness.is15percent()) {
                    createBtierNightmareEnderman(entity, config, randomness);
                    return;
                }
                if (randomness.is80percent()) {
                    createAtierNightmareEnderman(entity, config, randomness);
                    return;
                }
            
            } 
            
            else if (worldTime.isDayBelow(50)) {
                if (randomness.is10percent()) {
                    createCtierNightmareEnderman(entity, config, randomness);
                    return;
                }
                
                if (randomness.is30percent()) {
                    createBtierNightmareEnderman(entity, config, randomness);
                    return;
                }

                if (randomness.is60percent()) {
                    createAtierNightmareEnderman(entity, config, randomness);
                    return;
                }
            } 
            
            else if (worldTime.isDayAbove50()) {
                if (randomness.is30percent()) {
                    createCtierNightmareEnderman(entity, config, randomness);
                    return;
                }
                if (randomness.is40percent()) {
                    createBtierNightmareEnderman(entity, config, randomness);
                    return;
                }
                if (randomness.is30percent()) {
                    createAtierNightmareEnderman(entity, config, randomness);
                    return;
                }
            }
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

        mob.setMetadata("NightmareATierMob", new FixedMetadataValue(Main.getInstance(), true));

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
        
        mob.setMetadata("NightmareBTierMob", new FixedMetadataValue(Main.getInstance(), true));
    }

    private void createCtierNightmareEnderman(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Enderman mob = (Enderman) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Enderman.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setCanPickupItems(false);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 4));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2));

        mob.setMetadata("NightmareCTierMob", new FixedMetadataValue(Main.getInstance(), true));
    
    }

    public static void setCtierConstantEffects(Entity entity, World world) { 

        final Enderman enderman = (Enderman) entity;

        if (enderman.hasMetadata("NightmareCTierMob") && !enderman.isDead()) {
            enderman.getWorld().spawnParticle(Particle.END_ROD, enderman.getLocation(), 60);
            enderman.getWorld().spawnParticle(Particle.PORTAL, enderman.getLocation(), 60);
        }
    
    }

}
