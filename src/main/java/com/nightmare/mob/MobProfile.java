package com.nightmare.mob;

import java.util.List;

import org.bukkit.entity.EntityType;

public final class MobProfile {

    public final EntityType type;
    public final MobCategory category;
    public final int maxTierIndex;
    public final boolean gear;
    public final List<String> powers;

    public MobProfile(EntityType type, MobCategory category, int maxTierIndex, boolean gear, List<String> powers) {
        this.type = type;
        this.category = category;
        this.maxTierIndex = maxTierIndex;
        this.gear = gear;
        this.powers = List.copyOf(powers);
    }

}
