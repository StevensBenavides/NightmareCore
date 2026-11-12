package com.nightmare.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.lang.LangService;

public final class BackpackItem {

    private BackpackItem() {
    }

    public static UUID owner(ItemStack item) {
        if (!is(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return null;
        }

        String raw = meta.getPersistentDataContainer().get(PlayerItemKeys.BACKPACK_OWNER,
                PersistentDataType.STRING);

        if (raw == null) {
            return null;
        }

        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static ItemStack create(Player owner) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(color(LangService.msg("backpack.name", "&6Backpack")));
        meta.setLore(lore());
        meta.setMaxStackSize(1);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(PlayerItemKeys.BACKPACK, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(PlayerItemKeys.BACKPACK_OWNER, PersistentDataType.STRING,
                owner.getUniqueId().toString());
        item.setItemMeta(meta);

        return item;
    }

    private static List<String> lore() {
        List<String> raw = LangService.list("backpack.lore");
        List<String> result = new ArrayList<>();

        if (raw.isEmpty()) {
            result.add("&7A personal backpack.");
            result.add("&8Click the chest slot to open it.");
            return colored(result);
        }

        return colored(raw);
    }

    public static boolean is(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null && meta.getPersistentDataContainer().has(PlayerItemKeys.BACKPACK,
                PersistentDataType.BYTE);
    }

    private static List<String> colored(List<String> lines) {
        List<String> result = new ArrayList<>();

        for (String line : lines) {
            result.add(color(line));
        }

        return result;
    }

    public static boolean isOwnedBy(ItemStack item, Player player) {
        UUID owner = owner(item);

        return owner != null && owner.equals(player.getUniqueId());
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
