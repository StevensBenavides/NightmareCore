package com.nightmare.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantRegistry;

public final class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (!(top.getHolder() instanceof NightmareGuiHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() != top || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        int contentIndex = -1;

        for (int index = 0; index < GuiManager.CONTENT_SLOTS.length; index++) {
            if (GuiManager.CONTENT_SLOTS[index] == slot) {
                contentIndex = index;
                break;
            }
        }

        switch (holder.type) {
            case MAIN -> {
                if (slot == 12) {
                    click(player);
                    GuiManager.openEnchantList(player);
                } else if (slot == 14) {
                    click(player);
                    GuiManager.openRelics(player);
                }
            }
            case ENCHANTS -> {
                if (contentIndex >= 0 && contentIndex < EnchantRegistry.ALL.size()) {
                    CustomEnchant enchant = EnchantRegistry.ALL.get(contentIndex);
                    int level = enchant.id.equals("nightmare_mark") ? 5 : 1;
                    click(player);
                    GuiManager.openEnchantDetail(player, enchant.id, level);
                } else if (slot == GuiManager.BACK_SLOT) {
                    click(player);
                    GuiManager.openMain(player);
                } else if (slot == GuiManager.CLOSE_SLOT) {
                    player.closeInventory();
                }
            }
            case ENCHANT_DETAIL -> {
                if (slot >= 19 && slot <= 23) {
                    int level = slot - 18;
                    CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(holder.enchantId);

                    if (enchant != null && level <= enchant.maxLevel
                            && !(enchant.id.equals("nightmare_mark") && level < 5)
                            && !(enchant.id.equals("abyssal_howl") && level > 1)) {
                        click(player);
                        GuiManager.openEnchantDetail(player, holder.enchantId, level);
                    }
                } else if (slot == 31) {
                    click(player);
                    GuiManager.openRecipe(player, holder.enchantId, holder.level, 1);
                } else if (slot == GuiManager.BACK_SLOT) {
                    click(player);
                    GuiManager.openEnchantList(player);
                } else if (slot == GuiManager.CLOSE_SLOT) {
                    player.closeInventory();
                }
            }
            case RECIPE -> {
                boolean abyssal = holder.enchantId.equals("abyssal_howl");

                if (slot == GuiManager.BACK_SLOT) {
                    click(player);
                    GuiManager.openEnchantDetail(player, holder.enchantId, holder.level);
                } else if (slot == GuiManager.CLOSE_SLOT) {
                    player.closeInventory();
                } else if (abyssal && slot == GuiManager.PREV_SLOT) {
                    click(player);
                    GuiManager.openRecipe(player, holder.enchantId, holder.level, holder.abyssalStep - 1);
                } else if (abyssal && slot == GuiManager.NEXT_SLOT) {
                    click(player);
                    GuiManager.openRecipe(player, holder.enchantId, holder.level, holder.abyssalStep + 1);
                }
            }
            case RELICS -> {
                if (slot == 20) {
                    click(player);
                    GuiManager.openSoulHeartRecipe(player);
                } else if (slot == 24) {
                    click(player);
                    GuiManager.openTendrilsRecipe(player);
                } else if (slot == GuiManager.BACK_SLOT) {
                    click(player);
                    GuiManager.openMain(player);
                } else if (slot == GuiManager.CLOSE_SLOT) {
                    player.closeInventory();
                }
            }
            case SOUL_HEART_RECIPE, TENDRILS_RECIPE -> {
                if (slot == GuiManager.BACK_SLOT) {
                    click(player);
                    GuiManager.openRelics(player);
                } else if (slot == GuiManager.CLOSE_SLOT) {
                    player.closeInventory();
                }
            }
            case STORY -> {
                if (slot == 48) {
                    click(player);
                    StoryService.open(player, Math.max(0, holder.page - 1));
                } else if (slot == 50) {
                    click(player);
                    StoryService.open(player, Math.min(StoryService.size() - 1, holder.page + 1));
                } else if (slot == 49) {
                    click(player);
                    StoryService.togglePause(player);
                } else if (slot == GuiManager.CLOSE_SLOT) {
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof NightmareGuiHolder) {
            event.setCancelled(true);
        }
    }

    private static void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7F, 1.2F);
    }

}