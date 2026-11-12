package com.nightmare.relic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.CircleEffect;
import de.slikey.effectlib.effect.ConeEffect;
import de.slikey.effectlib.effect.HelixEffect;
import de.slikey.effectlib.effect.LineEffect;
import de.slikey.effectlib.effect.ShieldEffect;
import de.slikey.effectlib.effect.SphereEffect;
import de.slikey.effectlib.effect.TornadoEffect;
import de.slikey.effectlib.effect.VortexEffect;

public final class TendrilsEffects {

    private static final Map<UUID, Long> PASSIVE_COOLDOWNS = new ConcurrentHashMap<>();
    private static EffectManager manager;

    private TendrilsEffects() {
    }

    public static void impact(Location center, double radius, boolean falling) {
        if (manager == null) {
            return;
        }

        Location location = flat(center.clone());
        CircleEffect circle = new CircleEffect(manager);

        circle.setLocation(location);
        circle.particle = Particle.DUST;
        circle.color = Color.BLACK;
        circle.particleSize = 1.4F;
        circle.radius = (float) radius;
        circle.particles = falling ? 180 : 140;
        circle.wholeCircle = true;
        circle.enableRotation = false;
        circle.period = 2;
        circle.iterations = falling ? 12 : 9;

        safeStart(circle);

        SphereEffect sphere = new SphereEffect(manager);

        sphere.setLocation(location);
        sphere.particle = Particle.SQUID_INK;
        sphere.color = Color.BLACK;
        sphere.particleSize = 1.5F;
         sphere.radius = falling ? 2.2 : 1.8;
        sphere.particles = falling ? 120 : 90;
        sphere.radiusIncrease = 0.3F;
        sphere.particleIncrease = falling ? 12 : 10;
        sphere.period = 1;
        sphere.iterations = falling ? 3 : 2;

        safeStart(sphere);

        VortexEffect vortex = new VortexEffect(manager);

        vortex.setLocation(location);
        vortex.particle = Particle.SQUID_INK;
        vortex.radius = (float) Math.min(3.0, radius / 2.0);
        vortex.radiusGrow = 0.08F;
        vortex.grow = 0.22F;
        vortex.circles = 4;
        vortex.helixes = 5;
        vortex.period = 1;
        vortex.iterations = falling ? 30 : 24;

        safeStart(vortex);

        ConeEffect cone = new ConeEffect(manager);

        cone.setLocation(location);
        cone.particle = Particle.DUST;
        cone.color = Color.BLACK;
        cone.particleSize = 1.3F;
        cone.lengthGrow = 0.28F;
        cone.radiusGrow = 0.045F;
        cone.particles = falling ? 50 : 38;
        cone.particlesCone = 12;
        cone.period = 1;
        cone.iterations = falling ? 24 : 18;

        safeStart(cone);
    }

    public static void cataclysmCast(Location center) {
        if (manager == null) {
            return;
        }

        Location location = flat(center.clone());
        CircleEffect circle = new CircleEffect(manager);

        circle.setLocation(location);
        circle.particle = Particle.DUST;
        circle.color = Color.BLACK;
        circle.particleSize = 1.4F;
        circle.radius = 7.5F;
        circle.particles = 140;
        circle.wholeCircle = true;
        circle.enableRotation = false;
        circle.period = 2;
        circle.iterations = 8;

        safeStart(circle);

        TornadoEffect tornado = new TornadoEffect(manager);

        tornado.setLocation(location);
        tornado.tornadoParticle = Particle.SQUID_INK;
        tornado.tornadoColor = Color.BLACK;
        tornado.showCloud = false;
        tornado.tornadoHeight = 3.5F;
        tornado.maxTornadoRadius = 2.5F;
        tornado.circleParticles = 32;
        tornado.distance = 0.45;
        tornado.period = 2;
        tornado.iterations = 10;

        safeStart(tornado);
    }

    public static void finish(Location center, double radius, boolean falling) {
        if (manager == null) {
            return;
        }

        Location location = flat(center.clone());

        SphereEffect sphere = new SphereEffect(manager);

        sphere.setLocation(location);
        sphere.particle = Particle.SQUID_INK;
        sphere.color = Color.BLACK;
        sphere.particleSize = 1.6F;
        sphere.radius = falling ? 2.4 : 2.0;
        sphere.particles = falling ? 150 : 110;
        sphere.radiusIncrease = 0.3F;
        sphere.particleIncrease = falling ? 16 : 12;
        sphere.period = 1;
        sphere.iterations = falling ? 4 : 3;

        safeStart(sphere);

        CircleEffect circle = new CircleEffect(manager);

        circle.setLocation(location);
        circle.particle = Particle.SQUID_INK;
        circle.radius = (float) radius;
        circle.particles = falling ? 220 : 170;
        circle.wholeCircle = true;
        circle.enableRotation = false;
        circle.period = 1;
        circle.iterations = 7;

        safeStart(circle);
    }

    public static void cast(Player player) {
        if (manager == null) {
            return;
        }

        Location location = flat(player.getLocation().add(0.0, 1.0, 0.0));
        HelixEffect helix = new HelixEffect(manager);

        helix.setLocation(location);
        helix.particle = Particle.SQUID_INK;
        helix.radius = 1.8F;
        helix.strands = 3;
        helix.particles = 35;
        helix.period = 1;
        helix.iterations = 20;
        helix.enableRotation = false;

        safeStart(helix);

        VortexEffect vortex = new VortexEffect(manager);

        vortex.setLocation(location);
        vortex.particle = Particle.SQUID_INK;
        vortex.radius = 1.4F;
        vortex.radiusGrow = 0.08F;
        vortex.grow = 0.16F;
        vortex.period = 1;
        vortex.iterations = 24;

        safeStart(vortex);
    }

    public static void howlKill(Location location) {
        if (manager == null) {
            return;
        }

        Location center = flat(location.clone());
        SphereEffect sphere = new SphereEffect(manager);

        sphere.setLocation(center);
        sphere.particle = Particle.SQUID_INK;
        sphere.color = Color.BLACK;
        sphere.particleSize = 1.4F;
        sphere.radius = 1.6;
        sphere.particles = 60;
        sphere.radiusIncrease = 0.3F;
        sphere.particleIncrease = 8;
        sphere.period = 1;
        sphere.iterations = 2;

        safeStart(sphere);
    }

    public static void burstImpact(Location location) {
        if (manager == null) {
            return;
        }

        SphereEffect sphere = new SphereEffect(manager);

        sphere.setLocation(flat(location.clone()));
        sphere.particle = Particle.SQUID_INK;
        sphere.color = Color.BLACK;
        sphere.particleSize = 1.4F;
        sphere.radius = 1.8;
        sphere.particles = 80;
        sphere.radiusIncrease = 0.3F;
        sphere.particleIncrease = 10;
        sphere.period = 1;
        sphere.iterations = 2;

        safeStart(sphere);
    }

    public static void cataclysmFinish(Location center) {
        if (manager == null) {
            return;
        }

        SphereEffect sphere = new SphereEffect(manager);

        sphere.setLocation(flat(center.clone()));
        
        sphere.particle = Particle.SQUID_INK;
        sphere.color = Color.BLACK;
        sphere.particleSize = 1.6F;
        sphere.radius = 2.0;
        sphere.particles = 100;
        sphere.radiusIncrease = 0.3F;
        sphere.particleIncrease = 12;
        sphere.period = 1;
        sphere.iterations = 3;

        safeStart(sphere);
    }

    public static void burstLine(Location from, Location to) {
        if (manager == null) {
            return;
        }

        LineEffect line = new LineEffect(manager);

        line.setLocation(from.clone());
        line.setTarget(to.clone());
        line.particle = Particle.DUST;
        line.color = Color.BLACK;
        line.particleSize = 1.3F;
        line.particles = 90;
        line.period = 1;
        line.iterations = 1;

        safeStart(line);
    }

    private static void corePassive(Player player) {
        ShieldEffect shield = new ShieldEffect(manager);

        shield.setLocation(flat(player.getLocation().add(0.0, 0.9, 0.0)));
        shield.particle = Particle.DUST;
        shield.color = Color.BLACK;
        shield.particleSize = 1.2F;
        shield.radius = 1.6;
        shield.particles = 16;
        shield.sphere = false;
        shield.reverse = false;
        shield.period = 5;
        shield.iterations = 4;

        safeStart(shield);
    }

    private static void howlPassive(Player player) {
        Location location = flat(player.getLocation().add(0.0, 1.0, 0.0));
        HelixEffect helix = new HelixEffect(manager);

        helix.setLocation(location);
        helix.particle = Particle.SQUID_INK;
        helix.radius = 1.4F;
        helix.strands = 2;
        helix.particles = 24;
        helix.period = 4;
        helix.iterations = 12;
        helix.enableRotation = false;

        safeStart(helix);
    }

    public static void passive(Player player, boolean core) {
        long now = System.currentTimeMillis();

        if (PASSIVE_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) > now) {
            return;
        }

        PASSIVE_COOLDOWNS.put(player.getUniqueId(), now + 5000L);

        if (core) {
            corePassive(player);
        } else {
            howlPassive(player);
        }
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

        PASSIVE_COOLDOWNS.clear();
    }

    private static Location flat(Location location) {
        Location flat = location.clone();

        flat.setYaw(0.0F);
        flat.setPitch(0.0F);

        return flat;
    }

    public static void init(Plugin plugin) {
        shutdown();
        manager = new EffectManager(plugin);
    }

}


