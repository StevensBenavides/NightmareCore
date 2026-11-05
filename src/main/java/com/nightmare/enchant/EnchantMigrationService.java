package com.nightmare.enchant;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.item.CustomItemService;

public final class EnchantMigrationService implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack[] contents = player.getInventory().getContents();

        for (ItemStack item : contents) {
            migrate(item);
        }

        ItemStack[] armor = player.getInventory().getArmorContents();

        for (ItemStack item : armor) {
            migrate(item);
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();

        migrate(offhand);
    }

    private void migrate(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        if (meta.getPersistentDataContainer().get(EnchantKeys.OLD_STORMBREAKER,
                PersistentDataType.STRING) != null) {
            meta.getPersistentDataContainer().remove(EnchantKeys.OLD_STORMBREAKER);
            item.setItemMeta(meta);
            CustomItemService.applyEnchant(item, "stormcaller", 1);
        }

        ItemMeta refreshed = item.getItemMeta();

        if (refreshed != null && hasCustomEnchant(refreshed)) {
            EnchantLoreService.rebuildEquipmentLore(item);
        }
    }

    private boolean hasCustomEnchant(ItemMeta meta) {
        for (org.bukkit.NamespacedKey key : EnchantKeys.KEYS.values()) {
            Integer level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

            if (level != null && level > 0) {
                return true;
            }
        }

        return false;
    }

}
