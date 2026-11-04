package com.nightmare;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public final class Tablist {

    private final Plugin plugin = Main.getInstance();

    public void startTablist() throws IOException {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            final YamlConfiguration config = Main.getSettings();

            if (config == null || !config.getBoolean("tablist.enable", true)) {
                return;
            }

            String header = config.getString("tablist.header", "Header TabList");
            String footer = config.getString("tablist.footer", "Footer TabList");

            if (header == null) header = "Header TabList";
            if (footer == null) footer = "Footer TabList";

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (player == null) continue;

                player.setPlayerListHeaderFooter(
                        ChatColor.translateAlternateColorCodes('&', applyPlaceholders(player, header)),
                        ChatColor.translateAlternateColorCodes('&', applyPlaceholders(player, footer)));

            }

        }, 0, 50L);

    }

    private static String applyPlaceholders(Player player, String text) {
        Matcher matcher = Pattern.compile("%.+?%").matcher(text);
        StringBuilder result = new StringBuilder();
        int last = 0;

        while (matcher.find()) {
            result.append(text, last, matcher.start());
            result.append(PlaceholderAPI.setPlaceholders(player, matcher.group()));
            last = matcher.end();
        }

        result.append(text.substring(last));
        return result.toString();
    }

}
