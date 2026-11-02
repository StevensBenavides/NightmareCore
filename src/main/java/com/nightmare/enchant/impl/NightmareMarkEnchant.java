package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class NightmareMarkEnchant extends CustomEnchant {

    private static final Map<UUID, Map<UUID, Integer>> STACKS = new HashMap<>();
    private static final Map<UUID, Map<UUID, Long>> EXPIRY = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public NightmareMarkEnchant() {
        super("nightmare_mark", "Nightmare's Mark", 5, false,
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
                Material.MACE,
                Material.BOW);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.HIT || context.attacker == null || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        UUID attackerId = context.attacker.getUniqueId();
        UUID victimId = victim.getUniqueId();
        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(attackerId, key -> new HashMap<>());

        long now = System.currentTimeMillis();

        if (cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        Map<UUID, Integer> stacks = STACKS.computeIfAbsent(attackerId, key -> new HashMap<>());
        Map<UUID, Long> expiry = EXPIRY.computeIfAbsent(attackerId, key -> new HashMap<>());

        int maxStacks = Main.enchants.getInt("enchants.nightmare_mark.max-stacks", 5);
        int stackDuration = Main.enchants.getInt("enchants.nightmare_mark.stack-duration", 120);

        if (expiry.getOrDefault(victimId, 0L) < now) {
            stacks.put(victimId, 0);
        }

        int current = stacks.getOrDefault(victimId, 0) + 1;

        stacks.put(victimId, current);
        expiry.put(victimId, now + stackDuration * 50L);

        victim.getWorld().spawnParticle(Particle.SCULK_SOUL, victim.getLocation().add(0.0, 1.0, 0.0), 6, 0.3, 0.5, 0.3, 0.01);

        if (current < maxStacks) {
            return;
        }

        double damage = Main.enchants.getDouble("enchants.nightmare_mark.damage", 8.0);
        long cooldown = Main.enchants.getLong("enchants.nightmare_mark.cooldown", 15000L);

        stacks.put(victimId, 0);
        cooldowns.put(id, now + cooldown);

        victim.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, victim.getLocation().add(0.0, 1.0, 0.0), 32, 0.6, 0.8, 0.6, 0.05);

        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, true, true));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, true, true));

        victim.damage(damage, DamageSource.builder(DamageType.MAGIC)
                .withCausingEntity(context.attacker)
                .withDirectEntity(context.attacker)
                .build());
    }
    
    public static void resetCooldowns(java.util.UUID uuid) {
        STACKS.remove(uuid);
        EXPIRY.remove(uuid);
        COOLDOWNS.remove(uuid);
    }
}
