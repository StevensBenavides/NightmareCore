package com.nightmare.enchant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nightmare.Main;

public class EnchantLoreTest {

    private static final String[] VANILLA_TERMS = {
            "strength", "speed", "slowness", "poison", "wither", "absorption",
            "blindness", "weakness", "hunger", "bad omen", "resistance",
            "invisibility", "jump boost", "freeze", "lightning", "blocks:", "trim", "netherite"
    };

    @BeforeClass
    public static void loadConfigs() {
        Main.enchants = YamlConfiguration.loadConfiguration(new File("src/main/resources/enchants.yml"));
        Main.balance = YamlConfiguration.loadConfiguration(new File("src/main/resources/balance.yml"));
        Main.enchantLore = YamlConfiguration.loadConfiguration(new File("src/main/resources/enchant-lore.yml"));
    }

    @Test
    public void craftableBookHasOnlyLevelAndApplicability() {
        List<String> lore = EnchantLoreService.buildCraftableBookLore("lifesteal", 1);
        String all = String.join(" ", lore);

        assertTrue(all.contains("Level"));
        assertTrue(all.contains("Applicable on:"));
        assertFalse(all.contains("Healing:"));
        assertFalse(all.contains("pale blade"));
        assertFalse(all.contains("━"));
    }

    @Test
    public void abyssalCraftableBookIsLegendary() {
        String name = EnchantLoreService.buildBookName("abyssal_howl", 1);
        List<String> lore = EnchantLoreService.buildCraftableBookLore("abyssal_howl", 1);
        String visible = String.join(" ", lore).replaceAll("§.", "");

        assertTrue(name.contains("❖"));
        assertTrue(visible.contains("Legendary"));
        assertTrue(visible.contains("Any weapon"));
        assertFalse(visible.toLowerCase().contains("strength"));
        assertFalse(visible.toLowerCase().contains("speed"));
    }

    @Test
    public void helpListShowsLevelOneLore() {
        List<String> lore = EnchantLoreService.buildHelpListLore("lifesteal");
        String all = String.join(" ", lore).replaceAll("§.", "");

        assertTrue(all.contains("pale blade"));
        assertTrue(all.contains("Levels:"));
        assertTrue(all.contains("Applicable:"));
        assertFalse(all.contains("━"));
    }

    @Test
    public void helpDetailContainsLoreStatsApplicabilityAndEffect() {
        List<String> lore = EnchantLoreService.buildHelpDetailLore("lifesteal", 1);
        String all = String.join(" ", lore).replaceAll("§.", "");

        assertTrue(all.contains("pale blade"));
        assertTrue(all.contains("Healing:"));
        assertTrue(all.contains("Applicable on:"));
        assertTrue(all.contains("draws vitality"));
    }

    @Test
    public void helpDetailVariesLoreByLevel() {
        String levelOne = String.join(" ", EnchantLoreService.buildHelpDetailLore("void_rend", 1)).replaceAll("§.", "");
        String levelFive = String.join(" ", EnchantLoreService.buildHelpDetailLore("void_rend", 5)).replaceAll("§.", "");

        assertFalse(levelOne.equals(levelFive));
        assertTrue(levelOne.contains("narrow tear"));
        assertTrue(levelFive.contains("world folds inward"));
    }

    @Test
    public void abyssalCoreHelpShowsBothAttacksWithoutVanillaTerms() {
        List<String> lore = EnchantLoreService.buildHelpDetailLore("abyssal_core", 1);
        String all = String.join(" ", lore).replaceAll("§.", "");

        assertTrue(all.contains("Abyssal Burst"));
        assertTrue(all.contains("Sneak + Left Click (any time)"));
        assertTrue(all.contains("Abyssal Cataclysm"));
        assertTrue(all.contains("Sneak + Right Click"));
        assertTrue(all.contains("50 blocks"));
        assertTrue(all.contains("8x8 blocks"));
        assertTrue(all.contains("150 HP"));
        assertTrue(all.contains("60s"));
        assertTrue(all.contains("120s"));
        assertFalse(all.contains("Trim"));
        assertFalse(all.contains("Lightning"));
        assertFalse(all.contains("Blocks"));
        assertFalse(all.contains("Strength"));
        assertFalse(all.contains("Speed"));
    }

    @Test
    public void allHelpEntriesExistAndAvoidVanillaTerms() {
        for (CustomEnchant enchant : EnchantRegistry.ALL) {
            List<String> listLore = EnchantLoreService.buildHelpListLore(enchant.id);
            assertFalse(enchant.id + " has empty list lore", listLore.isEmpty());

            for (int level = 1; level <= enchant.maxLevel; level++) {
                List<String> detail = EnchantLoreService.buildHelpDetailLore(enchant.id, level);
                String visible = String.join(" ", detail).replaceAll("§.", "").toLowerCase();

                assertFalse(enchant.id + " has no lore at level " + level, detail.isEmpty());
                assertFalse(enchant.id + " still contains a separator", visible.contains("━"));

                for (String term : VANILLA_TERMS) {
                    assertFalse(enchant.id + " contains vanilla term " + term, visible.contains(term));
                }

                for (String line : detail) {
                    String plain = line.replaceAll("§.", "");
                    assertTrue(enchant.id + " line too long: " + plain, plain.length() <= 48);
                }
            }
        }
    }

    @Test
    public void sanitizeBaseLoreStopsAtCustomSection() {
        List<String> base = EnchantLoreService.sanitizeBaseLore(List.of(
                "Base one", "Base two", "", "Custom Enchantments:", "- Lifesteal III", ""));

        assertEquals(2, base.size());
        assertEquals("Base one", base.get(0));
        assertEquals("Base two", base.get(1));
    }

    @Test
    public void sanitizeBaseLoreStopsAtLegacySeparator() {
        List<String> base = EnchantLoreService.sanitizeBaseLore(List.of(
                "Base one", "§8━━━━━━━━━━━━━━━━", "- Lifesteal III"));

        assertEquals(1, base.size());
        assertEquals("Base one", base.get(0));
    }

}
