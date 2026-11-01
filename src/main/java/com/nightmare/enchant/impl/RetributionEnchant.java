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

public final class RetributionEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public RetributionEnchant() {
        super("retribution", "Retribution", 5, false,
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
        if (context.trigger != EnchantTrigger.DAMAGED || !(context.victim instanceof LivingEntity victim)
                || !(context.attacker instanceof LivingEntity attacker)) {
            return;
        }

        if (victim.getUniqueId().equals(attacker.getUniqueId())) {
            return;
        }

        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(victim.getUniqueId(), key -> new HashMap<>());

        long now = System.currentTimeMillis();

        if (cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        if (EnchantService.PROCESSING.contains(attacker.getUniqueId())) {
            return;
        }

        double reflect = Main.enchants.getDouble("enchants.retribution.levels." + context.level + ".reflect", 0.08);
        double cap = Main.enchants.getDouble("enchants.retribution.levels." + context.level + ".cap", 1.0);
        double damage = Math.min(cap, context.damage * reflect);

        if (damage <= 0.0) {
            return;
        }

        cooldowns.put(id, now + 1000L);
        attacker.getWorld().spawnParticle(Particle.CRIT, attacker.getLocation().add(0.0, 1.0, 0.0), 12, 0.3, 0.4, 0.3, 0.1);
        attacker.damage(damage, DamageSource.builder(DamageType.THORNS)
                .withCausingEntity(victim)
                .withDirectEntity(victim)
                .build());
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
