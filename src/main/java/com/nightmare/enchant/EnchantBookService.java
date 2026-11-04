package com.nightmare.enchant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.nightmare.item.ItemKeys;
import com.nightmare.item.ItemModels;

public final class EnchantBookService {

    public static final Map<NamespacedKey, String> RECIPE_ENCHANTS = new HashMap<>();
    public static final Map<NamespacedKey, Integer> RECIPE_LEVELS = new HashMap<>();
    public static final Map<Integer, Integer> LEVEL_UNLOCK_DAYS = RecipeCatalog.LEVEL_UNLOCK_DAYS;

    private EnchantBookService() {
    }

    public static void registerRecipes(Plugin plugin) {
        for (CustomEnchant enchant : EnchantRegistry.ALL) {
            if (enchant.id.equals("abyssal_howl") || enchant.id.equals("abyssal_core")) {
                continue;
            }

            for (int level = 1; level <= enchant.maxLevel; level++) {
                if (enchant.id.equals("nightmare_mark") && level < enchant.maxLevel) {
                    continue;
                }

                NamespacedKey key = new NamespacedKey(plugin, enchant.id + "_" + level);
                ShapedRecipe recipe = new ShapedRecipe(key, createBook(enchant.id, level));

                recipe.shape("TCT", "CBC", "TCT");
                recipe.setIngredient('T', RecipeCatalog.TIER_MATERIALS.get(level));
                recipe.setIngredient('C', RecipeCatalog.CATALYSTS.get(enchant.id));
                recipe.setIngredient('B', Material.BOOK);

                if (Bukkit.getRecipe(key) != null) {
                    Bukkit.removeRecipe(key);
                }

                Bukkit.addRecipe(recipe);

                RECIPE_ENCHANTS.put(key, enchant.id);
                RECIPE_LEVELS.put(key, level);
            }
        }

        NamespacedKey inkKey = new NamespacedKey(plugin, "abyssal_ink");
        ShapelessRecipe ink = new ShapelessRecipe(inkKey, createAbyssalInk());

        ink.addIngredient(4, Material.INK_SAC);
        ink.addIngredient(4, Material.ECHO_SHARD);
        ink.addIngredient(1, Material.NETHERITE_SCRAP);

        if (Bukkit.getRecipe(inkKey) != null) {
            Bukkit.removeRecipe(inkKey);
        }

        Bukkit.addRecipe(ink);

        NamespacedKey coreKey = new NamespacedKey(plugin, "abyssal_nucleus");
        ShapelessRecipe core = new ShapelessRecipe(coreKey, createAbyssalNucleus());

        core.addIngredient(1, Material.NETHER_STAR);
        core.addIngredient(1, Material.WITHER_SKELETON_SKULL);

        for (int i = 0; i < 4; i++) {
            core.addIngredient(new RecipeChoice.ExactChoice(createAbyssalInk()));
        }

        if (Bukkit.getRecipe(coreKey) != null) {
            Bukkit.removeRecipe(coreKey);
        }

        Bukkit.addRecipe(core);

        NamespacedKey howlKey = new NamespacedKey(plugin, "abyssal_howl");
        ShapelessRecipe howl = new ShapelessRecipe(howlKey, createBook("abyssal_howl", 1));

        howl.addIngredient(1, Material.BOOK);

        for (int i = 0; i < 4; i++) {
            howl.addIngredient(new RecipeChoice.ExactChoice(createAbyssalNucleus()));
        }

        howl.addIngredient(4, Material.CRYING_OBSIDIAN);

        if (Bukkit.getRecipe(howlKey) != null) {
            Bukkit.removeRecipe(howlKey);
        }

        Bukkit.addRecipe(howl);

        RECIPE_ENCHANTS.put(howlKey, "abyssal_howl");
        RECIPE_LEVELS.put(howlKey, 1);

        NamespacedKey coreBookKey = new NamespacedKey(plugin, "abyssal_core");
        ShapedRecipe coreBook = new ShapedRecipe(coreBookKey, createBook("abyssal_core", 1));

        coreBook.shape("NUN", "UBU", "NUN");
        coreBook.setIngredient('N', Material.NETHERITE_INGOT);
        coreBook.setIngredient('U', new RecipeChoice.ExactChoice(createAbyssalNucleus()));
        coreBook.setIngredient('B', new RecipeChoice.ExactChoice(createBook("abyssal_howl", 1)));

        if (Bukkit.getRecipe(coreBookKey) != null) {
            Bukkit.removeRecipe(coreBookKey);
        }

        Bukkit.addRecipe(coreBook);

        RECIPE_ENCHANTS.put(coreBookKey, "abyssal_core");
        RECIPE_LEVELS.put(coreBookKey, 1);
    }

    public static ItemStack createBook(String id, int level) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);

        if (enchant == null) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(EnchantLoreService.buildBookName(id, level));
        meta.setLore(EnchantLoreService.buildCraftableBookLore(id, level));

        meta.getPersistentDataContainer().set(EnchantKeys.BOOK_ID, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(EnchantKeys.BOOK_LEVEL, PersistentDataType.INTEGER, level);

        setCustomModel(meta, id);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createAbyssalInk() {
        ItemStack item = new ItemStack(Material.INK_SAC);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Abyssal Ink");
        meta.setLore(com.nightmare.lang.LangService.loreLines("items.ink_lore"));

        meta.getPersistentDataContainer().set(ItemKeys.ABYSSAL_INK, PersistentDataType.BOOLEAN, true);

        setCustomModel(meta, "abyssal_ink");

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createAbyssalNucleus() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Abyssal Nucleus");
        meta.setLore(com.nightmare.lang.LangService.loreLines("items.nucleus_lore"));

        meta.getPersistentDataContainer().set(ItemKeys.ABYSSAL_CORE, PersistentDataType.BOOLEAN, true);

        setCustomModel(meta, "abyssal_nucleus");

        item.setItemMeta(meta);

        return item;
    }

    private static void setCustomModel(ItemMeta meta, String id) {
        ItemModels.applyMeta(meta, id);
    }

    public static int maxLevelForDay(String id, long day) {
        if (id.equals("abyssal_howl") || id.equals("abyssal_core")) {
            return day >= 0 ? 1 : 0;
        }

        if (id.equals("nightmare_mark")) {
            return day >= 90 ? 5 : 0;
        }

        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);

        if (enchant == null) {
            return 0;
        }

        int max = 0;

        for (int level = 1; level <= enchant.maxLevel; level++) {
            if (day >= LEVEL_UNLOCK_DAYS.getOrDefault(level, Integer.MAX_VALUE)) {
                max = level;
            }
        }

        return max;
    }

}
