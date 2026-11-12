package com.nightmare.tier;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.nightmare.Main;

public final class TierSchedule {

    private TierSchedule() {
    }

    @SuppressWarnings("null")
    public static MobTier roll(long day, Random random) {
        if (random.nextDouble() < 0.10) {
            return null;
        }

        Map<MobTier, Double> weights = new EnumMap<>(MobTier.class);
        double totalWeight = 0.0;

        for (MobTier tier : MobTier.values()) {
            if (tier.unlockDay > day) {
                continue;
            }

            double weight = Main.balance == null
                    ? tier.baseWeight
                    : Main.balance.getDouble("tiers." + tier.name() + ".weight", tier.baseWeight);
            long daysSinceUnlock = day - tier.unlockDay;

            if (daysSinceUnlock < 9) {
                weight *= 3.0 - (2.0 * daysSinceUnlock / 9.0);
            }

            if (tier == MobTier.K && day >= 90) {
                weight *= 2.0;
            }

            weights.put(tier, weight);
            totalWeight += weight;
        }

        if (totalWeight <= 0.0) {
            return null;
        }

        double selectedWeight = random.nextDouble() * totalWeight;

        for (Map.Entry<MobTier, Double> entry : weights.entrySet()) {
            selectedWeight -= entry.getValue();

            if (selectedWeight <= 0.0) {
                return entry.getKey();
            }
        }

        return MobTier.A;
    }

    public static List<MobTier> unlocked(long day) {
        List<MobTier> tiers = new ArrayList<>();

        if (day < 0) {
            return tiers;
        }

        for (MobTier tier : MobTier.values()) {
            if (tier.unlockDay <= day) {
                tiers.add(tier);
            }
        }

        return tiers;
    }

    public static MobTier newestUnlocked(long day) {
        MobTier newest = null;

        if (day < 0) {
            return null;
        }

        for (MobTier tier : MobTier.values()) {
            if (tier.unlockDay <= day) {
                newest = tier;
            }
        }

        return newest;
    }

    public static int nextUnlockDay(long day) {
        if (day < 0) {
            return 0;
        }

        for (MobTier tier : MobTier.values()) {
            if (tier.unlockDay > day) {
                return tier.unlockDay;
            }
        }

        return 100;
    }

}
