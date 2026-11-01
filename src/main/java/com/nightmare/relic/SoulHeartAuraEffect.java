package com.nightmare.relic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.nightmare.Main;

public final class SoulHeartAuraEffect {

    private static final Vector RIGHT = new Vector(1, 0, 0);
    private static final Vector FORWARD = new Vector(0, 0, 1);

    private SoulHeartAuraEffect() {
    }

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, SoulHeartAuraEffect::tick, 4L, 4L);
    }

    public static void tick() {
        double radius = Main.balance == null ? 2.0 : Main.balance.getDouble("soul-heart.aura-radius", 2.0);
        long rotationMs = Main.balance == null ? 5000L : Main.balance.getLong("soul-heart.aura-rotation-ms", 5000L);
        long effectiveRotationMs = Math.max(200L, rotationMs);
        double rotation = (System.currentTimeMillis() % effectiveRotationMs)
                / (double) effectiveRotationMs * Math.PI * 2.0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead() || !SoulHeartService.hasHeart(player)) {
                continue;
            }

            Location center = player.getLocation().add(0.0, 0.05, 0.0);
            PentagramEffect.draw(player.getWorld(), center, RIGHT, FORWARD, radius, rotation, true);
        }
    }

}
