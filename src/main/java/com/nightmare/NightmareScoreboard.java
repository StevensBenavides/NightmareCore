package com.nightmare;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.nightmare.FastBoard.*;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public final class NightmareScoreboard {

    private final Plugin plugin = Main.getInstance();
    public static final Map<UUID, FastBoard> boards = new HashMap<UUID, FastBoard>();
    
    public void startScoreboard() {

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

        if (lines.size() > 15) {
            lines = lines.subList(0, 15);
        }

        ArrayList<String> newLines = new ArrayList<>();

        for (String line : lines) {

            if (line.contains("%player%")) {
                line = line.replace("%player%",
                        PlaceholderAPI.setPlaceholders(board.getPlayer(), "%player_name%"));
            }

            if (line.contains("%day%")) {
                Optional<WorldTime> currentWorldDay = TimeManagement
                        .getSpecificWorldTime(board.getPlayer().getWorld().getName());
                String day = currentWorldDay.map(worldTime -> String.valueOf(worldTime.getDay()))
                        .orElse("?");
                line = line.replace("%day%", day);
            }

            if (line.contains("%online%")) {
                line = line.replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size())
                        + " / " + plugin.getServer().getMaxPlayers());
            }

            if (line.contains("%location%")) {
                line = line.replace("%location%", String.format("%d %d %d",
                        Math.round(board.getPlayer().getLocation().getX()),
                        Math.round(board.getPlayer().getLocation().getY()),
                        Math.round(board.getPlayer().getLocation().getZ())));
            }

            newLines.add(ChatColor.translateAlternateColorCodes('&',
                    PlaceholderAPI.setPlaceholders(board.getPlayer(), line)));

        }

        board.updateLines(newLines);

    }

}
