package com.nightmare.world;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;


import com.nightmare.Main;
import com.nightmare.enchant.EnchantBookService;
import com.nightmare.enchant.EnchantRegistry;
import com.nightmare.gui.StoryService;

public final class StructureService implements Listener {

    public record Altar(String id, String world, int x, int y, int z, String theme, long seed,
            boolean looted, List<String> blocks, List<String> chests, List<String> texts,
            String interaction, String lectern) {
    }

    private static Plugin plugin;
    private static final Map<String, Altar> ALTARS = new ConcurrentHashMap<>();
    private static final Set<String> CHESTS = ConcurrentHashMap.newKeySet();
    private static final Set<String> LECTERNS = ConcurrentHashMap.newKeySet();

    public StructureService() {
    }

    public static void load(Plugin owner) {
        plugin = owner;

        plugin.saveResource("structures.yml", false);
        ALTARS.clear();
        CHESTS.clear();
        LECTERNS.clear();

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "structures.yml"));

        for (Map<?, ?> raw : yml.getMapList("structures")) {
            Altar altar = new Altar(
                    String.valueOf(raw.get("id")),
                    String.valueOf(raw.get("world")),
                    number(raw.get("x")), number(raw.get("y")), number(raw.get("z")),
                    String.valueOf(raw.get("theme")),
                    raw.get("seed") instanceof Number number ? number.longValue() : 0L,
                    Boolean.TRUE.equals(raw.get("looted")),
                    strings(raw.get("blocks")), strings(raw.get("chests")), strings(raw.get("texts")),
                    raw.get("interaction") == null ? null : String.valueOf(raw.get("interaction")),
                    raw.get("lectern") == null ? null : String.valueOf(raw.get("lectern")));
            ALTARS.put(altar.id(), altar);
            CHESTS.addAll(altar.chests());

            if (raw.get("lectern") != null) {
                LECTERNS.add(String.valueOf(raw.get("lectern")));
            }
        }
    }

    public static void save() {
        List<Map<String, Object>> list = new ArrayList<>();

        for (Altar altar : ALTARS.values()) {
            Map<String, Object> raw = new HashMap<>();

            raw.put("id", altar.id());
            raw.put("world", altar.world());
            raw.put("x", altar.x());
            raw.put("y", altar.y());
            raw.put("z", altar.z());
            raw.put("theme", altar.theme());
            raw.put("seed", altar.seed());
            raw.put("looted", altar.looted());
            raw.put("blocks", List.copyOf(altar.blocks()));
            raw.put("chests", List.copyOf(altar.chests()));
            raw.put("texts", List.copyOf(altar.texts()));
            raw.put("interaction", altar.interaction());
            raw.put("lectern", altar.lectern());
            list.add(raw);
        }

        YamlConfiguration yml = new YamlConfiguration();

        yml.set("structures", list);

        try {
            yml.save(new File(plugin.getDataFolder(), "structures.yml"));
        } catch (Exception exception) {
            plugin.getLogger().warning("Could not save structures.yml: " + exception.getMessage());
        }
    }

    public static List<Altar> list() {
        return new ArrayList<>(ALTARS.values());
    }

    public static Altar get(String id) {
        return ALTARS.get(id);
    }

    public static void start(Plugin owner) {
        Bukkit.getScheduler().runTaskTimer(owner, () -> {
            for (Altar altar : ALTARS.values()) {
                World world = Bukkit.getWorld(altar.world());

                if (world == null || !world.isChunkLoaded(altar.x() >> 4, altar.z() >> 4)) {
                    continue;
                }

                StructureTheme theme = StructureTheme.byName(altar.theme());

                if (theme == null) {
                    continue;
                }

                world.spawnParticle(theme.ambience, altar.x() + 0.5, altar.y() + 2.5, altar.z() + 0.5,
                        3, 1.5, 1.0, 1.5, 0.01);
            }
        }, 40L, 40L);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            return;
        }

        World world = event.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        long hash = hash(world.getSeed(), chunkX, chunkZ);
        int denominator = Math.max(1, Main.balance.getInt("altars.chance-denominator", 400));

        if (Long.remainderUnsigned(hash, denominator) != 0) {
            return;
        }

        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;

        Location spawn = world.getSpawnLocation();

        double distance = Math.hypot(centerX - spawn.getBlockX(), centerZ - spawn.getBlockZ());

        if (distance < Main.balance.getInt("altars.min-distance-from-spawn", 300)) {
            return;
        }

        int baseY = world.getHighestBlockYAt(centerX, centerZ);

        if (!suitable(world, centerX, baseY, centerZ)) {
            return;
        }

        StructureTheme theme = StructureTheme.values()[(int) Long.remainderUnsigned(hash >>> 8, 5)];

        build(world, centerX, baseY, centerZ, theme, hash, false);
    }

    private static long hash(long seed, int chunkX, int chunkZ) {
        long hash = seed * 31 + chunkX;
        hash = hash * 31 + chunkZ;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;

        return hash;
    }

    private static boolean suitable(World world, int baseX, int baseY, int baseZ) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int obstructed = 0;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int surface = world.getHighestBlockYAt(baseX + dx, baseZ + dz);

                min = Math.min(min, surface);
                max = Math.max(max, surface);

                Material top = world.getBlockAt(baseX + dx, surface, baseZ + dz).getType();

                if (top == Material.WATER || top == Material.LAVA || top == Material.BUBBLE_COLUMN) {
                    return false;
                }

                for (int y = surface + 1; y <= surface + 6; y++) {
                    if (world.getBlockAt(baseX + dx, y, baseZ + dz).getType().isSolid()) {
                        obstructed++;
                        break;
                    }
                }
            }
        }

        return max - min <= 3 && obstructed <= 16;
    }

    public static Altar buildAt(Player player, StructureTheme theme) {
        Block ground = player.getLocation().getBlock();

        while (ground.getY() > player.getWorld().getMinHeight() && !ground.getType().isSolid()) {
            ground = ground.getRelative(0, -1, 0);
        }

        return build(player.getWorld(), ground.getX(), ground.getY(), ground.getZ(), theme,
                ThreadLocalRandom.current().nextLong(), true);
    }

    private static Altar build(World world, int baseX, int baseY, int baseZ, StructureTheme theme, long seed,
            boolean manual
    ) {
        String id = theme.name().toLowerCase() + "@" + world.getName() + ":" + baseX + "," + baseY + "," + baseZ;

        int rotation = (int) ((seed >>> 16) & 3);

        StructureDesigns.BuildResult result = StructureDesigns.build(
                StructureDesigns.layout(theme), world, baseX, baseY, baseZ, seed, rotation);

        List<String> texts = new ArrayList<>();
        StoryService.Chapter chapter = StoryService.chapter(theme.storyChapter);

        if (chapter != null) {
            int clearTop = baseY;

            for (int dx = -5; dx <= 5; dx++) {

                for (int dz = -5; dz <= 5; dz++) {
                    clearTop = Math.max(clearTop, world.getHighestBlockYAt(baseX + dx, baseZ + dz));
                }

            }
            Location titleAt = result.center().clone();

            titleAt.setY(clearTop + 4.4);
            texts.add(spawnText(world, titleAt, chapter.title(), 220, true));

            List<String> preview = chapter.lines().subList(0, Math.min(3, chapter.lines().size()));
            Location loreAt = result.center().clone();

            loreAt.setY(clearTop + 2.9);
            texts.add(spawnText(world, loreAt, String.join("\n", preview), 220, false));

            int touchGround = world.getHighestBlockYAt(baseX + 6, baseZ);

            Location interAt = new Location(world, baseX + 6.5, touchGround + 1.0, baseZ + 0.5);
            Location hintAt = new Location(world, baseX + 6.5, touchGround + 3.4, baseZ + 0.5);

            texts.add(spawnText(world, hintAt, com.nightmare.lang.LangService.msg("structures.hint_tale",
                    "§eClick here to read the tale"), 160, false));

            Interaction interaction = world.spawn(interAt, Interaction.class, entity -> {
                entity.setInteractionWidth(1.2F);
                entity.setInteractionHeight(2.5F);
                entity.setResponsive(true);
                entity.getPersistentDataContainer().set(
                        new NamespacedKey(Main.getInstance(), "structure"), PersistentDataType.STRING, id);
            });

            Altar altar = new Altar(id, world.getName(), baseX, baseY, baseZ, theme.name(), seed, false,
                    result.blocks().stream().map(StructureDesigns.Placed::key).toList(),
                    result.chests().stream().map(StructureService::blockKey).toList(),
                    texts, interaction.getUniqueId().toString(),
                    result.lectern() == null ? null : blockKey(result.lectern()));

            ALTARS.put(id, altar);
            CHESTS.addAll(altar.chests());

            if (result.lectern() != null) {
                LECTERNS.add(blockKey(result.lectern()));
                fillLectern(result.lectern(), chapter);
            }

            for (Location chest : result.chests()) {
                fillChest(chest, theme, seed);
            }

            save();

            return altar;
        }

        Altar altar = new Altar(id, world.getName(), baseX, baseY, baseZ, theme.name(), seed, false,
                result.blocks().stream().map(StructureDesigns.Placed::key).toList(),
                result.chests().stream().map(StructureService::blockKey).toList(),
                List.of(), null, result.lectern() == null ? null : blockKey(result.lectern()));

        ALTARS.put(id, altar);

        save();

        return altar;
    }

    private static String spawnText(World world, Location location, String text, int width, boolean title) {
        TextDisplay display = world.spawn(location, TextDisplay.class, entity -> {
            entity.setText(ChatColor.translateAlternateColorCodes('&', text));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setLineWidth(width);
            entity.setSeeThrough(false);
            entity.setBackgroundColor(org.bukkit.Color.fromARGB(title ? 160 : 110, 10, 10, 18));
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
        });

        return display.getUniqueId().toString();
    }

    private static void fillLectern(Location location, StoryService.Chapter chapter) {
        Block block = location.getBlock();

        if (!(block.getState() instanceof Lectern lectern)) {
            return;
        }

        lectern.getInventory().addItem(loreBook(chapter));
        lectern.update();
    }

    public static ItemStack loreBook(StoryService.Chapter chapter) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null) {
            return book;
        }

        meta.setTitle("Nightmare Origin");
        meta.setAuthor("The Three Gods");

        StringBuilder page = new StringBuilder(chapter.title().replace("§", "")).append("\n\n");
        List<String> pages = new ArrayList<>();

        for (String line : chapter.lines()) {
            String clean = line.replace("§", "");

            if (page.length() + clean.length() > 200) {
                pages.add(page.toString());
                page = new StringBuilder();
            }

            page.append(clean).append("\n");
        }

        if (!page.isEmpty()) {
            pages.add(page.toString());
        }

        if (!pages.isEmpty()) {
            meta.setPages(pages.subList(0, Math.min(pages.size(), 40)));
        }

        book.setItemMeta(meta);

        return book;
    }

    private static void fillChest(Location location, StructureTheme theme, long seed) {
        Block block = location.getBlock();

        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        Inventory inventory = chest.getBlockInventory();
        Random random = new Random(seed ^ 0x9E3779B9L);
        List<String> pool = Main.balance.getStringList("altars.loot." + theme.name().toLowerCase());

        int rolls = Main.balance.getInt("altars.common-rolls", 3);

        for (int i = 0; i < rolls && !pool.isEmpty(); i++) {
            ItemStack item = parseLoot(pool.get(random.nextInt(pool.size())), random);

            if (item != null) {
                inventory.addItem(item);
            }
        }

        if (random.nextDouble() < Main.balance.getDouble("altars.book-chance", 0.10)) {
            ItemStack book = rollThemeBook(theme, random);

            if (book != null) {
                inventory.addItem(book);
            }
        }

        if (theme == StructureTheme.ABYSS) {
            if (random.nextDouble() < Main.balance.getDouble("altars.ink-chance", 0.04)) {
                inventory.addItem(EnchantBookService.createAbyssalInk());
            }

            if (random.nextDouble() < Main.balance.getDouble("altars.nucleus-chance", 0.02)) {
                inventory.addItem(EnchantBookService.createAbyssalNucleus());
            }
        }

        StoryService.Chapter chapter = StoryService.chapter(theme.storyChapter);

        if (chapter != null) {
            inventory.addItem(loreBook(chapter));
        }

        chest.update();
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

        return new ItemStack(material, Math.max(1, amount));
    }

    private static final Map<StructureTheme, List<String>> THEME_BOOKS = Map.of(
            StructureTheme.FORGE, List.of("blast_edge", "executioner", "retribution", "aegis"),
            StructureTheme.SOUL, List.of("lifesteal", "stormcaller", "frostbite", "guardian", "second_wind", "blink"),
            StructureTheme.ABYSS, List.of("void_rend", "venom", "nightmare_mark"),
            StructureTheme.NIGHTMARE, List.of("nightmare_mark", "berserker", "void_rend"),
            StructureTheme.RELICS, List.of("lifesteal", "venom", "second_wind", "blink"));

    private static ItemStack rollThemeBook(StructureTheme theme, Random random) {
        List<String> pool = new ArrayList<>(THEME_BOOKS.getOrDefault(theme, List.of()));

        java.util.Collections.shuffle(pool, random);

        long day = 0;

        try {
            day = Bukkit.getWorlds().get(0).getFullTime() / 24000L;
        } catch (Exception ignored) {
        }

        for (String id : pool) {
            if (!EnchantRegistry.ENCHANTS.containsKey(id)) {
                continue;
            }

            int cap = Math.min(EnchantRegistry.ENCHANTS.get(id).maxLevel,
                    EnchantBookService.maxLevelForDay(id, day));
            cap = Math.min(cap, Main.balance.getInt("altars.book-max-level", 2));

            if (cap < 1) {
                continue;
            }

            return EnchantBookService.createBook(id, 1 + random.nextInt(cap));
        }

        return null;
    }

    private static String blockKey(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ","
                + location.getBlockY() + "," + location.getBlockZ();
    }

    private static int number(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
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

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) {
            return;
        }

        String id = interaction.getPersistentDataContainer().get(
                new NamespacedKey(Main.getInstance(), "structure"), PersistentDataType.STRING);

        if (id == null || !ALTARS.containsKey(id)) {
            return;
        }

        Altar altar = ALTARS.get(id);
        StructureTheme theme = StructureTheme.byName(altar.theme());

        if (theme == null) {
            return;
        }

        event.setCancelled(true);
        StoryService.open(event.getPlayer(), theme.storyChapter);
    }

    @EventHandler
    public void onTakeBook(PlayerTakeLecternBookEvent event) {
        if (LECTERNS.contains(blockKey(event.getLectern().getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Location at = event.getInventory().getLocation();

        if (at == null || at.getWorld() == null) {
            return;
        }

        String key = blockKey(at);

        if (!CHESTS.contains(key)) {
            return;
        }

        for (Altar altar : ALTARS.values()) {
            if (altar.looted() || !altar.chests().contains(key)) {
                continue;
            }

            ALTARS.put(altar.id(), new Altar(altar.id(), altar.world(), altar.x(), altar.y(), altar.z(),
                    altar.theme(), altar.seed(), true, altar.blocks(), altar.chests(), altar.texts(),
                    altar.interaction(), altar.lectern()));

            save();

            return;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        String key = blockKey(event.getClickedBlock().getLocation());

        if (LECTERNS.contains(key)) {
            event.getPlayer().sendMessage(com.nightmare.lang.LangService.msg("structures.lectern_msg",
                    "§7Nightmare Origin &8- §fuse §6/nightmare story §fto read the full tale."));
        }
    }

    public static boolean remove(String id) {
        Altar altar = ALTARS.remove(id);

        if (altar == null) {
            return false;
        }

        World world = Bukkit.getWorld(altar.world());

        if (world != null) {
            for (String key : altar.blocks()) {
                String[] at = key.substring(key.indexOf(':') + 1).split(",");
                Block block = world.getBlockAt(Integer.parseInt(at[0]), Integer.parseInt(at[1]),
                        Integer.parseInt(at[2]));

                if (block.getType() != Material.AIR) {
                    block.setType(Material.AIR, false);
                }
            }

            for (String entityId : altar.texts()) {
                try {
                    Entity entity = Bukkit.getEntity(UUID.fromString(entityId));
                    if (entity != null) {
                        entity.remove();
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (altar.interaction() != null) {
                try {
                    Entity entity = Bukkit.getEntity(UUID.fromString(altar.interaction()));
                    if (entity != null) {
                        entity.remove();
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        CHESTS.removeAll(altar.chests());
        save();

        return true;
    }
}
