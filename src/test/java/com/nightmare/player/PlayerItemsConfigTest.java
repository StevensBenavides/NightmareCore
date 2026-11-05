package com.nightmare.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nightmare.Main;

public class PlayerItemsConfigTest {

    private static YamlConfiguration balance;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
    }

    @Test
    public void permanentSlotsAreConfigured() {
        assertTrue(balance.getBoolean("player-items.enabled"));
        assertEquals(9, balance.getInt("player-items.backpack-slot"));
        assertEquals(10, balance.getInt("player-items.recovery-slot"));
    }

    @Test
    public void backpackSizeIsValid() {
        int size = balance.getInt("player-items.backpack-size");

        assertEquals(27, size);
        assertEquals(0, size % 9);
        assertTrue(size >= 9 && size <= 54);

        YamlConfiguration previous = Main.balance;

        try {
            Main.balance = balance;
            assertEquals(size, BackpackService.size());
        } finally {
            Main.balance = previous;
        }
    }

    @Test
    public void graveDefaultsKeepOwnerLockButNotBlockProtection() {
        assertTrue(balance.getBoolean("player-items.grave.owner-only"));
        assertFalse(balance.getBoolean("player-items.grave.protect"));
    }
}
