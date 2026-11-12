package com.nightmare.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.nightmare.enchant.EnchantLoreService;
import com.nightmare.lang.LangService;

public final class StoryService implements Listener {

    public record Chapter(String title, Material icon, Material border, Material left, Material right,
            List<String> lines, int holdFrames, boolean battle) {
    }

    private static final List<Chapter> CHAPTERS = new ArrayList<>();
    private static final Map<UUID, Integer> FRAME = new ConcurrentHashMap<>();
    private static final Set<UUID> PAUSED = ConcurrentHashMap.newKeySet();

    public StoryService() {
    }

    static void render(Player player, Inventory inventory, int chapterIndex, int frame) {
        Chapter current = chapter(chapterIndex);

        if (current == null) {
            return;
        }

        int revealed = Math.min(current.lines().size(), frame / 2 + 1);
        
        List<String> lore = new ArrayList<>();

        for (String raw : current.lines().subList(0, revealed)) {
            lore.addAll(EnchantLoreService.wrap(raw, ChatColor.RESET, false));
        }

        if (revealed < current.lines().size()) {
            lore.add("§8§o(…)");
        } else if (chapterIndex + 1 >= CHAPTERS.size()) {
            lore.add("§r");
            lore.add("§e" + LangService.msg("story.the_end", "The End — use ← to reread."));
        }

        Material border = current.border();

        if (current.battle() && frame % 4 == 1) {
            border = Material.WHITE_STAINED_GLASS_PANE;
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot < 9 || slot % 9 == 0 || slot % 9 == 8 || slot >= 45) {
                inventory.setItem(slot, GuiItemFactory.item(border, " ", List.of()));
            }
        }

        inventory.setItem(4, GuiItemFactory.item(Material.PAPER,
                current.title() + " §8[" + (chapterIndex + 1) + "/" + CHAPTERS.size() + "]",
                List.of("§7" + LangService.msg("gui.origin_label", "Nightmare Origin"))));

        if (current.battle()) {
            battleFrame(inventory, current, frame);
        } else {
            inventory.setItem(20, GuiItemFactory.item(current.left(), "§f" + current.title(), List.of()));
            inventory.setItem(22, GuiItemFactory.item(current.icon(), current.title(), lore));
            inventory.setItem(24, GuiItemFactory.item(current.right(), "§f" + current.title(), List.of()));
        }

        inventory.setItem(45, GuiItemFactory.item(Material.BARRIER, "§c" + LangService.msg("story.close", "Close"),
                List.of("§7" + LangService.msg("story.close_desc", "Close the chronicle."))));
        inventory.setItem(48, GuiItemFactory.item(Material.ARROW, "§e" + LangService.msg("story.previous", "Previous"),
                List.of("§7" + LangService.msg("story.previous_desc", "Go to the previous chapter."))));
        inventory.setItem(49, GuiItemFactory.item(Material.CLOCK,
                paused(player.getUniqueId()) ? "§e" + LangService.msg("story.resume", "Resume") : "§e" + LangService.msg("story.pause", "Pause"),
                List.of("§7" + LangService.msg("story.pause_desc", "Pause or resume the tale."))));
        inventory.setItem(50, GuiItemFactory.item(Material.ARROW, "§e" + LangService.msg("story.next", "Next"),
                List.of("§7" + LangService.msg("story.next_desc", "Go to the next chapter."))));

        player.updateInventory();
    }

    @SuppressWarnings("null")
    private static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof NightmareGuiHolder holder)
                    || holder.type != GuiType.STORY) {
                continue;
            }

            if (PAUSED.contains(player.getUniqueId())) {
                continue;
            }

            Chapter current = chapter(holder.page);
            if (current == null) {
                continue;
            }

            int frame = FRAME.getOrDefault(player.getUniqueId(), 0) + 1;

            FRAME.put(player.getUniqueId(), frame);

            Inventory inventory = player.getOpenInventory().getTopInventory();

            if (!(inventory.getHolder() instanceof NightmareGuiHolder)) {
                continue;
            }

            render(player, inventory, holder.page, frame);

            int revealed = Math.min(current.lines().size(), frame / 2 + 1);

            if (revealed >= current.lines().size() && frame / 2 + 1 > current.lines().size() + current.holdFrames()) {

                if (holder.page + 1 < CHAPTERS.size()) {
                    open(player, holder.page + 1);
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8F, 1.0F);
                }

            }

            if (current.battle() && frame % 4 == 1) {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25F, 1.6F);
            }
        }
    }

    public static void load(Plugin plugin) {
        plugin.saveResource("story.yml", false);
        CHAPTERS.clear();

        java.io.File file = new java.io.File(plugin.getDataFolder(), "story.yml");

        if ("es".equals(LangService.language())) {
            java.io.File esFile = new java.io.File(plugin.getDataFolder(), "lang/es/story.yml");

            if (esFile.exists()) {
                file = esFile;
            }
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        for (Map<?, ?> raw : yml.getMapList("chapters")) {
            List<String> lines = new ArrayList<>();
            Object rawLines = raw.get("lines");

            if (rawLines instanceof List<?> list) {
                for (Object line : list) {
                    lines.add(ChatColor.translateAlternateColorCodes('&', String.valueOf(line)));
                }
            }

            Object rawTitle = raw.get("title");

            CHAPTERS.add(new Chapter(
                    ChatColor.translateAlternateColorCodes('&', String.valueOf(rawTitle == null ? "Untitled" : rawTitle)),
                    match(raw.get("icon"), Material.ENCHANTED_BOOK),
                    match(raw.get("border"), Material.RED_STAINED_GLASS_PANE),
                    match(raw.get("left"), Material.SOUL_LANTERN),
                    match(raw.get("right"), Material.ENDER_EYE),
                    lines,
                    Math.max(20, intOf(raw.get("hold-seconds"), 8) * 4),
                    Boolean.TRUE.equals(raw.get("battle"))));
        }
    }

    private static void battleFrame(Inventory inventory, Chapter current, int frame) {
        int phase = frame % 4;

        ItemStack clash = GuiItemFactory.item(Material.NETHER_STAR, "§c§l" + LangService.msg("story.clash", "CLASH!"),
                List.of("§7" + LangService.msg("story.clash_desc", "Soul and Abyss strike as one.")));
        ItemStack foe = GuiItemFactory.item(Material.WITHER_SKELETON_SKULL, "§8" + LangService.msg("story.foe", "Nightmare"),
                List.of("§7" + LangService.msg("story.foe_desc", "answered with rusted dreams.")));
        ItemStack soul = GuiItemFactory.item(current.left(), "§f" + LangService.msg("story.soul_god", "Soul God"), List.of());
        ItemStack abyss = GuiItemFactory.item(current.right(), "§5" + LangService.msg("story.abyss_god", "Abyss God"), List.of());
        ItemStack spark = GuiItemFactory.item(Material.FIRE_CHARGE, "§6" + LangService.msg("story.impact", "Impact"), List.of());

        inventory.setItem(13, new ItemStack(Material.AIR));
        inventory.setItem(31, new ItemStack(Material.AIR));

        if (phase == 1) {
            inventory.setItem(20, GuiItemFactory.item(current.left(), "§f" + LangService.msg("story.soul_god", "Soul God"), List.of("§7" + LangService.msg("story.strikes", "strikes!"))));
            inventory.setItem(22, clash);
            inventory.setItem(24, GuiItemFactory.item(current.right(), "§5" + LangService.msg("story.abyss_god", "Abyss God"), List.of("§7" + LangService.msg("story.strikes", "strikes!"))));
            inventory.setItem(13, spark);
            inventory.setItem(31, spark);
        } else if (phase == 3) {
            inventory.setItem(19, soul);
            inventory.setItem(22, foe);
            inventory.setItem(25, abyss);
        } else {
            inventory.setItem(20, soul);
            inventory.setItem(22, GuiItemFactory.item(Material.WITHER_SKELETON_SKULL, "§8" + LangService.msg("story.foe", "Nightmare"),
                    List.of("§7" + LangService.msg("story.looms", "looms."))));
            inventory.setItem(24, abyss);
        }
    }

    public static void open(Player player, int chapter) {
        Chapter current = chapter(chapter);

        if (current == null) {
            return;
        }

        FRAME.put(player.getUniqueId(), 0);
        PAUSED.remove(player.getUniqueId());

        GuiManager.openStory(player, chapter);

        click(player);
    }

    private static int intOf(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static boolean togglePause(Player player) {

        UUID uuid = player.getUniqueId();
        if (PAUSED.contains(uuid)) {
            PAUSED.remove(uuid);
            return false;
        }

        PAUSED.add(uuid);

        return true;
    }

    private static Material match(Object value, Material fallback) {
        if (value == null) {
            return fallback;
        }

        Material material = Material.matchMaterial(String.valueOf(value));

        return material == null ? fallback : material;
    }

    public static Chapter chapter(int index) {
        if (index < 0 || index >= CHAPTERS.size()) {
            return null;
        }
        
        return CHAPTERS.get(index);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        FRAME.remove(event.getPlayer().getUniqueId());
        PAUSED.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FRAME.remove(event.getPlayer().getUniqueId());
        PAUSED.remove(event.getPlayer().getUniqueId());
    }

    public static int size() {
        return CHAPTERS.size();
    }

    public static boolean paused(UUID uuid) {
        return PAUSED.contains(uuid);
    }

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, StoryService::tick, 5L, 5L);
    }

    private static void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7F, 1.2F);
    }
}
