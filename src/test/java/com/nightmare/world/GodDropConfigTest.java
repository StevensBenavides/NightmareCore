package com.nightmare.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class GodDropConfigTest {

    private static YamlConfiguration balance;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
    }

    @Test
    public void scheduleStopsAfterLastTier() {
        assertEquals(9, balance.getInt("god-drops.every-days"));
        assertEquals(90, balance.getInt("god-drops.max-day"));
        assertTrue(balance.getBoolean("god-drops.enabled"));
        assertTrue(balance.getBoolean("god-drops.announce"));
    }

    @Test
    public void locationIsBounded() {
        assertEquals(3000, balance.getInt("god-drops.max-distance-from-spawn"));
        assertEquals(100, balance.getInt("god-drops.border-margin"));
    }

    @Test
    public void lootLeansTowardsBooksWithoutFlooding() {
        int common = balance.getInt("god-drops.common-rolls");
        int books = balance.getInt("god-drops.book-rolls");
        double extra = balance.getDouble("god-drops.extra-book-chance");

        assertEquals(2, common);
        assertEquals(2, books);
        assertEquals(0.35D, extra, 0.0001D);
        assertTrue("books should be at least as common as normal loot", books + extra > common);
        assertEquals(3, balance.getInt("god-drops.book-max-level"));
        assertEquals(0.10D, balance.getDouble("god-drops.ink-chance"), 0.0001D);
        assertEquals(0.10D, balance.getDouble("god-drops.nucleus-chance"), 0.0001D);
        assertEquals(0.10D, balance.getDouble("god-drops.lore-book-chance"), 0.0001D);
    }
}
