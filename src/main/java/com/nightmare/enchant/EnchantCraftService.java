package com.nightmare.enchant;

import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.item.ItemKeys;

public final class EnchantCraftService implements Listener {

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof Keyed keyed)) {
            return;
        }

        String id = EnchantBookService.RECIPE_ENCHANTS.get(keyed.getKey());

        if (id == null) {
            return;
        }

        int level = EnchantBookService.RECIPE_LEVELS.getOrDefault(keyed.getKey(), 1);

        if (id.equals("abyssal_core")) {
            ItemStack center = event.getInventory().getItem(4);

            if (center == null || center.getType() != Material.ENCHANTED_BOOK || center.getItemMeta() == null
                    || !"abyssal_howl".equals(center.getItemMeta().getPersistentDataContainer()
                            .get(EnchantKeys.BOOK_ID, PersistentDataType.STRING))) {
                event.getInventory().setResult(null);
                return;
            }
        }

        if (id.equals("abyssal_core") || id.equals("abyssal_howl")) {
            int nuclei = 0;

            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item == null || item.getType() != Material.NETHER_STAR) {
                    continue;
                }

                if (item.getItemMeta() == null || !Boolean.TRUE.equals(item.getItemMeta()
                        .getPersistentDataContainer().get(ItemKeys.ABYSSAL_CORE, PersistentDataType.BOOLEAN))) {
                    event.getInventory().setResult(null);
                    return;
                }

                nuclei++;
            }

            if (nuclei != 4) {
                event.getInventory().setResult(null);
                return;
            }
        }

        long day = event.getView().getPlayer().getWorld().getFullTime() / 24000L;
        int requiredDay = (id.equals("abyssal_howl") || id.equals("abyssal_core")) ? 0
                : EnchantBookService.LEVEL_UNLOCK_DAYS.getOrDefault(level, 0);

        if (day >= requiredDay) {
            return;
        }

        event.getInventory().setResult(null);

        if (event.getView().getPlayer() instanceof Player player) {
            player.sendMessage(ChatColor.RED + com.nightmare.lang.LangService.format("craft.unlock_day",
                    "This enchantment unlocks on day %day%.", "%day%", String.valueOf(requiredDay)));
        }
    }

}
