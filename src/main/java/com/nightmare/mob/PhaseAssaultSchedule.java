package com.nightmare.mob;

import java.util.ArrayList;
import java.util.List;

import com.nightmare.tier.MobTier;
import com.nightmare.tier.TierSchedule;

/**
 * Pure calendar helpers for the phase assault, extracted so the multi-tier
 * queueing and night window can be tested without a running server.
 */
public final class PhaseAssaultSchedule {

    private PhaseAssaultSchedule() {
    }

    public static List<MobTier> tiersToQueue(long day, int lastIndex) {
        List<MobTier> result = new ArrayList<>();

        for (MobTier tier : TierSchedule.unlocked(day)) {
            if (tier.index > lastIndex) {
                result.add(tier);
            }
        }

        return result;
    }

    public static boolean isNightTime(long time) {
        return time >= 12541L && time <= 23459L;
    }
}
