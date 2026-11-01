package com.nightmare.enchant;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;

public abstract class CustomEnchant {

    public final String id;
    public final String displayName;
    public final int maxLevel;
    public final boolean playerOnly;
    public final Set<Material> applicable;

    protected CustomEnchant(String id, String displayName, int maxLevel, boolean playerOnly, Material... applicable) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.playerOnly = playerOnly;
        this.applicable = EnumSet.copyOf(Arrays.asList(applicable));
    }

    public abstract void apply(EnchantContext context);

}
