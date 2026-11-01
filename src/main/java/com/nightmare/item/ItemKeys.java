package com.nightmare.item;

import java.util.Objects;

import org.bukkit.NamespacedKey;

public final class ItemKeys {

    public static final NamespacedKey CUSTOM_ITEM = Objects.requireNonNull(NamespacedKey.fromString("nightmare:custom_item"));
    public static final NamespacedKey ITEM_TIER = Objects.requireNonNull(NamespacedKey.fromString("nightmare:item_tier"));
    public static final NamespacedKey ITEM_LEGENDARY = Objects.requireNonNull(NamespacedKey.fromString("nightmare:item_legendary"));
    public static final NamespacedKey LORE_BASE = Objects.requireNonNull(NamespacedKey.fromString("nightmare:lore_base"));
    public static final NamespacedKey ABYSSAL_INK = Objects.requireNonNull(NamespacedKey.fromString("nightmare:abyssal_ink"));
    public static final NamespacedKey ABYSSAL_CORE = Objects.requireNonNull(NamespacedKey.fromString("nightmare:abyssal_core"));

    private ItemKeys() {
    }

}
