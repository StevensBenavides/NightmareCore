package com.nightmare.relic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public final class RelicRecipeService {

    private static NamespacedKey tendrilsKey;
    private static NamespacedKey soulHeartKey;

    private RelicRecipeService() {
    }

    public static void register(Plugin plugin) {
        NamespacedKey tendrils = new NamespacedKey(plugin, "abyssal_tendrils");
        ShapedRecipe tendrilsRecipe = new ShapedRecipe(tendrils, AbyssalTendrilsItem.create());

        tendrilsRecipe.shape("ENE", "NSN", "ENE");
        tendrilsRecipe.setIngredient('E', Material.ECHO_SHARD);
        tendrilsRecipe.setIngredient('N', Material.NETHER_STAR);
        tendrilsRecipe.setIngredient('S', Material.NAUTILUS_SHELL);

        if (Bukkit.getRecipe(tendrils) != null) {
            Bukkit.removeRecipe(tendrils);
        }

        Bukkit.addRecipe(tendrilsRecipe);

        tendrilsKey = tendrils;

        NamespacedKey soulHeart = new NamespacedKey(plugin, "soul_heart");
        ShapedRecipe soulHeartRecipe = new ShapedRecipe(soulHeart, SoulHeartItem.create());

        soulHeartRecipe.shape("NUN", "SCS", " U ");
        soulHeartRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        soulHeartRecipe.setIngredient('U', Material.NETHER_STAR);
        soulHeartRecipe.setIngredient('S', Material.SOUL_LANTERN);
        soulHeartRecipe.setIngredient('C', Material.ENCHANTED_BOOK);

        if (Bukkit.getRecipe(soulHeart) != null) {
            Bukkit.removeRecipe(soulHeart);
        }

        Bukkit.addRecipe(soulHeartRecipe);

        soulHeartKey = soulHeart;
    }

    public static boolean isTendrilsRecipe(NamespacedKey key) {
        return tendrilsKey != null && tendrilsKey.equals(key);
    }

    public static boolean isSoulHeartRecipe(NamespacedKey key) {
        return soulHeartKey != null && soulHeartKey.equals(key);
    }

}
