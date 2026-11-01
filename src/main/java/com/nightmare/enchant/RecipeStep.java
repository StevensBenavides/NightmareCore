package com.nightmare.enchant;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public record RecipeStep(String title, boolean shapeless, List<RecipeIngredient> ingredients,
        ItemStack result, List<String> summary) {
}
