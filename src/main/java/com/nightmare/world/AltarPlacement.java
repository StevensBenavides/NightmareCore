package com.nightmare.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Resolves where an altar can be placed inside a chunk.
 *
 * <p>Surface placement keeps the original flat-terrain rules. Cave placement
 * looks for an underground pocket with enough floor support, open volume and a
 * solid ceiling. Both paths produce the same {@link Site} so the rest of the
 * altar pipeline does not need to know how the location was found.</p>
 */
public final class AltarPlacement {

    public enum Kind {
        SURFACE,
        CAVE
    }

    public record Site(int centerX, int centerZ, int baseY, Kind kind, int headroom) {
    }

    public enum Probe {
        SOLID,
        LIQUID,
        OPEN
    }

    /** Abstraction used by tests to feed synthetic terrain without a server. */
    public interface BlockReader {
        Probe probeAt(int x, int y, int z);

        int highestY(int x, int z);

        int minY();

        int maxY();
    }

    private static final int RADIUS = 4;

    private AltarPlacement() {
    }

    public static Site find(World world, int centerX, int centerZ, long hash, YamlConfiguration config) {
        return find(new WorldReader(world), centerX, centerZ, hash, config);
    }

    static Site find(BlockReader reader, int centerX, int centerZ, long hash, YamlConfiguration config) {
        YamlConfiguration settings = config == null ? new YamlConfiguration() : config;
        String mode = settings.getString("altars.placement.mode", "either");

        if (mode == null) {
            mode = "either";
        }

        mode = mode.trim().toLowerCase();
        boolean surfaceOnly = mode.equals("surface");
        boolean caveOnly = mode.equals("cave");
        boolean either = !surfaceOnly && !caveOnly;
        double caveChance = clamp01(settings.getDouble("altars.placement.cave-chance", 0.35));
        boolean preferCave = caveOnly || (either && prefersCave(mode, caveChance, hash));

        if (surfaceOnly) {
            return findSurface(reader, centerX, centerZ, settings);
        }

        if (caveOnly) {
            return findCave(reader, centerX, centerZ, settings);
        }

        if (preferCave) {
            Site cave = findCave(reader, centerX, centerZ, settings);

            if (cave != null) {
                return cave;
            }

            return findSurface(reader, centerX, centerZ, settings);
        }

        Site surface = findSurface(reader, centerX, centerZ, settings);

        if (surface != null) {
            return surface;
        }

        return findCave(reader, centerX, centerZ, settings);
    }

    static boolean prefersCave(String mode, double chance, long hash) {
        if (mode == null) {
            return false;
        }

        String normalized = mode.trim().toLowerCase();

        if (normalized.equals("cave")) {
            return true;
        }

        if (normalized.equals("surface")) {
            return false;
        }

        if (chance <= 0.0D) {
            return false;
        }

        if (chance >= 1.0D) {
            return true;
        }

        long bucket = Long.remainderUnsigned(hash >>> 32, 10_000L);

        return bucket / 10_000.0D < chance;
    }

    private static Site findSurface(BlockReader reader, int centerX, int centerZ, YamlConfiguration config) {
        int baseY = surfaceMax(reader, centerX, centerZ);

        if (!suitableSurface(reader, centerX, baseY, centerZ, config)) {
            return null;
        }

        return new Site(centerX, centerZ, baseY, Kind.SURFACE, Math.max(0, reader.maxY() - baseY));
    }

    private static int surfaceMax(BlockReader reader, int centerX, int centerZ) {
        int max = Integer.MIN_VALUE;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                max = Math.max(max, reader.highestY(centerX + dx, centerZ + dz));
            }
        }

        return max == Integer.MIN_VALUE ? reader.highestY(centerX, centerZ) : max;
    }

    private static boolean suitableSurface(BlockReader reader, int baseX, int baseY, int baseZ,
            YamlConfiguration config) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int obstructed = 0;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                int surface = reader.highestY(baseX + dx, baseZ + dz);

                min = Math.min(min, surface);
                max = Math.max(max, surface);

                if (reader.probeAt(baseX + dx, surface, baseZ + dz) == Probe.LIQUID) {
                    return false;
                }

                for (int y = surface + 1; y <= surface + 6; y++) {
                    if (reader.probeAt(baseX + dx, y, baseZ + dz) == Probe.SOLID) {
                        obstructed++;
                        break;
                    }
                }
            }
        }

        int maxHeightDifference = Math.max(0, config.getInt("altars.max-height-difference", 4));
        int maxObstructed = Math.max(0, config.getInt("altars.max-obstructed", 20));

        return max - min <= maxHeightDifference && obstructed <= maxObstructed;
    }

    private static Site findCave(BlockReader reader, int centerX, int centerZ, YamlConfiguration config) {
        int surfaceY = surfaceMax(reader, centerX, centerZ);
        int minDepth = Math.max(0, config.getInt("altars.cave.min-depth", 12));
        int maxDepth = Math.max(minDepth, config.getInt("altars.cave.max-depth", 64));
        int minHeight = Math.max(3, config.getInt("altars.cave.min-height", 5));
        int maxHeight = Math.max(minHeight, config.getInt("altars.cave.max-height", 24));
        double minOpen = clamp01(config.getDouble("altars.cave.min-open-ratio", 0.85D));
        double minFloor = clamp01(config.getDouble("altars.cave.min-floor-ratio", 0.75D));
        boolean requireCeiling = config.getBoolean("altars.cave.require-ceiling", true);
        boolean avoidLiquids = config.getBoolean("altars.cave.avoid-liquids", true);

        int top = surfaceY - minDepth;
        int bottom = Math.max(reader.minY() + 1, surfaceY - maxDepth);

        for (int y = top; y >= bottom; y--) {
            if (y + minHeight > reader.maxY()) {
                continue;
            }

            Site site = evaluateCave(reader, centerX, centerZ, y, minHeight, maxHeight, minOpen, minFloor,
                    requireCeiling, avoidLiquids);

            if (site != null) {
                return site;
            }
        }

        return null;
    }

    private static Site evaluateCave(BlockReader reader, int centerX, int centerZ, int baseY, int minHeight,
            int maxHeight, double minOpen, double minFloor, boolean requireCeiling, boolean avoidLiquids) {
        int total = 0;
        int floorCells = 0;
        int openCells = 0;
        int ceilingCells = 0;
        int headroom = Integer.MAX_VALUE;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                total++;

                int x = centerX + dx;
                int z = centerZ + dz;
                Probe floor = reader.probeAt(x, baseY - 1, z);

                if (floor == Probe.LIQUID) {
                    return null;
                }

                if (floor == Probe.SOLID) {
                    floorCells++;
                }

                boolean open = true;

                for (int dy = 0; dy < minHeight; dy++) {
                    Probe probe = reader.probeAt(x, baseY + dy, z);

                    if (probe == Probe.LIQUID) {
                        if (avoidLiquids) {
                            return null;
                        }

                        open = false;
                        break;
                    }

                    if (probe == Probe.SOLID) {
                        open = false;
                        break;
                    }
                }

                if (!open) {
                    continue;
                }

                openCells++;

                int cellHeadroom = 0;

                for (int dy = minHeight; dy <= maxHeight; dy++) {
                    int y = baseY + dy;

                    if (y > reader.maxY()) {
                        break;
                    }

                    Probe probe = reader.probeAt(x, y, z);

                    if (probe == Probe.LIQUID) {
                        if (avoidLiquids) {
                            cellHeadroom = -1;
                            break;
                        }

                        continue;
                    }

                    if (probe == Probe.SOLID) {
                        cellHeadroom = dy;
                        break;
                    }
                }

                if (cellHeadroom == 0) {
                    if (requireCeiling) {
                        continue;
                    }

                    cellHeadroom = maxHeight;
                }

                if (cellHeadroom < 0) {
                    return null;
                }

                ceilingCells++;
                headroom = Math.min(headroom, cellHeadroom);
            }
        }

        if (total == 0 || headroom == Integer.MAX_VALUE || headroom < minHeight) {
            return null;
        }

        double openRatio = openCells / (double) total;
        double floorRatio = floorCells / (double) total;
        double ceilingRatio = ceilingCells / (double) total;

        if (openRatio < minOpen || floorRatio < minFloor) {
            return null;
        }

        if (requireCeiling && ceilingRatio < minOpen) {
            return null;
        }

        return new Site(centerX, centerZ, baseY, Kind.CAVE, headroom);
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static final class WorldReader implements BlockReader {
        private final World world;

        private WorldReader(World world) {
            this.world = world;
        }

        @Override
        public Probe probeAt(int x, int y, int z) {
            Material material = world.getBlockAt(x, y, z).getType();

            if (material == Material.WATER || material == Material.LAVA || material == Material.BUBBLE_COLUMN) {
                return Probe.LIQUID;
            }

            return material.isSolid() ? Probe.SOLID : Probe.OPEN;
        }

        @Override
        public int highestY(int x, int z) {
            return world.getHighestBlockYAt(x, z);
        }

        @Override
        public int minY() {
            return world.getMinHeight();
        }

        @Override
        public int maxY() {
            return world.getMaxHeight() - 1;
        }
    }
}
