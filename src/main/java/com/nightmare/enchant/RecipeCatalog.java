package com.nightmare.enchant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.nightmare.relic.AbyssalTendrilsItem;
import com.nightmare.relic.SoulHeartItem;
import com.nightmare.lang.LangService;

public final class RecipeCatalog {

    public static final Map<Integer, Material> TIER_MATERIALS = Map.of(
            1, Material.IRON_BLOCK,
            2, Material.EMERALD_BLOCK,
            3, Material.NETHERITE_SCRAP,
            4, Material.NETHERITE_INGOT,
            5, Material.NETHERITE_BLOCK);

    public static final Map<String, Material> CATALYSTS = Map.ofEntries(
            Map.entry("lifesteal", Material.REDSTONE),
            Map.entry("stormcaller", Material.LIGHTNING_ROD),
            Map.entry("blast_edge", Material.GUNPOWDER),
            Map.entry("frostbite", Material.PACKED_ICE),
            Map.entry("venom", Material.SPIDER_EYE),
            Map.entry("executioner", Material.IRON_SWORD),
            Map.entry("berserker", Material.BLAZE_POWDER),
            Map.entry("soul_harvest", Material.SOUL_SAND),
            Map.entry("void_rend", Material.ENDER_PEARL),
            Map.entry("guardian", Material.SHIELD),
            Map.entry("retribution", Material.CACTUS),
            Map.entry("second_wind", Material.GOLDEN_CARROT),
            Map.entry("blink", Material.ENDER_EYE),
            Map.entry("nightmare_mark", Material.WITHER_SKELETON_SKULL),
            Map.entry("aegis", Material.ARROW));

    public static final Map<Integer, Integer> LEVEL_UNLOCK_DAYS = Map.of(
            1, 9,
            2, 27,
            3, 45,
            4, 63,
            5, 81);

    private RecipeCatalog() {
    }

    public static List<RecipeStep> abyssalSteps() {
        List<RecipeStep> steps = new ArrayList<>();
        List<RecipeIngredient> inkIngredients = new ArrayList<>();

        for (int slot = 0; slot < 4; slot++) {
            inkIngredients.add(new RecipeIngredient(slot, new ItemStack(Material.INK_SAC), 1, label("recipe_ink_sac", "Ink Sac")));
        }

        for (int slot = 4; slot < 8; slot++) {
            inkIngredients.add(new RecipeIngredient(slot, new ItemStack(Material.ECHO_SHARD), 1, label("recipe_echo_shard", "Echo Shard")));
        }

        inkIngredients.add(new RecipeIngredient(8, new ItemStack(Material.NETHERITE_SCRAP), 1, label("recipe_netherite_scrap", "Netherite Scrap")));
        steps.add(new RecipeStep(label("recipe_ink", "Abyssal Ink"), true, inkIngredients, EnchantBookService.createAbyssalInk(),
                List.of("4× " + itemName(Material.INK_SAC), "4× " + itemName(Material.ECHO_SHARD), "1× " + itemName(Material.NETHERITE_SCRAP))));

        List<RecipeIngredient> coreIngredients = new ArrayList<>();

        coreIngredients.add(new RecipeIngredient(0, new ItemStack(Material.NETHER_STAR), 1, label("recipe_nether_star", "Nether Star")));
        coreIngredients.add(new RecipeIngredient(1, new ItemStack(Material.WITHER_SKELETON_SKULL), 1, label("recipe_wither_skeleton_skull", "Wither Skeleton Skull")));

        for (int slot = 5; slot < 9; slot++) {
            coreIngredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalInk(), 1, label("recipe_ink", "Abyssal Ink")));
        }

        steps.add(new RecipeStep(label("recipe_nucleus", "Abyssal Nucleus"), true, coreIngredients, EnchantBookService.createAbyssalNucleus(),
                List.of("1× " + itemName(Material.NETHER_STAR), "1× " + itemName(Material.WITHER_SKELETON_SKULL), "4× " + label("recipe_ink", "Abyssal Ink"))));

        List<RecipeIngredient> howlIngredients = new ArrayList<>();

        howlIngredients.add(new RecipeIngredient(0, new ItemStack(Material.BOOK), 1, label("recipe_book", "Book")));

        for (int slot = 1; slot < 5; slot++) {
            howlIngredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, label("recipe_nucleus", "Abyssal Nucleus")));
        }

        for (int slot = 5; slot < 9; slot++) {
            howlIngredients.add(new RecipeIngredient(slot, new ItemStack(Material.CRYING_OBSIDIAN), 1, label("recipe_crying_obsidian", "Crying Obsidian")));
        }

        steps.add(new RecipeStep(label("recipe_howl", "Abyssal Howl"), true, howlIngredients,
                EnchantBookService.createBook("abyssal_howl", 1),
                List.of("1× " + label("recipe_book", "Book"), "4× " + label("recipe_nucleus", "Abyssal Nucleus"), "4× " + itemName(Material.CRYING_OBSIDIAN))));

        return steps;
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
            ingredients.add(new RecipeIngredient(slot, new ItemStack(tierMaterial), 1, label("recipe_tier", "Tier material")));
        }

        for (int slot : catalystSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(catalyst), 1, label("recipe_catalyst", "Catalyst")));
        }

        ingredients.add(new RecipeIngredient(4, new ItemStack(Material.BOOK), 1, label("recipe_book", "Book")));

        List<String> summary = List.of(
                "4× " + itemName(tierMaterial),
                "4× " + itemName(catalyst),
                "1× " + label("recipe_book", "Book"));

        return new RecipeStep(label("recipe_crafting", "Crafting Recipe"), false, ingredients,
                EnchantBookService.createBook(id, level), summary);
    }

    public static RecipeStep soulHeartRecipe() {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        int[] netheriteSlots = { 0, 2 };
        int[] nucleusSlots = { 1, 7 };
        int[] soulSlots = { 3, 5 };

        for (int slot : netheriteSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.NETHERITE_INGOT), 1, label("recipe_netherite_ingot", "Netherite Ingot")));
        }

        for (int slot : nucleusSlots) {
            ingredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, label("recipe_nucleus", "Abyssal Nucleus")));
        }

        for (int slot : soulSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.SOUL_LANTERN), 1, label("recipe_soul_lantern", "Soul Lantern")));
        }

        ingredients.add(new RecipeIngredient(4, EnchantBookService.createBook("abyssal_core", 1), 1, label("recipe_core", "Abyssal Core")));

        return new RecipeStep(label("recipe_soul", "Soul Heart"), false, ingredients, SoulHeartItem.create(),
                List.of("2× " + itemName(Material.NETHERITE_INGOT), "2× " + label("recipe_nucleus", "Abyssal Nucleus"), "2× " + itemName(Material.SOUL_LANTERN), "1× " + label("recipe_core_book", "Abyssal Core book") + ""));
    }

    public static RecipeStep abyssalCoreRecipe() {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        int[] netheriteSlots = { 0, 2, 6, 8 };
        int[] nucleusSlots = { 1, 3, 5, 7 };

        for (int slot : netheriteSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.NETHERITE_INGOT), 1, label("recipe_netherite_ingot", "Netherite Ingot")));
        }

        for (int slot : nucleusSlots) {
            ingredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, label("recipe_nucleus", "Abyssal Nucleus")));
        }

        ingredients.add(new RecipeIngredient(4, EnchantBookService.createBook("abyssal_howl", 1), 1, label("recipe_howl", "Abyssal Howl")));

        return new RecipeStep(label("recipe_core", "Abyssal Core"), false, ingredients,
                EnchantBookService.createBook("abyssal_core", 1),
                List.of("4× " + itemName(Material.NETHERITE_INGOT), "4× " + label("recipe_nucleus", "Abyssal Nucleus"), "1× " + label("recipe_howl", "Abyssal Howl")));
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

    public static RecipeStep abyssalTendrilsRecipe() {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        int[] echoSlots = { 0, 2, 6, 8 };
        int[] nucleusSlots = { 1, 3, 5, 7 };

        for (int slot : echoSlots) {
            ingredients.add(new RecipeIngredient(slot, new ItemStack(Material.ECHO_SHARD), 1, label("recipe_echo_shard", "Echo Shard")));
        }

        for (int slot : nucleusSlots) {
            ingredients.add(new RecipeIngredient(slot, EnchantBookService.createAbyssalNucleus(), 1, label("recipe_nucleus", "Abyssal Nucleus")));
        }

        ingredients.add(new RecipeIngredient(4, new ItemStack(Material.NAUTILUS_SHELL), 1, label("recipe_nautilus_shell", "Nautilus Shell")));

        return new RecipeStep(label("recipe_tendrils", "Abyssal Tendrils"), false, ingredients, AbyssalTendrilsItem.create(),
                List.of("4× " + itemName(Material.ECHO_SHARD), "4× " + label("recipe_nucleus", "Abyssal Nucleus"), "1× " + itemName(Material.NAUTILUS_SHELL)));
    }

    private static String label(String key, String fallback) {
        return LangService.msg("gui." + key, fallback);
    }

    private static String itemName(Material material) {
        return label("recipe_" + material.name().toLowerCase(), pretty(material));
    }

}


