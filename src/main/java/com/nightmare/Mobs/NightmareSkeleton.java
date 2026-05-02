package com.nightmare.Mobs;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.ConfigEvaluator;
import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;

import net.md_5.bungee.api.ChatColor;

public class NightmareSkeleton {

    NightmareSkeleton(Entity entity, YamlConfiguration config) {

        try {
            ConfigEvaluator.evaluate("MobSpawning", config); 
        } catch (Exception e) {
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            e.printStackTrace();
        }  

        final RandomnessManagement randomness = new RandomnessManagement();

        if (randomness.is50percent())
            createAtierNightmareSkeleton(entity, config, randomness);

        if (randomness.is25percent())
            createBtierNightmareSkeleton(entity, config, randomness);

        if (randomness.is5percent()) 
            createCtierNightmareSkeleton(entity, config, randomness);

    }
    
    private void createAtierNightmareSkeleton(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Skeleton mob = (Skeleton) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_a.getValue()).replace("%mob%", Skeleton.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);

        EntityEquipment equipment = mob.getEquipment();

        helmet.addUnsafeEnchantment(Enchantment.PROTECTION, 5);
        helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);

        equipment.setHelmet(helmet);

        mob.setArrowCooldown(20);
        mob.setCanPickupItems(false);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 6));

    }

    
    private void createBtierNightmareSkeleton(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Skeleton mob = (Skeleton) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", Skeleton.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        mob.setPersistent(true);

        ItemStack helmet = new ItemStack(Material.IRON_HELMET, 1);
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS, 1);

        EntityEquipment equipment = mob.getEquipment();

        helmet.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        chestplate.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        chestplate.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        leggings.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        leggings.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        boots.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        boots.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);

        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);

        ItemStack bow = equipment.getItemInMainHand();

        if (bow.getType() == Material.BOW) {
            bow.addUnsafeEnchantment(Enchantment.POWER, 7);
            bow.addEnchantment(Enchantment.FLAME, 1);
            
            ItemMeta meta = bow.getItemMeta();

            bow.setItemMeta(meta);
        }

        mob.getEquipment().setItemInMainHand(bow);
        mob.setArrowCooldown(randomness.random(20, 50));
        mob.setCanPickupItems(false);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 5));

    }

    private void createCtierNightmareSkeleton(Entity entity, YamlConfiguration config, RandomnessManagement randomness) {

        Skeleton mob = (Skeleton) entity;
        
        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Skeleton.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        mob.setPersistent(true);

        EntityEquipment equipment = mob.getEquipment();

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);

        helmet.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        helmet.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        chestplate.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        chestplate.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        leggings.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        leggings.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        boots.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
        boots.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);

        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);

        mob.setArrowCooldown(randomness.random(10, 20));
        mob.setCanPickupItems(false);
        
        ItemStack bow = equipment.getItemInMainHand();

        if (bow.getType() == Material.BOW) {
            bow.addUnsafeEnchantment(Enchantment.POWER, 10);
            bow.addEnchantment(Enchantment.FLAME, 1);
            
            ItemMeta meta = bow.getItemMeta();

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lNightmare Skeleton - Bow &4☠"));

            bow.setItemMeta(meta);
        }

        mob.getEquipment().setItemInMainHand(bow);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
    }
    
}
