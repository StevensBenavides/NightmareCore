package com.nightmare.relic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.Main;
import com.nightmare.item.ItemModels;

public final class AbyssalTendrilsItem {

    private AbyssalTendrilsItem() {
    }

    public static ItemStack create() {
        return create(null);
    }

    public static ItemStack create(UUID owner) {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                + com.nightmare.lang.LangService.msg("relics.abyssal_tendrils.name", "❖ Abyssal Tendrils"));
        meta.setLore(buildLore());
        meta.setMaxStackSize(1);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(RelicKeys.TENDRILS_AMULET, PersistentDataType.BOOLEAN, true);

        setCustomModel(meta, "abyssal_tendrils");

        if (owner != null) {
            meta.getPersistentDataContainer().set(RelicKeys.TENDRILS_OWNER, PersistentDataType.STRING, owner.toString());
        }

        item.setItemMeta(meta);
        return item;
    }

    private static void setCustomModel(ItemMeta meta, String id) {
        ItemModels.applyMeta(meta, id);
    }

    public static boolean isTendrils(ItemStack item) {
        return item != null && item.getType() == Material.ENDER_EYE && item.getItemMeta() != null
                && Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
                        .get(RelicKeys.TENDRILS_AMULET, PersistentDataType.BOOLEAN));
    }

    public static UUID owner(ItemStack item) {
        if (!isTendrils(item)) {
            return null;
        }

        String value = item.getItemMeta().getPersistentDataContainer()
                .get(RelicKeys.TENDRILS_OWNER, PersistentDataType.STRING);

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static ItemStack bind(ItemStack item, UUID owner) {
        if (!isTendrils(item)) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.getPersistentDataContainer().set(RelicKeys.TENDRILS_OWNER, PersistentDataType.STRING, owner.toString());
        item.setItemMeta(meta);
        return item;
    }

    public static void refreshLore(ItemStack item) {
        if (!isTendrils(item) || item.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = buildLore();

        if (!lore.equals(meta.getLore())) {
            meta.setLore(lore);
        }

        ItemModels.ensure(item, "abyssal_tendrils");

        item.setItemMeta(meta);
    }

    public static List<String> buildLore() {
        long cooldown = balance().getLong("abyssal-tendrils.cooldown-ms", 60000L);

        return com.nightmare.lang.LangService.loreLines("relics.abyssal_tendrils.lines",
                "%range%", number(balance().getDouble("abyssal-tendrils.range", 50.0)),
                "%patch%", number(balance().getDouble("abyssal-tendrils.patch-radius", 7.5) * 2.0),
                "%damage%", number(balance().getDouble("abyssal-tendrils.damage", 75.0)),
                "%falling%", number(balance().getDouble("abyssal-tendrils.falling-damage", 100.0)),
                "%cooldown%", number(cooldown / 1000.0));
    }

    private static org.bukkit.configuration.file.YamlConfiguration balance() {
        return Main.balance == null ? new org.bukkit.configuration.file.YamlConfiguration() : Main.balance;
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value).replace(".0", "");
    }

}
