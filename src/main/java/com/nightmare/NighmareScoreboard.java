package com.nightmare;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.nightmare.FastBoard.*;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public final class NighmareScoreboard {

    private final Plugin plugin = Main.getInstance();
    public static final Map<UUID, FastBoard> boards = new HashMap<UUID, FastBoard>();
    
    public void initScorebaord() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            for (FastBoard board : boards.values()) {

                updateScoreboard(board);
            }

        }, 0, 20L);

    }

    private void updateScoreboard(FastBoard board) {

        File settings = new File(plugin.getDataFolder(), "settings.yml");   

        if (!settings.exists()) return;

        final YamlConfiguration config = Main.getSettings();

        if (config.get("scoreboard.lines") == null) return;

        List<String> lines = config.getStringList("scoreboard.lines");

        if (lines.size() >= 14) return;

        ArrayList<String> newLines = new ArrayList<>();

        for (String line : lines) {
            
            if (line.contains("%player%")) {

                newLines.add(ChatColor.translateAlternateColorCodes('&', line.replace("%player%", PlaceholderAPI.setPlaceholders(board.getPlayer(), "%player_name%"))));
                continue;

            } else if (line.contains("%online%")) { 

                newLines.add(ChatColor.translateAlternateColorCodes('&', line.replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()) + " / " + plugin.getServer().getMaxPlayers())));
                continue;

            } else if (line.contains("%location%")) {

                newLines.add(ChatColor.translateAlternateColorCodes('&', line.replace("%location%", String.format("%d %d %d", Math.round(board.getPlayer().getLocation().getX()), Math.round(board.getPlayer().getLocation().getY()), Math.round(board.getPlayer().getLocation().getZ())))));
                continue;

            } 

            Pattern pattern = Pattern.compile("%.+?%");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {

                newLines.add(ChatColor.translateAlternateColorCodes('&', line.replace(matcher.group(), PlaceholderAPI.setPlaceholders(board.getPlayer(), matcher.group()))));
                continue;

            }
            
            newLines.add(ChatColor.translateAlternateColorCodes('&', line));

        }

        board.updateLines(newLines);

    }

}
