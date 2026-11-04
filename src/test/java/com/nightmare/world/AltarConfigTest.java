package com.nightmare.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class AltarConfigTest {

    private static YamlConfiguration balance;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
    }

    @Test
    public void altarGenerationIsDense() {
        assertEquals(40, balance.getInt("altars.chance-denominator"));
        assertFalse(balance.getBoolean("altars.only-new-chunks", true));
        assertEquals(5, balance.getInt("altars.common-rolls"));
        assertEquals(150, balance.getInt("altars.min-distance-from-spawn"));
    }

    @Test
    public void chestChancesCoverFullRange() {
        int none = balance.getInt("altars.chest-none-chance");
        int one = balance.getInt("altars.chest-one-chance");
        int two = balance.getInt("altars.chest-two-chance");

        assertEquals(100, none + one + two);
        assertEquals(30, none);
        assertEquals(40, one);
        assertEquals(30, two);
    }

    @Test
    public void lootPoolsAreVaried() {
        String[] themes = {"forge", "soul", "abyss", "nightmare", "relics"};

        for (String theme : themes) {
            List<String> pool = balance.getStringList("altars.loot." + theme);
            assertTrue(theme + " pool must have at least 10 entries", pool.size() >= 10);
        }

        assertEquals(0.10, balance.getDouble("altars.ink-chance"), 0.0001);
        assertEquals(0.10, balance.getDouble("altars.nucleus-chance"), 0.0001);
        assertEquals(0.15, balance.getDouble("altars.book-chance"), 0.0001);
    }
}
