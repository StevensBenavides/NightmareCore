package com.nightmare.enchant;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.item.CustomItemService;

public final class EnchantAnvilService implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);

        if (first == null || second == null || first.getType() == Material.AIR || second.getType() == Material.AIR) {
            return;
        }

        ItemMeta firstMeta = first.getItemMeta();
        ItemMeta secondMeta = second.getItemMeta();

        if (firstMeta == null || secondMeta == null) {
            return;
        }

        PersistentDataContainer firstContainer = firstMeta.getPersistentDataContainer();
        PersistentDataContainer secondContainer = secondMeta.getPersistentDataContainer();

        String firstId = firstContainer.get(EnchantKeys.BOOK_ID, PersistentDataType.STRING);
        String secondId = secondContainer.get(EnchantKeys.BOOK_ID, PersistentDataType.STRING);
        Integer firstLevel = firstContainer.get(EnchantKeys.BOOK_LEVEL, PersistentDataType.INTEGER);
        Integer secondLevel = secondContainer.get(EnchantKeys.BOOK_LEVEL, PersistentDataType.INTEGER);

        HumanEntity player = event.getView().getPlayer();
        long day = player.getWorld().getFullTime() / 24000L;

        if (firstId != null && secondId != null) {
            if (!firstId.equals(secondId)) {
                event.setResult(null);
                return;
            }

            CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(firstId);

            if (enchant == null) {
                event.setResult(null);
                return;
            }

            int currentLevel = Math.max(firstLevel == null ? 1 : firstLevel, secondLevel == null ? 1 : secondLevel);
            int combinedLevel = (firstLevel == null ? 1 : firstLevel) + (secondLevel == null ? 0 : secondLevel);
            int maximumLevel = Math.min(enchant.maxLevel, EnchantBookService.maxLevelForDay(firstId, day));
            int resultLevel = Math.min(maximumLevel, Math.max(currentLevel, combinedLevel));

            if (resultLevel <= 0) {
                event.setResult(null);
                return;
            }

            event.setResult(EnchantBookService.createBook(firstId, resultLevel));
            inventory.setRepairCost(1 + resultLevel * 5);

            return;
        }

        ItemStack target = null;
        String bookId = null;

        int bookLevel = 1;

        if (firstId != null && secondId == null) {
            target = second;
            bookId = firstId;
            bookLevel = firstLevel == null ? 1 : firstLevel;
        } else if (secondId != null && firstId == null) {
            target = first;
            bookId = secondId;
            bookLevel = secondLevel == null ? 1 : secondLevel;
        }

        if (target == null || bookId == null) {
            return;
        }

        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(bookId);
        ItemMeta targetMeta = target.getItemMeta();

        if (enchant == null || targetMeta == null || !enchant.applicable.contains(target.getType())) {
            event.setResult(null);
            return;
        }

        PersistentDataContainer targetContainer = targetMeta.getPersistentDataContainer();
        Integer existingLevel = targetContainer.get(EnchantKeys.KEYS.get(bookId), PersistentDataType.INTEGER);

        int maximumLevel = Math.min(enchant.maxLevel, EnchantBookService.maxLevelForDay(bookId, day));
        int resultLevel = Math.min(maximumLevel, (existingLevel == null ? 0 : existingLevel) + bookLevel);

        if (resultLevel <= 0) {
            event.setResult(null);
            return;
        }

        int customCount = 0;

        for (Map.Entry<String, org.bukkit.NamespacedKey> entry : EnchantKeys.KEYS.entrySet()) {
            if (targetContainer.get(entry.getValue(), PersistentDataType.INTEGER) != null) {
                customCount++;
            }
        }

        if (existingLevel == null && customCount >= 3) {
            event.setResult(null);
            return;
        }

        boolean targetHasAbyssal = targetContainer.get(EnchantKeys.KEYS.get("abyssal_howl"), PersistentDataType.INTEGER) != null;
        boolean targetHasBerserker = targetContainer.get(EnchantKeys.KEYS.get("berserker"), PersistentDataType.INTEGER) != null;

        if ((bookId.equals("abyssal_howl") && targetHasBerserker) || (bookId.equals("berserker") && targetHasAbyssal)) {
            event.setResult(null);
            return;
        }

        ItemStack result = target.clone();
        CustomItemService.applyEnchant(result, bookId, resultLevel);

        event.setResult(result);
        inventory.setRepairCost(1 + resultLevel * 5);
    }

}
