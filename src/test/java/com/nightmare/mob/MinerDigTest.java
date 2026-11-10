package com.nightmare.mob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MinerDigTest {

    @Test
    public void findsWallAlongPath() {
        MinerDig.BlockPos hit = MinerDig.find(0.5D, 64.5D, 0.5D, 8.5D, 64.5D, 0.5D, 6.0D,
                (x, y, z) -> x == 4 && z == 0 && (y == 64 || y == 65));

        assertNotNull(hit);
        assertEquals(4, hit.x());
        assertEquals(64, hit.y());
        assertEquals(0, hit.z());
    }

    @Test
    public void returnsNullWhenPathIsOpen() {
        assertNull(MinerDig.find(0.5D, 64.5D, 0.5D, 8.5D, 64.5D, 0.5D, 6.0D,
                (x, y, z) -> false));
    }

    @Test
    public void ignoresBlocksBeyondRange() {
        assertNull(MinerDig.find(0.5D, 64.5D, 0.5D, 10.5D, 64.5D, 0.5D, 6.0D,
                (x, y, z) -> x == 10 && z == 0 && y == 64));
    }

    @Test
    public void stopsAtFirstBlockingBlock() {
        MinerDig.BlockPos hit = MinerDig.find(0.5D, 64.5D, 0.5D, 8.5D, 64.5D, 0.5D, 6.0D,
                (x, y, z) -> z == 0 && y == 64 && (x == 3 || x == 5));

        assertNotNull(hit);
        assertEquals(3, hit.x());
    }
}
