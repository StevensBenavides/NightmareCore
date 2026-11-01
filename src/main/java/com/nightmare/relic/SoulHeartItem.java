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

public final class SoulHeartItem {

    private SoulHeartItem() {
    }

    public static ItemStack create() {
        return create(null);
    }

    public static ItemStack create(UUID owner) {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.WHITE + "" + ChatColor.BOLD + "❖ Soul Heart");
        meta.setLore(buildLore());
        meta.setMaxStackSize(1);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(RelicKeys.SOUL_HEART_AMULET, PersistentDataType.BOOLEAN, true);

        setCustomModel(meta, "soul_heart");

        if (owner != null) {
            meta.getPersistentDataContainer().set(RelicKeys.SOUL_HEART_OWNER, PersistentDataType.STRING, owner.toString());
        }

        item.setItemMeta(meta);

        return item;
    }

    public static boolean isSoulHeart(ItemStack item) {
        return item != null && item.getType() == Material.HEART_OF_THE_SEA && item.getItemMeta() != null
                && Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
                        .get(RelicKeys.SOUL_HEART_AMULET, PersistentDataType.BOOLEAN));
    }

    public static UUID owner(ItemStack item) {
        if (!isSoulHeart(item)) {
            return null;
        }

        String value = item.getItemMeta().getPersistentDataContainer()
                .get(RelicKeys.SOUL_HEART_OWNER, PersistentDataType.STRING);

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
        if (!isSoulHeart(item)) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.getPersistentDataContainer().set(RelicKeys.SOUL_HEART_OWNER, PersistentDataType.STRING, owner.toString());
        item.setItemMeta(meta);

        return item;
    }

    public static void refreshLore(ItemStack item) {
        if (!isSoulHeart(item) || item.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = buildLore();

        if (!lore.equals(meta.getLore())) {
            meta.setLore(lore);
        }

        ItemModels.ensure(item, "soul_heart");
        item.setItemMeta(meta);
    }

    public static List<String> buildLore() {
        long burstCooldown = balance().getLong("soul-heart.burst-cooldown-ms", 45000L);
        long divineCooldown = balance().getLong("soul-heart.divine-cooldown-ms", 3600000L);

        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.WHITE + "" + ChatColor.BOLD + "Legendary Relic");
        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.GRAY + "The soul god shaped this heart from soul fire.");
        lore.add(ChatColor.GRAY + "It beats for the living, and hates the Nightmare god's rusted thinking.");
        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.GRAY + "" + ChatColor.BOLD + "How to use:");
        lore.add(ChatColor.GRAY + "Sprint + Left Click: Soul Burst");
        lore.add(ChatColor.GRAY + "Sneak + Swap Hand: Divine Cut");
        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.GRAY + "Burst: " + ChatColor.WHITE + number(balance().getDouble("soul-heart.burst-damage", 200.0)) + " HP / "
                + ChatColor.YELLOW + number(burstCooldown / 1000.0) + "s");
        lore.add(ChatColor.GRAY + "Divine Cut: " + ChatColor.WHITE + number(balance().getDouble("soul-heart.divine-damage", 150.0)) + " HP / "
                + ChatColor.YELLOW + number(divineCooldown / 1000.0) + "s");

        return lore;
    }

    private static void setCustomModel(ItemMeta meta, String id) {
        ItemModels.applyMeta(meta, id);
    }

    private static org.bukkit.configuration.file.YamlConfiguration balance() {
        return Main.balance == null ? new org.bukkit.configuration.file.YamlConfiguration() : Main.balance;
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value).replace(".0", "");
    }

}
