package com.nightmare.enchant.impl;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;
import com.nightmare.relic.TendrilsEffects;

public final class AbyssalHowlEnchant extends CustomEnchant {

    public AbyssalHowlEnchant() {
        super("abyssal_howl", "Abyssal Howl", 1, true,
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
                Material.BOW,
                Material.CROSSBOW,
                Material.TRIDENT);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger == EnchantTrigger.KILL && context.victim != null) {
            TendrilsEffects.howlKill(context.victim.getLocation());
            return;
        }

        if (context.trigger != EnchantTrigger.HIT || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        victim.getWorld().spawnParticle(Particle.SQUID_INK, victim.getLocation().add(0.0, 1.0, 0.0), 24, 0.5, 0.7, 0.5, 0.03);
        victim.getWorld().spawnParticle(Particle.SCULK_SOUL, victim.getLocation().add(0.0, 1.0, 0.0), 12, 0.4, 0.6, 0.4, 0.02);
        victim.getWorld().spawnParticle(Particle.DUST, victim.getLocation().add(0.0, 1.0, 0.0), 16,
                0.4, 0.6, 0.4, 0.01, new Particle.DustOptions(Color.BLACK, 1.2F));
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.6F, 0.4F);
    }

}
