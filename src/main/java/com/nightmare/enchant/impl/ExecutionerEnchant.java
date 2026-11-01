package com.nightmare.enchant.impl;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;
import com.nightmare.enchant.EnchantTrigger;

public final class ExecutionerEnchant extends CustomEnchant {

    public ExecutionerEnchant() {
        super("executioner", "Executioner", 5, false,
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
                Material.MACE);
    }

    @Override
    public void apply(EnchantContext context) {
        if (context.trigger != EnchantTrigger.HIT || context.event == null || !(context.victim instanceof LivingEntity victim)) {
            return;
        }

        double threshold = Main.enchants.getDouble("enchants.executioner.levels." + context.level + ".threshold", 0.20);
        double bonus = Main.enchants.getDouble("enchants.executioner.levels." + context.level + ".bonus", 0.10);

        if (victim.getHealth() / victim.getMaxHealth() > threshold) {
            return;
        }

        context.event.setDamage(context.event.getDamage() * (1.0 + bonus));
    }

}
