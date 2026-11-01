package com.nightmare.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.enchant.EnchantKeys;

public final class ArmorTrimService implements Listener {

    public static void applyAbyssalTrim(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (!(meta instanceof ArmorMeta armorMeta)) {
            return;
        }

        armorMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.EYE));
        item.setItemMeta(armorMeta);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ensureTrim(player.getInventory().getChestplate());

        ItemStack[] contents = player.getInventory().getContents();

        for (ItemStack item : contents) {
            ensureTrim(item);
        }
    }

    private static void ensureTrim(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        Integer level = meta.getPersistentDataContainer().get(EnchantKeys.KEYS.get("abyssal_core"),
                PersistentDataType.INTEGER);

        if (level == null || level <= 0) {
            return;
        }

        applyAbyssalTrim(item);
    }

}
