package com.nightmare;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Recipes {

    public static void registerRecipes() {
        registerStormBreakerRecipe();
    }

    private static void registerStormBreakerRecipe() {
        ItemStack result = CustomEnchants.createStormBreakerEnchant();

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(Main.getInstance(), "stormbreaker_book"),
                result
        );

        recipe.shape(
            " B ",
            "BEB",
            " B "
        );

        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(createSharpnessBook()));

        Bukkit.addRecipe(recipe);
    }

    private static ItemStack createSharpnessBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
        
        storageMeta.addStoredEnchant(Enchantment.SHARPNESS, 3, false);
        book.setItemMeta(storageMeta);

        return book;
    }
}