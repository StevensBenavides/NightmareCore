package com.nightmare.enchant.impl;

import org.bukkit.Material;

import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantContext;

public final class AbyssalCoreEnchant extends CustomEnchant {

    public AbyssalCoreEnchant() {
        super("abyssal_core", "Abyssal Core", 1, true,
                Material.LEATHER_CHESTPLATE,
                Material.CHAINMAIL_CHESTPLATE,
                Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE,
                Material.DIAMOND_CHESTPLATE,
                Material.NETHERITE_CHESTPLATE);
    }

    @Override
    public void apply(EnchantContext context) {
    }

}
