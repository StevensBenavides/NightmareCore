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

public final class VenomEnchant extends CustomEnchant {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public VenomEnchant() {
        super("venom", "Venom", 5, false, Material.MACE, Material.BOW, Material.CROSSBOW);
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

        cooldowns.put(id, now + Main.enchants.getLong("enchants.venom.cooldown", 4000L));

        int duration = Main.enchants.getInt("enchants.venom.levels." + context.level + ".duration", 40);
        int amplifier = Main.enchants.getInt("enchants.venom.levels." + context.level + ".amplifier", 0);
        boolean wither = Main.enchants.getBoolean("enchants.venom.levels." + context.level + ".wither", false);
        double antiHeal = Main.enchants.getDouble("enchants.venom.levels." + context.level + ".anti-heal", 0.0);

        victim.addPotionEffect(new PotionEffect(wither ? PotionEffectType.WITHER : PotionEffectType.POISON,
                duration, amplifier, true, true, true));
        victim.getWorld().spawnParticle(Particle.WITCH, victim.getLocation().add(0.0, 1.0, 0.0), 16, 0.4, 0.6, 0.4, 0.02);

        if (antiHeal > 0.0) {
            victim.getPersistentDataContainer().set(EnchantKeys.ANTI_HEAL_UNTIL,
                    PersistentDataType.LONG, now + duration * 50L);
            victim.getPersistentDataContainer().set(EnchantKeys.ANTI_HEAL_PERCENT,
                    PersistentDataType.DOUBLE, antiHeal);
        }
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
    }
}
