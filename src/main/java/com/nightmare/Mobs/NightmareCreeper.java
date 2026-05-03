package com.nightmare.Mobs;

import com.nightmare.ConfigEvaluator;
import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

import java.util.Objects;
import java.util.Optional;

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

        final TimeManagement timeManagement = Main.getTimeManagement();
        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> worldTime = timeManagement.getSpecificWorldTime(currentWorldName);

        if (worldTime.isPresent() && randomness.is5percent()) {
            final WorldTime currentWorldTime = worldTime.get();

            if (currentWorldTime.isDayAbove50())  {
                createCtierNightmareCreeper(entity, config, randomness);
            }
        }

        if (randomness.is15percent()) {
            createBtierNightmareCreeper(entity, config, randomness);
            return;
        }
          
        if (randomness.is70percent())  {
            createAtierNightmareCreeper(entity, config, randomness);
            return;
        }
   
    }

    private void createAtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {
        
        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_a.getValue()).replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(false);
        
        {

            RandomnessManagement random = new RandomnessManagement();
            
            if (random.is1percent()) {
                mob.setGliding(true);
                mob.setPowered(true);
                mob.setArrowsInBody(15);
                mob.setSilent(true);
            }
    
        }
        
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));

    }

    private void createBtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);

        {

            RandomnessManagement random = new RandomnessManagement();

            if (random.is25percent()) {
                mob.setCustomNameVisible(true);
            } 

        }

        mob.setGliding(true);
        mob.setExplosionRadius(randomness.random(5, 20));

        
        {

            RandomnessManagement random = new RandomnessManagement();

            if (random.is1percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 1));
                mob.setPowered(true);
                mob.setRiptiding(true);
            }

        }


        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, randomness.random(1,2)));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, randomness.random(1,2)));


    }

    private void createCtierNightmareCreeper(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Creeper.class.getSimpleName()));

        Creeper mob = (Creeper) entity;

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        mob.setGliding(true);
        mob.setPersistent(true);
        mob.setPowered(true);
        mob.setRiptiding(true);

        mob.setExplosionRadius(randomness.random(15, 50));

        {
            RandomnessManagement random = new RandomnessManagement();
            
            mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
        }


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

                    if (creeper.getLocation().getY() >= creeper.getWorld().getHighestBlockAt((int) creeper.getLocation().getX(), (int) creeper.getLocation().getZ()).getLocation().getY())
                        creeper.getWorld().spawnEntity(creeper.getLocation(), EntityType.LIGHTNING_BOLT);

                    {
                        RandomnessManagement random = new RandomnessManagement();
                        Optional<WorldTime> currentWorldTime = Main.getTimeManagement().getSpecificWorldTime(world.getName());

                        if (random.is5percent()) {

                            for (int a = 0; a < 2; a++) {
                                if (player.getLocation().getY() >= player.getWorld().getHighestBlockAt((int) player.getLocation().getX(), (int) player.getLocation().getZ()).getLocation().getY())
                                    player.getWorld().spawnEntity(player.getLocation(), EntityType.LIGHTNING_BOLT);
                            }

                            if (currentWorldTime.isPresent()) {
                                RandomnessManagement random_2 = new RandomnessManagement();
                                WorldTime worldTime = currentWorldTime.get();

                                if (random_2.is5percent()) {
                                    if (worldTime.isDayAbove50()) {
                                        for (int a = 0; a < 10; a++) {
                                            if (player.getLocation().getY() >= player.getWorld().getHighestBlockAt((int) player.getLocation().getX(), (int) player.getLocation().getZ()).getLocation().getY())
                                                player.getWorld().spawnEntity(player.getLocation(), EntityType.LIGHTNING_BOLT);
                                        }
                                    }
                                }
                               
                            }
                                
                        }

                    }

            }

        } 

    }
}
