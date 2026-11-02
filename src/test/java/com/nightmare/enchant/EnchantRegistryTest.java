package com.nightmare.enchant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bukkit.Material;
import org.junit.Test;

public class EnchantRegistryTest {

    @Test
    public void allSeventeenEnchantsAreRegistered() {
        assertEquals(17, EnchantRegistry.ALL.size());
        assertEquals(17, EnchantKeys.KEYS.size());
    }

    @Test
    public void abyssalHowlIsPlayerOnlyAndLevelOne() {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get("abyssal_howl");
        assertTrue(enchant.playerOnly);
        assertEquals(1, enchant.maxLevel);
        assertTrue(enchant.applicable.contains(Material.NETHERITE_SWORD));
        assertTrue(enchant.applicable.contains(Material.BOW));
    }

    @Test
    public void abyssalCoreIsPlayerOnlyChestplateEnchant() {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get("abyssal_core");
        assertTrue(enchant.playerOnly);
        assertEquals(1, enchant.maxLevel);
        assertTrue(enchant.applicable.contains(Material.NETHERITE_CHESTPLATE));
        assertTrue(enchant.applicable.contains(Material.IRON_CHESTPLATE));
        assertTrue(!enchant.applicable.contains(Material.NETHERITE_SWORD));
    }

    @Test
    public void nightmareMarkIsLevelFive() {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get("nightmare_mark");
        assertEquals(5, enchant.maxLevel);
        assertTrue(enchant.applicable.contains(Material.NETHERITE_SWORD));
    }

    @Test
    public void applicabilityMatchesDesign() {
        assertTrue(EnchantRegistry.ENCHANTS.get("stormcaller").applicable.contains(Material.NETHERITE_AXE));
        assertTrue(!EnchantRegistry.ENCHANTS.get("stormcaller").applicable.contains(Material.BOW));
        assertTrue(EnchantRegistry.ENCHANTS.get("venom").applicable.contains(Material.BOW));
        assertTrue(EnchantRegistry.ENCHANTS.get("guardian").applicable.contains(Material.NETHERITE_CHESTPLATE));
    }

    @Test
    public void enchantLevelsUnlockByDay() {
        assertEquals(0, EnchantBookService.maxLevelForDay("lifesteal", 8));
        assertEquals(1, EnchantBookService.maxLevelForDay("lifesteal", 9));
        assertEquals(2, EnchantBookService.maxLevelForDay("lifesteal", 27));
        assertEquals(3, EnchantBookService.maxLevelForDay("lifesteal", 45));
        assertEquals(4, EnchantBookService.maxLevelForDay("lifesteal", 63));
        assertEquals(5, EnchantBookService.maxLevelForDay("lifesteal", 81));
        assertEquals(1, EnchantBookService.maxLevelForDay("abyssal_howl", 0));
        assertEquals(0, EnchantBookService.maxLevelForDay("nightmare_mark", 89));
        assertEquals(5, EnchantBookService.maxLevelForDay("nightmare_mark", 90));
    }

}
