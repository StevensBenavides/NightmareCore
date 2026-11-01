package com.nightmare.gui;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantBookService;
import com.nightmare.enchant.EnchantLoreService;
import com.nightmare.item.ItemModels;
import com.nightmare.enchant.EnchantRegistry;
import com.nightmare.enchant.RecipeCatalog;
import com.nightmare.enchant.RecipeStep;

public final class GuiManager {

    public static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static final int BACK_SLOT = 49;
    public static final int CLOSE_SLOT = 45;
    public static final int PREV_SLOT = 48;
    public static final int NEXT_SLOT = 50;

    private GuiManager() {
    }

    public static void openMain(Player player) {
        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.MAIN, null, 0, 0, null, null, null, false, 0);
        Inventory inventory = create(holder, 27, "§8Nightmare Guide");

        inventory.setItem(12, GuiItemFactory.item(Material.ENCHANTED_BOOK, "§6§l✦ Enchantments",
                List.of("§eClick to browse enchantments, levels and recipes.")));
        inventory.setItem(14, GuiItemFactory.item(Material.ENDER_EYE, "§5§l✦ Relics",
                List.of("§eClick to view the relic and recipe.")));

        player.openInventory(inventory);
    }

    public static void openEnchantList(Player player) {
        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.ENCHANTS, null, 0, 0, null, null, null, false, 0);
        Inventory inventory = create(holder, 54, "§8Enchantments");

        for (int index = 0; index < EnchantRegistry.ALL.size(); index++) {
            CustomEnchant enchant = EnchantRegistry.ALL.get(index);

            ItemStack icon = ItemModels.apply(GuiItemFactory.item(Material.ENCHANTED_BOOK,
                    EnchantLoreService.buildBookName(enchant.id, 1),
                    EnchantLoreService.buildHelpListLore(enchant.id)), enchant.id);

            inventory.setItem(CONTENT_SLOTS[index], icon);
        }

        applyNavigation(inventory, false, false);

        player.openInventory(inventory);
    }

    public static void openEnchantDetail(Player player, String id, int level) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);

        if (enchant == null) {
            return;
        }

        int safeLevel = Math.min(Math.max(level, 1), enchant.maxLevel);

        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.ENCHANT_DETAIL, id, safeLevel, 0,
                null, null, null, false, 0);

        Inventory inventory = create(holder, 54, "§8" + enchant.displayName + " " + roman(safeLevel));

        long day = player.getWorld().getFullTime() / 24000L;

        inventory.setItem(4, ItemModels.apply(GuiItemFactory.item(Material.ENCHANTED_BOOK,
                EnchantLoreService.buildBookName(id, safeLevel),
                EnchantLoreService.buildHelpDetailLore(id, safeLevel)), id));

        for (int buttonLevel = 1; buttonLevel <= 5; buttonLevel++) {
            inventory.setItem(18 + buttonLevel, GuiItemFactory.levelButton(enchant, buttonLevel, safeLevel, day));
        }

        inventory.setItem(31, GuiItemFactory.item(Material.CRAFTING_TABLE, "§eView Recipe",
                List.of("§7See the crafting grid", "§7for this enchantment level.")));

        applyNavigation(inventory, false, false);

        player.openInventory(inventory);
    }

    public static void openRecipe(Player player, String id, int level, int step) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);

        if (enchant == null) {
            return;
        }

        boolean abyssal = id.equals("abyssal_howl");
        boolean core = id.equals("abyssal_core");

        List<RecipeStep> steps = abyssal ? RecipeCatalog.abyssalSteps() : null;

        int safeStep = Math.min(Math.max(step, 1), abyssal ? steps.size() : 1);

        RecipeStep recipe = abyssal ? steps.get(safeStep - 1)
                : core ? RecipeCatalog.abyssalCoreRecipe()
                : RecipeCatalog.bookRecipe(id, level);

        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.RECIPE, id, level, 0, null, null, null,
                false, safeStep);

        String title = abyssal
                ? "§8Abyssal Chain " + safeStep + "/" + steps.size()
                : "§8Recipe: " + enchant.displayName + (core ? "" : " " + roman(level));

        Inventory inventory = create(holder, 54, title);

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, GuiItemFactory.border());
        }

        for (var ingredient : recipe.ingredients()) {
            int guiSlot = 10 + (ingredient.slot() / 3) * 9 + (ingredient.slot() % 3);
            inventory.setItem(guiSlot, GuiItemFactory.recipeIngredient(ingredient));
        }

        inventory.setItem(23, GuiItemFactory.item(Material.ARROW, "§eCraft", List.of()));
        inventory.setItem(24, recipe.result());
        inventory.setItem(40, GuiItemFactory.item(Material.PAPER,
                "§7" + (recipe.shapeless() ? "Shapeless" : "Shaped"), recipe.summary()));

        if (abyssal) {
            inventory.setItem(4, GuiItemFactory.item(Material.PAPER,
                    "§dStep " + safeStep + " / " + steps.size(), List.of("§7" + recipe.title())));
        }

        applyNavigation(inventory, abyssal && safeStep > 1, abyssal && safeStep < steps.size());

        player.openInventory(inventory);
    }

    public static void openStory(Player player, int chapter) {
        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.STORY, null, 0, chapter, null, null, null, false, 0);
        Inventory inventory = create(holder, 54, "§8Nightmare Origin");

        StoryService.render(player, inventory, chapter, 0);
        player.openInventory(inventory);
    }

    public static void openRelics(Player player) {
        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.RELICS, null, 0, 0, null, null, null, false, 0);
        Inventory inventory = create(holder, 54, "§8Relics");

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, GuiItemFactory.border());
        }

        inventory.setItem(20, ItemModels.apply(GuiItemFactory.item(Material.HEART_OF_THE_SEA, "§f§l❖ Soul Heart",
                List.of("§f❖ A heart that beats for the living.",
                        "§7A gift of the soul god, shaped from soul fire.",
                        "§7It hates the Nightmare god, and burns it where it stands.",
                        "§r",
                        "§7Sprint + Left Click: Soul Burst",
                        "§7Sneak + Swap Hand: Divine Cut",
                        "§r",
                        "§eClick to view recipe.")), "soul_heart"));
        inventory.setItem(24, ItemModels.apply(GuiItemFactory.item(Material.ENDER_EYE, "§5§l❖ Abyssal Tendrils",
                List.of("§5❖ An eye that never blinks.",
                        "§7Pulled from the dark by the deep god.",
                        "§7Its tendrils reach out and crush his rusted thinking.",
                        "§r",
                        "§7Sprint + Swap Hand: release tendrils",
                        "§7Falling 10+ blocks: deeper rupture",
                        "§r",
                        "§eClick to view recipe.")), "abyssal_tendrils"));

        applyNavigation(inventory, false, false);
        player.openInventory(inventory);
    }

    public static void openTendrilsRecipe(Player player) {
        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.TENDRILS_RECIPE, null, 0, 0, null, null, null, false, 0);
        Inventory inventory = create(holder, 54, "§8Abyssal Tendrils Recipe");
        RecipeStep recipe = RecipeCatalog.abyssalTendrilsRecipe();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, GuiItemFactory.border());
        }

        for (var ingredient : recipe.ingredients()) {
            int guiSlot = 10 + (ingredient.slot() / 3) * 9 + (ingredient.slot() % 3);
            inventory.setItem(guiSlot, GuiItemFactory.recipeIngredient(ingredient));
        }

        inventory.setItem(4, ItemModels.apply(GuiItemFactory.item(Material.ENDER_EYE, "§5§l❖ Abyssal Tendrils",
                List.of("§5❖ An eye that never blinks.",
                        "§7Pulled from the dark by the deep god.",
                        "§7Its tendrils reach out and crush the Nightmare god.",
                        "§r",
                        "§7Sprint + Swap Hand: release tendrils.",
                        "§7Falling 10+ blocks: deeper rupture.")), "abyssal_tendrils"));
        inventory.setItem(23, GuiItemFactory.item(Material.ARROW, "§eCraft", List.of()));
        inventory.setItem(24, recipe.result());
        inventory.setItem(40, GuiItemFactory.item(Material.PAPER,
                "§7" + (recipe.shapeless() ? "Shapeless" : "Shaped"), recipe.summary()));
        applyNavigation(inventory, false, false);
        player.openInventory(inventory);
    }

    public static void openSoulHeartRecipe(Player player) {
        NightmareGuiHolder holder = new NightmareGuiHolder(GuiType.SOUL_HEART_RECIPE, null, 0, 0, null, null, null, false, 0);
        Inventory inventory = create(holder, 54, "§8Soul Heart Recipe");
        RecipeStep recipe = RecipeCatalog.soulHeartRecipe();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, GuiItemFactory.border());
        }

        for (var ingredient : recipe.ingredients()) {
            int guiSlot = 10 + (ingredient.slot() / 3) * 9 + (ingredient.slot() % 3);
            inventory.setItem(guiSlot, GuiItemFactory.recipeIngredient(ingredient));
        }

        inventory.setItem(4, GuiItemFactory.item(Material.PAPER, "§fRelic: Soul Heart",
                List.of("§f❖ A heart that beats for the living.",
                        "§7A gift of the soul god, shaped from soul fire.",
                        "§7It hates the Nightmare god's rusted thinking, and burns it where it stands.",
                        "§r",
                        "§7Soul Burst and Divine Cut.")));
        inventory.setItem(23, GuiItemFactory.item(Material.ARROW, "§eCraft", List.of()));
        inventory.setItem(24, recipe.result());
        inventory.setItem(40, GuiItemFactory.item(Material.PAPER,
                "§7" + (recipe.shapeless() ? "Shapeless" : "Shaped"), recipe.summary()));

        applyNavigation(inventory, false, false);
        player.openInventory(inventory);
    }

    private static Inventory create(NightmareGuiHolder holder, int size, String title) {
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        for (int index = 0; index < size; index++) {
            inventory.setItem(index, GuiItemFactory.border());
        }

        return inventory;
    }

    private static void applyNavigation(Inventory inventory, boolean previousPage, boolean nextPage) {
        inventory.setItem(BACK_SLOT, GuiItemFactory.item(Material.ARROW, "§aBack",
                List.of("§7Return to the previous menu.")));
        inventory.setItem(CLOSE_SLOT, GuiItemFactory.item(Material.BARRIER, "§cClose",
                List.of("§7Close this menu.")));

        if (previousPage) {
            inventory.setItem(PREV_SLOT, GuiItemFactory.item(Material.ARROW, "§ePrevious Page",
                    List.of("§7Go to the previous page.")));
        }

        if (nextPage) {
            inventory.setItem(NEXT_SLOT, GuiItemFactory.item(Material.ARROW, "§eNext Page",
                    List.of("§7Go to the next page.")));
        }
    }

    private static String roman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "V";
        };
    }

}
