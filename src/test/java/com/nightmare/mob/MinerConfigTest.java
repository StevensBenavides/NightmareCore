package com.nightmare.mob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class MinerConfigTest {

    private static YamlConfiguration balance;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
    }

    @Test
    public void minerSpawnsScaleWithTier() {
        assertTrue(balance.getBoolean("miner.enabled"));
        assertEquals(0.18D, balance.getDouble("miner.chance"), 0.0001D);
        assertEquals(0.01D, balance.getDouble("miner.chance-per-tier"), 0.0001D);
        assertEquals(0.30D, balance.getDouble("miner.chance-max"), 0.0001D);
    }

    @Test
    public void minerHitsSoftAndTracksClose() {
        assertEquals(2.0D, balance.getDouble("miner.damage"), 0.0001D);
        assertEquals(16.0D, balance.getDouble("miner.follow-range"), 0.0001D);
    }

    @Test
    public void digTuningIsSane() {
        assertEquals(6.0D, balance.getDouble("miner.dig-range"), 0.0001D);
        assertEquals(8000, balance.getInt("miner.dig-cooldown-wood-ms"));
        assertEquals(6000, balance.getInt("miner.dig-cooldown-stone-ms"));
        assertEquals(4000, balance.getInt("miner.dig-cooldown-iron-ms"));
        assertEquals(2, balance.getInt("miner.dig-charges-min"));
        assertEquals(6, balance.getInt("miner.dig-charges-max"));
        assertEquals(false, balance.getBoolean("miner.only-natural-blocks"));
    }

    @Test
    public void pickaxeWeightsPreferWoodOverIron() {
        int wood = balance.getInt("miner.pickaxe-weights.wood");
        int stone = balance.getInt("miner.pickaxe-weights.stone");
        int iron = balance.getInt("miner.pickaxe-weights.iron");

        assertTrue(wood > 0 && stone > 0 && iron > 0);
        assertTrue("wood should be most common", wood > stone && stone > iron);
    }
}
