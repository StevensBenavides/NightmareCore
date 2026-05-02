package com.nightmare.Mobs;

import com.nightmare.ConfigEvaluator;
import com.nightmare.Constants;
import com.nightmare.Main;
import com.nightmare.RandomnessManagement;
import net.md_5.bungee.api.ChatColor;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class NightmareZombie {

    NightmareZombie(Entity entity, YamlConfiguration config) {

        try {
            ConfigEvaluator.evaluate("MobSpawning", config); 
        } catch (Exception e) {
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            e.printStackTrace();
        }  

        final RandomnessManagement random = new RandomnessManagement();

        if (random.is50percent())
            createAtierNightmareZombie(entity, config);

        if (random.is25percent())
            createBtierNightmareZombie(entity, config);

        if (random.is5percent())
            createCtierNightmareZombie(entity, config);

    }

    private void createCtierNightmareZombie(Entity entity, YamlConfiguration config) {
        Zombie mob = (Zombie) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_c.getValue()).replace("%mob%", Zombie.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setVisualFire(false);
        mob.setCanPickupItems(false);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 5));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2));

        mob.setPersistent(true);

        EntityEquipment equipment = mob.getEquipment();

        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS, 1);

        ItemStack sword = new ItemStack(Material.IRON_SWORD, 1);

        chestplate.addEnchantment(Enchantment.PROTECTION, 1);
        leggings.addEnchantment(Enchantment.PROTECTION, 1);
        boots.addEnchantment(Enchantment.PROTECTION, 1);

        sword.addEnchantment(Enchantment.SHARPNESS, 3);

        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);

        mob.getEquipment().setItemInMainHand(sword);
    }


    private void createBtierNightmareZombie(Entity entity, YamlConfiguration config) {

        Zombie mob = (Zombie) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_b.getValue()).replace("%mob%", Zombie.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setVisualFire(false);
        mob.setCanPickupItems(false);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 4));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 2));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3));

        mob.setPersistent(true);

        ItemStack sword = new ItemStack(Material.DIAMOND_AXE, 1);

        sword.addEnchantment(Enchantment.SHARPNESS, 3);

        mob.getEquipment().setItemInMainHand(sword);
    }

    public void createAtierNightmareZombie(Entity entity, YamlConfiguration config) {

        Zombie mob = (Zombie) entity;

        final String name = ChatColor.translateAlternateColorCodes('&', config.getString(Constants.Mobs.config_mobs_name_a.getValue()).replace("%mob%", Zombie.class.getSimpleName()));

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);

        mob.setVisualFire(false);
        mob.setCanPickupItems(false);

        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
    }

    public static void setCtierConstantEffects(Entity entity, World world) {

        YamlConfiguration settings = Main.getSettings();

        final Zombie zombie = (Zombie) entity;
        final String name = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(settings.getString(Constants.Mobs.config_mobs_name_c.getValue())).replace("%mob%", Zombie.class.getSimpleName()));

        if (zombie.getCustomName() != null && zombie.getCustomName().equalsIgnoreCase(name) && !zombie.isDead()) {
            world.spawnParticle(Particle.EXPLOSION, zombie.getLocation(), 60);
            world.playSound(zombie.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 1);
        }
    }
}
