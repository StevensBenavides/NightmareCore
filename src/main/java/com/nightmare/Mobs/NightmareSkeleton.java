package com.nightmare.Mobs;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.Randomness;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

import net.md_5.bungee.api.ChatColor;

public class NightmareSkeleton {

    NightmareSkeleton(Entity entity, YamlConfiguration config) {
        final Randomness randomness = new Randomness();
        final String currentWorldName = entity.getWorld().getName();
        final Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(currentWorldName);
    
        if (currentWorldTime.isPresent()) {
            final WorldTime worldTime = currentWorldTime.get();
    
            if (worldTime.isDayBelow(20)) {
                if (randomness.is1percent()) {
                    createCtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
                if (randomness.is10percent()) {
                    createBtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
                if (randomness.is80percent()) {
                    createAtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
            } 
            else if (worldTime.isDayBelow(50)) {
                if (randomness.is10percent()) {
                    createCtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
                if (randomness.is25percent()) {
                    createBtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
                if (randomness.is60percent()) {
                    createAtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
            }  else if (worldTime.isDayAbove(50)) {
                if (randomness.is35percent()) {
                    createCtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
                if (randomness.is40percent()) {
                    createBtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
                if (randomness.is25percent()) {
                    createAtierNightmareSkeleton(entity, config, randomness);
                    return;
                }
            }
        }
    }


    private void createAtierNightmareSkeleton(Entity entity, YamlConfiguration config, Randomness randomness) {

        Skeleton mob = (Skeleton) entity;
    
        final String name = ChatColor.translateAlternateColorCodes('&', 
            config.getString(Constants.MobATierConfigPath)
                .replace("%mob%", Skeleton.class.getSimpleName()));
    
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        {

            Randomness random = new Randomness();

            if (random.is30percent()) {
                mob.setPersistent(true);
            }

            mob.setCanPickupItems(false);

        }
    
        EntityEquipment equipment = mob.getEquipment();
    
        {
            Randomness random = new Randomness();
    
            if (random.is5percent()) {
                ItemStack helmet = new ItemStack(Material.IRON_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 3));
                helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(1, 3));
                helmet.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setHelmet(helmet);
            } else if (random.is25percent()) {
                ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 3));
                helmet.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setHelmet(helmet);
            }
        }
    
        {
            Randomness random = new Randomness();
    
            if (random.is5percent()) {
                ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
                chest.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 2));
                chest.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(1, 2));
                chest.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chest);
            } else if (random.is25percent()) {
                ItemStack chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                chest.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 2));
                chest.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chest);
            }
        }
    
        {
            Randomness random = new Randomness();
    
            if (random.is5percent()) {
                ItemStack legs = new ItemStack(Material.IRON_LEGGINGS);
                legs.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 2));
                legs.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(1, 2));
                legs.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setLeggings(legs);
            } else if (random.is15percent()) {
                ItemStack legs = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                legs.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 2));
                legs.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setLeggings(legs);
            }
        }
    
        {
            Randomness random = new Randomness();
    
            if (random.is5percent()) {
                ItemStack boots = new ItemStack(Material.IRON_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 2));
                boots.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, random.random(1, 2));
                boots.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(1, 2));
                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setBoots(boots);
            } else if (random.is15percent()) {
                ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 2));
                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setBoots(boots);
            }
        }
    
        {
            Randomness random = new Randomness();
            int speedLevel = random.random(1, 2);
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, speedLevel));
        }
    
        {
            Randomness random = new Randomness();
    
            ItemStack bow = new ItemStack(Material.BOW);
    
            if (random.is5percent()) {
                bow.addUnsafeEnchantment(Enchantment.POWER, random.random(1, 3));
            } else if (random.is15percent()) {
                bow.addUnsafeEnchantment(Enchantment.POWER, random.random(1, 2));
            }
    
            bow.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            equipment.setItemInMainHand(bow);
        }
    
        {
            Randomness random = new Randomness();
            mob.setArrowCooldown(random.random(20, 60));
        }
    
        
        mob.setMetadata("NightmareATierMob", new FixedMetadataValue(Main.getInstance(), true));
    }

    private void createBtierNightmareSkeleton(Entity entity, YamlConfiguration config, Randomness randomness) {

        Skeleton mob = (Skeleton) entity;
    
        final String name = ChatColor.translateAlternateColorCodes('&', 
            config.getString(Constants.MobBTierConfigPath)
                .replace("%mob%", Skeleton.class.getSimpleName()));
    
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
    
        {
            Randomness random = new Randomness();
            if (random.is25percent()) {
                mob.setPersistent(true);
            }
        }
    
        EntityEquipment equipment = mob.getEquipment();
    
        {
            Randomness random = new Randomness();
            
            if (random.is5percent()) {
                ItemStack helmet = new ItemStack(Material.IRON_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(2, 4));
                helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 3));
                helmet.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setHelmet(helmet);
            } else if (random.is15percent()) {
                ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 3));
                helmet.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setHelmet(helmet);
            }
        }
    
        {
            Randomness random = new Randomness();
    
            if (random.is5percent()) {
                ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
                chest.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(2, 4));
                chest.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 3));
                chest.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chest);
            } else if (random.is15percent()) {
                ItemStack chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                chest.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 3));
                chest.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setChestplate(chest);
            }
        }
    
        {
            Randomness random = new Randomness();
            if (random.is5percent()) {
                ItemStack legs = new ItemStack(Material.IRON_LEGGINGS);
                legs.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(2, 4));
                legs.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 3));
                legs.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setLeggings(legs);
            } else if (random.is15percent()) {
                ItemStack legs = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                legs.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 3));
                legs.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setLeggings(legs);
            }
        }
    
        {
            Randomness random = new Randomness();
            if (random.is5percent()) {
                ItemStack boots = new ItemStack(Material.IRON_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(2, 4));
                boots.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, random.random(1, 3));
                boots.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 3));
                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setBoots(boots);
            } else if (random.is15percent()) {
                ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(1, 3));
                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setBoots(boots);
            }
        }
    
        {
            Randomness random = new Randomness();
            ItemStack bow = new ItemStack(Material.BOW);
    
            if (random.is5percent()) {
                int level = random.random(2, 4);
                bow.addUnsafeEnchantment(Enchantment.POWER, level);
                bow.addUnsafeEnchantment(Enchantment.FLAME, random.random(1, 2));
            } else if (random.is15percent()) {
                bow.addUnsafeEnchantment(Enchantment.POWER, random.random(1, 3));
            }
    
            bow.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);  
            equipment.setItemInMainHand(bow);
        }
    
        {
            Randomness random = new Randomness();

            if (random.is5percent()) {  
                ItemStack axe = new ItemStack(Material.IRON_AXE);
                axe.addUnsafeEnchantment(Enchantment.SHARPNESS, random.random(1, 5));
                axe.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
                equipment.setItemInOffHand(axe);
            } else if (random.is15percent()) {
                ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
                equipment.setItemInOffHand(totem);
            }
        }
    
        mob.setArrowCooldown(randomness.random(10, 50));
        mob.setCanPickupItems(false);

        {
            Randomness random = new Randomness();
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, random.random(1, 2))); 
        }
    
        mob.setMetadata("NightmareBTierMob", new FixedMetadataValue(Main.getInstance(), true));
    }

    private void createCtierNightmareSkeleton(Entity entity, YamlConfiguration config, Randomness randomness) {

        Skeleton mob = (Skeleton) entity;
        
        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.MobCTierConfigPath).replace("%mob%", Skeleton.class.getSimpleName()));

        mob.setCustomName(name);

        {
            Randomness random = new Randomness();

            if (random.is25percent()) {
                mob.setCustomNameVisible(true);
            }
        }

        mob.setPersistent(true);

        EntityEquipment equipment = mob.getEquipment();

        {
            
            Randomness random = new Randomness();

            if (random.is5percent()) {
                ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(4, 5));
                helmet.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, random.random(3, 5));
                helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(3, 5));
                helmet.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1); 
                equipment.setHelmet(helmet);

            } else if (random.is25percent()) {
                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(3, 5));
                helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 4));
                helmet.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);  
                equipment.setHelmet(helmet);
            }
        }

        {
            
            Randomness random = new Randomness();

            if (random.is5percent()) {
                ItemStack chest = new ItemStack(Material.NETHERITE_CHESTPLATE);
                chest.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(4, 5));
                chest.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, random.random(3, 5));
                chest.addUnsafeEnchantment(Enchantment.THORNS, random.random(2, 3));
                chest.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(3, 5));
                chest.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1); 
                equipment.setChestplate(chest);

            } else if (random.is25percent()) {
                ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chest.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(3, 5));
                chest.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 4));
                chest.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);  //
                equipment.setChestplate(chest);
            }
        }

        {
            Randomness random = new Randomness();

            if (random.is5percent()) {
                ItemStack legs = new ItemStack(Material.NETHERITE_LEGGINGS);
                legs.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(4, 5));
                legs.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, random.random(3, 5));
                legs.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, random.random(2, 3));
                legs.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(3, 5));
                legs.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);  
                equipment.setLeggings(legs);

            } else if (random.is25percent()) {
                ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS);
                legs.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(3, 5));
                legs.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 4));
                legs.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);  
                equipment.setLeggings(legs);
            }
        }

        {
            Randomness random = new Randomness();

            if (random.is5percent()) {
                ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(4, 5));
                boots.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, random.random(3, 4));
                boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, random.random(2, 3));
                boots.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(3, 5));
                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1); 
                equipment.setBoots(boots);

            } else if (random.is25percent()) {
                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION, random.random(3, 5));
                boots.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, random.random(2, 4));
                boots.addUnsafeEnchantment(Enchantment.UNBREAKING, random.random(2, 4));
                boots.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1); 
                equipment.setBoots(boots);
            }
        }

        mob.setArrowCooldown(randomness.random(10, 20));
        mob.setCanPickupItems(false);
        
        ItemStack bow = equipment.getItemInMainHand();

        {

            
            Randomness random = new Randomness();

            if (random.is15percent()) {

                bow.addUnsafeEnchantment(Enchantment.POWER, random.random(1, 10));
                bow.addUnsafeEnchantment(Enchantment.FLAME, 1);
                
                ItemMeta meta = bow.getItemMeta();
    
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "NightmareSkeletonCTierBow");
                pdc.set(key, PersistentDataType.BOOLEAN, true);
    
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lNightmare Skeleton - Bow &4☠"));
                bow.setItemMeta(meta);
            }

        }

        {
            Randomness random = new Randomness();
            
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, random.random(1, 3)));
        }

        mob.getEquipment().setItemInMainHand(bow);

        mob.setMetadata("NightmareCTierMob", new FixedMetadataValue(Main.getInstance(), true));

    }
    
}
