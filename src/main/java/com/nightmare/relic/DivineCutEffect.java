package com.nightmare.relic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;

import com.nightmare.Main;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class DivineCutEffect {

    private DivineCutEffect() {
    }

    public static void cast(Player player) {
        World world = player.getWorld();
        Location start = player.getLocation().clone();
        double eyeHeight = player.getEyeHeight();
        Vector forward = player.getLocation().getDirection().setY(0.0).normalize();

        if (forward.lengthSquared() < 0.001) {
            forward = new Vector(0, 0, 1);
        }

        Vector up = new Vector(0, 1, 0);
        Vector normal = forward.clone().crossProduct(up).normalize();

        UUID casterId = player.getUniqueId();

        int duration = Math.max(20, Main.balance.getInt("soul-heart.divine-windup-ticks", 100));
        int length = Math.max(10, Main.balance.getInt("soul-heart.divine-length", 30));
        int height = Math.max(10, Main.balance.getInt("soul-heart.divine-height", 150));
        int thickness = Math.max(1, Main.balance.getInt("soul-heart.divine-thickness", 4));
        double damage = Main.balance.getDouble("soul-heart.divine-damage", 150.0);
        int batch = Math.max(100, Main.balance.getInt("soul-heart.divine-batch-blocks", 1000));

        Vector finalForward = forward;
        Vector finalNormal = normal;

        final BukkitTask[] windup = new BukkitTask[1];
       
        windup[0] = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                Player caster = Bukkit.getPlayer(casterId);

                if (caster == null || !caster.isOnline() || caster.isDead() || !caster.getWorld().equals(world)) {
                    SoulHeartService.refundDivineWindup(casterId);
                    cancel();
                    return;
                }

                if (ticks >= duration) {
                    SoulHeartService.finishDivineWindup(casterId, windup[0]);
                    Location boom = start.clone().add(finalForward.clone().multiply(length / 2.0));
                    
                    world.playSound(boom, Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.0F);
                    caster.playSound(caster.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.0F);
                    world.playSound(boom, Sound.ENTITY_GENERIC_EXPLODE, 0.55F, 0.9F);
                    
                    drawBurst(world, start, finalForward, up, length, height, eyeHeight);
                    damage(world, start, finalForward, finalNormal, length, height, thickness, damage, casterId);
                    destroy(world, start, finalForward, up, finalNormal, length, height, thickness, batch, casterId);
                    startAfterglow(world, start, finalForward, up, length, height, eyeHeight);
                    cancel();

                    return;
                }

                if (ticks % 5 == 0) {
                    drawWall(world, start, finalForward, up, length, height, eyeHeight);
                }

                if (ticks % 10 == 0) {
                    chargeGaps(world, start, finalForward, up, length, eyeHeight);
                    
                    float progress = duration <= 0 ? 1.0F : Math.min(1.0F, (float) ticks / duration);
                    float pitch = 0.6F + progress * 0.9F;

                    caster.playSound(caster.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0F, pitch);
                    world.playSound(start.clone().add(finalForward.clone().multiply(length / 2.0)),
                            Sound.ITEM_TRIDENT_RETURN, 0.6F, pitch);
                }

                ticks++;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);

        SoulHeartService.trackDivineWindup(casterId, windup[0]);
    }

    private static void destroy(World world, Location start, Vector forward, Vector up, Vector normal, int length,
            int height, int thickness, int batch, UUID casterId) {
        List<Block> blocks = new ArrayList<>(length * height * thickness);

        for (int distance = 0; distance < length; distance++) {
            for (int vertical = 0; vertical < height; vertical++) {
                for (int layer = 0; layer < thickness; layer++) {
                    Location location = start.clone()
                            .add(forward.clone().multiply(distance))
                            .add(up.clone().multiply(vertical))
                            .add(normal.clone().multiply(layer - (thickness - 1) / 2.0));
                    Block block = world.getBlockAt(location);

                    if (block.getType().isAir() || block.getType().getHardness() < 0.0F) {
                        continue;
                    }

                    blocks.add(block);
                }
            }
        }

        new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                int processed = 0;

                while (index < blocks.size() && processed < batch) {
                    Block block = blocks.get(index++);

                    if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                        continue;
                    }

                    if (block.getType().isAir() || block.getType().getHardness() < 0.0F) {
                        continue;
                    }

                    Player caster = Bukkit.getPlayer(casterId);

                    if (caster != null && caster.isOnline()) {
                        BlockBreakEvent event = new BlockBreakEvent(block, caster);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            continue;
                        }
                    }

                    block.setType(Material.AIR, false);
                    processed++;
                }

                if (index >= blocks.size()) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    private static void startAfterglow(World world, Location start, Vector forward, Vector up, int length,
            int height, double eyeHeight
    ) {
        int duration = Math.max(20, Main.balance.getInt("soul-heart.divine-afterglow-ticks", 1200));
        int interval = Math.max(5, Main.balance.getInt("soul-heart.divine-afterglow-interval-ticks", 20));

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }

                if (ticks % interval == 0) {
                    drawWall(world, start, forward, up, length, height, eyeHeight);
                    chargeGapsRotating(world, start, forward, up, length, eyeHeight, ticks / interval);
                }

                if (ticks > 0 && ticks % 100 == 0) {
                    double distance = 10.0 + ThreadLocalRandom.current().nextInt(Math.max(1, (length - 10) / 10)) * 10.0;

                    Location point = start.clone()
                            .add(forward.clone().multiply(distance))
                            .add(up.clone().multiply(eyeHeight));

                    WhiteExplosionEffect.spawn(world, point, false);
                }

                ticks++;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    private static void damage(World world, Location start, Vector forward, Vector normal, int length, int height,
            int thickness, double damage, UUID casterId
    ) {
        Location center = start.clone().add(forward.clone().multiply(length / 2.0))
                .add(new Vector(0, height / 2.0, 0));
        Player caster = Bukkit.getPlayer(casterId);

        for (Entity entity : world.getNearbyEntities(center, length, height, length)) {
            if (!(entity instanceof LivingEntity living) || living.getUniqueId().equals(casterId) || living.isDead()) {
                continue;
            }

            Vector offset = living.getLocation().toVector().subtract(start.toVector());
            
            double along = offset.dot(forward);
            double vertical = offset.getY();
            double side = offset.dot(normal);

            if (along < 0.0 || along > length || vertical < 0.0 || vertical > height
                    || Math.abs(side) > thickness / 2.0 + 1.0) {
                continue;
            }

            if (caster != null && caster.isOnline()) {
                living.damage(damage, DamageSource.builder(DamageType.MAGIC)
                        .withCausingEntity(caster)
                        .withDirectEntity(caster)
                        .build());
            } else {
                living.damage(damage, DamageSource.builder(DamageType.MAGIC).build());
            }
        }
    }

    private static void drawBurst(World world, Location start, Vector forward, Vector up, int length, int height,
            double eyeHeight
    ) {
        for (int index = 0; index < 12; index++) {
            double distance = gapDistance(index);

            Location point = start.clone()
                .add(forward.clone().multiply(distance))
                .add(up.clone().multiply(eyeHeight));

            WhiteExplosionEffect.spawn(world, point, false);
        }

        WhiteExplosionEffect.spawn(world,
            start.clone().add(forward.clone().multiply(length / 2.0 - 5.0))
                    .add(up.clone().multiply(eyeHeight)),
            true
        );
    }

    private static void chargeGapsRotating(World world, Location start, Vector forward, Vector up, int length,
            double eyeHeight, int wave
    ) {

        int gaps = Math.max(1, (length - 10) / 10);

        for (int slot = 0; slot < 4; slot++) {
            int gap = (wave * 4 + slot) % gaps;

            Location point = start.clone()
                .add(forward.clone().multiply(10.0 + gap * 10.0))
                .add(up.clone().multiply(eyeHeight));

            WhiteExplosionEffect.chargeOnly(world, point, false);
        }
    }

    private static void chargeGaps(World world, Location start, Vector forward, Vector up, int length,
            double eyeHeight
    ) {

        for (int distance = 10; distance < length; distance += 10) {
            Location point = start.clone()
                    .add(forward.clone().multiply(distance))
                    .add(up.clone().multiply(eyeHeight));
            WhiteExplosionEffect.chargeOnly(world, point, false);
        }
    }

    private static void drawWall(World world, Location start, Vector forward, Vector up, int length,
            int height, double eyeHeight
    ) {

        for (int distance = 5; distance < length; distance += 10) {
            Location center = start.clone().add(forward.clone().multiply(distance)).add(up.clone().multiply(eyeHeight));
            PentagramEffect.draw(world, center, forward, up, 2.5, 0.0, true);
        }

    }

    private static double gapDistance(int smallExplosionIndex) {
        int gap = Math.round(smallExplosionIndex * 13.0F / 11.0F);
        return 10.0 + gap * 10.0;
    }

}


