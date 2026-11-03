package com.nightmare.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantBookService;
import com.nightmare.enchant.EnchantRegistry;
import com.nightmare.enchant.RecipeCatalog;
import com.nightmare.enchant.RecipeIngredient;
import com.nightmare.item.ItemModels;
import com.nightmare.lang.LangService;

public final class GuiItemFactory {

    private static final String[] ROMAN = { "I", "II", "III", "IV", "V" };

    private GuiItemFactory() {
    }

    public static ItemStack border() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setItemName(" ");
            meta.setLore(List.of());
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(name);
        meta.setLore(lore == null ? List.of() : lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack levelButton(CustomEnchant enchant, int level, int selectedLevel, long day) {
        boolean notAvailable = level > enchant.maxLevel
                || (enchant.id.equals("nightmare_mark") && level < 5)
                || (enchant.id.equals("abyssal_howl") && level > 1);

        if (notAvailable) {
            return item(Material.GRAY_DYE, ChatColor.DARK_GRAY + LangService.msg("gui.level", "Level") + " " + ROMAN[level - 1],
                    List.of(ChatColor.GRAY + LangService.msg("gui.not_available", "Not available")));
        }

        int unlocked = EnchantBookService.maxLevelForDay(enchant.id, day);

        if (level > unlocked) {
            int unlockDay = RecipeCatalog.LEVEL_UNLOCK_DAYS.getOrDefault(level, 0);
            return item(Material.GRAY_DYE, ChatColor.GRAY + LangService.msg("gui.level", "Level") + " " + ROMAN[level - 1],
                    List.of(ChatColor.GRAY + LangService.format("gui.unlocks_on_day", "Unlocks on day %day%",
                            "%day%", String.valueOf(unlockDay))));
        }

        String name = (enchant.id.equals("abyssal_howl") || enchant.id.equals("abyssal_core"))
                ? ChatColor.LIGHT_PURPLE + LangService.msg("gui.legendary", "Legendary")
                : ChatColor.YELLOW + LangService.msg("gui.level", "Level") + " " + ROMAN[level - 1];
        ItemStack item = ItemModels.apply(item(Material.ENCHANTED_BOOK, name,
                List.of(ChatColor.GRAY + LangService.msg("gui.click_to_view", "Click to view."))), enchant.id);

        if (level == selectedLevel) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    public static ItemStack recipeIngredient(RecipeIngredient ingredient) {
        ItemStack item = ingredient.display().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + LangService.msg("gui.amount", "Amount") + ": " + ChatColor.WHITE + ingredient.amount());
        lore.add(ChatColor.DARK_GRAY + ingredient.label());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

}