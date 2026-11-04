package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class SecondWindEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public SecondWindEnchant() {
        super("second_wind", "Second Wind", 5, true,
                Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS,
                Material.CHAINMAIL_CHESTPLATE,
                Material.CHAINMAIL_LEGGINGS,
                Material.IRON_CHESTPLATE,
                Material.IRON_LEGGINGS,
                Material.GOLDEN_CHESTPLATE,
                Material.GOLDEN_LEGGINGS,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.DAMAGED || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(victim.getUniqueId(), key -> new HashMap<>());
        long now = System.currentTimeMillis();

        if (cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        double threshold = Main.enchants.getDouble("enchants.second_wind.threshold", 0.25);

        if (victim.getHealth() / victim.getMaxHealth() > threshold) {
            return;
        }

        double heal = Main.enchants.getDouble("enchants.second_wind.levels." + context.level + ".heal", 2.0);
        int duration = Main.enchants.getInt("enchants.second_wind.levels." + context.level + ".duration", 40);
        int absorption = Main.enchants.getInt("enchants.second_wind.levels." + context.level + ".absorption", 0);
        long cooldown = Main.enchants.getLong("enchants.second_wind.levels." + context.level + ".cooldown", 60000L);

        cooldowns.put(id, now + cooldown);
        victim.setHealth(Math.min(victim.getMaxHealth(), victim.getHealth() + heal));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, absorption, true, true, true));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 0, true, true, true));

        if (context.level >= 5) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, true, true));
        }

        victim.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation().add(0.0, 1.0, 0.0), 24, 0.5, 0.7, 0.5, 0.1);
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
