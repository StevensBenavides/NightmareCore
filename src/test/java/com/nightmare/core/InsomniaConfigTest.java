package com.nightmare.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class InsomniaConfigTest {

    private static YamlConfiguration balance;
    private static YamlConfiguration en;
    private static YamlConfiguration es;

    @BeforeClass
    public static void load() {
        balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
        en = YamlConfiguration.loadConfiguration(new File("src/main/resources/lang/en/messages.yml"));
        es = YamlConfiguration.loadConfiguration(new File("src/main/resources/lang/es/messages.yml"));
    }

    @Test
    public void scheduleIsSemiFrequentAndBounded() {
        assertTrue(balance.getBoolean("insomnia.enabled"));
        assertEquals(20, balance.getInt("insomnia.interval-min-minutes"));
        assertEquals(45, balance.getInt("insomnia.interval-max-minutes"));
        assertTrue(balance.getInt("insomnia.interval-max-minutes")
                > balance.getInt("insomnia.interval-min-minutes"));

        assertEquals(2, balance.getInt("insomnia.duration-min-minutes"));
        assertEquals(6, balance.getInt("insomnia.duration-max-minutes"));
        assertTrue(balance.getInt("insomnia.duration-max-minutes")
                > balance.getInt("insomnia.duration-min-minutes"));

        assertTrue(balance.getBoolean("insomnia.only-at-night"));
        assertTrue(balance.getBoolean("insomnia.announce"));
    }

    @Test
    public void messagesExistInBothLanguages() {
        assertTrue(en.contains("insomnia.start"));
        assertTrue(en.contains("insomnia.end"));
        assertTrue(en.contains("insomnia.denied"));
        assertTrue(es.contains("insomnia.start"));
        assertTrue(es.contains("insomnia.end"));
        assertTrue(es.contains("insomnia.denied"));
    }
}
