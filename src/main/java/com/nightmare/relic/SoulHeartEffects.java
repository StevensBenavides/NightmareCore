package com.nightmare.relic;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public final class SoulHeartEffects {

    private SoulHeartEffects() {
    }

    public static void soulBurst(World world, Location center, double radius) {
        world.spawnParticle(Particle.DUST, center, 18, radius / 2.0, radius / 2.0, radius / 2.0, 0.01,
                new Particle.DustOptions(Color.WHITE, 1.2F));
        world.spawnParticle(Particle.END_ROD, center, 8, radius / 2.0, radius / 2.0, radius / 2.0, 0.02);
        world.spawnParticle(Particle.SOUL, center, 10, radius / 2.0, radius / 2.0, radius / 2.0, 0.02);
    }

    public static void divineBurst(World world, Location center) {
        world.spawnParticle(Particle.DUST, center, 30, 1.0, 1.0, 1.0, 0.02,
                new Particle.DustOptions(Color.WHITE, 1.6F));
        world.spawnParticle(Particle.END_ROD, center, 18, 1.0, 1.0, 1.0, 0.04);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 12, 0.8, 0.8, 0.8, 0.02);
    }

}
