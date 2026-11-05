package com.nightmare.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public class AltarPlacementTest {

    private static final int SURFACE = 70;

    @Test
    public void findsCaveWithEnoughSpace() {
        TestReader reader = caveReader(5, 0);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 0L, config("cave", 0.35D));

        assertNotNull(site);
        assertEquals(AltarPlacement.Kind.CAVE, site.kind());
        assertEquals(40, site.baseY());
        assertEquals(5, site.headroom());
    }

    @Test
    public void rejectsCaveWithLowCeiling() {
        TestReader reader = caveReader(3, 0);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 0L, config("cave", 0.35D));

        assertNull(site);
    }

    @Test
    public void rejectsCaveWithLiquidInside() {
        TestReader reader = caveReader(5, 0);
        reader.set(2, 41, 0, AltarPlacement.Probe.LIQUID);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 0L, config("cave", 0.35D));

        assertNull(site);
    }

    @Test
    public void rejectsCaveWithoutCeiling() {
        TestReader reader = caveReader(5, SURFACE);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 0L, config("cave", 0.35D));

        assertNull(site);
    }

    @Test
    public void surfaceModeNeverUsesCave() {
        TestReader reader = caveReader(5, 0);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 12345L, config("surface", 1.0D));

        assertNotNull(site);
        assertEquals(AltarPlacement.Kind.SURFACE, site.kind());
        assertEquals(SURFACE, site.baseY());
    }

    @Test
    public void caveModeAlwaysUsesCave() {
        TestReader reader = caveReader(5, 0);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 12345L, config("cave", 0.0D));

        assertNotNull(site);
        assertEquals(AltarPlacement.Kind.CAVE, site.kind());
    }

    @Test
    public void eitherModeFallsBackToSurfaceWhenCaveFails() {
        TestReader reader = caveReader(3, 0);

        AltarPlacement.Site site = AltarPlacement.find(reader, 0, 0, 12345L, config("either", 1.0D));

        assertNotNull(site);
        assertEquals(AltarPlacement.Kind.SURFACE, site.kind());
    }

    @Test
    public void cavePreferenceIsDeterministicAndBounded() {
        assertTrue(AltarPlacement.prefersCave("cave", 0.0D, 0L));
        assertEquals(false, AltarPlacement.prefersCave("surface", 1.0D, 0L));
        assertEquals(false, AltarPlacement.prefersCave("either", 0.0D, 123L));
        assertTrue(AltarPlacement.prefersCave("either", 1.0D, 123L));
        boolean first = AltarPlacement.prefersCave("either", 0.35D, 987654321L);
        boolean second = AltarPlacement.prefersCave("either", 0.35D, 987654321L);
        assertEquals(first, second);
    }

    @Test
    public void caveRoofIsFittedToHeadroom() {
        assertEquals(0, StructureDesigns.fitRoof(3, true, 4));
        assertEquals(2, StructureDesigns.fitRoof(3, true, 5));
        assertEquals(3, StructureDesigns.fitRoof(3, true, 7));
        assertEquals(2, StructureDesigns.fitRoof(2, true, 5));
        assertEquals(1, StructureDesigns.fitRoof(1, false, 0));
    }

    private static YamlConfiguration config(String mode, double caveChance) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("altars.placement.mode", mode);
        config.set("altars.placement.cave-chance", caveChance);
        config.set("altars.cave.min-depth", 12);
        config.set("altars.cave.max-depth", 64);
        config.set("altars.cave.min-height", 5);
        config.set("altars.cave.max-height", 24);
        config.set("altars.cave.min-open-ratio", 0.85D);
        config.set("altars.cave.min-floor-ratio", 0.75D);
        config.set("altars.cave.require-ceiling", true);
        config.set("altars.cave.avoid-liquids", true);
        return config;
    }

    /**
     * Synthetic world: solid stone up to {@link #SURFACE}, a flat cave floor at
     * y=39 and a ceiling starting at y=39+height.
     */
    private static TestReader caveReader(int caveHeight, int ceilingOffset) {
        TestReader reader = new TestReader();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                reader.set(dx, 39, dz, AltarPlacement.Probe.SOLID);

                for (int dy = 0; dy < caveHeight; dy++) {
                    reader.set(dx, 40 + dy, dz, AltarPlacement.Probe.OPEN);
                }

                int ceiling = 40 + caveHeight + ceilingOffset;

                for (int y = 40 + caveHeight; y <= SURFACE; y++) {
                    reader.set(dx, y, dz, y >= ceiling ? AltarPlacement.Probe.SOLID : AltarPlacement.Probe.OPEN);
                }
            }
        }

        return reader;
    }

    private static final class TestReader implements AltarPlacement.BlockReader {
        private final Map<String, AltarPlacement.Probe> blocks = new HashMap<>();

        private void set(int x, int y, int z, AltarPlacement.Probe probe) {
            blocks.put(key(x, y, z), probe);
        }

        @Override
        public AltarPlacement.Probe probeAt(int x, int y, int z) {
            AltarPlacement.Probe probe = blocks.get(key(x, y, z));

            if (probe != null) {
                return probe;
            }

            return y <= SURFACE ? AltarPlacement.Probe.SOLID : AltarPlacement.Probe.OPEN;
        }

        @Override
        public int highestY(int x, int z) {
            return SURFACE;
        }

        @Override
        public int minY() {
            return -64;
        }

        @Override
        public int maxY() {
            return 319;
        }

        private static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }
    }
}
