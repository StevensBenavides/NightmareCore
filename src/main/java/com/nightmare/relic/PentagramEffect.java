package com.nightmare.relic;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class PentagramEffect {

    private static final int[] STAR_ORDER = { 0, 2, 4, 1, 3, 0 };

    private PentagramEffect() {
    }

    public static void draw(World world, Location center, Vector axisA, Vector axisB, double radius,
            double rotation, boolean circle) {
        Vector a = axisA.clone().normalize();
        Vector b = axisB.clone().normalize();
        Location[] vertices = new Location[5];

        for (int index = 0; index < 5; index++) {
            double angle = Math.toRadians(90.0 + index * 72.0) + rotation;

            vertices[index] = center.clone()
                    .add(a.clone().multiply(Math.cos(angle) * radius))
                    .add(b.clone().multiply(Math.sin(angle) * radius));
        }

        for (int edge = 0; edge < STAR_ORDER.length - 1; edge++) {
            drawLine(world, vertices[STAR_ORDER[edge]], vertices[STAR_ORDER[edge + 1]]);
        }

        for (Location vertex : vertices) {
            world.spawnParticle(Particle.END_ROD, vertex, 1, 0.0, 0.0, 0.0, 0.0);
        }

        if (circle) {
            int points = 72;

            for (int index = 0; index < points; index++) {
                double angle = Math.toRadians(index * 360.0 / points) + rotation;

                Location point = center.clone()
                        .add(a.clone().multiply(Math.cos(angle) * radius * 1.1))
                        .add(b.clone().multiply(Math.sin(angle) * radius * 1.1));

                spawnDust(world, point);
            }
        }
    }

    private static void drawLine(World world, Location start, Location end) {
        int points = 18;

        for (int index = 0; index <= points; index++) {
            double t = index / (double) points;

            Location point = start.clone().add(end.clone().subtract(start).multiply(t));

            spawnDust(world, point);
        }
    }

    private static void spawnDust(World world, Location location) {
        world.spawnParticle(Particle.DUST, location, 1, 0.0, 0.0, 0.0, 0.0,
                new Particle.DustOptions(Color.WHITE, 1.4F));
    }

}
