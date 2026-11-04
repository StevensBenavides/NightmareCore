package com.nightmare.world;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public final class StructureDesigns {

    private StructureDesigns() {
    }

    private static org.bukkit.configuration.file.YamlConfiguration balance() {
        return com.nightmare.Main.balance == null
                ? new org.bukkit.configuration.file.YamlConfiguration()
                : com.nightmare.Main.balance;
    }

    public record Placed(String world, int x, int y, int z) {
        String key() {
            return world + ":" + x + "," + y + "," + z;
        }
    }

    public record BuildResult(List<Placed> blocks, List<Location> chests, Location lectern, Location center) {
    }

    private static final Set<Material> REPLACEABLE = EnumSet.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.SNOW, Material.VINE, Material.GLOW_LICHEN,
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
            Material.DEAD_BUSH, Material.SUGAR_CANE, Material.KELP, Material.SEAGRASS, Material.TALL_SEAGRASS);

    public interface Recorder {
        void placed(Block block);
    }

    public static BuildResult build(ThemeLayout theme, World world, int baseX, int baseY, int baseZ, long seed,
            int rotation) {
        Random random = new Random(seed);
        List<Placed> placed = new ArrayList<>();
        List<Location> chests = new ArrayList<>();
        Location[] lectern = new Location[1];

        int shape = (int) ((seed >>> 20) & 3);
        int roof = (int) ((seed >>> 22) & 3);
        int floorMode = (int) ((seed >>> 24) % 3);
        int lootRoll = random.nextInt(100);

        java.util.Set<String> footprint = new java.util.HashSet<>();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean in;
                switch (shape) {
                    case 1 -> in = dx * dx + dz * dz <= 20;
                    case 2 -> in = (Math.abs(dx) <= 4 && Math.abs(dz) <= 1)
                            || (Math.abs(dz) <= 4 && Math.abs(dx) <= 1);
                    case 3 -> in = !(dx > 0 && dz > 0);
                    default -> in = true;
                }
                if (in) {
                    footprint.add(dx + "," + dz);
                }
            }
        }

        for (String cell : footprint) {
            String[] parts = cell.split(",");

            int dx = Integer.parseInt(parts[0]);
            int dz = Integer.parseInt(parts[1]);
            int surface = world.getHighestBlockYAt(baseX + dx, baseZ + dz);

            if (baseY - surface > 8) {
                continue;
            }

            for (int y = surface + 1; y < baseY; y++) {
                set(world, baseX + dx, y, baseZ + dz, theme.pillar().apply(random), placed);
            }
        }

        double floorKeep = Math.max(0.85, Math.min(1.0, theme.floorKeep));

        for (String cell : footprint) {
            String[] parts = cell.split(",");

            int dx = Integer.parseInt(parts[0]);
            int dz = Integer.parseInt(parts[1]);

            if (floorMode == 1 && random.nextDouble() < 0.30) {
                continue;
            }

            if (floorMode == 2 && random.nextDouble() < 0.15) {
                continue;
            }

            int[] r = rotate(dx, dz, rotation);

            Material floor = theme.floor().apply(random, Math.abs(dx) + Math.abs(dz));

            if (floor != null && random.nextDouble() < floorKeep) {
                set(world, baseX + r[0], baseY, baseZ + r[1], floor, placed);
            }
        }

        List<int[]> pillars = new ArrayList<>();

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                int bestDx = 0;
                int bestDz = 0;
                int bestScore = Integer.MIN_VALUE;

                for (String cell : footprint) {
                    String[] parts = cell.split(",");

                    int dx = Integer.parseInt(parts[0]);
                    int dz = Integer.parseInt(parts[1]);
                    int score = sx * dx + sz * dz;

                    if (score > bestScore) {
                        bestScore = score;
                        bestDx = dx;
                        bestDz = dz;
                    }
                }

                pillars.add(new int[] { bestDx, bestDz });
            }
        }

        int pillarHeight = roof == 3 ? 5 : 2 + random.nextInt(3);
        double ruin = Math.min(0.20, Math.max(0.0, theme.ruinChance));

        for (int[] corner : pillars) {
            int[] r = rotate(corner[0], corner[1], rotation);
            int height = roof == 3 ? pillarHeight : 2 + random.nextInt(3);

            for (int y = 1; y <= height; y++) {
                if (y > 2 && random.nextDouble() < ruin) {
                    continue;
                }
                set(world, baseX + r[0], baseY + y, baseZ + r[1], theme.pillar().apply(random), placed);
            }
        }

        if (roof == 2 && pillars.size() == 4 && shape == 0) {
            int[][] pairs = { { 0, 1 }, { 2, 3 } };

            for (int[] pair : pairs) {
                int[] a = rotate(pillars.get(pair[0])[0], pillars.get(pair[0])[1], rotation);
                int[] b = rotate(pillars.get(pair[1])[0], pillars.get(pair[1])[1], rotation);
                int beamY = Math.min(pillarHeight, 4);
                int steps = Math.max(Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1]));

                for (int i = 1; i < steps; i++) {
                    int bx = a[0] + (b[0] - a[0]) * i / steps;
                    int bz = a[1] + (b[1] - a[1]) * i / steps;
                    if (random.nextDouble() < ruin) {
                        continue;
                    }
                    set(world, baseX + bx, baseY + beamY, baseZ + bz, theme.pillar().apply(random), placed);
                }

            }
        }

        if (roof == 3) {
            for (String cell : footprint) {
                String[] parts = cell.split(",");

                int dx = Integer.parseInt(parts[0]);
                int dz = Integer.parseInt(parts[1]);

                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    continue;
                }

                if (random.nextDouble() < ruin) {
                    continue;
                }
                int[] r = rotate(dx, dz, rotation);

                set(world, baseX + r[0], baseY + 6, baseZ + r[1], theme.pillar().apply(random), placed);
            }
        }

        set(world, baseX, baseY + 1, baseZ, theme.pedestal(), placed);
        set(world, baseX, baseY + 2, baseZ, theme.centerpiece(), placed);

        for (int[] extra : theme.extras()) {
            int[] r = rotate(extra[0], extra[1], rotation);

            Material extraMaterial = Material.values()[extra[3]];

            if (!extraMaterial.isBlock()) {
                continue;
            }
            Block extraBlock = world.getBlockAt(baseX + r[0], baseY + extra[2], baseZ + r[1]);

            if (canReplace(extraBlock)) {
                extraBlock.setType(extraMaterial, false);
                placed.add(new Placed(world.getName(), extraBlock.getX(), extraBlock.getY(), extraBlock.getZ()));
            }
        }

        int[] lecternCell = nearestCell(footprint, 2, 0);

        if (lecternCell != null) {
            int[] lecternRel = rotate(lecternCell[0], lecternCell[1], rotation);
            Block lecternBlock = world.getBlockAt(baseX + lecternRel[0], baseY + 1, baseZ + lecternRel[1]);

            if (canReplace(lecternBlock)) {
                lecternBlock.setType(Material.LECTERN, false);
                placed.add(new Placed(world.getName(), lecternBlock.getX(), lecternBlock.getY(), lecternBlock.getZ()));

                if (lecternBlock.getBlockData() instanceof Directional directional) {
                    directional.setFacing(faceToward(lecternRel[0], lecternRel[1]));
                    lecternBlock.setBlockData(directional, false);
                }

                lectern[0] = lecternBlock.getLocation();
            }
        }

        int oneChance = Math.max(0, balance().getInt("altars.chest-one-chance", 40));
        int twoChance = Math.max(0, balance().getInt("altars.chest-two-chance", 30));
        int chestCount = lootRoll < oneChance ? 1 : lootRoll < oneChance + twoChance ? 2 : 0;
        int[][] chestSpots = { { -2, 0 }, { 2, 0 } };

        for (int i = 0; i < chestCount; i++) {
            int[] chestCell = nearestCell(footprint, chestSpots[i][0], chestSpots[i][1]);

            if (chestCell == null) {
                continue;
            }

            int[] chestRel = rotate(chestCell[0], chestCell[1], rotation);
            Block chestBlock = world.getBlockAt(baseX + chestRel[0], baseY + 1, baseZ + chestRel[1]);

            if (canReplace(chestBlock)) {
                chestBlock.setType(Material.CHEST, false);
                placed.add(new Placed(world.getName(), chestBlock.getX(), chestBlock.getY(), chestBlock.getZ()));

                if (chestBlock.getBlockData() instanceof Directional directional) {
                    directional.setFacing(faceToward(chestRel[0], chestRel[1]));
                    chestBlock.setBlockData(directional, false);
                }

                chests.add(chestBlock.getLocation());
            }
        }

        return new BuildResult(placed, chests, lectern[0], new Location(world, baseX + 0.5, baseY, baseZ + 0.5));
    }

    private static int[] nearestCell(java.util.Set<String> footprint, int tx, int tz) {
        int[] best = null;
        int bestDist = Integer.MAX_VALUE;

        for (String cell : footprint) {
            String[] parts = cell.split(",");

            int dx = Integer.parseInt(parts[0]);
            int dz = Integer.parseInt(parts[1]);
            int dist = Math.abs(dx - tx) + Math.abs(dz - tz);

            if (dist < bestDist) {
                bestDist = dist;
                best = new int[] { dx, dz };
            }
        }

        return best;
    }

    private static int[] rotate(int dx, int dz, int rotation) {
        return switch ((rotation & 3)) {
            case 1 -> new int[] { -dz, dx };
            case 2 -> new int[] { -dx, -dz };
            case 3 -> new int[] { dz, -dx };
            default -> new int[] { dx, dz };
        };
    }

    private static BlockFace faceToward(int dx, int dz) {
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx > 0 ? BlockFace.WEST : BlockFace.EAST;
        }
        return dz > 0 ? BlockFace.NORTH : BlockFace.SOUTH;
    }

    private static boolean canReplace(Block block) {
        return REPLACEABLE.contains(block.getType());
    }

    private static void set(World world, int x, int y, int z, Material material, List<Placed> placed) {
        if (material == null || !material.isBlock()) {
            return;
        }
        Block block = world.getBlockAt(x, y, z);

        if (!canReplace(block)) {
            return;
        }

        block.setType(material, false);
        placed.add(new Placed(world.getName(), x, y, z));
    }

    public record ThemeLayout(float ruinChance, float floorKeep,
            java.util.function.BiFunction<Random, Integer, Material> floor,
            java.util.function.Function<Random, Material> pillar,
            Material pedestal,
            Material centerpiece,
            List<int[]> extras) {
    }

    public static ThemeLayout layout(StructureTheme theme) {
        return switch (theme) {
            case FORGE -> new ThemeLayout(0.30f, 0.95f,
                    (random, dist) -> pick(random, Material.STONE_BRICKS, Material.STONE_BRICKS,
                            Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.COPPER_BLOCK),
                    random -> pick(random, Material.STONE_BRICKS, Material.COBBLESTONE),
                    Material.ANVIL,
                    Material.FURNACE,
                    List.of(new int[] { -3, -3, 0, Material.FURNACE.ordinal() },
                            new int[] { 3, 3, 0, Material.MAGMA_BLOCK.ordinal() }));
            case SOUL -> new ThemeLayout(0.15f, 1.0f,
                    (random, dist) -> pick(random, Material.QUARTZ_BLOCK, Material.QUARTZ_BLOCK,
                            Material.SMOOTH_QUARTZ, Material.WHITE_WOOL),
                    random -> Material.QUARTZ_PILLAR,
                    Material.QUARTZ_BLOCK,
                    Material.SOUL_LANTERN,
                    List.of(new int[] { -3, 0, 1, Material.SOUL_LANTERN.ordinal() },
                            new int[] { 3, 0, 1, Material.SOUL_LANTERN.ordinal() }));
            case ABYSS -> new ThemeLayout(0.25f, 0.95f,
                    (random, dist) -> pick(random, Material.DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES,
                            Material.CRACKED_DEEPSLATE_BRICKS, Material.SCULK),
                    random -> pick(random, Material.OBSIDIAN, Material.CRYING_OBSIDIAN),
                    Material.DEEPSLATE_BRICKS,
                    Material.OBSIDIAN,
                    List.of(new int[] { 0, -3, 0, Material.WATER_CAULDRON.ordinal() }));
            case NIGHTMARE -> new ThemeLayout(0.35f, 0.9f,
                    (random, dist) -> pick(random, Material.BLACKSTONE, Material.POLISHED_BLACKSTONE,
                            Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, Material.NETHERRACK),
                    random -> pick(random, Material.BLACKSTONE, Material.POLISHED_BLACKSTONE_BRICKS),
                    Material.POLISHED_BLACKSTONE,
                    Material.WITHER_SKELETON_SKULL,
                    List.of(new int[] { -3, 2, 0, Material.NETHERRACK.ordinal() },
                            new int[] { -3, 2, 1, Material.WITHER_ROSE.ordinal() },
                            new int[] { 3, -2, 0, Material.NETHERRACK.ordinal() },
                            new int[] { 3, -2, 1, Material.WITHER_ROSE.ordinal() }));
            case RELICS -> new ThemeLayout(0.30f, 0.9f,
                    (random, dist) -> pick(random, Material.OAK_PLANKS, Material.OAK_PLANKS,
                            Material.BOOKSHELF, Material.CHISELED_BOOKSHELF),
                    random -> pick(random, Material.BOOKSHELF, Material.OAK_LOG),
                    Material.OAK_LOG,
                    Material.CHISELED_BOOKSHELF,
                    List.of(new int[] { -3, -3, 0, Material.CHISELED_BOOKSHELF.ordinal() },
                            new int[] { 3, 3, 0, Material.ENCHANTING_TABLE.ordinal() }));
        };
    }

    private static Material pick(Random random, Material... options) {
        return options[random.nextInt(options.length)];
    }
}
