package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;
import com.nightmare.enchant.EnchantService;

public final class VoidRendEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public VoidRendEnchant() {
        super("void_rend", "Void Rend", 5, false,
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
        if (context.trigger != EnchantTrigger.HIT || context.attacker == null || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(context.attacker.getUniqueId(), key -> new HashMap<>());
        long now = System.currentTimeMillis();

        if (cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        double damage = Main.enchants.getDouble("enchants.void_rend.levels." + context.level + ".damage", 2.0);
        long cooldown = Main.enchants.getLong("enchants.void_rend.levels." + context.level + ".cooldown", 5000L);

        cooldowns.put(id, now + cooldown);

        victim.getWorld().spawnParticle(Particle.PORTAL, victim.getLocation().add(0.0, 1.0, 0.0), 24, 0.4, 0.6, 0.4, 0.1);

        victim.damage(damage, DamageSource.builder(DamageType.MAGIC)
                .withCausingEntity(context.attacker)
                .withDirectEntity(context.attacker)
                .build());
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
