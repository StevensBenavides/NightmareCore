package com.nightmare.relic;

import java.util.Objects;

import org.bukkit.NamespacedKey;

public final class RelicKeys {

    public static final NamespacedKey TENDRILS_AMULET = Objects.requireNonNull(NamespacedKey.fromString("nightmare:abyssal_tendrils"));
    public static final NamespacedKey TENDRILS_OWNER = Objects.requireNonNull(NamespacedKey.fromString("nightmare:abyssal_tendrils_owner"));
    public static final NamespacedKey TENDRILS_BOUND = Objects.requireNonNull(NamespacedKey.fromString("nightmare:abyssal_tendrils_bound"));
    public static final NamespacedKey SOUL_HEART_AMULET = Objects.requireNonNull(NamespacedKey.fromString("nightmare:soul_heart"));
    public static final NamespacedKey SOUL_HEART_OWNER = Objects.requireNonNull(NamespacedKey.fromString("nightmare:soul_heart_owner"));
    public static final NamespacedKey SOUL_HEART_BOUND = Objects.requireNonNull(NamespacedKey.fromString("nightmare:soul_heart_bound"));

    private RelicKeys() {
    }

}
