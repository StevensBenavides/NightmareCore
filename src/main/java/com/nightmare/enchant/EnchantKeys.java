package com.nightmare.enchant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.NamespacedKey;

public final class EnchantKeys {

    public static final NamespacedKey BOOK_ID = Objects.requireNonNull(NamespacedKey.fromString("nightmare:ench_book_id"));
    public static final NamespacedKey BOOK_LEVEL = Objects.requireNonNull(NamespacedKey.fromString("nightmare:ench_book_level"));
    public static final NamespacedKey ABYSSAL_ACTIVE = Objects.requireNonNull(NamespacedKey.fromString("nightmare:abyssal_active"));
    public static final NamespacedKey ANTI_HEAL_UNTIL = Objects.requireNonNull(NamespacedKey.fromString("nightmare:anti_heal_until"));
    public static final NamespacedKey ANTI_HEAL_PERCENT = Objects.requireNonNull(NamespacedKey.fromString("nightmare:anti_heal_percent"));

    public static final Map<String, NamespacedKey> KEYS;

    private static final String[] IDS = {
            "lifesteal",
            "stormcaller",
            "blast_edge",
            "frostbite",
            "venom",
            "executioner",
            "berserker",
            "soul_harvest",
            "void_rend",
            "guardian",
            "retribution",
            "second_wind",
            "blink",
            "nightmare_mark",
            "aegis",
            "abyssal_howl",
            "abyssal_core"
    };

    static {
        Map<String, NamespacedKey> keys = new HashMap<>();

        for (String id : IDS) {
            keys.put(id, Objects.requireNonNull(NamespacedKey.fromString("nightmare:ench_" + id)));
        }

        KEYS = Collections.unmodifiableMap(keys);
    }

    private EnchantKeys() {
    }

}
