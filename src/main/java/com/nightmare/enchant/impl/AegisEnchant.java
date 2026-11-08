package com.nightmare.enchant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class AegisEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public AegisEnchant() {
        super("aegis", "Aegis", 5, true,
                Material.LEATHER_HELMET,
                Material.LEATHER_CHESTPLATE,
                Material.CHAINMAIL_HELMET,
                Material.CHAINMAIL_CHESTPLATE,
                Material.IRON_HELMET,
                Material.IRON_CHESTPLATE,
                Material.GOLDEN_HELMET,
                Material.GOLDEN_CHESTPLATE,
                Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.DAMAGED || context.event == null
                || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        if (context.event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE
                && context.event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        Map<String, Long> cooldowns = COOLDOWNS.computeIfAbsent(victim.getUniqueId(), key -> new HashMap<>());
        long now = System.currentTimeMillis();

        if (cooldowns.getOrDefault(id, 0L) > now) {
            return;
        }

        double chance = Main.enchants.getDouble("enchants.aegis.levels." + context.level + ".chance", 0.10);

        if (Math.random() > chance) {
            return;
        }

        double reduction = Main.enchants.getDouble("enchants.aegis.levels." + context.level + ".reduction", 0.20);
        long cooldown = Main.enchants.getLong("enchants.aegis.levels." + context.level + ".cooldown", 5000L);

        cooldowns.put(id, now + cooldown);
        context.event.setDamage(context.event.getDamage() * (1.0 - reduction));
        victim.getWorld().spawnParticle(Particle.END_ROD, victim.getLocation().add(0.0, 1.0, 0.0), 16, 0.4, 0.6, 0.4, 0.02);

        if (context.level >= 5) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, true, true));
        }
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
