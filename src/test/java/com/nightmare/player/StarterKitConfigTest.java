package com.nightmare.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class StarterKitConfigTest {

    private static YamlConfiguration balance;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
    }

    @Test
    public void starterKitIsEnabled() {
        assertTrue(balance.getBoolean("starter-kit.enabled"));
    }

    @Test
    public void starterKitNeverOverlapsPermanentSlots() {
        int backpack = balance.getInt("player-items.backpack-slot");
        int recovery = balance.getInt("player-items.recovery-slot");

        Set<Integer> permanent = new HashSet<>();
        permanent.add(backpack);
        permanent.add(recovery);

        assertEquals(9, backpack);
        assertEquals(10, recovery);

        for (int slot = 0; slot <= 7; slot++) {
            assertTrue("starter kit slot " + slot + " overlaps a permanent slot",
                    !permanent.contains(slot));
        }
    }
}
