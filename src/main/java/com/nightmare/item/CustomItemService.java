package com.nightmare.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.Main;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantKeys;
import com.nightmare.enchant.EnchantLoreService;
import com.nightmare.enchant.EnchantRegistry;
import com.nightmare.tier.MobTier;

public final class CustomItemService {

    private CustomItemService() {
    }

    public static void applyEnchant(ItemStack item, String id, int level) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);

        if (enchant == null || item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        if (!meta.getPersistentDataContainer().has(ItemKeys.LORE_BASE, PersistentDataType.STRING)
                && meta.getLore() != null) {
            meta.getPersistentDataContainer().set(ItemKeys.LORE_BASE, PersistentDataType.STRING,
                    String.join("\n", EnchantLoreService.sanitizeBaseLore(meta.getLore())));
        }

        meta.getPersistentDataContainer().set(EnchantKeys.KEYS.get(id), PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);

        if (id.equals("abyssal_core")) {
            ArmorTrimService.applyAbyssalTrim(item);
        }

        EnchantLoreService.rebuildEquipmentLore(item);
    }

    public static ItemStack createMeleeWeapon(MobTier tier, boolean legendary) {
        int enchantLevel = Main.balance.getInt("tiers." + tier.name() + ".enchant-level", tier.enchantLevel);

        Material material;

        if (tier.index >= 9) {
            material = Material.NETHERITE_SWORD;
        } else if (tier.index >= 3) {
            material = Material.DIAMOND_SWORD;
        } else {
            material = Material.IRON_SWORD;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_RED + "Nightmare Blade " + ChatColor.GRAY + tier.name());
        meta.setLore(new ArrayList<>(Arrays.asList(
                ChatColor.DARK_GRAY + "Forged by the nightmare.",
                ChatColor.GRAY + "Tier " + tier.name())));
        meta.getPersistentDataContainer().set(ItemKeys.LORE_BASE, PersistentDataType.STRING,
                String.join("\n", meta.getLore()));
        meta.getPersistentDataContainer().set(ItemKeys.CUSTOM_ITEM, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(ItemKeys.ITEM_TIER, PersistentDataType.STRING, tier.name());
        meta.getPersistentDataContainer().set(ItemKeys.ITEM_LEGENDARY, PersistentDataType.BOOLEAN, legendary);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.SHARPNESS, Math.max(1, enchantLevel));
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, Math.max(1, enchantLevel));

        if (enchantLevel <= 0) {
            return item;
        }

        List<CustomEnchant> candidates = new ArrayList<>();

        for (CustomEnchant enchant : EnchantRegistry.ALL) {
            if (enchant.playerOnly || !enchant.applicable.contains(material) || enchant.maxLevel < enchantLevel) {
                continue;
            }

            candidates.add(enchant);
        }

        Collections.shuffle(candidates, ThreadLocalRandom.current());

        if (legendary) {
            applyEnchant(item, "nightmare_mark", 5);
        }

        int count = legendary ? 2 : Math.min(3, Math.max(1, enchantLevel));

        for (CustomEnchant enchant : candidates) {
            if (count <= 0) {
                break;
            }

            if (enchant.id.equals("nightmare_mark")) {
                continue;
            }

            applyEnchant(item, enchant.id, enchantLevel);
            count--;
        }

        return item;
    }

    public static ItemStack createBow(MobTier tier, boolean legendary) {
        int enchantLevel = Main.balance.getInt("tiers." + tier.name() + ".enchant-level", tier.enchantLevel);

        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_RED + "Nightmare Bow " + ChatColor.GRAY + tier.name());
        meta.setLore(new ArrayList<>(Arrays.asList(
                ChatColor.DARK_GRAY + "The hunt never ends.",
                ChatColor.GRAY + "Tier " + tier.name())));
        meta.getPersistentDataContainer().set(ItemKeys.LORE_BASE, PersistentDataType.STRING,
                String.join("\n", meta.getLore()));
        meta.getPersistentDataContainer().set(ItemKeys.CUSTOM_ITEM, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(ItemKeys.ITEM_TIER, PersistentDataType.STRING, tier.name());
        meta.getPersistentDataContainer().set(ItemKeys.ITEM_LEGENDARY, PersistentDataType.BOOLEAN, legendary);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.POWER, Math.max(1, enchantLevel));
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, Math.max(1, enchantLevel));

        List<CustomEnchant> candidates = new ArrayList<>();

        for (CustomEnchant enchant : EnchantRegistry.ALL) {
            if (enchant.playerOnly || !enchant.applicable.contains(Material.BOW) || enchant.maxLevel < enchantLevel) {
                continue;
            }

            candidates.add(enchant);
        }

        Collections.shuffle(candidates, ThreadLocalRandom.current());

        if (legendary) {
            applyEnchant(item, "nightmare_mark", 5);
        }

        int count = legendary ? 2 : Math.min(3, Math.max(1, enchantLevel));

        for (CustomEnchant enchant : candidates) {
            if (count <= 0) {
                break;
            }

            if (enchant.id.equals("nightmare_mark")) {
                continue;
            }

            applyEnchant(item, enchant.id, enchantLevel);
            count--;
        }

        return item;
    }

    public static ItemStack createArmor(MobTier tier, EquipmentSlot slot, boolean legendary) {
        int enchantLevel = Main.balance.getInt("tiers." + tier.name() + ".enchant-level", tier.enchantLevel);

        Material material;

        if (tier.index >= 9) {
            material = switch (slot) {
                case HEAD -> Material.NETHERITE_HELMET;
                case CHEST -> Material.NETHERITE_CHESTPLATE;
                case LEGS -> Material.NETHERITE_LEGGINGS;
                default -> Material.NETHERITE_BOOTS;
            };
        } else if (tier.index >= 4) {
            material = switch (slot) {
                case HEAD -> Material.DIAMOND_HELMET;
                case CHEST -> Material.DIAMOND_CHESTPLATE;
                case LEGS -> Material.DIAMOND_LEGGINGS;
                default -> Material.DIAMOND_BOOTS;
            };
        } else {
            material = switch (slot) {
                case HEAD -> Material.IRON_HELMET;
                case CHEST -> Material.IRON_CHESTPLATE;
                case LEGS -> Material.IRON_LEGGINGS;
                default -> Material.IRON_BOOTS;
            };
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_RED + "Nightmare Armor " + ChatColor.GRAY + tier.name());
        meta.setLore(new ArrayList<>(Arrays.asList(
                ChatColor.DARK_GRAY + "Clad in the nightmare.",
                ChatColor.GRAY + "Tier " + tier.name())));
        meta.getPersistentDataContainer().set(ItemKeys.LORE_BASE, PersistentDataType.STRING,
                String.join("\n", meta.getLore()));
        meta.getPersistentDataContainer().set(ItemKeys.CUSTOM_ITEM, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(ItemKeys.ITEM_TIER, PersistentDataType.STRING, tier.name());
        meta.getPersistentDataContainer().set(ItemKeys.ITEM_LEGENDARY, PersistentDataType.BOOLEAN, legendary);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        item.addUnsafeEnchantment(Enchantment.PROTECTION, Math.max(1, enchantLevel));
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, Math.max(1, enchantLevel));

        if (enchantLevel <= 0) {
            return item;
        }

        List<CustomEnchant> candidates = new ArrayList<>();

        for (CustomEnchant enchant : EnchantRegistry.ALL) {
            if (enchant.playerOnly || !enchant.applicable.contains(material) || enchant.maxLevel < enchantLevel) {
                continue;
            }

            candidates.add(enchant);
        }

        Collections.shuffle(candidates, ThreadLocalRandom.current());
        int count = legendary ? Math.min(3, candidates.size()) : Math.min(2, Math.max(1, enchantLevel));

        for (CustomEnchant enchant : candidates) {
            if (count <= 0) {
                break;
            }

            applyEnchant(item, enchant.id, enchantLevel);
            count--;
        }

        return item;
    }

}
