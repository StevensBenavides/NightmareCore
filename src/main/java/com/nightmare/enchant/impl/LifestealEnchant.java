package com.nightmare.enchant.impl;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class LifestealEnchant extends CustomEnchant {

    public LifestealEnchant() {
        super("lifesteal", "Lifesteal", 5, true,
                Material.MACE,
                Material.TRIDENT,
                Material.BOW,
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.HIT) {
            return;
        }

        if (!(context.attacker instanceof LivingEntity attacker) || !(context.victim instanceof LivingEntity)) {
            return;
        }

        double healPercent = Main.enchants.getDouble("enchants.lifesteal.levels." + context.level + ".heal-percent", 0.08);
        double healCap = Main.enchants.getDouble("enchants.lifesteal.levels." + context.level + ".heal-cap", 2.0);
        double heal = Math.min(healCap, context.damage * healPercent);

        if (heal <= 0.0) {
            return;
        }

        attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + heal));
        attacker.getWorld().spawnParticle(org.bukkit.Particle.HEART, attacker.getLocation().add(0.0, 1.5, 0.0), 3);
    }

}
