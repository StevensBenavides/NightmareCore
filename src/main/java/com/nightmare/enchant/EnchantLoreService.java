package com.nightmare.enchant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.Main;
import com.nightmare.item.ItemKeys;
import com.nightmare.lang.LangService;

public final class EnchantLoreService {

    private static final String FOOTER = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Nightmare Core";
    private static final int MAX_WIDTH = 30;

    private static final List<String> CATEGORY_ORDER = List.of(
            "Swords",
            "Axes",
            "Bows",
            "Crossbows",
            "Tridents",
            "Maces",
            "Helmets",
            "Chestplates",
            "Leggings",
            "Boots",
            "Shields");

    private static final String[] ROMAN = { "I", "II", "III", "IV", "V" };

    private static final Map<String, ChatColor> COLORS = Map.ofEntries(
            Map.entry("lifesteal", ChatColor.RED),
            Map.entry("stormcaller", ChatColor.YELLOW),
            Map.entry("blast_edge", ChatColor.RED),
            Map.entry("frostbite", ChatColor.AQUA),
            Map.entry("venom", ChatColor.GREEN),
            Map.entry("executioner", ChatColor.RED),
            Map.entry("berserker", ChatColor.RED),
            Map.entry("soul_harvest", ChatColor.DARK_PURPLE),
            Map.entry("void_rend", ChatColor.DARK_PURPLE),
            Map.entry("guardian", ChatColor.AQUA),
            Map.entry("retribution", ChatColor.AQUA),
            Map.entry("second_wind", ChatColor.AQUA),
            Map.entry("blink", ChatColor.GREEN),
            Map.entry("nightmare_mark", ChatColor.DARK_RED),
            Map.entry("aegis", ChatColor.AQUA),
            Map.entry("abyssal_howl", ChatColor.LIGHT_PURPLE),
            Map.entry("abyssal_core", ChatColor.LIGHT_PURPLE));

    private static final Map<String, String> SYMBOLS = Map.of(
            "abyssal_howl", "❖",
            "abyssal_core", "❖");

    private EnchantLoreService() {
    }

    private static String stat(String key, String fallback) {
        return LangService.msg("stats." + key, fallback) + ":";
    }

    private static String label(String key, String fallback) {
        return LangService.msg("gui." + key, fallback);
    }

    public static String buildBookName(String id, int level) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);

        if (enchant == null) {
            return ChatColor.GOLD + LangService.msg("gui.unknown_enchantment", "Unknown Enchantment");
        }

        ChatColor color = COLORS.getOrDefault(id, ChatColor.GOLD);
        String symbol = SYMBOLS.getOrDefault(id, "✦");
        String levelText;

        if (id.equals("abyssal_howl") || id.equals("abyssal_core")) {
            levelText = "";
        } else {
            int index = Math.min(Math.max(level, 1), ROMAN.length) - 1;
            levelText = " " + ROMAN[index];
        }

        return color + "" + ChatColor.BOLD + symbol + " " + enchant.displayName + levelText;
    }

    public static List<String> buildCraftableBookLore(String id, int level) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
        List<String> lore = new ArrayList<>();

        if (enchant == null) {
            return lore;
        }

        if (id.equals("abyssal_howl") || id.equals("abyssal_core")) {
            lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + label("legendary", "Legendary"));
        } else {
            int index = Math.min(Math.max(level, 1), ROMAN.length) - 1;

            lore.add(ChatColor.YELLOW + label("level", "Level") + " " + ChatColor.GOLD + ROMAN[index]
                    + ChatColor.GRAY + " / " + ChatColor.GOLD + ROMAN[enchant.maxLevel - 1]);
        }

        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.GRAY + "" + ChatColor.BOLD + label("applicable_on", "Applicable on:"));
        lore.addAll(buildApplicabilityLines(enchant));

        return lore;
    }

    public static List<String> buildHelpListLore(String id) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
        List<String> lore = new ArrayList<>();

        if (enchant == null) {
            return lore;
        }

        lore.addAll(wrap(helpParagraph(id, 1), ChatColor.GRAY, true));
        lore.addAll(wrap(helpMotto(id), COLORS.getOrDefault(id, ChatColor.GOLD), true));
        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.GRAY + label("levels_label", "Levels:") + " " + ChatColor.WHITE
                + (id.equals("abyssal_howl") || id.equals("abyssal_core") ? label("legendary", "Legendary") : "I-V"));
        lore.addAll(wrap(label("applicable_short", "Applicable:") + " " + applicabilitySummary(enchant), ChatColor.GRAY, false));
        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.YELLOW + label("click_to_read", "Click to read the lore and stats."));

        return lore;
    }

    public static List<String> buildHelpDetailLore(String id, int level) {
        CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
        List<String> lore = new ArrayList<>();

        if (enchant == null) {
            return lore;
        }

        lore.addAll(wrap(helpParagraph(id, level), ChatColor.GRAY, true));
        lore.addAll(wrap(helpMotto(id), COLORS.getOrDefault(id, ChatColor.GOLD), true));
        lore.add(ChatColor.RESET.toString());

        if (id.equals("abyssal_howl") || id.equals("abyssal_core")) {
            lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + label("legendary", "Legendary"));
        } else {
            int index = Math.min(Math.max(level, 1), ROMAN.length) - 1;

            lore.add(ChatColor.YELLOW + label("level", "Level") + " " + ChatColor.GOLD + ROMAN[index]
                    + ChatColor.GRAY + " / " + ChatColor.GOLD + ROMAN[enchant.maxLevel - 1]);
        }

        lore.addAll(buildStatLines(id, level));
        lore.add(ChatColor.RESET.toString());
        lore.add(ChatColor.GRAY + "" + ChatColor.BOLD + label("applicable_on", "Applicable on:"));
        lore.addAll(buildApplicabilityLines(enchant));
        lore.add(ChatColor.RESET.toString());
        lore.addAll(wrap(helpEffect(id), ChatColor.GRAY, false));
        lore.add(ChatColor.RESET.toString());

        String mechanics = helpMechanics(id);

        if (!mechanics.isBlank()) {
            lore.addAll(wrap(mechanics, ChatColor.WHITE, false));
            lore.add(ChatColor.RESET.toString());
        }

        lore.add(FOOTER);

        return lore;
    }

    private static String helpParagraph(String id, int level) {
        String text = LangService.lore("enchants." + id + ".lore." + level);

        if (text == null || text.isBlank()) {
            text = LangService.lore("enchants." + id + ".lore.1");
        }

        return text == null ? "" : text;
    }

    private static String helpMotto(String id) {
        String text = LangService.lore("enchants." + id + ".motto");

        return text == null ? "" : text;
    }

    private static String helpEffect(String id) {
        String text = LangService.lore("enchants." + id + ".effect");
        return text == null ? "" : text;

    }

    private static String helpMechanics(String id) {
        String text = LangService.lore("enchants." + id + ".mechanics");
        return text == null ? "" : text;

    }

    private static List<String> wrap(String text, ChatColor color, boolean italic) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return lines;
        }

        String prefix = color.toString() + (italic ? ChatColor.ITALIC.toString() : "");
        String[] words = text.replaceAll("\\s+", " ").trim().split(" ");
        StringBuilder current = new StringBuilder(prefix);

        int width = 0;

        for (String word : words) {
            int extra = width == 0 ? word.length() : word.length() + 1;

            if (width > 0 && width + extra > 36) {
                lines.add(current.toString());
                current = new StringBuilder(prefix);
                width = 0;
                extra = word.length();
            }

            if (width > 0) {
                current.append(' ');
            }

            current.append(word);
            width += extra;
        }

        if (width > 0) {
            lines.add(current.toString());
        }

        return lines;
    }

    public static void rebuildEquipmentLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String storedBase = container.get(ItemKeys.LORE_BASE, PersistentDataType.STRING);
        List<String> baseLore;

        if (storedBase != null && !storedBase.isEmpty()) {
            baseLore = sanitizeBaseLore(Arrays.asList(storedBase.split("\n")));
        } else {
            baseLore = sanitizeBaseLore(meta.getLore());
        }

        List<String> lore = new ArrayList<>(baseLore);
        List<String> enchantLines = new ArrayList<>();

        for (Map.Entry<String, org.bukkit.NamespacedKey> entry : EnchantKeys.KEYS.entrySet()) {
            Integer level = container.get(entry.getValue(), PersistentDataType.INTEGER);

            if (level == null || level <= 0) {
                continue;
            }

            CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(entry.getKey());

            if (enchant == null) {
                continue;
            }

            ChatColor color = COLORS.getOrDefault(entry.getKey(), ChatColor.GOLD);
            String levelText = entry.getKey().equals("abyssal_howl")
                    ? ""
                    : " " + ROMAN[Math.min(Math.max(level, 1), ROMAN.length) - 1];

            enchantLines.add(color + "- " + enchant.displayName + levelText);
        }

        if (!enchantLines.isEmpty()) {
            lore.add(ChatColor.RESET.toString());
            lore.add(ChatColor.GRAY + "" + ChatColor.BOLD + label("custom_enchantments", "Custom Enchantments:"));
            lore.addAll(enchantLines);
            lore.add(ChatColor.RESET.toString());
            lore.add(FOOTER);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static String applicabilitySummary(CustomEnchant enchant) {
        List<String> lines = buildApplicabilityLines(enchant);
        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            String visible = line.replaceAll("§.", "").trim();

            if (visible.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(visible);
        }

        return builder.toString();
    }

    public static List<String> sanitizeBaseLore(List<String> currentLore) {
        List<String> base = new ArrayList<>();

        if (currentLore == null) {
            return base;
        }

        for (String line : currentLore) {
            String visible = line.replaceAll("§.", "");

            if (visible.contains("Custom Enchantments:") || visible.contains("━")) {
                break;
            }

            base.add(line);
        }

        while (!base.isEmpty() && base.get(base.size() - 1).replaceAll("§.", "").isBlank()) {
            base.remove(base.size() - 1);
        }

        return base;
    }

    private static List<String> buildStatLines(String id, int level) {
        List<String> lines = new ArrayList<>();

        if (Main.enchants == null) {
            lines.add(ChatColor.GRAY + LangService.msg("gui.stats_unavailable", "Stats unavailable."));
            return lines;
        }

        switch (id) {
            case "lifesteal" -> {
                double percent = Main.enchants.getDouble("enchants.lifesteal.levels." + level + ".heal-percent", 0.08);
                double cap = Main.enchants.getDouble("enchants.lifesteal.levels." + level + ".heal-cap", 2.0);

                lines.add(ChatColor.GRAY + stat("healing", "Healing") + " " + ChatColor.GREEN + percent(percent));
                lines.add(ChatColor.GRAY + stat("cap", "Cap") + " " + ChatColor.GREEN + number(cap) + " HP");
            }
            case "stormcaller" -> {
                double chance = Main.enchants.getDouble("enchants.stormcaller.levels." + level + ".chance", 0.05);
                int targets = Main.enchants.getInt("enchants.stormcaller.levels." + level + ".targets", 2);
                double damage = Main.enchants.getDouble("enchants.stormcaller.levels." + level + ".damage", 3.0);
                long cooldown = Main.enchants.getLong("enchants.stormcaller.cooldown", 2000L);

                lines.add(ChatColor.GRAY + stat("chance", "Chance") + " " + ChatColor.GREEN + percent(chance));
                lines.add(ChatColor.GRAY + stat("targets", "Targets") + " " + ChatColor.GREEN + targets);
                lines.add(ChatColor.GRAY + stat("damage", "Damage") + " " + ChatColor.GREEN + number(damage) + " HP");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "blast_edge" -> {
                double damage = Main.enchants.getDouble("enchants.blast_edge.levels." + level + ".damage", 5.0);
                double radius = Main.enchants.getDouble("enchants.blast_edge.levels." + level + ".radius", 2.0);
                long cooldown = Main.enchants.getLong("enchants.blast_edge.cooldown", 3000L);

                lines.add(ChatColor.GRAY + "Hits: " + ChatColor.YELLOW + "4");
                lines.add(ChatColor.GRAY + stat("damage", "Damage") + " " + ChatColor.GREEN + number(damage) + " HP");
                lines.add(ChatColor.GRAY + stat("radius", "Radius") + " " + ChatColor.GREEN + number(radius));
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "frostbite" -> {
                int duration = Main.enchants.getInt("enchants.frostbite.levels." + level + ".duration", 30);
                long cooldown = Main.enchants.getLong("enchants.frostbite.cooldown", 2000L);
                lines.add(ChatColor.GRAY + stat("duration", "Duration") + " " + ChatColor.AQUA + seconds(duration / 20.0) + "s");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "venom" -> {
                int duration = Main.enchants.getInt("enchants.venom.levels." + level + ".duration", 40);
                double antiHeal = Main.enchants.getDouble("enchants.venom.levels." + level + ".anti-heal", 0.0);
                long cooldown = Main.enchants.getLong("enchants.venom.cooldown", 4000L);

                lines.add(ChatColor.GRAY + stat("duration", "Duration") + " " + ChatColor.GREEN + seconds(duration / 20.0) + "s");

                if (antiHeal > 0.0) {
                    lines.add(ChatColor.GRAY + "Anti-heal: " + ChatColor.GREEN + percent(antiHeal));
                }

                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "executioner" -> {
                double threshold = Main.enchants.getDouble("enchants.executioner.levels." + level + ".threshold", 0.20);
                double bonus = Main.enchants.getDouble("enchants.executioner.levels." + level + ".bonus", 0.10);
                lines.add(ChatColor.GRAY + stat("threshold", "Threshold") + " " + ChatColor.RED + percent(threshold) + " HP");
                lines.add(ChatColor.GRAY + stat("bonus", "Bonus") + " " + ChatColor.RED + percent(bonus));
            }
            case "berserker" -> {
                double threshold = Main.enchants.getDouble("enchants.berserker.threshold", 0.40);
                int duration = Main.enchants.getInt("enchants.berserker.levels." + level + ".duration", 60);
                long cooldown = Main.enchants.getLong("enchants.berserker.levels." + level + ".cooldown", 30000L);

                lines.add(ChatColor.GRAY + stat("threshold", "Threshold") + " " + ChatColor.RED + percent(threshold) + " HP");
                lines.add(ChatColor.GRAY + stat("duration", "Duration") + " " + ChatColor.YELLOW + seconds(duration / 20.0) + "s");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "soul_harvest" -> {
                double perStack = Main.enchants.getDouble("enchants.soul_harvest.levels." + level + ".per-stack", 0.02);
                int maxStacks = Main.enchants.getInt("enchants.soul_harvest.levels." + level + ".max-stacks", 3);
                int duration = Main.enchants.getInt("enchants.soul_harvest.levels." + level + ".duration", 200);
                lines.add(ChatColor.GRAY + stat("per_stack", "Per stack") + " " + ChatColor.GREEN + percent(perStack));
                lines.add(ChatColor.GRAY + stat("max_stacks", "Max stacks") + " " + ChatColor.GREEN + maxStacks);
                lines.add(ChatColor.GRAY + stat("duration", "Duration") + " " + ChatColor.YELLOW + seconds(duration / 20.0) + "s");
            }
            case "void_rend" -> {
                double damage = Main.enchants.getDouble("enchants.void_rend.levels." + level + ".damage", 2.0);
                long cooldown = Main.enchants.getLong("enchants.void_rend.levels." + level + ".cooldown", 5000L);

                lines.add(ChatColor.GRAY + stat("magic_damage", "Magic damage") + " " + ChatColor.DARK_PURPLE + number(damage) + " HP");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "guardian" -> {
                double chance = Main.enchants.getDouble("enchants.guardian.levels." + level + ".chance", 0.10);
                long cooldown = Main.enchants.getLong("enchants.guardian.levels." + level + ".cooldown", 12000L);

                lines.add(ChatColor.GRAY + stat("chance", "Chance") + " " + ChatColor.GREEN + percent(chance));
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "retribution" -> {
                double reflect = Main.enchants.getDouble("enchants.retribution.levels." + level + ".reflect", 0.08);
                double cap = Main.enchants.getDouble("enchants.retribution.levels." + level + ".cap", 1.0);
                lines.add(ChatColor.GRAY + stat("reflect", "Reflect") + " " + ChatColor.RED + percent(reflect));
                lines.add(ChatColor.GRAY + stat("cap", "Cap") + " " + ChatColor.RED + number(cap) + " HP");
            }
            case "second_wind" -> {
                double threshold = Main.enchants.getDouble("enchants.second_wind.threshold", 0.25);
                double heal = Main.enchants.getDouble("enchants.second_wind.levels." + level + ".heal", 2.0);
                long cooldown = Main.enchants.getLong("enchants.second_wind.levels." + level + ".cooldown", 60000L);

                lines.add(ChatColor.GRAY + stat("threshold", "Threshold") + " " + ChatColor.RED + percent(threshold) + " HP");
                lines.add(ChatColor.GRAY + stat("healing", "Healing") + " " + ChatColor.GREEN + number(heal) + " HP");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "blink" -> {
                double chance = Main.enchants.getDouble("enchants.blink.levels." + level + ".chance", 0.10);
                int distance = Main.enchants.getInt("enchants.blink.levels." + level + ".distance", 3);
                long cooldown = Main.enchants.getLong("enchants.blink.levels." + level + ".cooldown", 20000L);
                lines.add(ChatColor.GRAY + stat("chance", "Chance") + " " + ChatColor.GREEN + percent(chance));
                lines.add(ChatColor.GRAY + "Distance: " + ChatColor.GREEN + distance + " blocks");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "nightmare_mark" -> {
                int maxStacks = Main.enchants.getInt("enchants.nightmare_mark.max-stacks", 5);
                double damage = Main.enchants.getDouble("enchants.nightmare_mark.damage", 8.0);
                long cooldown = Main.enchants.getLong("enchants.nightmare_mark.cooldown", 15000L);

                lines.add(ChatColor.GRAY + stat("marks", "Marks") + " " + ChatColor.DARK_PURPLE + maxStacks);
                lines.add(ChatColor.GRAY + stat("damage", "Damage") + " " + ChatColor.DARK_PURPLE + number(damage) + " HP");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "aegis" -> {
                double chance = Main.enchants.getDouble("enchants.aegis.levels." + level + ".chance", 0.10);
                double reduction = Main.enchants.getDouble("enchants.aegis.levels." + level + ".reduction", 0.20);
                long cooldown = Main.enchants.getLong("enchants.aegis.levels." + level + ".cooldown", 5000L);
                lines.add(ChatColor.GRAY + stat("chance", "Chance") + " " + ChatColor.GREEN + percent(chance));
                lines.add(ChatColor.GRAY + "Reduction: " + ChatColor.GREEN + percent(reduction));
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(cooldown / 1000.0) + "s");
            }
            case "abyssal_howl" -> {
                lines.add(ChatColor.GRAY + stat("duration", "Duration") + " " + ChatColor.WHITE + "Permanent");
                lines.add(ChatColor.GRAY + stat("activation", "Activation") + " " + ChatColor.WHITE + "Hold");
            }
            case "abyssal_core" -> {
                int range = Main.balance.getInt("abyssal-core.burst-range", 50);
                double burstRadius = Main.balance.getDouble("abyssal-core.burst-radius", 4.0);
                double burstDamage = Main.balance.getDouble("abyssal-core.burst-damage", 150.0);
                long burstCooldown = Main.balance.getLong("abyssal-core.burst-cooldown-ms", 60000L);
                double radius = Main.balance.getDouble("abyssal-core.area-radius", 7.5);
                double areaDamage = Main.balance.getDouble("abyssal-core.area-damage", 150.0);
                long areaCooldown = Main.balance.getLong("abyssal-core.area-cooldown-ms", 120000L);

                lines.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Abyssal Burst");
                lines.add(ChatColor.GRAY + stat("activation", "Activation") + " " + ChatColor.WHITE + "Sneak + Left Click (any time)");
                lines.add(ChatColor.GRAY + stat("range", "Range") + " " + ChatColor.WHITE + range + " blocks");
                lines.add(ChatColor.GRAY + stat("beam", "Beam") + " " + ChatColor.WHITE + number(burstRadius * 2.0)
                        + "x" + number(burstRadius * 2.0) + " blocks");
                lines.add(ChatColor.GRAY + stat("damage", "Damage") + " " + ChatColor.WHITE + number(burstDamage) + " HP");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(burstCooldown / 1000.0) + "s");
                lines.add(ChatColor.RESET.toString());
                lines.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Abyssal Cataclysm");
                lines.add(ChatColor.GRAY + stat("activation", "Activation") + " " + ChatColor.WHITE + "Sneak + Right Click");
                lines.add(ChatColor.GRAY + stat("area", "Area") + " " + ChatColor.WHITE + number(radius * 2.0) + " blocks diameter");
                lines.add(ChatColor.GRAY + stat("damage", "Damage") + " " + ChatColor.WHITE + number(areaDamage) + " HP");
                lines.add(ChatColor.GRAY + stat("cooldown", "Cooldown") + " " + ChatColor.YELLOW + seconds(areaCooldown / 1000.0) + "s");
            }
            default -> lines.add(ChatColor.GRAY + LangService.msg("gui.no_details", "No details available."));
        }

        return lines;
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value * 100).replace(".0", "");
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value).replace(".0", "");
    }

    private static String seconds(double value) {
        return String.format(Locale.ROOT, "%.1f", value).replace(".0", "");
    }

    private static List<String> buildApplicabilityLines(CustomEnchant enchant) {
        if (enchant.id.equals("abyssal_howl")) {
            return List.of(ChatColor.WHITE + "  " + label("any_weapon", "Any weapon"));
        }

        Set<String> categories = new LinkedHashSet<>();

        for (Material material : enchant.applicable) {
            String name = material.name();

            if (name.endsWith("_SWORD")) {
                categories.add("Swords");
            } else if (name.endsWith("_AXE")) {
                categories.add("Axes");
            } else if (name.equals("BOW")) {
                categories.add("Bows");
            } else if (name.equals("CROSSBOW")) {
                categories.add("Crossbows");
            } else if (name.equals("TRIDENT")) {
                categories.add("Tridents");
            } else if (name.equals("MACE")) {
                categories.add("Maces");
            } else if (name.endsWith("_HELMET")) {
                categories.add("Helmets");
            } else if (name.endsWith("_CHESTPLATE")) {
                categories.add("Chestplates");
            } else if (name.endsWith("_LEGGINGS")) {
                categories.add("Leggings");
            } else if (name.endsWith("_BOOTS")) {
                categories.add("Boots");
            } else if (name.equals("SHIELD")) {
                categories.add("Shields");
            }
        }

        if (categories.isEmpty()) {
            return List.of(ChatColor.WHITE + "  " + label("categories_none", "None"));
        }

        List<String> ordered = new ArrayList<>();

        for (String category : CATEGORY_ORDER) {
            if (categories.contains(category)) {
                ordered.add(label("cat_" + category.toLowerCase(), category));
            }
        }

        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder(ChatColor.WHITE + "  ");
        int currentWidth = 0;

        for (String category : ordered) {
            int extra = currentWidth == 0 ? category.length() : category.length() + 2;

            if (currentWidth > 0 && currentWidth + extra > MAX_WIDTH) {
                lines.add(current.toString());
                current = new StringBuilder(ChatColor.WHITE + "  ");
                currentWidth = 0;
                extra = category.length();
            }

            if (currentWidth > 0) {
                current.append(", ");
            }

            current.append(category);
            currentWidth += extra;
        }

        if (currentWidth > 0) {
            lines.add(current.toString());
        }

        return lines;
    }

}
