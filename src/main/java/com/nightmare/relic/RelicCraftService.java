package com.nightmare.relic;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.Main;
import com.nightmare.enchant.EnchantKeys;
import com.nightmare.item.ItemKeys;

public final class RelicCraftService implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof Keyed keyed)) {
            return;
        }

        boolean tendrils = RelicRecipeService.isTendrilsRecipe(keyed.getKey());
        boolean soulHeart = RelicRecipeService.isSoulHeartRecipe(keyed.getKey());

        if (!tendrils && !soulHeart) {
            return;
        }

        if (soulHeart) {
            ItemStack center = event.getInventory().getItem(4);

            if (center == null || center.getType() != Material.ENCHANTED_BOOK || center.getItemMeta() == null
                    || !"abyssal_core".equals(center.getItemMeta().getPersistentDataContainer()
                            .get(EnchantKeys.BOOK_ID, PersistentDataType.STRING))) {
                event.getInventory().setResult(null);
                
                return;
            }
        }

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null || item.getType() != Material.NETHER_STAR) {
                continue;
            }

            if (item.getItemMeta() == null || !Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
                    .get(ItemKeys.ABYSSAL_CORE, PersistentDataType.BOOLEAN))) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (AbyssalTendrilsItem.isTendrils(event.getCurrentItem())) {
            Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
                    () -> {
                        if (player.isOnline()) {
                            AbyssalTendrilsService.ensureBound(player);
                        }
                    });
        } else if (SoulHeartItem.isSoulHeart(event.getCurrentItem())) {
            Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
                    () -> {
                        if (player.isOnline()) {
                            SoulHeartService.ensureBound(player);
                        }
                    });
        }
    }

}
