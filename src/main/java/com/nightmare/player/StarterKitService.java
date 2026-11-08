package com.nightmare.player;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.nightmare.Main;

/**
 * Gives first-time players a silent starter kit directly into the hotbar.
 * No chat messages, titles or sounds are ever sent from here.
 */
public final class StarterKitService {

    private static final int[] KIT_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private StarterKitService() {
    }

    public static void give(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (player.hasPlayedBefore()) {
            return;
        }

        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (Main.balance != null && !Main.balance.getBoolean("starter-kit.enabled", true)) {
            return;
        }

        ItemStack[] kit = {
                new ItemStack(Material.COPPER_SWORD),
                new ItemStack(Material.COPPER_PICKAXE),
                new ItemStack(Material.COPPER_AXE),
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_BOOTS),
                waterBottles()
        };

        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < kit.length; i++) {
            place(player, inventory, KIT_SLOTS[i], kit[i]);
        }

        player.updateInventory();
    }

    private static ItemStack waterBottles() {
        ItemStack bottles = new ItemStack(Material.POTION, 2);

        if (bottles.getItemMeta() instanceof PotionMeta meta) {
            meta.setBasePotionType(PotionType.WATER);
            bottles.setItemMeta(meta);
        }

        return bottles;
    }

    private static void place(Player player, PlayerInventory inventory, int preferred, ItemStack item) {
        ItemStack current = inventory.getItem(preferred);

        if (current == null || current.getType().isAir()) {
            inventory.setItem(preferred, item);
            return;
        }

        for (int slot = 0; slot < 9; slot++) {
            ItemStack occupant = inventory.getItem(slot);

            if (occupant == null || occupant.getType().isAir()) {
                inventory.setItem(slot, item);
                return;
            }
        }

        int empty = inventory.firstEmpty();

        if (empty >= 0) {
            inventory.setItem(empty, item);
            return;
        }

        player.getWorld().dropItemNaturally(player.getLocation(), item);
    }
}
