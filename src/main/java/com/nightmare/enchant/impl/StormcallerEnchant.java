package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;
import com.nightmare.enchant.EnchantService;

public final class StormcallerEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public StormcallerEnchant() {
        super("stormcaller", "Stormcaller", 5, false,
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
                Material.TRIDENT,
                Material.MACE);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.HIT || context.attacker == null || context.victim == null) {
            return;
        }

        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(context.attacker.getUniqueId(), key -> new HashMap<>());
        long now = System.currentTimeMillis();

        if (cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        double chance = Main.enchants.getDouble("enchants.stormcaller.levels." + context.level + ".chance", 0.05);
        int targets = Main.enchants.getInt("enchants.stormcaller.levels." + context.level + ".targets", 2);
        double damage = Main.enchants.getDouble("enchants.stormcaller.levels." + context.level + ".damage", 3.0);
        long cooldown = Main.enchants.getLong("enchants.stormcaller.cooldown", 2000L);

        if (Math.random() > chance) {
            return;
        }

        cooldowns.put(id, now + cooldown);

        int hit = 0;

        for (Entity entity : context.victim.getWorld().getNearbyEntities(context.victim.getLocation(), 5.0, 5.0, 5.0)) {
            if (hit >= targets) {
                break;
            }

            if (!(entity instanceof LivingEntity living) || entity.getUniqueId().equals(context.attacker.getUniqueId())) {
                continue;
            }

            if (!living.getUniqueId().equals(context.victim.getUniqueId())
                    && EnchantService.PROCESSING_HIT.contains(living.getUniqueId())) {
                continue;
            }

            living.getWorld().strikeLightningEffect(living.getLocation());
            living.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, living.getLocation().add(0.0, 1.0, 0.0), 24);
            living.damage(damage, DamageSource.builder(DamageType.MAGIC)
                    .withCausingEntity(context.attacker)
                    .withDirectEntity(context.attacker)
                    .build());
            hit++;
        }
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
