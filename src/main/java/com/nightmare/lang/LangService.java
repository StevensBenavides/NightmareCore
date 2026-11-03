package com.nightmare.lang;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.nightmare.IO;
import com.nightmare.Main;

public final class LangService {

    private static String language = "en";
    private static YamlConfiguration base;
    private static YamlConfiguration override;
    private static YamlConfiguration loreEs;
    private static final Map<String, Boolean> warnedMissing = new HashMap<>();

    private LangService() {
    }

    public static void init(Plugin plugin) {
        language = Main.getSettings().getString("language", "en");

        if (!language.equalsIgnoreCase("es")) {
            language = "en";
        } else {
            language = "es";
        }

        ensureLangFile(plugin, "lang/en/messages.yml");
        ensureLangFile(plugin, "lang/es/messages.yml");
        ensureLangFile(plugin, "lang/es/enchant-lore.yml");
        ensureLangFile(plugin, "lang/es/story.yml");

        base = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/en/messages.yml"));

        if (language.equals("es")) {
            override = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/es/messages.yml"));
            loreEs = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/es/enchant-lore.yml"));
        } else {
            override = null;
            loreEs = null;
        }

        warnedMissing.clear();
    }

    public static void reload(Plugin plugin) {
        init(plugin);
    }

    public static String language() {
        return language;
    }

    private static void ensureLangFile(Plugin plugin, String path) {
        File file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    public static String msg(String path) {
        return msg(path, "");
    }

    public static String msg(String path, String fallback) {
        if (override != null && override.contains(path)) {
            String value = override.getString(path);
            if (value != null) {
                return value;
            }
        }

        if (base != null && base.contains(path)) {
            String value = base.getString(path);
            if (value != null) {
                return value;
            }
        }

        String legacy = IO.HashMapOfMessages.get(path);

        if (legacy != null) {
            return legacy;
        }

        warnMissing(path);
        return fallback;
    }

    public static String format(String path, String... pairs) {
        String text = msg(path);

        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = text.replace(pairs[i], pairs[i + 1]);
        }

        return text;
    }

    public static List<String> list(String path) {
        List<String> result = new ArrayList<>();

        if (override != null && override.contains(path)) {
            result.addAll(override.getStringList(path));
        }

        if (result.isEmpty() && base != null) {
            result.addAll(base.getStringList(path));
        }

        if (result.isEmpty()) {
            warnMissing(path);
        }

        return result;
    }

    public static String lore(String path) {
        if (language.equals("es") && loreEs != null && loreEs.contains(path)) {
            String value = loreEs.getString(path);

            if (value != null) {
                return value;
            }
        }

        if (Main.enchantLore != null) {
            String value = Main.enchantLore.getString(path);

            if (value != null) {
                return value;
            }
        }

        warnMissing(path);
        return "";
    }

    public static java.util.List<String> loreLines(String path, String... pairs) {
        java.util.List<String> out = new java.util.ArrayList<>();

        for (String line : list(path)) {
            String text = line;

            for (int i = 0; i + 1 < pairs.length; i += 2) {
                text = text.replace(pairs[i], pairs[i + 1]);
            }

            out.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', text));
        }

        return out;
    }

    private static void warnMissing(String path) {
        if (warnedMissing.putIfAbsent(path, Boolean.TRUE) == null) {
            Plugin plugin = Main.getInstance();

            if (plugin != null) {
                plugin.getLogger().warning("[Lang] Missing message key '" + path + "', using fallback.");
            }
        }
    }
}
