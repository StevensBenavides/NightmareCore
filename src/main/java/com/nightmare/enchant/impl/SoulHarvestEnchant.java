package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class SoulHarvestEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Integer>> STACKS = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> EXPIRY = new HashMap<>();

    public SoulHarvestEnchant() {
        super("soul_harvest", "Soul Harvest", 5, true,
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
        if (context.attacker == null) {
            return;
        }

        UUID attackerId = context.attacker.getUniqueId();
        Map<String, Integer> stacks = STACKS.computeIfAbsent(attackerId, key -> new HashMap<>());
        Map<String, Long> expiry = EXPIRY.computeIfAbsent(attackerId, key -> new HashMap<>());

        long now = System.currentTimeMillis();

        if (expiry.getOrDefault(id, 0L) < now) {
            stacks.put(id, 0);
        }

        if (context.trigger == EnchantTrigger.HIT && context.event != null && stacks.getOrDefault(id, 0) > 0) {
            double perStack = Main.enchants.getDouble("enchants.soul_harvest.levels." + context.level + ".per-stack", 0.02);
            context.event.setDamage(context.event.getDamage() * (1.0 + perStack * stacks.getOrDefault(id, 0)));

            return;
        }

        if (context.trigger != EnchantTrigger.KILL) {
            return;
        }

        int maxStacks = Main.enchants.getInt("enchants.soul_harvest.levels." + context.level + ".max-stacks", 3);
        int duration = Main.enchants.getInt("enchants.soul_harvest.levels." + context.level + ".duration", 200);
        double heal = Main.enchants.getDouble("enchants.soul_harvest.levels." + context.level + ".heal", 1.0);

        stacks.put(id, Math.min(maxStacks, stacks.getOrDefault(id, 0) + 1));
        expiry.put(id, now + duration * 50L);

        if (context.attacker instanceof LivingEntity living) {
            living.setHealth(Math.min(living.getMaxHealth(), living.getHealth() + heal));
        }
    }

    public static void resetCooldowns(java.util.UUID uuid) {
        STACKS.remove(uuid);
        EXPIRY.remove(uuid);
    }

}
