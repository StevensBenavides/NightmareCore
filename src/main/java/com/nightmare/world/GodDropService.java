package com.nightmare.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;
import com.nightmare.enchant.EnchantBookService;
import com.nightmare.enchant.EnchantRegistry;
import com.nightmare.lang.LangService;

public final class GodDropService {

    private static final Map<String, Long> LAST_DAY = new ConcurrentHashMap<>();

    private static final Map<StructureTheme, List<String>> THEME_BOOKS = Map.of(
            StructureTheme.SOUL, List.of("lifesteal", "stormcaller", "frostbite", "guardian", "second_wind", "blink"),
            StructureTheme.ABYSS, List.of("void_rend", "venom", "nightmare_mark"));

    private static Plugin plugin;

    private GodDropService() {
    }

    public static void start(Plugin owner) {
        plugin = owner;
        LAST_DAY.clear();
        int everyDays = everyDays();

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            long day = day(world);
            LAST_DAY.put(world.getName(), (day / everyDays) * everyDays);
        }

        Bukkit.getScheduler().runTaskTimer(owner, GodDropService::tick, 20L, 20L);
    }

    public static void shutdown() {
        LAST_DAY.clear();
    }

    public static boolean triggerNow(World world) {
        if (world == null || world.getEnvironment() != World.Environment.NORMAL) {
            return false;
        }

        try {
            return trigger(world);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Could not create a god drop in " + world.getName() + ": "
                    + exception.getMessage());
            return false;
        }
    }

    private static void tick() {
        if (!enabled()) {
            return;
        }

        int everyDays = everyDays();
        int maxDay = maxDay();

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            long day = day(world);

            if (day <= 0 || day > maxDay || day % everyDays != 0) {
                continue;
            }

            long last = LAST_DAY.getOrDefault(world.getName(), day - everyDays);

            if (day > last) {
                LAST_DAY.put(world.getName(), day);

                try {
                    trigger(world);
                } catch (RuntimeException exception) {
                    plugin.getLogger().warning("Could not create a god drop in " + world.getName() + ": "
                            + exception.getMessage());
                }
            } else if (day < last) {
                LAST_DAY.put(world.getName(), day);
            }
        }
    }

    private static boolean trigger(World world) {
        Location site = findDropSite(world);

        if (site == null) {
            plugin.getLogger().warning("Could not find a safe god drop location in " + world.getName());
            return false;
        }

        Location second = findSecondChest(world, site);

        if (second == null) {
            plugin.getLogger().warning("Could not find a second god drop chest location in " + world.getName());
            return false;
        }

        Chest soul = placeChest(site, StructureTheme.SOUL);
        Chest abyss = placeChest(second, StructureTheme.ABYSS);

        if (soul == null || abyss == null) {
            return false;
        }

        fill(soul, StructureTheme.SOUL, world);
        fill(abyss, StructureTheme.ABYSS, world);

        if (Main.balance == null || Main.balance.getBoolean("god-drops.announce", true)) {
            announce(world, site);
        }

        plugin.getLogger().info("God drop created in " + world.getName() + " at "
                + site.getBlockX() + "," + site.getBlockY() + "," + site.getBlockZ());
        return true;
    }

    private static Location findDropSite(World world) {
        Location spawn = world.getSpawnLocation();
        WorldBorder border = world.getWorldBorder();
        int margin = Math.max(0, Main.balance == null ? 100 : Main.balance.getInt("god-drops.border-margin", 100));
        int maxDistance = Math.max(100, Main.balance == null ? 3000
                : Main.balance.getInt("god-drops.max-distance-from-spawn", 3000));
        double borderRadius = Math.max(8.0D, border.getSize() / 2.0D - margin);
        double radius = Math.min(maxDistance, borderRadius);

        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * radius;
            int x = spawn.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = spawn.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            Location candidate = new Location(world, x, 0.0D, z);

            if (!border.isInside(candidate)) {
                continue;
            }

            Location safe = safeSurface(world, x, z);

            if (safe != null) {
                return safe;
            }
        }

        return null;
    }

    private static Location safeSurface(World world, int x, int z) {
        world.getChunkAt(x >> 4, z >> 4);
        int top = world.getHighestBlockYAt(x, z);

        for (int y = top; y >= Math.max(world.getMinHeight() + 1, top - 24); y--) {
            Block ground = world.getBlockAt(x, y, z);

            if (!ground.getType().isSolid() || ground.isLiquid()) {
                continue;
            }

            Block feet = world.getBlockAt(x, y + 1, z);
            Block head = world.getBlockAt(x, y + 2, z);

            if (isOpen(feet) && isOpen(head)) {
                return new Location(world, x, y + 1, z);
            }
        }

        return null;
    }

    private static Location findSecondChest(World world, Location first) {
        int[][] offsets = {
                { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 },
                { 3, 0 }, { -3, 0 }, { 0, 3 }, { 0, -3 },
                { 2, 2 }, { -2, 2 }, { 2, -2 }, { -2, -2 }
        };

        for (int[] offset : offsets) {
            int x = first.getBlockX() + offset[0];
            int z = first.getBlockZ() + offset[1];
            Location safe = safeSurface(world, x, z);

            if (safe == null) {
                continue;
            }

            if (Math.abs(safe.getBlockY() - first.getBlockY()) > 1) {
                continue;
            }

            if (world.getBlockAt(x, safe.getBlockY() - 1, z).getType().isSolid()) {
                return safe;
            }
        }

        return null;
    }

    private static Chest placeChest(Location location, StructureTheme theme) {
        Block block = location.getBlock();

        if (!isOpen(block)) {
            return null;
        }

        block.setType(Material.CHEST, false);

        if (block.getBlockData() instanceof Directional directional) {
            directional.setFacing(org.bukkit.block.BlockFace.SOUTH);
            block.setBlockData(directional, false);
        }

        if (!(block.getState() instanceof Chest chest)) {
            return null;
        }

        spawnLabel(location, theme);
        return chest;
    }

    private static void spawnLabel(Location location, StructureTheme theme) {
        String path = theme == StructureTheme.SOUL ? "god_drop.soul_label" : "god_drop.abyss_label";
        String fallback = theme == StructureTheme.SOUL ? "&dSoul God's Gift" : "&5Abyss God's Gift";
        String text = ChatColor.translateAlternateColorCodes('&', LangService.msg(path, fallback));
        Location at = new Location(location.getWorld(), location.getBlockX() + 0.5D,
                location.getBlockY() + 1.4D, location.getBlockZ() + 0.5D);

        location.getWorld().spawn(at, TextDisplay.class, entity -> {
            entity.setText(text);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setLineWidth(120);
            entity.setSeeThrough(false);
            entity.setBackgroundColor(Color.fromARGB(150, 10, 10, 18));
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
        });
    }

    private static void fill(Chest chest, StructureTheme theme, World world) {
        Inventory inventory = chest.getSnapshotInventory();
        Random random = ThreadLocalRandom.current();
        String key = theme.name().toLowerCase();
        List<String> pool = Main.balance == null ? List.of()
                : Main.balance.getStringList("altars.loot." + key);
        int commonRolls = Math.max(0, Main.balance == null ? 2
                : Main.balance.getInt("god-drops.common-rolls", 2));
        int bookRolls = Math.max(0, Main.balance == null ? 2
                : Main.balance.getInt("god-drops.book-rolls", 2));
        double extraBookChance = Main.balance == null ? 0.35D
                : Main.balance.getDouble("god-drops.extra-book-chance", 0.35D);
        int maxBookLevel = Math.max(1, Main.balance == null ? 3
                : Main.balance.getInt("god-drops.book-max-level", 3));
        long day = day(world);
        Set<String> usedBooks = new HashSet<>();
        int added = 0;

        for (int i = 0; i < commonRolls && !pool.isEmpty(); i++) {
            String entry = pool.get(random.nextInt(pool.size()));
            ItemStack item = parseLoot(entry, random);

            if (item != null && addAtRandomSlot(inventory, item)) {
                added++;
            }
        }

        for (int i = 0; i < bookRolls; i++) {
            ItemStack book = rollThemeBook(theme, day, maxBookLevel, usedBooks, random);

            if (book != null && addAtRandomSlot(inventory, book)) {
                added++;
            }
        }

        if (random.nextDouble() < extraBookChance) {
            ItemStack book = rollThemeBook(theme, day, maxBookLevel, usedBooks, random);

            if (book != null && addAtRandomSlot(inventory, book)) {
                added++;
            }
        }

        if (random.nextDouble() < chance("ink-chance", 0.10D)) {
            if (addAtRandomSlot(inventory, EnchantBookService.createAbyssalInk())) {
                added++;
            }
        }

        if (random.nextDouble() < chance("nucleus-chance", 0.10D)) {
            if (addAtRandomSlot(inventory, EnchantBookService.createAbyssalNucleus())) {
                added++;
            }
        }

        if (random.nextDouble() < chance("lore-book-chance", 0.10D)) {
            com.nightmare.gui.StoryService.Chapter chapter = com.nightmare.gui.StoryService
                    .chapter(theme.storyChapter);

            if (chapter != null && addAtRandomSlot(inventory, StructureService.loreBook(chapter))) {
                added++;
            }
        }

        if (added == 0 && !pool.isEmpty()) {
            ItemStack fallback = parseLoot(pool.get(random.nextInt(pool.size())), random);

            if (fallback != null) {
                addAtRandomSlot(inventory, fallback);
            }
        }

        chest.update();
    }

    private static ItemStack rollThemeBook(StructureTheme theme, long day, int maxBookLevel,
            Set<String> usedBooks, Random random) {
        List<String> pool = new ArrayList<>(THEME_BOOKS.getOrDefault(theme, List.of()));
        java.util.Collections.shuffle(pool, random);

        for (String id : pool) {
            if (usedBooks.contains(id) || !EnchantRegistry.ENCHANTS.containsKey(id)) {
                continue;
            }

            int cap = Math.min(EnchantRegistry.ENCHANTS.get(id).maxLevel,
                    EnchantBookService.maxLevelForDay(id, day));
            cap = Math.min(cap, maxBookLevel);

            if (cap < 1) {
                continue;
            }

            usedBooks.add(id);
            return EnchantBookService.createBook(id, 1 + random.nextInt(cap));
        }

        return null;
    }

    private static boolean addAtRandomSlot(Inventory inventory, ItemStack item) {
        if (item == null) {
            return false;
        }

        int size = inventory.getSize();
        Random random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < size * 2; attempt++) {
            int slot = random.nextInt(size);
            ItemStack existing = inventory.getItem(slot);

            if (existing == null || existing.getType().isAir()) {
                inventory.setItem(slot, item);
                return true;
            }
        }

        return inventory.addItem(item).isEmpty();
    }

    private static ItemStack parseLoot(String entry, Random random) {
        if (entry == null) {
            return null;
        }

        String[] parts = entry.split(":");

        if (parts.length < 2) {
            return null;
        }

        Material material = Material.matchMaterial(parts[0]);

        if (material == null) {
            return null;
        }

        int min = 1;
        int max = 1;
        String[] range = parts[1].split("-");

        try {
            min = Integer.parseInt(range[0]);
            max = range.length > 1 ? Integer.parseInt(range[1]) : min;
        } catch (NumberFormatException ignored) {
        }

        int amount = min + (max > min ? random.nextInt(max - min + 1) : 0);

        try {
            return new ItemStack(material, Math.max(1, amount));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void announce(World world, Location site) {
        String message = LangService.format("god_drop.announce",
                "&7A whisper drifts through the world... &5The gods of Soul and Abyss left a gift at &f%x%, %y%, %z% &7in &f%world%&7.",
                "%x%", String.valueOf(site.getBlockX()),
                "%y%", String.valueOf(site.getBlockY()),
                "%z%", String.valueOf(site.getBlockZ()),
                "%world%", world.getName());

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static boolean isOpen(Block block) {
        Material type = block.getType();
        return type.isAir() || (!type.isSolid() && !block.isLiquid());
    }

    private static double chance(String key, double fallback) {
        return Main.balance == null ? fallback : Main.balance.getDouble("god-drops." + key, fallback);
    }

    private static boolean enabled() {
        return Main.balance == null || Main.balance.getBoolean("god-drops.enabled", true);
    }

    private static int everyDays() {
        return Math.max(1, Main.balance == null ? 9 : Main.balance.getInt("god-drops.every-days", 9));
    }

    private static int maxDay() {
        return Math.max(0, Main.balance == null ? 90 : Main.balance.getInt("god-drops.max-day", 90));
    }

    private static long day(World world) {
        return world.getFullTime() / 24000L;
    }
}
