package com.nightmare.Mobs;

import java.util.Optional;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

public class NightmareSpider {

    NightmareSpider(Entity entity, YamlConfiguration config) {
        final RandomnessManagement randomness = new RandomnessManagement();
        final TimeManagement timeManagement = Main.getTimeManagement();
        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> currentWorldTime = timeManagement.getSpecificWorldTime(currentWorldName);
    
        if (currentWorldTime.isPresent()) {
            final WorldTime worldTime = currentWorldTime.get();
            
            if (worldTime.isDayBelow(20)) {
                if (randomness.is1percent()) {
                    createCtierNightmareSpider(entity, config, randomness);
                    return;
                }
                if (randomness.is15percent()) {
                    createBtierNightmareSpider(entity, config, randomness);
                    return;
                }
                if (randomness.is80percent()) {
                    createAtierNightmareSpider(entity, config, randomness);
                    return;
                }
            } 
            else if (worldTime.isDayBelow(50)) {
                if (randomness.is8percent()) {
                    createCtierNightmareSpider(entity, config, randomness);
                    return;
                }
                if (randomness.is25percent()) {
                    createBtierNightmareSpider(entity, config, randomness);
                    return;
                }
                if (randomness.is60percent()) {
                    createAtierNightmareSpider(entity, config, randomness);
                    return;
                }
            } 
            else if (worldTime.isDayAbove(50)) {
                if (randomness.is30percent()) {
                    createCtierNightmareSpider(entity, config, randomness);
                    return;
                }
                if (randomness.is40percent()) {
                    createBtierNightmareSpider(entity, config, randomness);
                    return;
                }
                if (randomness.is30percent()) {
                    createAtierNightmareSpider(entity, config, randomness);
                    return;
                }
            }
        }
    }


    private void createAtierNightmareSpider(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {
        
        Spider mob = (Spider) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_a.getValue()).replace("%mob%", Spider.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        
        {
            RandomnessManagement random = new RandomnessManagement();

            if (random.is5percent()) {
                mob.setPersistent(true);
            }
        }

        {
            RandomnessManagement random = new RandomnessManagement();
            
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, random.random(1, 2)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 2)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, random.random(1, 2)));
        }

        mob.setMetadata("NightmareATierMob", new FixedMetadataValue(Main.getInstance(), true));
      
    }
    

    private void createBtierNightmareSpider(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Spider mob = (Spider) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", Spider.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setGliding(true);
        mob.setCustomNameVisible(true);
            

        {
            RandomnessManagement random = new RandomnessManagement();

            if (random.is5percent()) {
                mob.setPersistent(true);
            }
        }

        {

            RandomnessManagement random = new RandomnessManagement();

            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, random.random(1, 3)));

        }
        
        mob.setMetadata("NightmareBTierMob", new FixedMetadataValue(Main.getInstance(), true));

    }

    private void createCtierNightmareSpider(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Spider mob = (Spider) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Spider.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setGliding(true);
        mob.setCustomNameVisible(false);
        mob.setRiptiding(true);

        {
            RandomnessManagement random = new RandomnessManagement();

            if (random.is5percent()) {
                mob.setCustomNameVisible(true);
            }
        }

        {

            RandomnessManagement random = new RandomnessManagement();
            
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, random.random(1, 10)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 10)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, random.random(1, 5)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, random.random(1, 2)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, random.random(1, 10)));
        }


        {
            RandomnessManagement random = new RandomnessManagement();

            if (random.is50percent()) {
                mob.setPersistent(true);
            }
        }

        mob.setMetadata("NightmareCTierMob", new FixedMetadataValue(Main.getInstance(), true));
    }

    public static void setCtierConstantEffects(Entity entity, World world) {

        if (!(entity instanceof Spider spider)) return;

        if (!spider.hasMetadata("NightmareCTierMob")) {
            return;
        }

        if (!(spider.getTarget() instanceof Player player)) return;

        {
            if (Math.random() < 0.4) { 
                for (int i = 0; i < 2; i++) {
                    Location loc = player.getLocation().clone().add(
                        Math.random() * 6 - 3, 
                        0, 
                        Math.random() * 6 - 3
                    );
                    world.spawnEntity(loc, EntityType.LIGHTNING_BOLT);
                }
            }
        }

        {
            spider.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 60 * 2, 2, true, false));

            if (Math.random() < 0.3) {
                spider.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 2, 0, true, false));
            }
        }

        {
            if (Math.random() < 0.6) {
                player.getWorld().spawnParticle(Particle.SMOKE, 
                    player.getLocation().add(0, 1.8, 0), 25, 0.4, 0.4, 0.4, 0.02);
                
                player.getWorld().spawnParticle(Particle.END_ROD, 
                    player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1.0, 
                    new Particle.DustOptions(Color.fromRGB(100, 0, 200), 1.5f));
            }
        }
    }

}
