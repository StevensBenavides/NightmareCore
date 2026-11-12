package com.nightmare.world;

import java.util.Set;

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

    /**
     * Cave naturalness is decided from block probes. Natural cave air and
     * natural decorations are allowed; player/ordinary air and constructed

     * blocks are not.
     */
    public enum Probe {
        NATURAL_SOLID,
        ARTIFICIAL,
        NATURAL_OPEN,
        AIR,
        LIQUID
    }

    /** Abstraction used by tests to feed synthetic terrain without a server. */
    public interface BlockReader {
        Probe probeAt(int x, int y, int z);

        int highestY(int x, int z);

        int minY();

        int maxY();
    }

    private static final int RADIUS = 4;

    private static final Set<String> ARTIFICIAL_NAMES = Set.of(
            "COBBLESTONE", "MOSSY_COBBLESTONE", "BRICKS", "GLASS", "GLASS_PANE",
            "TORCH", "WALL_TORCH", "SOUL_TORCH", "SOUL_WALL_TORCH", "FIRE", "SOUL_FIRE",
            "RAIL", "POWERED_RAIL", "DETECTOR_RAIL", "ACTIVATOR_RAIL",
            "COBWEB", "SPAWNER", "TRIAL_SPAWNER", "VAULT",
            "CHEST", "TRAPPED_CHEST", "BARREL", "FURNACE", "BLAST_FURNACE", "SMOKER",
            "CRAFTING_TABLE", "CARTOGRAPHY_TABLE", "FLETCHING_TABLE", "SMITHING_TABLE",
            "ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL", "GRINDSTONE", "STONECUTTER", "LOOM",
            "BELL", "COMPOSTER", "HOPPER", "DROPPER", "DISPENSER", "OBSERVER",
            "PISTON", "STICKY_PISTON", "PISTON_HEAD", "MOVING_PISTON",
            "BREWING_STAND", "CAULDRON", "WATER_CAULDRON", "LAVA_CAULDRON", "POWDER_SNOW_CAULDRON",
            "ENCHANTING_TABLE", "LECTERN", "BOOKSHELF", "CHISELED_BOOKSHELF", "JUKEBOX", "NOTE_BLOCK",
            "REDSTONE_LAMP", "TNT", "IRON_BARS", "IRON_DOOR", "IRON_TRAPDOOR",
            "CHAIN", "LANTERN", "SOUL_LANTERN", "END_PORTAL_FRAME", "END_PORTAL", "NETHER_PORTAL",
            "FARMLAND", "DIRT_PATH", "DECORATED_POT", "REDSTONE_WIRE", "REDSTONE_TORCH",
            "REDSTONE_WALL_TORCH", "REPEATER", "COMPARATOR", "DAYLIGHT_DETECTOR", "TRIPWIRE",
            "TRIPWIRE_HOOK", "LEVER", "STONE_BUTTON", "PRISMARINE", "PRISMARINE_BRICKS",
            "DARK_PRISMARINE", "SEA_LANTERN");

    private AltarPlacement() {
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
                int localSurface = reader.highestY(x, z);

                if (baseY + minHeight > localSurface) {
                    return null;
                }

                Probe floor = reader.probeAt(x, baseY - 1, z);

                if (floor == Probe.LIQUID || floor == Probe.ARTIFICIAL) {
                    return null;
                }

                if (floor == Probe.NATURAL_SOLID) {
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

                    if (probe == Probe.ARTIFICIAL || probe == Probe.AIR) {
                        return null;
                    }

                    if (probe == Probe.NATURAL_SOLID) {
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

                    if (probe == Probe.ARTIFICIAL) {
                        return null;
                    }

                    if (probe == Probe.LIQUID) {
                        if (avoidLiquids) {
                            cellHeadroom = -1;
                            break;
                        }

                        continue;
                    }

                    if (probe == Probe.NATURAL_SOLID) {
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

    public static boolean isArtificialName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        if (ARTIFICIAL_NAMES.contains(name)) {
            return true;
        }

        return name.endsWith("_PLANKS")
                || name.endsWith("_LOG")
                || name.endsWith("_WOOD")
                || name.startsWith("STRIPPED_")
                || name.endsWith("_LEAVES")
                || name.endsWith("_SLAB")
                || name.endsWith("_STAIRS")
                || name.endsWith("_WALL")
                || name.endsWith("_FENCE")
                || name.endsWith("_FENCE_GATE")
                || name.endsWith("_DOOR")
                || name.endsWith("_TRAPDOOR")
                || name.endsWith("_BRICKS")
                || name.endsWith("_TILES")
                || name.endsWith("_GLASS")
                || name.endsWith("_GLASS_PANE")
                || name.endsWith("_PANE")
                || name.endsWith("_WOOL")
                || name.endsWith("_CARPET")
                || name.endsWith("_BANNER")
                || name.endsWith("_BED")
                || name.endsWith("_CONCRETE")
                || name.endsWith("_CONCRETE_POWDER")
                || name.endsWith("_TERRACOTTA")
                || name.endsWith("_GLAZED_TERRACOTTA")
                || name.endsWith("_SHULKER_BOX")
                || name.endsWith("_CANDLE")
                || name.endsWith("_CANDLE_CAKE")
                || name.endsWith("_SIGN")
                || name.endsWith("_BUTTON")
                || name.endsWith("_PRESSURE_PLATE")
                || name.endsWith("_BARS")
                || name.endsWith("_PILLAR")
                || name.endsWith("_GRATE")
                || name.endsWith("_BULB")
                || name.startsWith("POLISHED_")
                || name.startsWith("CHISELED_")
                || name.startsWith("CUT_")
                || name.startsWith("SMOOTH_")
                || name.startsWith("WAXED_")
                || name.startsWith("INFESTED_")
                || (name.startsWith("COPPER_") && !name.endsWith("_ORE"))
                || name.startsWith("QUARTZ_")
                || (name.startsWith("RAW_") && name.endsWith("_BLOCK"))
                || (name.endsWith("_BLOCK") && (name.startsWith("IRON")
                        || name.startsWith("GOLD")
                        || name.startsWith("DIAMOND")
                        || name.startsWith("EMERALD")
                        || name.startsWith("LAPIS")
                        || name.startsWith("REDSTONE")
                        || name.startsWith("COAL")
                        || name.startsWith("COPPER")
                        || name.startsWith("NETHERITE")));
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
                    Probe probe = reader.probeAt(baseX + dx, y, baseZ + dz);

                    if (probe == Probe.NATURAL_SOLID || probe == Probe.ARTIFICIAL) {
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
        int surfaceY = reader.highestY(centerX, centerZ);
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

    private static int surfaceMax(BlockReader reader, int centerX, int centerZ) {
        int max = Integer.MIN_VALUE;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                max = Math.max(max, reader.highestY(centerX + dx, centerZ + dz));
            }
        }

        return max == Integer.MIN_VALUE ? reader.highestY(centerX, centerZ) : max;
    }

    private static Site findSurface(BlockReader reader, int centerX, int centerZ, YamlConfiguration config) {
        int baseY = surfaceMax(reader, centerX, centerZ);

        if (!suitableSurface(reader, centerX, baseY, centerZ, config)) {
            return null;
        }

        return new Site(centerX, centerZ, baseY, Kind.SURFACE, Math.max(0, reader.maxY() - baseY));
    }

    public static Site find(World world, int centerX, int centerZ, long hash, YamlConfiguration config) {
        return find(new WorldReader(world), centerX, centerZ, hash, config);
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
            return classify(world.getBlockAt(x, y, z).getType());
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

        private static Probe classify(Material material) {
            if (material == Material.WATER || material == Material.LAVA
                    || material == Material.BUBBLE_COLUMN) {
                return Probe.LIQUID;
            }

            if (material == Material.CAVE_AIR) {
                return Probe.NATURAL_OPEN;
            }

            if (material.isAir()) {
                return Probe.AIR;
            }

            if (isArtificialName(material.name())) {
                return Probe.ARTIFICIAL;
            }

            return material.isSolid() ? Probe.NATURAL_SOLID : Probe.NATURAL_OPEN;
        }
    }
}
