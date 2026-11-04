package com.nightmare.enchant.impl;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class FrostbiteEnchant extends CustomEnchant {

    public FrostbiteEnchant() {
        super("frostbite", "Frostbite", 5, true,
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
        if (context.trigger != EnchantTrigger.HIT || context.attacker == null || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        int duration = Main.enchants.getInt("enchants.frostbite.levels." + context.level + ".duration", 30);
        int amplifier = Main.enchants.getInt("enchants.frostbite.levels." + context.level + ".amplifier", 0);
        int freezeTicks = Main.enchants.getInt("enchants.frostbite.levels." + context.level + ".freeze-ticks", 0);

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier, true, true, true));

        if (freezeTicks > 0) {
            victim.setFreezeTicks(Math.max(victim.getFreezeTicks(), freezeTicks));
        }

        victim.getWorld().spawnParticle(Particle.SNOWFLAKE, victim.getLocation().add(0.0, 1.0, 0.0), 16, 0.4, 0.6, 0.4, 0.02);
    }

}
