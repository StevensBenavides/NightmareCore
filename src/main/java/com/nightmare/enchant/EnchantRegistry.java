package com.nightmare.enchant;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nightmare.enchant.impl.AbyssalCoreEnchant;
import com.nightmare.enchant.impl.AbyssalHowlEnchant;
import com.nightmare.enchant.impl.AegisEnchant;
import com.nightmare.enchant.impl.BerserkerEnchant;
import com.nightmare.enchant.impl.BlastEdgeEnchant;
import com.nightmare.enchant.impl.BlinkEnchant;
import com.nightmare.enchant.impl.ExecutionerEnchant;
import com.nightmare.enchant.impl.FrostbiteEnchant;
import com.nightmare.enchant.impl.GuardianEnchant;
import com.nightmare.enchant.impl.LifestealEnchant;
import com.nightmare.enchant.impl.NightmareMarkEnchant;
import com.nightmare.enchant.impl.RetributionEnchant;
import com.nightmare.enchant.impl.SecondWindEnchant;
import com.nightmare.enchant.impl.SoulHarvestEnchant;
import com.nightmare.enchant.impl.StormcallerEnchant;
import com.nightmare.enchant.impl.VenomEnchant;
import com.nightmare.enchant.impl.VoidRendEnchant;

public final class EnchantRegistry {

    public static final Map<String, CustomEnchant> ENCHANTS;
    public static final List<CustomEnchant> ALL;

    static {
        Map<String, CustomEnchant> enchants = new LinkedHashMap<>();

        CustomEnchant[] registered = {
                new LifestealEnchant(),
                new StormcallerEnchant(),
                new BlastEdgeEnchant(),
                new FrostbiteEnchant(),
                new VenomEnchant(),
                new ExecutionerEnchant(),
                new BerserkerEnchant(),
                new SoulHarvestEnchant(),
                new VoidRendEnchant(),
                new GuardianEnchant(),
                new RetributionEnchant(),
                new SecondWindEnchant(),
                new BlinkEnchant(),
                new NightmareMarkEnchant(),
                new AegisEnchant(),
                new AbyssalHowlEnchant(),
                new AbyssalCoreEnchant()
        };

        for (CustomEnchant enchant : registered) {
            enchants.put(enchant.id, enchant);
        }

        ENCHANTS = Collections.unmodifiableMap(enchants);
        ALL = List.copyOf(enchants.values());
    }

    private EnchantRegistry() {
    }

}
