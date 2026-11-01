package com.nightmare.core;

import java.util.UUID;

import com.nightmare.enchant.AbyssalCoreService;
import com.nightmare.enchant.impl.AegisEnchant;
import com.nightmare.enchant.impl.BerserkerEnchant;
import com.nightmare.enchant.impl.BlastEdgeEnchant;
import com.nightmare.enchant.impl.BlinkEnchant;
import com.nightmare.enchant.impl.GuardianEnchant;
import com.nightmare.enchant.impl.NightmareMarkEnchant;
import com.nightmare.enchant.impl.RetributionEnchant;
import com.nightmare.enchant.impl.SecondWindEnchant;
import com.nightmare.enchant.impl.StormcallerEnchant;
import com.nightmare.enchant.impl.VenomEnchant;
import com.nightmare.enchant.impl.VoidRendEnchant;
import com.nightmare.relic.AbyssalTendrilsService;
import com.nightmare.relic.SoulHeartService;

public final class CooldownResetService {

    private CooldownResetService() {
    }

    public static void reset(UUID uuid) {
        AbyssalCoreService.resetCooldowns(uuid);
        AbyssalTendrilsService.resetCooldowns(uuid);
        SoulHeartService.resetCooldowns(uuid);
        StormcallerEnchant.resetCooldowns(uuid);
        BlastEdgeEnchant.resetCooldowns(uuid);
        VenomEnchant.resetCooldowns(uuid);
        BerserkerEnchant.resetCooldowns(uuid);
        VoidRendEnchant.resetCooldowns(uuid);
        GuardianEnchant.resetCooldowns(uuid);
        RetributionEnchant.resetCooldowns(uuid);
        SecondWindEnchant.resetCooldowns(uuid);
        BlinkEnchant.resetCooldowns(uuid);
        NightmareMarkEnchant.resetCooldowns(uuid);
        AegisEnchant.resetCooldowns(uuid);
    }

}
