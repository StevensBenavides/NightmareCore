package com.nightmare.mob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class RaidSpawnConfigTest {

    private static YamlConfiguration balance;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
    }

    @Test
    public void spawnsRequireVanillaDarkness() {
        assertEquals(7, balance.getInt("mob-spawn.max-light-level"));
        assertEquals(7, balance.getInt("phase-assault.max-light-level"));
    }

    @Test
    public void surfacePacksStayOutsideArtificialBlocks() {
        int artificialMin = balance.getInt("phase-assault.artificial-spawn-min-distance");
        int artificialMax = balance.getInt("phase-assault.artificial-spawn-max-distance");
        int surfaceMin = balance.getInt("phase-assault.surface-spawn-min-radius");
        int surfaceMax = balance.getInt("phase-assault.surface-spawn-max-radius");

        assertEquals(10, artificialMin);
        assertEquals(20, artificialMax);
        assertTrue(artificialMax > artificialMin);
        assertTrue(surfaceMax > surfaceMin);
        assertTrue(balance.getInt("phase-assault.pack-radius") > 0);
        assertTrue(balance.getInt("phase-assault.artificial-scan-radius") > 0);
    }

    @Test
    public void undergroundPacksHaveTheirOwnRadius() {
        int min = balance.getInt("phase-assault.underground-spawn-min-radius");
        int max = balance.getInt("phase-assault.underground-spawn-max-radius");

        assertTrue(min > 0);
        assertTrue(max > min);
        assertEquals(32, balance.getInt("phase-assault.base-detection-radius"));
    }
}
