package com.nightmare.Mobs;

import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.Randomness;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightmareCreeper {

    NightmareCreeper(Entity entity, YamlConfiguration config) {

        final Randomness randomness = new Randomness();

        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(currentWorldName);

        if (currentWorldTime.isPresent()) {
            
            final WorldTime worldTime = currentWorldTime.get();

            if (worldTime.isDayBelow(10)) {
                if (randomness.is1percent()) {
                    createCtierNightmareCreeper(entity, config, randomness);
                    return;
                }
    
                if (randomness.is15percent()) {
                    createBtierNightmareCreeper(entity, config, randomness);
                    return;
                }
                  
                if (randomness.is80percent())  {
                    createAtierNightmareCreeper(entity, config, randomness);
                    return;
                }    
            } else if (worldTime.isDayBelow(20)) {
                if (randomness.is5percent()) {
                    createCtierNightmareCreeper(entity, config, randomness);
                    return;
                }
    
                if (randomness.is20percent()) {
                    createBtierNightmareCreeper(entity, config, randomness);
                    return;
                }
                  
                if (randomness.is70percent())  {
                    createAtierNightmareCreeper(entity, config, randomness);
                    return;
                }    
            } else if (worldTime.isDayBelow(30)) {
                if (randomness.is10percent()) {
                    createCtierNightmareCreeper(entity, config, randomness);
                    return;
                }
    
                if (randomness.is25percent()) {
                    createBtierNightmareCreeper(entity, config, randomness);
                    return;
                }
                  
                if (randomness.is50percent())  {
                    createAtierNightmareCreeper(entity, config, randomness);
                    return;
                }    
            } else if (worldTime.isDayBelow(50)) {
                if (randomness.is20percent()) {
                    createCtierNightmareCreeper(entity, config, randomness);
                    return;
                }
    
                if (randomness.is25percent()) {
                    createBtierNightmareCreeper(entity, config, randomness);
                    return;
                }
                  
                if (randomness.is50percent())  {
                    createAtierNightmareCreeper(entity, config, randomness);
                    return;
                }    
            }

            if (worldTime.isDayAbove50()) {
                if (randomness.is40percent()) {
                    createCtierNightmareCreeper(entity, config, randomness);
                    return;
                }
    
                if (randomness.is40percent()) {
                    createBtierNightmareCreeper(entity, config, randomness);
                    return;
                }
                  
                if (randomness.is20percent())  {
                    createAtierNightmareCreeper(entity, config, randomness);
                    return;
                }    
            }
    
        }

       
   
    }

    private void createAtierNightmareCreeper(Entity entity, YamlConfiguration config, Randomness randomness) {
        
        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.MobATierConfigPath).replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(false);
        
        {

            Randomness random = new Randomness();
            
            if (random.is1percent()) {
                mob.setGliding(true);
                mob.setPowered(true);
                mob.setArrowsInBody(15);
                mob.setSilent(true);
                
                mob.setExplosionRadius(randomness.random(50, 100));
            }
    
        }

        mob.setExplosionRadius(randomness.random(5, 10));

        mob.setMetadata("NightmareATierMob", new FixedMetadataValue(Main.getInstance(), true));

    }

    private void createBtierNightmareCreeper(Entity entity, YamlConfiguration config, Randomness randomness) {

        Creeper mob = (Creeper) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.MobBTierConfigPath).replace("%mob%", Creeper.class.getSimpleName()));

        mob.setCustomName(name);

        {

            Randomness random = new Randomness();

            if (random.is25percent()) {
                mob.setCustomNameVisible(true);
            } 

        }

        mob.setGliding(true);
        mob.setExplosionRadius(randomness.random(5, 20));

        
        {

            Randomness random = new Randomness();

            if (random.is1percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 1));
                mob.setPowered(true);
                mob.setRiptiding(true);
            }

        }

        AttributeInstance speedAttribute = mob.getAttribute(Attribute.MOVEMENT_SPEED);

        speedAttribute.setBaseValue(speedAttribute.getDefaultValue() + randomness.random(0.1F, 0.4F));

        mob.setMetadata("NightmareBTierMob", new FixedMetadataValue(Main.getInstance(), true));

    }

    private void createCtierNightmareCreeper(Entity entity, YamlConfiguration config, Randomness randomness) {

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.MobCTierConfigPath).replace("%mob%", Creeper.class.getSimpleName()));

        Creeper mob = (Creeper) entity;

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        mob.setGliding(true);
        mob.setPersistent(true);
        mob.setPowered(true);
        mob.setRiptiding(true);

        mob.setExplosionRadius(randomness.random(15, 70));

        AttributeInstance speedAttribute = mob.getAttribute(Attribute.MOVEMENT_SPEED);

        speedAttribute.setBaseValue(speedAttribute.getDefaultValue() + randomness.random(0.3F,0.5F));

        mob.setMetadata("NightmareCTierMob", new FixedMetadataValue(Main.getInstance(), true));
        
    }


    public static void setCtierConstantEffects(Entity entity, World world) {

        final Creeper creeper = (Creeper) entity;
      
        if (creeper.hasMetadata("NightmareCTierMob") && !creeper.isDead() && creeper.getTarget() == null) {

            for (Entity entity_2 : creeper.getNearbyEntities(15.0, 15.0, 15.0).stream().filter(entity_ -> entity_ instanceof Player).toList()) {
                Player player = (Player) entity_2;

                if (player.getGameMode() == GameMode.SURVIVAL) 
                    creeper.setTarget(player);

                    if (creeper.getLocation().getY() >= creeper.getWorld().getHighestBlockAt((int) creeper.getLocation().getX(), (int) creeper.getLocation().getZ()).getLocation().getY())
                    {
                        Randomness random = new Randomness();
                        Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(world.getName());

                        if (random.is5percent()) {

                            for (int a = 0; a < 2; a++) {
                                if (player.getLocation().getY() >= player.getWorld().getHighestBlockAt((int) player.getLocation().getX(), (int) player.getLocation().getZ()).getLocation().getY())
                                    player.getWorld().spawnEntity(player.getLocation(), EntityType.LIGHTNING_BOLT);
                            }

                            if (currentWorldTime.isPresent()) {
                                Randomness random_2 = new Randomness();
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
