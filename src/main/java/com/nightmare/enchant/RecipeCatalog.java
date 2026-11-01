package com.nightmare.enchant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.nightmare.relic.AbyssalTendrilsItem;
import com.nightmare.relic.SoulHeartItem;

public final class RecipeCatalog {

    public static final Map<Integer, Material> TIER_MATERIALS = Map.of(
            1, Material.IRON_BLOCK,
            2, Material.EMERALD_BLOCK,
            3, Material.NETHERITE_SCRAP,
            4, Material.NETHERITE_INGOT,
            5, Material.NETHER_STAR);

    public static final Map<String, Material> CATALYSTS = Map.ofEntries(
            Map.entry("lifesteal", Material.REDSTONE),
            Map.entry("stormcaller", Material.LIGHTNING_ROD),
            Map.entry("blast_edge", Material.GUNPOWDER),
            Map.entry("frostbite", Material.BLUE_ICE),
            Map.entry("venom", Material.SPIDER_EYE),
            Map.entry("executioner", Material.IRON_AXE),
            Map.entry("berserker", Material.BLAZE_POWDER),
            Map.entry("soul_harvest", Material.SOUL_SAND),
            Map.entry("void_rend", Material.ENDER_PEARL),
            Map.entry("guardian", Material.SHIELD),
            Map.entry("retribution", Material.CACTUS),
            Map.entry("second_wind", Material.GOLDEN_APPLE),
            Map.entry("blink", Material.ENDER_EYE),
            Map.entry("nightmare_mark", Material.WITHER_SKELETON_SKULL),
            Map.entry("aegis", Material.ARROW));

    public static final Map<Integer, Integer> LEVEL_UNLOCK_DAYS = Map.of(
            1, 27,
            2, 54,
            3, 72,
            4, 81,
            5, 90);

    private RecipeCatalog() {
    }

    public static RecipeStep bookRecipe(String id, int level) {
        Material tierMaterial = TIER_MATERIALS.get(level);
        Material catalyst = CATALYSTS.get(id);

        if (tierMaterial == null || catalyst == null) {
            return null;
        }

        List<RecipeIngredient> ingredients = new ArrayList<>();

        int[] tierSlots = { 0, 2, 6, 8 };
        int[] catalystSlots = { 1, 3, 5, 7 };

        for (int slot : tierSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(tierMaterial), 1, "Tier material"));
        }

        for (int slot : catalystSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(catalyst), 1, "Catalyst"));
        }

        ingredients.add(new RecipeIngredient(4, new ItemStack(Material.BOOK), 1, "Book"));

        List<String> summary = List.of(
                "4× " + pretty(tierMaterial),
                "4× " + pretty(catalyst),
                "1× Book");

        return new RecipeStep("Crafting Recipe", false, ingredients,
                EnchantBookService.createBook(id, level), summary);
    }

    public static List<RecipeStep> abyssalSteps() {
        List<RecipeStep> steps = new ArrayList<>();
        List<RecipeIngredient> inkIngredients = new ArrayList<>();

        for (int slot = 0; slot < 4; slot++) {
            inkIngredients.add(new RecipeIngredient(slot, new ItemStack(Material.INK_SAC), 1, "Ink Sac"));
        }

        for (int slot = 4; slot < 8; slot++) {
            inkIngredients.add(new RecipeIngredient(slot, new ItemStack(Material.ECHO_SHARD), 1, "Echo Shard"));
        }

        inkIngredients.add(new RecipeIngredient(8, new ItemStack(Material.NETHERITE_SCRAP), 1, "Netherite Scrap"));
        steps.add(new RecipeStep("Abyssal Ink", true, inkIngredients, EnchantBookService.createAbyssalInk(),
                List.of("4× Ink Sac", "4× Echo Shard", "1× Netherite Scrap")));

        List<RecipeIngredient> coreIngredients = new ArrayList<>();

        coreIngredients.add(new RecipeIngredient(0, new ItemStack(Material.NETHER_STAR), 1, "Nether Star"));
        coreIngredients.add(new RecipeIngredient(1, new ItemStack(Material.WITHER_SKELETON_SKULL), 1, "Wither Skeleton Skull"));

        for (int slot = 5; slot < 9; slot++) {
            coreIngredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalInk(), 1, "Abyssal Ink"));
        }

        steps.add(new RecipeStep("Abyssal Nucleus", true, coreIngredients, EnchantBookService.createAbyssalNucleus(),
                List.of("1× Nether Star", "1× Wither Skeleton Skull", "4× Abyssal Ink")));

        List<RecipeIngredient> howlIngredients = new ArrayList<>();

        howlIngredients.add(new RecipeIngredient(0, new ItemStack(Material.BOOK), 1, "Book"));

        for (int slot = 1; slot < 5; slot++) {
            howlIngredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, "Abyssal Nucleus"));
        }

        for (int slot = 5; slot < 9; slot++) {
            howlIngredients.add(new RecipeIngredient(slot, new ItemStack(Material.CRYING_OBSIDIAN), 1, "Crying Obsidian"));
        }

        steps.add(new RecipeStep("Abyssal Howl", true, howlIngredients,
                EnchantBookService.createBook("abyssal_howl", 1),
                List.of("1× Book", "4× Abyssal Nucleus", "4× Crying Obsidian")));

        return steps;
    }

    public static RecipeStep abyssalCoreRecipe() {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        int[] netheriteSlots = { 0, 2, 6, 8 };
        int[] nucleusSlots = { 1, 3, 5, 7 };

        for (int slot : netheriteSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.NETHERITE_INGOT), 1, "Netherite Ingot"));
        }

        for (int slot : nucleusSlots) {
            ingredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, "Abyssal Nucleus"));
        }

        ingredients.add(new RecipeIngredient(4, EnchantBookService.createBook("abyssal_howl", 1), 1, "Abyssal Howl"));

        return new RecipeStep("Abyssal Core", false, ingredients,
                EnchantBookService.createBook("abyssal_core", 1),
                List.of("4× Netherite Ingot", "4× Abyssal Nucleus", "1× Abyssal Howl"));
    }

    public static RecipeStep abyssalTendrilsRecipe() {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        int[] echoSlots = { 0, 2, 6, 8 };
        int[] nucleusSlots = { 1, 3, 5, 7 };

        for (int slot : echoSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.ECHO_SHARD), 1, "Echo Shard"));
        }

        for (int slot : nucleusSlots) {
            ingredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, "Abyssal Nucleus"));
        }

        ingredients.add(new RecipeIngredient(4, new ItemStack(Material.NAUTILUS_SHELL), 1, "Nautilus Shell"));

        return new RecipeStep("Abyssal Tendrils", false, ingredients, AbyssalTendrilsItem.create(),
                List.of("4× Echo Shard", "4× Abyssal Nucleus", "1× Nautilus Shell"));
    }

    public static RecipeStep soulHeartRecipe() {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        int[] netheriteSlots = { 0, 2 };
        int[] nucleusSlots = { 1, 7 };
        int[] soulSlots = { 3, 5 };

        for (int slot : netheriteSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.NETHERITE_INGOT), 1, "Netherite Ingot"));
        }

        for (int slot : nucleusSlots) {
            ingredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, "Abyssal Nucleus"));
        }

        for (int slot : soulSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.SOUL_LANTERN), 1, "Soul Lantern"));
        }

        ingredients.add(new RecipeIngredient(4, EnchantBookService.createBook("abyssal_core", 1), 1, "Abyssal Core"));

        return new RecipeStep("Soul Heart", false, ingredients, SoulHeartItem.create(),
                List.of("2× Netherite Ingot", "2× Abyssal Nucleus", "2× Soul Lantern", "1× Abyssal Core book"));
    }

    private static String pretty(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(part.charAt(0)));
            builder.append(part.substring(1));
        }

        return builder.toString();
    }

}
