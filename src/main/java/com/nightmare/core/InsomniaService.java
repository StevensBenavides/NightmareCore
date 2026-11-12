package com.nightmare.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;
import com.nightmare.lang.LangService;

/**
 * Random "sleepless night" windows: while one is active nobody can sleep in
 * normal worlds. Players already asleep when the window starts are woken up
 * without changing their spawn point.
 */
public final class InsomniaService implements Listener {

    private static Plugin plugin;
    private static long activeUntil = 0L;
    private static long nextStartAt = 0L;

    private InsomniaService() {
    }

    private static void tick() {
        if (!enabled()) {
            activeUntil = 0L;
            return;
        }

        long now = System.currentTimeMillis();

        if (isActive()) {
            if (now >= activeUntil) {
                end();
                scheduleNext(now);
            }

            return;
        }

        if (nextStartAt == 0L) {
            scheduleNext(now);
            return;
        }

        if (now < nextStartAt) {
            return;
        }

        if (onlyAtNight() && !isNightSomewhere()) {
            return;
        }

        begin(now);
    }

    @SuppressWarnings("null")
    private static void begin(long now) {
        int min = Math.max(1, configInt("duration-min-minutes", 2));
        int max = Math.max(min, configInt("duration-max-minutes", 6));
        int duration = min == max ? min : min + (int) (Math.random() * (max - min + 1));

        activeUntil = now + duration * 60_000L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();

            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            if (player.isSleeping()) {
                player.wakeup(false);
            }

            if (announce()) {
                player.sendMessage(colored(LangService.msg("insomnia.start",
                        "&7Sleep won't come tonight.")));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.4F, 0.6F);
            }
        }

        if (plugin != null) {
            plugin.getLogger().info("Insomnia started for " + duration + " minute(s).");
        }
    }

    @SuppressWarnings("null")
    private static void end() {
        activeUntil = 0L;

        if (announce()) {
            String message = colored(LangService.msg("insomnia.end",
                    "&7You feel drowsy again."));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                    continue;
                }

                player.sendMessage(message);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.4F, 0.8F);
            }
        }

        if (plugin != null) {
            plugin.getLogger().info("Insomnia ended.");
        }
    }

    private static boolean isNightSomewhere() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            if (world.getPlayers().isEmpty()) {
                continue;
            }

            long time = world.getTime();

            if (time >= 12541L && time <= 23459L) {
                return true;
            }
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isActive()) {
            return;
        }

        if (event.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        player.sendMessage(colored(LangService.msg("insomnia.denied",
                "&cYou can't sleep tonight.")));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.4F, 0.5F);
    }

    public static void start(Plugin owner) {
        plugin = owner;
        shutdown();
        
        Bukkit.getPluginManager().registerEvents(new InsomniaService(), owner);
        Bukkit.getScheduler().runTaskTimer(owner, InsomniaService::tick, 20L, 20L);
    }

    private static void scheduleNext(long now) {
        int min = Math.max(1, configInt("interval-min-minutes", 20));
        int max = Math.max(min, configInt("interval-max-minutes", 45));
        int interval = min == max ? min : min + (int) (Math.random() * (max - min + 1));

        nextStartAt = now + interval * 60_000L;
    }

    public static void shutdown() {
        activeUntil = 0L;
        nextStartAt = 0L;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntil;
    }

    private static boolean enabled() {
        return Main.balance == null || Main.balance.getBoolean("insomnia.enabled", true);
    }

    private static boolean onlyAtNight() {
        return Main.balance == null || Main.balance.getBoolean("insomnia.only-at-night", true);
    }

    private static boolean announce() {
        return Main.balance == null || Main.balance.getBoolean("insomnia.announce", true);
    }

    private static int configInt(String key, int fallback) {
        return Main.balance == null ? fallback : Main.balance.getInt("insomnia." + key, fallback);
    }

    private static String colored(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
