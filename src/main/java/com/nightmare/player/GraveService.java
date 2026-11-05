package com.nightmare.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;
import com.nightmare.lang.LangService;

public final class GraveService implements Listener {

    private static final int[][] DIRECTIONS = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 }
    };

    private static final int[] VERTICAL_OFFSETS = { 0, -1, 1, -2, 2, -3, 3, -4, 4, -5, 5, -6, 6 };

    private static final Map<String, Grave> GRAVES = new ConcurrentHashMap<>();
    private static final Map<String, String> CHEST_TO_GRAVE = new ConcurrentHashMap<>();
    private static Plugin plugin;

    public record Grave(String id, UUID owner, String ownerName, String world, int x, int y, int z,
            List<String> chests, String display, long createdAt) {
    }

    private record ChestPair(List<String> keys, Inventory primary, Inventory secondary) {
    }

    private GraveService() {
    }

    public static void start(Plugin owner) {
        plugin = owner;
        Bukkit.getPluginManager().registerEvents(new GraveService(), owner);
        load();
    }

    public static void shutdown() {
        save();
    }

    public static List<Grave> list() {
        return new ArrayList<>(GRAVES.values());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getGameMode() != GameMode.SURVIVAL || !enabled()) {
            return;
        }

        List<ItemStack> items = collect(player);
        add(items, player.getItemOnCursor());
        items.addAll(BackpackService.drain(player));

        event.getDrops().clear();
        player.setItemOnCursor(null);
        player.getInventory().clear();
        event.setKeepInventory(true);

        if (!items.isEmpty()) {
            createGrave(player, player.getLocation(), items);
        }

        player.getInventory().setItem(PermanentItemService.BACKPACK_SLOT, BackpackItem.create(player));
        player.getInventory().setItem(PermanentItemService.RECOVERY_SLOT, new ItemStack(Material.RECOVERY_COMPASS));
        player.updateInventory();
    }

    private static List<ItemStack> collect(Player player) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack[] storage = player.getInventory().getStorageContents();

        for (int slot = 0; slot < storage.length; slot++) {
            if (slot == PermanentItemService.BACKPACK_SLOT || slot == PermanentItemService.RECOVERY_SLOT) {
                continue;
            }

            add(items, storage[slot]);
        }

        for (ItemStack item : player.getInventory().getArmorContents()) {
            add(items, item);
        }

        add(items, player.getInventory().getItemInOffHand());

        return items;
    }

    private static void add(List<ItemStack> items, ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            items.add(item);
        }
    }

    private static void createGrave(Player player, Location death, List<ItemStack> items) {
        World world = death.getWorld();
        Location site = findGraveSite(world, death);
        List<ItemStack> remaining = new ArrayList<>(items);
        List<String> chests = new ArrayList<>();
        int attempts = 0;

        while (!remaining.isEmpty() && attempts < 16) {
            ChestPair pair = placeChestPair(world, site);

            if (pair == null) {
                break;
            }

            chests.addAll(pair.keys());
            remaining = fill(pair, remaining);
            attempts++;
        }

        if (chests.isEmpty()) {
            for (ItemStack item : remaining) {
                world.dropItemNaturally(death, item);
            }

            return;
        }

        if (!remaining.isEmpty()) {
            for (ItemStack item : remaining) {
                world.dropItemNaturally(site, item);
            }
        }

        long created = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        String display = spawnLabel(world, site, player.getName());
        Grave grave = new Grave(id, player.getUniqueId(), player.getName(), world.getName(),
                site.getBlockX(), site.getBlockY(), site.getBlockZ(), List.copyOf(chests), display, created);

        GRAVES.put(id, grave);
        mapChests(grave);
        save();

        plugin.getLogger().info("Created grave for " + player.getName() + " at "
                + world.getName() + ":" + site.getBlockX() + "," + site.getBlockY() + "," + site.getBlockZ()
                + " chests=" + chests.size());
    }

    private static Location findGraveSite(World world, Location death) {
        int baseY = death.getBlockY();

        for (int radius = 0; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    int x = death.getBlockX() + dx;
                    int z = death.getBlockZ() + dz;

                    for (int offset : VERTICAL_OFFSETS) {
                        int y = baseY + offset;

                        if (isSafeCell(world, x, y, z)) {
                            return new Location(world, x, y, z);
                        }
                    }
                }
            }
        }

        Location spawn = world.getSpawnLocation();
        int y = world.getHighestBlockYAt(spawn);

        if (isSafeCell(world, spawn.getBlockX(), y, spawn.getBlockZ())) {
            return new Location(world, spawn.getBlockX(), y, spawn.getBlockZ());
        }

        return new Location(world, spawn.getBlockX(), y + 1, spawn.getBlockZ());
    }

    private static boolean isSafeCell(World world, int x, int y, int z) {
        if (y <= world.getMinHeight() || y + 2 > world.getMaxHeight()) {
            return false;
        }

        Material below = world.getBlockAt(x, y - 1, z).getType();

        if (!below.isSolid() || isLiquid(below)) {
            return false;
        }

        return canPlaceChest(world.getBlockAt(x, y, z)) && canPlaceChest(world.getBlockAt(x, y + 1, z));
    }

    private static boolean canPlaceChest(Block block) {
        Material type = block.getType();

        if (isLiquid(type) || type.isSolid()) {
            return false;
        }

        return true;
    }

    private static boolean isLiquid(Material material) {
        return material == Material.WATER || material == Material.LAVA || material == Material.BUBBLE_COLUMN;
    }

    private static ChestPair placeChestPair(World world, Location base) {
        for (Location cell : candidateCells(world, base)) {
            for (int[] direction : DIRECTIONS) {
                Location other = cell.clone().add(direction[0], 0, direction[1]);

                if (canPlacePair(world, cell, other)) {
                    return placePair(world, cell, other);
                }
            }
        }

        return null;
    }

    private static List<Location> candidateCells(World world, Location base) {
        List<Location> cells = new ArrayList<>();

        for (int radius = 0; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    int x = base.getBlockX() + dx;
                    int z = base.getBlockZ() + dz;

                    for (int offset : VERTICAL_OFFSETS) {
                        int y = base.getBlockY() + offset;

                        if (isSafeCell(world, x, y, z)) {
                            cells.add(new Location(world, x, y, z));
                            break;
                        }
                    }
                }
            }
        }

        return cells;
    }

    private static boolean canPlacePair(World world, Location first, Location second) {
        if (first.getBlockX() == second.getBlockX() && first.getBlockZ() == second.getBlockZ()) {
            return false;
        }

        if (!isSafeCell(world, second.getBlockX(), second.getBlockY(), second.getBlockZ())) {
            return false;
        }

        return !hasChestNeighbor(world, first, second);
    }

    private static boolean hasChestNeighbor(World world, Location first, Location second) {
        Set<String> pair = new HashSet<>();
        pair.add(blockKey(first));
        pair.add(blockKey(second));

        for (Location cell : List.of(first, second)) {
            for (int[] direction : DIRECTIONS) {
                Location neighbor = cell.clone().add(direction[0], 0, direction[1]);

                if (pair.contains(blockKey(neighbor))) {
                    continue;
                }

                Material type = world.getBlockAt(neighbor).getType();

                if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
                    return true;
                }
            }
        }

        return false;
    }

    private static ChestPair placePair(World world, Location first, Location second) {
        Block firstBlock = world.getBlockAt(first);
        Block secondBlock = world.getBlockAt(second);

        firstBlock.setType(Material.CHEST, false);
        secondBlock.setType(Material.CHEST, false);
        face(firstBlock);
        face(secondBlock);

        Inventory primary = ((Chest) firstBlock.getState()).getInventory();
        Inventory secondary = ((Chest) secondBlock.getState()).getInventory();
        List<String> keys = List.of(blockKey(first), blockKey(second));

        if (primary.getSize() >= 54) {
            return new ChestPair(keys, primary, null);
        }

        return new ChestPair(keys, primary, secondary);
    }

    private static void face(Block block) {
        if (block.getBlockData() instanceof Directional directional) {
            directional.setFacing(org.bukkit.block.BlockFace.NORTH);
            block.setBlockData(directional, false);
        }
    }

    private static List<ItemStack> fill(ChestPair pair, List<ItemStack> items) {
        Map<Integer, ItemStack> leftover = pair.primary().addItem(items.toArray(new ItemStack[0]));
        List<ItemStack> remaining = new ArrayList<>(leftover.values());

        if (pair.secondary() != null && !remaining.isEmpty()) {
            leftover = pair.secondary().addItem(remaining.toArray(new ItemStack[0]));
            remaining = new ArrayList<>(leftover.values());
        }

        return remaining;
    }

    private static String spawnLabel(World world, Location site, String playerName) {
        Location at = new Location(world, site.getBlockX() + 0.5D, site.getBlockY() + 2.2D,
                site.getBlockZ() + 0.5D);
        String text = LangService.format("grave.label", "&7R.I.P. %player%", "%player%", playerName);
        TextDisplay display = world.spawn(at, TextDisplay.class, entity -> {
            entity.setText(ChatColor.translateAlternateColorCodes('&', text));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setLineWidth(160);
            entity.setSeeThrough(false);
            entity.setBackgroundColor(Color.fromARGB(160, 10, 10, 18));
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
        });

        return display.getUniqueId().toString();
    }

    private static void mapChests(Grave grave) {
        for (String key : grave.chests()) {
            CHEST_TO_GRAVE.put(key, grave.id());
        }
    }

    private static void unmapChests(Grave grave) {
        for (String key : grave.chests()) {
            CHEST_TO_GRAVE.remove(key);
        }
    }

    private static void removeGrave(Grave grave, boolean removeBlocks) {
        GRAVES.remove(grave.id());
        unmapChests(grave);

        if (removeBlocks) {
            for (String key : grave.chests()) {
                Location location = parseKey(key);

                if (location != null && location.getWorld() != null) {
                    Block block = location.getBlock();

                    if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }

        if (grave.display() != null) {
            try {
                Entity entity = Bukkit.getEntity(UUID.fromString(grave.display()));

                if (entity != null) {
                    entity.remove();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        save();
    }

    private static void checkEmpty(Grave grave) {
        for (String key : grave.chests()) {
            Location location = parseKey(key);

            if (location == null || location.getWorld() == null) {
                return;
            }

            World world = location.getWorld();

            if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                return;
            }

            if (location.getBlock().getState() instanceof Chest chest && !chest.getInventory().isEmpty()) {
                return;
            }
        }

        removeGrave(grave, true);
    }

    private static boolean isGraveChest(Block block) {
        return CHEST_TO_GRAVE.containsKey(blockKey(block.getLocation()));
    }

    private static String blockKey(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ","
                + location.getBlockY() + "," + location.getBlockZ();
    }

    private static Location parseKey(String key) {
        int colon = key.indexOf(':');

        if (colon <= 0) {
            return null;
        }

        String worldName = key.substring(0, colon);
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        String[] parts = key.substring(colon + 1).split(",");

        if (parts.length != 3) {
            return null;
        }

        try {
            return new Location(world, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static boolean enabled() {
        return Main.balance == null || Main.balance.getBoolean("player-items.enabled", true);
    }

    private static boolean ownerOnly() {
        return Main.balance == null || Main.balance.getBoolean("player-items.grave.owner-only", true);
    }

    private static boolean protect() {
        return Main.balance != null && Main.balance.getBoolean("player-items.grave.protect", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpen(InventoryOpenEvent event) {
        Location at = event.getInventory().getLocation();

        if (at == null || !(event.getPlayer() instanceof Player player)) {
            return;
        }

        String id = CHEST_TO_GRAVE.get(blockKey(at));

        if (id == null) {
            return;
        }

        Grave grave = GRAVES.get(id);

        if (grave == null) {
            return;
        }

        if (ownerOnly() && !grave.owner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LangService.format("grave.not_owner", "&cThis grave belongs to %player%.",
                            "%player%", grave.ownerName())));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        Location at = event.getInventory().getLocation();

        if (at == null) {
            return;
        }

        String id = CHEST_TO_GRAVE.get(blockKey(at));

        if (id == null) {
            return;
        }

        Grave grave = GRAVES.get(id);

        if (grave == null) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Grave current = GRAVES.get(grave.id());

            if (current != null) {
                checkEmpty(current);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        String id = CHEST_TO_GRAVE.get(blockKey(event.getBlock().getLocation()));

        if (id == null) {
            return;
        }

        if (protect()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    LangService.msg("grave.protected", "&cThis grave is protected.")));
            return;
        }

        Grave grave = GRAVES.get(id);

        if (grave != null) {
            removeGrave(grave, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private static void handleExplosion(List<Block> blocks) {
        if (protect()) {
            blocks.removeIf(GraveService::isGraveChest);
            return;
        }

        for (Block block : new ArrayList<>(blocks)) {
            String id = CHEST_TO_GRAVE.get(blockKey(block.getLocation()));
            Grave grave = id == null ? null : GRAVES.get(id);

            if (grave != null) {
                removeGrave(grave, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (protect() && event.getBlocks().stream().anyMatch(GraveService::isGraveChest)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (protect() && event.getBlocks().stream().anyMatch(GraveService::isGraveChest)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMoveItem(InventoryMoveItemEvent event) {
        if (!protect()) {
            return;
        }

        Location source = event.getSource().getLocation();
        Location destination = event.getDestination().getLocation();

        if ((source != null && CHEST_TO_GRAVE.containsKey(blockKey(source)))
                || (destination != null && CHEST_TO_GRAVE.containsKey(blockKey(destination)))) {
            event.setCancelled(true);
        }
    }

    private static File file() {
        return new File(plugin.getDataFolder(), "graves.yml");
    }

    private static void load() {
        GRAVES.clear();
        CHEST_TO_GRAVE.clear();
        File file = file();

        if (!file.exists()) {
            return;
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        for (Map<?, ?> raw : yml.getMapList("graves")) {
            try {
                String id = String.valueOf(raw.get("id"));
                UUID owner = UUID.fromString(String.valueOf(raw.get("owner")));
                String ownerName = String.valueOf(raw.get("owner-name"));
                String world = String.valueOf(raw.get("world"));
                int x = number(raw.get("x"));
                int y = number(raw.get("y"));
                int z = number(raw.get("z"));
                List<String> chests = strings(raw.get("chests"));
                String display = raw.get("display") == null ? null : String.valueOf(raw.get("display"));
                long created = raw.get("created") instanceof Number number ? number.longValue() : 0L;
                Grave grave = new Grave(id, owner, ownerName, world, x, y, z, chests, display, created);

                GRAVES.put(id, grave);
                mapChests(grave);
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("Skipping invalid grave entry: " + exception.getMessage());
            }
        }
    }

    private static void save() {
        if (plugin == null) {
            return;
        }

        List<Map<String, Object>> list = new ArrayList<>();

        for (Grave grave : GRAVES.values()) {
            Map<String, Object> raw = new HashMap<>();
            raw.put("id", grave.id());
            raw.put("owner", grave.owner().toString());
            raw.put("owner-name", grave.ownerName());
            raw.put("world", grave.world());
            raw.put("x", grave.x());
            raw.put("y", grave.y());
            raw.put("z", grave.z());
            raw.put("chests", List.copyOf(grave.chests()));
            raw.put("display", grave.display());
            raw.put("created", grave.createdAt());
            list.add(raw);
        }

        YamlConfiguration yml = new YamlConfiguration();
        yml.set("graves", list);

        try {
            yml.save(file());
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save graves.yml: " + exception.getMessage());
        }
    }

    private static int number(Object value) {
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        for (Object entry : list) {
            result.add(String.valueOf(entry));
        }

        return result;
    }
}
