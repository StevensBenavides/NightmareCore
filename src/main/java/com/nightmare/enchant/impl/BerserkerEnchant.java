package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantKeys;
import com.nightmare.enchant.EnchantTrigger;

public final class BerserkerEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public BerserkerEnchant() {
        super("berserker", "Berserker", 5, true,
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD,
                Material.WOODEN_AXE,
                Material.STONE_AXE,
                Material.IRON_AXE,
                Material.GOLDEN_AXE,
                Material.DIAMOND_AXE,
                Material.NETHERITE_AXE,
                Material.MACE);
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

        double threshold = Main.enchants.getDouble("enchants.berserker.threshold", 0.40);

        if (victim.getHealth() / victim.getMaxHealth() > threshold) {
            return;
        }

        if (context.item != null && context.item.getItemMeta() != null
                && context.item.getItemMeta().getPersistentDataContainer()
                        .get(EnchantKeys.KEYS.get("abyssal_howl"), PersistentDataType.INTEGER) != null) {
            return;
        }

        int duration = Main.enchants.getInt("enchants.berserker.levels." + context.level + ".duration", 60);
        int strength = Main.enchants.getInt("enchants.berserker.levels." + context.level + ".strength", 0);
        int speed = Main.enchants.getInt("enchants.berserker.levels." + context.level + ".speed", 0);
        long cooldown = Main.enchants.getLong("enchants.berserker.levels." + context.level + ".cooldown", 30000L);

        cooldowns.put(id, now + cooldown);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, strength, true, true, true));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, speed, true, true, true));
        victim.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, victim.getLocation().add(0.0, 2.0, 0.0), 8);
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
