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

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (player == null) return;

                final YamlConfiguration config = Main.getSettings();    

                if (config.get("tablist.header") == null  || config.get("tablist.footer") == null)
                    try {
                        throw new IOException("tablist.header or tablist.footer not found in settings.yml.");
                    } catch (IOException e) {
                        plugin.getServer().getPluginManager().disablePlugin(plugin);
                        e.printStackTrace();
                    }

                String header = config.getString("tablist.header");
                String footer = config.getString("tablist.footer");

                if (header == null) header = "Header TabList";
                if (footer == null) footer = "Footer TabList";

                Pattern patternHeader = Pattern.compile("%.+?%");
                Matcher matcherHeader = patternHeader.matcher(header);

                Pattern patternFooter = Pattern.compile("%.+?%");
                Matcher matcherFooter = patternFooter.matcher(footer);

                if (matcherHeader.find() && matcherFooter.find()) {
                    player.setPlayerListHeaderFooter(ChatColor.translateAlternateColorCodes('&', header.replace(matcherHeader.group(), PlaceholderAPI.setPlaceholders(player, matcherHeader.group()))), ChatColor.translateAlternateColorCodes('&', footer.replace(matcherFooter.group(), PlaceholderAPI.setPlaceholders(player, matcherFooter.group()))));
                    continue;
                } else if (matcherHeader.find()) {
                    player.setPlayerListHeaderFooter(ChatColor.translateAlternateColorCodes('&', header.replace(matcherHeader.group(), PlaceholderAPI.setPlaceholders(player, matcherHeader.group()))), ChatColor.translateAlternateColorCodes('&', footer));
                    continue;
                } else if (matcherFooter.find()) {
                    player.setPlayerListHeaderFooter(ChatColor.translateAlternateColorCodes('&', header), ChatColor.translateAlternateColorCodes('&', footer.replace(matcherFooter.group(), PlaceholderAPI.setPlaceholders(player, matcherFooter.group()))));
                    continue;
                }

                player.setPlayerListHeaderFooter(ChatColor.translateAlternateColorCodes('&', header), ChatColor.translateAlternateColorCodes('&', footer));

            }

        }, 0, 50L);

    }

}
