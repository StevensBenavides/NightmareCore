package com.nightmare.player;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;

public final class PermanentItemService implements Listener {

    public static int BACKPACK_SLOT = 9;
    public static int RECOVERY_SLOT = 10;
    private static Plugin plugin;

    private PermanentItemService() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == player.getInventory()) {
            int slot = event.getSlot();

            if (slot == BACKPACK_SLOT) {
                event.setCancelled(true);

                if (event.getCursor() == null || event.getCursor().getType().isAir()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            BackpackService.open(player);
                        }
                    });
                }

                return;
            }

            if (slot == RECOVERY_SLOT) {
                event.setCancelled(true);
                return;
            }
        }

        if (isPermanentItem(event.getCurrentItem()) || isPermanentItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && wouldMoveIntoProtected(player, event, event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    public static void ensure(Player player) {
        if (player == null || !player.isOnline() || player.isDead() || !enabled()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean changed = false;
        ItemStack backpack = inventory.getItem(BACKPACK_SLOT);

        if (!BackpackItem.is(backpack) || !BackpackItem.isOwnedBy(backpack, player)) {
            if (backpack != null && !backpack.getType().isAir()) {
                drop(player, backpack);
            }

            inventory.setItem(BACKPACK_SLOT, BackpackItem.create(player));
            changed = true;
        }

        ItemStack recovery = inventory.getItem(RECOVERY_SLOT);

        if (recovery == null || recovery.getType() != Material.RECOVERY_COMPASS) {
            if (recovery != null && !recovery.getType().isAir()) {
                drop(player, recovery);
            }

            inventory.setItem(RECOVERY_SLOT, new ItemStack(Material.RECOVERY_COMPASS));
            changed = true;
        }

        if (changed) {
            player.updateInventory();
        }
    }

    private static boolean wouldMoveIntoProtected(Player player, InventoryClickEvent event, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        Inventory top = event.getView().getTopInventory();
        boolean sourceIsPlayer = event.getClickedInventory() == player.getInventory();

        if (sourceIsPlayer && top != player.getInventory()) {
            return false;
        }

        for (int slot : new int[] { BACKPACK_SLOT, RECOVERY_SLOT }) {
            ItemStack existing = player.getInventory().getItem(slot);

            if (existing == null || existing.getType().isAir() || existing.isSimilar(item)) {
                return true;
            }
        }

        return false;
    }

    public static void start(Plugin owner) {
        plugin = owner;

        if (Main.balance != null) {
            BACKPACK_SLOT = slot(Main.balance.getInt("player-items.backpack-slot", 9), 9);
            RECOVERY_SLOT = slot(Main.balance.getInt("player-items.recovery-slot", 10), 10);

            if (BACKPACK_SLOT == RECOVERY_SLOT) {
                RECOVERY_SLOT = BACKPACK_SLOT == 10 ? 9 : 10;
            }
        }

        Bukkit.getPluginManager().registerEvents(new PermanentItemService(), owner);
        Bukkit.getScheduler().runTaskTimer(owner, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ensure(player);
            }
        }, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (isPermanentItem(event.getOldCursor())) {
            event.setCancelled(true);
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (isProtectedRawSlot(player, event.getView(), rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private static boolean isProtectedRawSlot(Player player, InventoryView view, int rawSlot) {
        Inventory inventory = view.getInventory(rawSlot);

        if (inventory != player.getInventory()) {
            return false;
        }

        int slot = view.convertSlot(rawSlot);

        return slot == BACKPACK_SLOT || slot == RECOVERY_SLOT;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (isPermanentItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (isPermanentItem(event.getMainHandItem()) || isPermanentItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    private static boolean enabled() {
        return Main.balance == null
                || Main.balance.getBoolean("player-items.enabled", true);
    }

    private static void drop(Player player, ItemStack item) {
        player.getWorld().dropItemNaturally(player.getLocation(), item);
    }

    private static int slot(int configured, int fallback) {
        return configured >= 0 && configured < 36 ? configured : fallback;
    }

    private static boolean isPermanentItem(ItemStack item) {
        return BackpackItem.is(item);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        safeEnsureLater(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        safeEnsureLater(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        safeEnsureLater(event.getPlayer());
    }

    private static void safeEnsureLater(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> ensure(player));
    }
}
