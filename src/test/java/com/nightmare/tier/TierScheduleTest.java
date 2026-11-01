package com.nightmare.tier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.Test;

public class TierScheduleTest {

    @Test
    public void unlockScheduleIsCorrect() {
        assertEquals(MobTier.A, TierSchedule.newestUnlocked(0));
        assertEquals(MobTier.B, TierSchedule.newestUnlocked(9));
        assertEquals(MobTier.C, TierSchedule.newestUnlocked(18));
        assertEquals(MobTier.D, TierSchedule.newestUnlocked(27));
        assertEquals(MobTier.E, TierSchedule.newestUnlocked(36));
        assertEquals(MobTier.F, TierSchedule.newestUnlocked(45));
        assertEquals(MobTier.G, TierSchedule.newestUnlocked(54));
        assertEquals(MobTier.H, TierSchedule.newestUnlocked(63));
        assertEquals(MobTier.I, TierSchedule.newestUnlocked(72));
        assertEquals(MobTier.J, TierSchedule.newestUnlocked(81));
        assertEquals(MobTier.K, TierSchedule.newestUnlocked(90));
        assertEquals(MobTier.K, TierSchedule.newestUnlocked(100));
    }

    @Test
    public void noUnlockGapsAtDayBoundaries() {
        assertEquals(9, TierSchedule.nextUnlockDay(0));
        assertEquals(9, TierSchedule.nextUnlockDay(8));
        assertEquals(18, TierSchedule.nextUnlockDay(9));
        assertEquals(100, TierSchedule.nextUnlockDay(90));
        assertEquals(100, TierSchedule.nextUnlockDay(100));

        List<MobTier> day50 = TierSchedule.unlocked(50);
        assertTrue(day50.contains(MobTier.F));
        assertTrue(!day50.contains(MobTier.G));
    }

    @Test
    public void onlyUnlockedTiersCanRoll() {
        Random random = new Random(12345L);

        for (int i = 0; i < 5000; i++) {
            MobTier tier = TierSchedule.roll(0, random);
            assertTrue(tier == null || tier == MobTier.A);
        }

        for (int i = 0; i < 5000; i++) {
            MobTier tier = TierSchedule.roll(50, random);
            assertTrue(tier == null || tier.index <= MobTier.F.index);
        }
    }

    @Test
    public void noTierChanceIsAboutTenPercent() {
        Random random = new Random(98765L);
        int noTier = 0;

        for (int i = 0; i < 100000; i++) {
            if (TierSchedule.roll(100, random) == null) {
                noTier++;
            }
        }

        double chance = noTier / 100000.0;
        assertTrue(chance > 0.08);
        assertTrue(chance < 0.12);
    }

    @Test
    public void kTierIsReachableAtDayNinety() {
        Random random = new Random(5555L);
        boolean foundK = false;

        for (int i = 0; i < 20000; i++) {
            MobTier tier = TierSchedule.roll(90, random);

            if (tier == MobTier.K) {
                foundK = true;
                break;
            }
        }

        assertTrue(foundK);
    }

    @Test
    public void emptyScheduleReturnsNoTier() {
        assertNull(TierSchedule.newestUnlocked(-1));
        assertNull(TierSchedule.roll(-1, new Random(1L)));
        assertNotNull(TierSchedule.unlocked(-1));
    }

    @Test
    public void enchantLevelsMatchTierBrackets() {
        assertEquals(0, MobTier.A.enchantLevel);
        assertEquals(0, MobTier.B.enchantLevel);
        assertEquals(0, MobTier.C.enchantLevel);
        assertEquals(1, MobTier.D.enchantLevel);
        assertEquals(1, MobTier.E.enchantLevel);
        assertEquals(1, MobTier.F.enchantLevel);
        assertEquals(2, MobTier.G.enchantLevel);
        assertEquals(2, MobTier.H.enchantLevel);
        assertEquals(3, MobTier.I.enchantLevel);
        assertEquals(4, MobTier.J.enchantLevel);
        assertEquals(5, MobTier.K.enchantLevel);
    }

}
