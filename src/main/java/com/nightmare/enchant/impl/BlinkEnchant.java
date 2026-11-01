package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class BlinkEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public BlinkEnchant() {
        super("blink", "Blink", 5, false,
                Material.LEATHER_BOOTS,
                Material.CHAINMAIL_BOOTS,
                Material.IRON_BOOTS,
                Material.GOLDEN_BOOTS,
                Material.DIAMOND_BOOTS,
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

        double chance = Main.enchants.getDouble("enchants.blink.levels." + context.level + ".chance", 0.10);

        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        int distance = Main.enchants.getInt("enchants.blink.levels." + context.level + ".distance", 3);
        long cooldown = Main.enchants.getLong("enchants.blink.levels." + context.level + ".cooldown", 20000L);

        Location origin = victim.getLocation();
        Location destination = null;

        for (int attempt = 0; attempt < 12; attempt++) {
            double offsetX = ThreadLocalRandom.current().nextDouble(-distance, distance + 1.0);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-distance, distance + 1.0);

            Location candidate = origin.clone().add(offsetX, 0.0, offsetZ);
            Block feet = candidate.getBlock();
            Block head = candidate.clone().add(0.0, 1.0, 0.0).getBlock();

            if (feet.isPassable() && head.isPassable()) {
                destination = candidate;
                break;
            }
        }

        if (destination == null) {
            return;
        }

        cooldowns.put(id, now + cooldown);
        victim.getWorld().spawnParticle(Particle.PORTAL, origin.clone().add(0.0, 1.0, 0.0), 24, 0.4, 0.6, 0.4, 0.1);
        victim.teleport(destination);
        victim.getWorld().spawnParticle(Particle.PORTAL, destination.clone().add(0.0, 1.0, 0.0), 24, 0.4, 0.6, 0.4, 0.1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, true, true));

        if (context.level >= 5) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, true, false, false));
        }
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
