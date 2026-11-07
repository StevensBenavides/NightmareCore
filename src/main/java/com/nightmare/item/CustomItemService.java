package com.nightmare.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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

        meta.getPersistentDataContainer().set(ItemKeys.LORE_BASE, PersistentDataType.STRING,
                meta.getLore() == null ? ""
                        : String.join("\n", EnchantLoreService.sanitizeBaseLore(meta.getLore())));

        meta.getPersistentDataContainer().set(EnchantKeys.KEYS.get(id), PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);

        if (id.equals("abyssal_core")) {
            ArmorTrimService.applyAbyssalTrim(item);
        }

        EnchantLoreService.rebuildEquipmentLore(item);
    }

    public static ItemStack createPickaxe(Random random) {
        int wood = Math.max(0, Main.balance == null ? 50
                : Main.balance.getInt("miner.pickaxe-weights.wood", 50));
        int stone = Math.max(0, Main.balance == null ? 35
                : Main.balance.getInt("miner.pickaxe-weights.stone", 35));
        int iron = Math.max(0, Main.balance == null ? 15
                : Main.balance.getInt("miner.pickaxe-weights.iron", 15));
        int total = wood + stone + iron;

        Material material = Material.WOODEN_PICKAXE;

        if (total > 0) {
            int roll = random.nextInt(total);

            if (roll < iron) {
                material = Material.IRON_PICKAXE;
            } else if (roll < iron + stone) {
                material = Material.STONE_PICKAXE;
            }
        }

        return new ItemStack(material);
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

        meta.setItemName(ChatColor.DARK_RED + com.nightmare.lang.LangService.msg("items.nightmare_blade_name", "Nightmare Blade") + " " + ChatColor.GRAY + tier.name());
        meta.setLore(com.nightmare.lang.LangService.loreLines("items.nightmare_blade_lore",
                "%tier%", tier.name()));
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

        meta.setItemName(ChatColor.DARK_RED + com.nightmare.lang.LangService.msg("items.nightmare_bow_name", "Nightmare Bow") + " " + ChatColor.GRAY + tier.name());
        meta.setLore(com.nightmare.lang.LangService.loreLines("items.nightmare_bow_lore",
                "%tier%", tier.name()));
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

    private static Material armorMaterialForTier(int tierIndex, EquipmentSlot slot) {
        if (tierIndex >= 9) {
            return switch (slot) {
                case HEAD -> Material.NETHERITE_HELMET;
                case CHEST -> Material.NETHERITE_CHESTPLATE;
                case LEGS -> Material.NETHERITE_LEGGINGS;
                default -> Material.NETHERITE_BOOTS;
            };
        }

        if (tierIndex >= 4) {
            return switch (slot) {
                case HEAD -> Material.DIAMOND_HELMET;
                case CHEST -> Material.DIAMOND_CHESTPLATE;
                case LEGS -> Material.DIAMOND_LEGGINGS;
                default -> Material.DIAMOND_BOOTS;
            };
        }

        if (tierIndex >= 2) {
            return switch (slot) {
                case HEAD -> Material.IRON_HELMET;
                case CHEST -> Material.IRON_CHESTPLATE;
                case LEGS -> Material.IRON_LEGGINGS;
                default -> Material.IRON_BOOTS;
            };
        }

        return switch (slot) {
            case HEAD -> Material.CHAINMAIL_HELMET;
            case CHEST -> Material.CHAINMAIL_CHESTPLATE;
            case LEGS -> Material.CHAINMAIL_LEGGINGS;
            default -> Material.CHAINMAIL_BOOTS;
        };
    }

    private static Material downgradeArmor(Material material, EquipmentSlot slot) {
        return switch (material) {
            case NETHERITE_HELMET -> Material.DIAMOND_HELMET;
            case NETHERITE_CHESTPLATE -> Material.DIAMOND_CHESTPLATE;
            case NETHERITE_LEGGINGS -> Material.DIAMOND_LEGGINGS;
            case NETHERITE_BOOTS -> Material.DIAMOND_BOOTS;
            case DIAMOND_HELMET -> Material.IRON_HELMET;
            case DIAMOND_CHESTPLATE -> Material.IRON_CHESTPLATE;
            case DIAMOND_LEGGINGS -> Material.IRON_LEGGINGS;
            case DIAMOND_BOOTS -> Material.IRON_BOOTS;
            case IRON_HELMET -> Material.CHAINMAIL_HELMET;
            case IRON_CHESTPLATE -> Material.CHAINMAIL_CHESTPLATE;
            case IRON_LEGGINGS -> Material.CHAINMAIL_LEGGINGS;
            case IRON_BOOTS -> Material.CHAINMAIL_BOOTS;
            default -> armorMaterialForTier(0, slot);
        };
    }

    public static ItemStack createArmor(MobTier tier, EquipmentSlot slot, boolean legendary) {
        int enchantLevel = Main.balance.getInt("tiers." + tier.name() + ".enchant-level", tier.enchantLevel);

        Material material = armorMaterialForTier(tier.index, slot);

        if (tier.index > 0 && ThreadLocalRandom.current().nextDouble() < 0.30) {
            material = downgradeArmor(material, slot);
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setItemName(ChatColor.DARK_RED + com.nightmare.lang.LangService.msg("items.nightmare_armor_name", "Nightmare Armor") + " " + ChatColor.GRAY + tier.name());
        meta.setLore(com.nightmare.lang.LangService.loreLines("items.nightmare_armor_lore",
                "%tier%", tier.name()));
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
