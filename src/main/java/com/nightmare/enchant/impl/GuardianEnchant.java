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

public final class GuardianEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public GuardianEnchant() {
        super("guardian", "Guardian", 5, false,
                Material.LEATHER_HELMET,
                Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS,
                Material.LEATHER_BOOTS,
                Material.CHAINMAIL_HELMET,
                Material.CHAINMAIL_CHESTPLATE,
                Material.CHAINMAIL_LEGGINGS,
                Material.CHAINMAIL_BOOTS,
                Material.IRON_HELMET,
                Material.IRON_CHESTPLATE,
                Material.IRON_LEGGINGS,
                Material.IRON_BOOTS,
                Material.GOLDEN_HELMET,
                Material.GOLDEN_CHESTPLATE,
                Material.GOLDEN_LEGGINGS,
                Material.GOLDEN_BOOTS,
                Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS,
                Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,
                Material.NETHERITE_BOOTS);
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

        double chance = Main.enchants.getDouble("enchants.guardian.levels." + context.level + ".chance", 0.10);

        if (Math.random() > chance) {
            return;
        }

        int duration = Main.enchants.getInt("enchants.guardian.levels." + context.level + ".duration", 60);
        int absorption = Main.enchants.getInt("enchants.guardian.levels." + context.level + ".absorption", 0);
        long cooldown = Main.enchants.getLong("enchants.guardian.levels." + context.level + ".cooldown", 12000L);

        cooldowns.put(id, now + cooldown);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, absorption, true, true, true));

        if (context.level >= 5) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, true, true, true));
        }

        victim.getWorld().spawnParticle(Particle.ENCHANT, victim.getLocation().add(0.0, 1.0, 0.0), 16, 0.5, 0.7, 0.5, 0.05);
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
