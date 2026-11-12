package com.nightmare.relic;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.SphereEffect;

import com.nightmare.Main;

public final class WhiteExplosionEffect {

    private static EffectManager manager;

    private WhiteExplosionEffect() {
    }

    private static void startBurst(Location center, boolean big) {
        if (manager == null) {
            return;
        }

        SphereEffect flash = new SphereEffect(manager);

        flash.setLocation(center.clone());
        flash.particle = Particle.FLASH;
        flash.color = Color.WHITE;
        flash.particleSize = big ? 3.2F : 2.6F;
        flash.radius = big ? 1.8 : 1.4;
        flash.particles = big ? 170 : 120;
        flash.radiusIncrease = 0.30F;
        flash.particleIncrease = 10;
        flash.period = 1;
        flash.iterations = 7;

        safeStart(flash);

        SphereEffect blast = new SphereEffect(manager);

        blast.setLocation(center.clone());
        blast.particle = Particle.WHITE_SMOKE;
        blast.color = Color.WHITE;
        blast.particleSize = big ? 2.8F : 2.2F;
        blast.radius = big ? 2.6 : 2.0;
        blast.particles = big ? 250 : 180;
        blast.radiusIncrease = 0.30F;
        blast.particleIncrease = 16;
        blast.period = 1;
        blast.iterations = 7;

        safeStart(blast);

        SphereEffect shell = new SphereEffect(manager);

        shell.setLocation(center.clone());
        shell.particle = Particle.WHITE_SMOKE;
        shell.color = Color.WHITE;
        shell.particleSize = big ? 1.7F : 1.4F;
        shell.radius = big ? 3.0 : 2.4;
        shell.particles = big ? 460 : 340;
        shell.radiusIncrease = 0.22F;
        shell.particleIncrease = 20;
        shell.period = 1;
        shell.iterations = 7;

        safeStart(shell);

        SphereEffect halo = new SphereEffect(manager);

        halo.setLocation(center.clone());
        halo.particle = Particle.CLOUD;
        halo.color = Color.WHITE;
        halo.particleSize = 1.5F;
        halo.radius = big ? 3.4 : 2.6;
        halo.particles = big ? 320 : 220;
        halo.radiusIncrease = 0.18F;
        halo.particleIncrease = 8;
        halo.period = 1;
        halo.iterations = 6;

        safeStart(halo);
    }

    private static void startCharge(Location center, boolean big) {
        SphereEffect core = new SphereEffect(manager);

        core.setLocation(center.clone());
        core.particle = Particle.FLASH;
        core.color = Color.WHITE;
        core.particleSize = big ? 2.2F : 1.8F;
        core.radius = big ? 1.6 : 1.2;
        core.particles = big ? 170 : 120;
        core.radiusIncrease = 0.30F;
        core.particleIncrease = 10;
        core.period = 1;
        core.iterations = 6;

        safeStart(core);

        SphereEffect shell = new SphereEffect(manager);

        shell.setLocation(center.clone());
        shell.particle = Particle.WHITE_SMOKE;
        shell.color = Color.WHITE;
        shell.particleSize = big ? 1.8F : 1.5F;
        shell.radius = big ? 2.4 : 1.8;
        shell.particles = big ? 250 : 190;
        shell.radiusIncrease = 0.30F;
        shell.particleIncrease = 13;
        shell.period = 1;
        shell.iterations = 6;

        safeStart(shell);

        SphereEffect halo = new SphereEffect(manager);

        halo.setLocation(center.clone());
        halo.particle = Particle.CLOUD;
        halo.color = Color.WHITE;
        halo.particleSize = 1.4F;
        halo.radius = big ? 2.8 : 2.2;
        halo.particles = big ? 170 : 120;
        halo.radiusIncrease = 0.22F;
        halo.particleIncrease = 8;
        halo.period = 1;
        halo.iterations = 6;

        safeStart(halo);
    }

    public static void spawn(World world, Location center, boolean big) {
        if (manager == null || center == null) {
            return;
        }

        Location point = center.clone();

        startCharge(point, big);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> startBurst(point, big), 6L);
    }

    private static void safeStart(Effect effect) {
        try {
            effect.asynchronous = false;
            effect.start();
        } catch (Throwable exception) {
            Main.getInstance().getLogger().warning("EffectLib effect failed: " + exception.getMessage());
        }
    }

    public static void shutdown() {
        if (manager != null) {
            manager.dispose();
            manager = null;
        }
    }

    public static void chargeOnly(World world, Location center, boolean big) {
        if (manager == null || center == null) {
            return;
        }

        startCharge(center.clone(), big);
    }

    public static void init(Plugin plugin) {
        shutdown();
        manager = new EffectManager(plugin);
    }

}
