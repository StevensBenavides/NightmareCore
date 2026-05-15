package com.nightmare.Mobs;

import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.Randomness;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightmareZombie {

    NightmareZombie(Entity entity, YamlConfiguration config) {
        final Randomness randomness = new Randomness();
        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(currentWorldName);
    
        if (currentWorldTime.isPresent()) {
            final WorldTime worldTime = currentWorldTime.get();
    
            if (worldTime.isDayBelow(20)) {
                if (randomness.is1percent()) {
                    createCtierNightmareZombie(entity, config);
                    return;
                }
                if (randomness.is15percent()) {
                    createBtierNightmareZombie(entity, config);
                    return;
                }
                if (randomness.is80percent()) {
                    createAtierNightmareZombie(entity, config);
                    return;
                }
            } 
        
            else if (worldTime.isDayBelow(50)) {
                if (randomness.is10percent()) {
                    createCtierNightmareZombie(entity, config);
                    return;
                }
                if (randomness.is25percent()) {
                    createBtierNightmareZombie(entity, config);
                    return;
                }
                if (randomness.is60percent()) {
                    createAtierNightmareZombie(entity, config);
                    return;
                }
            } 
            
            else if (worldTime.isDayAbove(50)) {
                if (randomness.is35percent()) {
                    createCtierNightmareZombie(entity, config);
                    return;
                }
                if (randomness.is40percent()) {
                    createBtierNightmareZombie(entity, config);
                    return;
                }
                if (randomness.is25percent()) {
                    createAtierNightmareZombie(entity, config);
                    return;
                }
            }
        }

    }

    
    public void createAtierNightmareZombie(Entity entity, YamlConfiguration config) {
        Zombie mob = (Zombie) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', 
            config.getString(Constants.MobATierConfigPath)
            .replace("%mob%", Zombie.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        mob.setVisualFire(false);
        mob.setCanPickupItems(false);
        mob.setPersistent(false);


        {
            Randomness random = new Randomness();
            if (random.is1percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0));
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is1percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is15percent()) {
                if (!mob.hasPotionEffect(PotionEffectType.SPEED)) {
                    mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0));
                }
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is10percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, PotionEffect.INFINITE_DURATION, 1));
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is10percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is20percent()) {
                Material type = new Randomness().is50percent() ? Material.WOODEN_SWORD : Material.WOODEN_AXE;
                mob.getEquipment().setItemInMainHand(new ItemStack(type));
                mob.getEquipment().setItemInMainHandDropChance(0.05f);
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is15percent()) {
                mob.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is15percent()) {
                mob.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
            }
        }
        
        {
            Randomness random = new Randomness();
            if (random.is5percent()) {
                mob.getEquipment().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
                mob.getEquipment().setItemInOffHandDropChance(0.1f);
            }
        }

        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
        
        AttributeInstance followAttr = mob.getAttribute(Attribute.FOLLOW_RANGE);

        if (followAttr != null) {
            followAttr.setBaseValue(30.0);
        }

        mob.setMetadata("NightmareATierMob", new FixedMetadataValue(Main.getInstance(), true));
    }

    private void createBtierNightmareZombie(Entity entity, YamlConfiguration config) {

        Zombie mob = (Zombie) entity;
    
        final String name = ChatColor.translateAlternateColorCodes('&', 
            config.getString(Constants.MobBTierConfigPath)
                .replace("%mob%", Zombie.class.getSimpleName()));
    
        mob.setCustomName(name);
    
        {
            Randomness random = new Randomness();

            if (random.is15percent()) {
                mob.setCustomNameVisible(true);
            }
        }
    
        {
            Randomness random = new Randomness();

            if (random.is15percent()) {
                mob.setPersistent(true);
            }
        }
    
        mob.setVisualFire(false);
        mob.setCanPickupItems(false);
        
        EntityEquipment equipment = mob.getEquipment();

        {

            final Randomness random = new Randomness();

            if (random.is1percent()) {
                int level = random.random(1, 2);
                
                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                chestplate.addUnsafeEnchantment(Enchantment.THORNS, 3);
                chestplate.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chestplate);

            } else if (random.is10percent()) {
                int level = random.random(1, 2);
                
                ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                chestplate.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chestplate);

            } else if (random.is25percent()) { 
                int level = random.random(1, 2);
                
                ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                chestplate.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chestplate);
            }   

        }

        {
            
            final Randomness random = new Randomness();

            if (random.is5percent()) {
                int level = random.random(1, 2);

                ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS, 1);

                leggings.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                leggings.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setLeggings(leggings);   
            } else if (random.is15percent()) { 
                int level = random.random(1, 2);

                ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS, 1);
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                equipment.setLeggings(leggings);   
            }
            
        }

        {

            final Randomness random = new Randomness();


            if (random.is5percent()) {
                int level = random.random(1, 2);
                
                ItemStack boots = new ItemStack(Material.IRON_BOOTS, 1);

                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                equipment.setBoots(boots);

            } else if (random.is15percent()) {
                int level = random.random(1, 2);
                
                ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS, 1);

                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                equipment.setBoots(boots);

            }

        }
    
        {
            Randomness random = new Randomness();
            int speedLevel = random.random(1, 3);

            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, speedLevel));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));

            if (random.is50percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
            }
            
        }

        {

            Randomness random = new Randomness();

            if (random.is50percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
            }
            
        }

        {

            Randomness random = new Randomness();

            if (random.is15percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1));
            }
            
        }

        {

            Randomness random = new Randomness();

            if (random.is25percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 2));
            }

        }

        
        {

            Randomness random = new Randomness();

            if (random.is40percent()) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
            }
            
        }
    
        {
            Randomness random = new Randomness();
    
            ItemStack mainHand;
    
            if (random.is5percent()) {                    
                mainHand = new ItemStack(Material.NETHERITE_AXE);
                mainHand.addUnsafeEnchantment(Enchantment.SHARPNESS, random.random(1, 3));
    
            } else if (random.is15percent()) {            
                mainHand = new ItemStack(Material.DIAMOND_AXE);
                mainHand.addUnsafeEnchantment(Enchantment.SHARPNESS, random.random(1, 3));
    
            } else {                                      
                mainHand = new ItemStack(Material.IRON_AXE);
            }
    
            mainHand.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            mob.getEquipment().setItemInMainHand(mainHand);
        }
    
        {
            Randomness random = new Randomness();

             if (random.is5percent()) {
                ItemStack offHand = new ItemStack(Material.TOTEM_OF_UNDYING);  
                mob.getEquipment().setItemInOffHand(offHand);
            } else if (random.is25percent()) {
                ItemStack offHand = new ItemStack(Material.SHIELD);
                offHand.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(1, 5));
                offHand.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);   
                mob.getEquipment().setItemInOffHand(offHand);
            } 
        }

        mob.setMetadata("NightmareBTierMob", new FixedMetadataValue(Main.getInstance(), true));
    }

    
    private void createCtierNightmareZombie(Entity entity, YamlConfiguration config) {
        Zombie mob = (Zombie) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.MobCTierConfigPath).replace("%mob%", Zombie.class.getSimpleName()));

        mob.setCustomName(name);

        {
            Randomness random = new Randomness();

            if (random.is50percent()) {
                mob.setCustomNameVisible(true);
            }
        }

        mob.setVisualFire(false);
        mob.setCanPickupItems(false);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2));

        mob.setPersistent(true);

        EntityEquipment equipment = mob.getEquipment();

        {

            final Randomness random = new Randomness();

            if (random.is5percent()) {
                int level = random.random(1, 5);
                
                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                chestplate.addUnsafeEnchantment(Enchantment.THORNS, 3);
                chestplate.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chestplate);

            } else if (random.is15percent()) {
                int level = random.random(1, 3);
                
                ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                chestplate.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chestplate);

            } else if (random.is30percent()) { 
                int level = random.random(1, 3);
                
                ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                chestplate.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chestplate);
            }   

        }

        {
            
            final Randomness random = new Randomness();

            if (random.is5percent()) {
                int level = random.random(1, 5);

                ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS, 1);

                leggings.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                leggings.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setLeggings(leggings);   
            } else if (random.is25percent()) { 
                int level = random.random(1, 3);

                ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                equipment.setLeggings(leggings);   
            }
            
        }


        {

            final Randomness random = new Randomness();


            if (random.is5percent()) {
                int level = random.random(1, 10);
                
                ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS, 1);

                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                equipment.setBoots(boots);

            } else if (random.is25percent()) {
                int level = random.random(1, 3);
                
                ItemStack boots = new ItemStack(Material.IRON_BOOTS, 1);

                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, level);
                equipment.setBoots(boots);

            }

        }


        {
            
            final Randomness random = new Randomness();

            if (random.is5percent()) {
                int level = random.random(1, 10);

                ItemStack sword = new ItemStack(Material.NETHERITE_AXE, 1);  
                
                sword.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                sword.addUnsafeEnchantment(Enchantment.PUNCH, 3);
                sword.addUnsafeEnchantment(Enchantment.SHARPNESS, level);

                mob.getEquipment().setItemInMainHand(sword);
            } else if (random.is25percent()) {
                ItemStack sword = new ItemStack(Material.IRON_SWORD, 1);  
                
                sword.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                sword.addUnsafeEnchantment(Enchantment.PUNCH, 2);
                sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

                mob.getEquipment().setItemInMainHand(sword);
            }

        }

        mob.setMetadata("NightmareCTierMob", new FixedMetadataValue(Main.getInstance(), true));

    }

    public static void setCtierConstantEffects(Entity entity, World world) {
        final Zombie zombie = (Zombie) entity;

        if (zombie.hasMetadata("NightmareCTierMob") && !zombie.isDead()) {
            world.spawnParticle(Particle.EXPLOSION, zombie.getLocation(), 60);
            world.playSound(zombie.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 1);
        }
    }
}
