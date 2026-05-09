package com.nightmare.Mobs;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import com.nightmare.WorldTime;

public final class MobTasks {

    public static void startCTierMobEffects(Plugin plugin) {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (World world : plugin.getServer().getWorlds()) {
                for (Entity mob : world.getEntitiesByClasses(Zombie.class, Creeper.class, Spider.class, Enderman.class)) {

                    if (mob instanceof Zombie) 
                        NightmareZombie.setCtierConstantEffects(mob, world);
                    
                    if (mob instanceof Creeper) 
                        NightmareCreeper.setCtierConstantEffects(mob, world);

                    if (mob instanceof Spider) 
                        NightmareSpider.setCtierConstantEffects(mob, world);
                    
                    if (mob instanceof Enderman) 
                        NightmareEnderman.setCtierConstantEffects(mob, world);

                }

            }
            
        }, 0, 30L);

    } 

    public static void startIdleTasks(Plugin plugin) {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (World world : plugin.getServer().getWorlds()) {

                if (world.getEnvironment() == Environment.NORMAL) {
                    for (Entity mob : world.getEntitiesByClasses(Villager.class)) {
    
                        if (mob instanceof Villager) {
    
                            Villager villager = (Villager) mob;
    
                            if (villager.hasMetadata("AngryVillager")) {

                                final List<Entity> nearbyEntities = villager.getNearbyEntities(25, 25, 25);
                                final boolean hasPlayer = nearbyEntities.stream().anyMatch(entity -> entity instanceof Player);
                                final Optional<WorldTime> currentWorldTime = Main.getTimeManagement().getSpecificWorldTime(villager.getWorld().getName());

                                if (hasPlayer && currentWorldTime.isPresent()) {
                                    final RandomnessManagement random = new RandomnessManagement();
    
                                    final Consumer<Entity> processNearbyEntities = entity -> {
                                        if (!(entity instanceof Player)) return;
                                    
                                        Main.getTimeManagement().getSpecificWorldTime(villager.getWorld().getName()).ifPresent(worldTime -> {
                                            Player player = (Player) entity;
                                            villager.setTarget(player);
                                            
                                            RandomnessManagement random_2 = new RandomnessManagement();
                                    
                                            int amountToSpawn = 0;
                                            int speedAmplifier = 1;
                                            boolean isNightmare = false;
                                    
                                            if (worldTime.isDayBelow(20)) {
                                                if (random_2.is1percent()) amountToSpawn = 5;
                                                else if (random_2.is10percent()) amountToSpawn = 2;
                                            } else if (worldTime.isDayBelow(40)) {
                                                if (random_2.is15percent()) amountToSpawn = 5;
                                                else if (random_2.is25percent()) amountToSpawn = 2;
                                            } else if (worldTime.isDayBelow50()) {
                                                if (random_2.is15percent()) amountToSpawn = 5;
                                                else if (random_2.is25percent()) amountToSpawn = 2;
                                            }  else if (worldTime.isDayAbove50()) {
                                                if (random_2.is40percent()) {
                                                    amountToSpawn = 5;
                                                    speedAmplifier = new RandomnessManagement().random(1, 4);
                                                    isNightmare = true;
                                                }
                                            }
                                    
                                            for (int i = 0; i < amountToSpawn; i++) {
                                                IronGolem golem = (IronGolem) villager.getWorld().spawnEntity(villager.getLocation(), EntityType.IRON_GOLEM);
                                                golem.setTarget(player);
                                                golem.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, speedAmplifier));
                                                
                                                if (isNightmare) {
                                                    golem.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
                                                    golem.setMetadata("VillagerNightmareGuardianIronGolem", new FixedMetadataValue(plugin, true));
                                                }
                                            }
                                        });
                                    };


                                    final WorldTime worldTime = currentWorldTime.get();

                                    if (worldTime.isDayBelow(10)) {
                                        if (random.is1percent() || random.is5percent()) {
                                            nearbyEntities.stream().forEach(processNearbyEntities);
                                        }
                                    } else if (worldTime.isDayBelow(20)) {
                                        if (random.is10percent() || random.is15percent()) {
                                            nearbyEntities.stream().forEach(processNearbyEntities);
                                        }
                                    } else if (worldTime.isDayBelow(40)) {
                                        if (random.is15percent() || random.is25percent()) {
                                            nearbyEntities.stream().forEach(processNearbyEntities);
                                        }
                                    } else if (worldTime.isDayAbove50()) {
                                        if (random.is30percent()) {
                                            nearbyEntities.stream().forEach(processNearbyEntities);
                                        }
                                    }
                                 
                                 
                                }
                            }
    
                        }
    
                    }
                }
    
            }
        },  0, 50L);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (World world : plugin.getServer().getWorlds()) {

                if (world.getEnvironment() == Environment.NORMAL) {
                    for (Entity mob : world.getEntitiesByClasses(IronGolem.class)) {

                        if (mob instanceof IronGolem) {
                            
                            if (mob.hasMetadata("VillagerNightmareGuardianIronGolem")) {

                                RandomnessManagement random = new RandomnessManagement();
    
                                IronGolem golem = (IronGolem) mob;
                                Location center = golem.getLocation();
                                World world_2 = center.getWorld();

                                for (int i = 0; i < random.random(5, 15); i++) {                   
                                    double offsetX = Math.random() * 20 - 10;   
                                    double offsetZ = Math.random() * 20 - 10;
                                    
                                    Location lightningLoc = center.clone().add(offsetX, 0, offsetZ);

                                    {
                                        
                                        RandomnessManagement random_2 = new RandomnessManagement();

                                        if (random_2.is5percent() || random_2.is30percent()) {
                                            lightningLoc.setY(world.getHighestBlockYAt(lightningLoc) + 1);
                                            world_2.strikeLightning(lightningLoc);
                                        }
                                     
                                    }
                                   
                                }
                            
                            }
    
                        }
                    }
                }
            }
        }, 0, 50L);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (World world : plugin.getServer().getWorlds()) {


            } 
        }, 0 , 100L);

    }
    
}
