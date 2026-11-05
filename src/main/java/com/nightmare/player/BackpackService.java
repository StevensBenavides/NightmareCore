package com.nightmare.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;
import com.nightmare.lang.LangService;

public final class BackpackService implements Listener {

    private static final Map<UUID, ItemStack[]> CACHE = new ConcurrentHashMap<>();
    private static Plugin plugin;
    private static File folder;

    private BackpackService() {
    }

    public static void start(Plugin owner) {
        plugin = owner;
        folder = new File(owner.getDataFolder(), "backpacks");

        if (!folder.exists() && !folder.mkdirs()) {
            owner.getLogger().warning("Could not create backpack data folder.");
        }

        Bukkit.getPluginManager().registerEvents(new BackpackService(), owner);
    }

    public static void open(Player player) {
        if (!enabled() || player.getOpenInventory().getTopInventory().getHolder() instanceof BackpackHolder) {
            return;
        }

        int size = size();
        BackpackHolder holder = new BackpackHolder(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, size, title());
        holder.setInventory(inventory);
        ItemStack[] contents = load(player.getUniqueId());

        for (int slot = 0; slot < Math.min(size, contents.length); slot++) {
            inventory.setItem(slot, contents[slot]);
        }

        player.openInventory(inventory);
    }

    private static boolean enabled() {
        return Main.balance == null || Main.balance.getBoolean("player-items.enabled", true);
    }

    public static int size() {
        int configured = Main.balance == null ? 27 : Main.balance.getInt("player-items.backpack-size", 27);
        configured = Math.max(9, Math.min(54, configured));
        configured = (configured / 9) * 9;

        return configured;
    }

    public static List<ItemStack> drain(Player player) {
        ItemStack[] contents = currentContents(player);
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack item : contents) {
            if (item != null && !item.getType().isAir()) {
                items.add(item);
            }
        }

        if (player.getOpenInventory().getTopInventory().getHolder() instanceof BackpackHolder) {
            player.closeInventory();
        }

        CACHE.put(player.getUniqueId(), empty());
        delete(player.getUniqueId());

        return items;
    }

    public static void save(Player player) {
        if (player == null) {
            return;
        }

        ItemStack[] contents;

        if (player.getOpenInventory().getTopInventory().getHolder() instanceof BackpackHolder holder
                && holder.owner().equals(player.getUniqueId())) {
            contents = holder.getInventory().getContents();
        } else {
            contents = CACHE.get(player.getUniqueId());

            if (contents == null) {
                return;
            }
        }

        CACHE.put(player.getUniqueId(), contents);
        write(player.getUniqueId(), contents);
    }

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            save(player);
        }

        CACHE.clear();
    }

    private static ItemStack[] currentContents(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof BackpackHolder holder
                && holder.owner().equals(player.getUniqueId())) {
            return holder.getInventory().getContents();
        }

        return load(player.getUniqueId());
    }

    private static ItemStack[] load(UUID uuid) {
        ItemStack[] cached = CACHE.get(uuid);

        if (cached != null) {
            return cached;
        }

        int size = size();
        ItemStack[] contents = empty(size);
        File file = file(uuid);

        if (file.exists()) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

            for (Map<?, ?> raw : yml.getMapList("items")) {
                Object slotValue = raw.get("slot");
                Object itemValue = raw.get("item");

                if (!(slotValue instanceof Number number) || !(itemValue instanceof Map<?, ?> itemMap)) {
                    continue;
                }

                int slot = number.intValue();

                if (slot < 0 || slot >= size) {
                    continue;
                }

                contents[slot] = deserialize(itemMap);
            }
        }

        CACHE.put(uuid, contents);

        return contents;
    }

    private static void write(UUID uuid, ItemStack[] contents) {
        File file = file(uuid);
        List<Map<String, Object>> items = new ArrayList<>();
        boolean any = false;

        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            Map<String, Object> entry = new HashMap<>();
            entry.put("slot", slot);
            entry.put("item", item.serialize());
            items.add(entry);
            any = true;
        }

        if (!any) {
            if (file.exists() && !file.delete()) {
                plugin.getLogger().warning("Could not delete empty backpack file for " + uuid);
            }

            return;
        }

        YamlConfiguration yml = new YamlConfiguration();
        yml.set("items", items);

        try {
            if (!folder.exists() && !folder.mkdirs()) {
                plugin.getLogger().warning("Could not create backpack data folder.");
                return;
            }

            yml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save backpack for " + uuid + ": " + exception.getMessage());
        }
    }

    private static void delete(UUID uuid) {
        File file = file(uuid);

        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Could not delete backpack file for " + uuid);
        }
    }

    private static File file(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }

    private static ItemStack[] empty() {
        return empty(size());
    }

    private static ItemStack[] empty(int size) {
        return new ItemStack[size];
    }

    private static ItemStack deserialize(Map<?, ?> raw) {
        Map<String, Object> typed = new HashMap<>();

        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            typed.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        try {
            return ItemStack.deserialize(typed);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static String title() {
        return ChatColor.translateAlternateColorCodes('&', LangService.msg("backpack.title", "&8Backpack"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof BackpackHolder holder) {
            UUID uuid = holder.owner();
            ItemStack[] contents = event.getInventory().getContents();
            CACHE.put(uuid, contents);
            write(uuid, contents);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        save(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder)) {
            return;
        }

        if (BackpackItem.is(event.getCurrentItem()) || BackpackItem.is(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder)) {
            return;
        }

        if (BackpackItem.is(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }
}
