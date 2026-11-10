package com.nightmare.mob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.nightmare.tier.MobTier;

public class PhaseAssaultScheduleTest {

    @Test
    public void queuesEveryMissedTier() {
        List<MobTier> tiers = PhaseAssaultSchedule.tiersToQueue(27L, 0);

        assertEquals(List.of(MobTier.B, MobTier.C, MobTier.D), tiers);
    }

    @Test
    public void onlyQueuesTiersAfterHighWaterMark() {
        List<MobTier> tiers = PhaseAssaultSchedule.tiersToQueue(90L, 5);

        assertEquals(List.of(MobTier.G, MobTier.H, MobTier.I, MobTier.J, MobTier.K), tiers);
    }

    @Test
    public void emptyBeforeFirstUnlock() {
        assertTrue(PhaseAssaultSchedule.tiersToQueue(-1L, -1).isEmpty());
    }

    @Test
    public void nightWindowIsCorrect() {
        assertFalse(PhaseAssaultSchedule.isNightTime(12540L));
        assertTrue(PhaseAssaultSchedule.isNightTime(12541L));
        assertTrue(PhaseAssaultSchedule.isNightTime(18000L));
        assertTrue(PhaseAssaultSchedule.isNightTime(23459L));
        assertFalse(PhaseAssaultSchedule.isNightTime(23460L));
    }
}
