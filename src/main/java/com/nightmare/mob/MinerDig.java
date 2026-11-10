package com.nightmare.mob;

/**
 * Pure geometry helper for the miner zombie. Traces the straight path toward
 * a target and returns the first blocking block, so it can be unit-tested
 * without a running server.
 */
public final class MinerDig {

    public record BlockPos(int x, int y, int z) {
    }

    @FunctionalInterface
    public interface Sampler {
        boolean blocked(int x, int y, int z);
    }

    private MinerDig() {
    }

    public static BlockPos find(double fromX, double fromY, double fromZ,
            double toX, double toY, double toZ, double maxDistance, Sampler sampler) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 0.01D || maxDistance <= 0.0D) {
            return null;
        }

        double limit = Math.min(length, maxDistance);
        int lastX = Integer.MIN_VALUE;
        int lastY = Integer.MIN_VALUE;
        int lastZ = Integer.MIN_VALUE;

        for (double travelled = 0.75D; travelled <= limit - 0.25D; travelled += 0.25D) {
            double ratio = travelled / length;
            int x = (int) Math.floor(fromX + dx * ratio);
            int y = (int) Math.floor(fromY + dy * ratio);
            int z = (int) Math.floor(fromZ + dz * ratio);

            if (x == lastX && y == lastY && z == lastZ) {
                continue;
            }

            lastX = x;
            lastY = y;
            lastZ = z;

            if (sampler.blocked(x, y, z)) {
                return new BlockPos(x, y, z);
            }
        }

        return null;
    }
}
