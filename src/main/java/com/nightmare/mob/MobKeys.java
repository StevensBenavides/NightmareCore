package com.nightmare.mob;

import java.util.Objects;

import org.bukkit.NamespacedKey;

public final class MobKeys {

    public static final NamespacedKey MOB_TIER = Objects.requireNonNull(NamespacedKey.fromString("nightmare:mob_tier"));
    public static final NamespacedKey MOB_ELITE = Objects.requireNonNull(NamespacedKey.fromString("nightmare:mob_elite"));
    public static final NamespacedKey MOB_PROCESSED = Objects.requireNonNull(NamespacedKey.fromString("nightmare:mob_processed"));
    public static final NamespacedKey MOB_SUMMON = Objects.requireNonNull(NamespacedKey.fromString("nightmare:mob_summon"));
    public static final NamespacedKey MOB_SPECIES = Objects.requireNonNull(NamespacedKey.fromString("nightmare:mob_species"));
    public static final NamespacedKey MOB_AGGRESSIVE = Objects.requireNonNull(NamespacedKey.fromString("nightmare:mob_aggressive"));
    public static final NamespacedKey PHASE_RAID = Objects.requireNonNull(NamespacedKey.fromString("nightmare:phase_raid"));
    public static final NamespacedKey PHASE_RAID_TIER = Objects.requireNonNull(NamespacedKey.fromString("nightmare:phase_raid_tier"));

    private MobKeys() {
    }

}
