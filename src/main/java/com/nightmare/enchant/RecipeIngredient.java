package com.nightmare.enchant;

import org.bukkit.inventory.ItemStack;

public record RecipeIngredient(int slot, ItemStack display, int amount, String label) {
}
