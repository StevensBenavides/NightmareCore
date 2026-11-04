package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;
import com.nightmare.enchant.EnchantService;

public final class BlastEdgeEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> LAST_HIT = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> HITS = new HashMap<>();

    public BlastEdgeEnchant() {
        super("blast_edge", "Blast Edge", 5, true,
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
                Material.TRIDENT);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.HIT || context.attacker == null || context.victim == null) {
            return;
        }

        UUID attackerId = context.attacker.getUniqueId();

        Map<String, Long> lastHit = LAST_HIT.computeIfAbsent(attackerId, key -> new HashMap<>());
        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(attackerId, key -> new HashMap<>());
        Map<String, Integer> hits = HITS.computeIfAbsent(attackerId, key -> new HashMap<>());

        long now = System.currentTimeMillis();
        long last = lastHit.getOrDefault(id, 0L);

        if (now - last > 4000L) {
            hits.put(id, 0);
        }

        lastHit.put(id, now);

        int hitCount = hits.getOrDefault(id, 0) + 1;

        hits.put(id, hitCount);

        if (hitCount < 4 || cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        cooldowns.put(id, now + Main.enchants.getLong("enchants.blast_edge.cooldown", 3000L));
        hits.put(id, 0);

        double damage = Main.enchants.getDouble("enchants.blast_edge.levels." + context.level + ".damage", 5.0);
        double radius = Main.enchants.getDouble("enchants.blast_edge.levels." + context.level + ".radius", 2.0);

        context.victim.getWorld().spawnParticle(Particle.EXPLOSION, context.victim.getLocation(), 1);
        context.victim.getWorld().playSound(context.victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7F, 1.0F);

        for (Entity entity : context.victim.getWorld().getNearbyEntities(context.victim.getLocation(), radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || entity.getUniqueId().equals(context.attacker.getUniqueId())) {
                continue;
            }

            if (!living.getUniqueId().equals(context.victim.getUniqueId())
                    && EnchantService.PROCESSING_HIT.contains(living.getUniqueId())) {
                continue;
            }

            living.damage(damage, DamageSource.builder(DamageType.MAGIC)
                    .withCausingEntity(context.attacker)
                    .withDirectEntity(context.attacker)
                    .build());
        }
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid); LAST_HIT.remove(uuid); HITS.remove(uuid);
    }
}
