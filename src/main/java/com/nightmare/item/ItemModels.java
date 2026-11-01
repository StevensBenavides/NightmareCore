package com.nightmare.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemModels {

    private ItemModels() {
    }

    public static ItemStack apply(ItemStack item, String id) {
        if (item == null || id == null) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        applyMeta(meta, id);
        item.setItemMeta(meta);
        return item;
    }

    public static void applyMeta(ItemMeta meta, String id) {
        if (meta == null || id == null) {
            return;
        }

        meta.setItemModel(new NamespacedKey("nightmare", id));
    }

    public static boolean ensure(ItemStack item, String id) {
        if (item == null || id == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || new NamespacedKey("nightmare", id).equals(meta.getItemModel())) {
            return false;
        }

        applyMeta(meta, id);
        item.setItemMeta(meta);
        return true;
    }
}
