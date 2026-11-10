package com.nightmare.mob;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;
import com.nightmare.tier.MobTier;
import com.nightmare.tier.TierSchedule;

public final class PhaseAssaultService implements Listener {

    private static final long MAX_RAID_LIFETIME_MS = 30L * 60L * 1000L;

    private static final Map<String, Integer> LAST_PHASE = new ConcurrentHashMap<>();
    private static final Map<String, Deque<MobTier>> PENDING = new ConcurrentHashMap<>();
    private static final Map<String, Long> NEXT_PENDING_AT = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RAID_MOBS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RAID_SPAWNED = new ConcurrentHashMap<>();

    private static final Set<EntityType> EXCLUDED_TYPES = EnumSet.of(
            EntityType.WARDEN, EntityType.CREAKING, EntityType.BREEZE);

    private Plugin plugin;

    public void start(Plugin plugin) {
        this.plugin = plugin;
        LAST_PHASE.clear();
        PENDING.clear();
        NEXT_PENDING_AT.clear();
        RAID_MOBS.clear();
        RAID_SPAWNED.clear();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(MobKeys.PHASE_RAID, PersistentDataType.BOOLEAN)) {
                    entity.remove();
                }
            }

            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            MobTier newest = TierSchedule.newestUnlocked(day(world));
            LAST_PHASE.put(world.getName(), newest == null ? -1 : newest.index);
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!LAST_PHASE.containsKey(world.getName())) {
            MobTier newest = TierSchedule.newestUnlocked(day(world));
            LAST_PHASE.put(world.getName(), newest == null ? -1 : newest.index);
        }
    }

    private void tick() {
        cleanupRaidMobs();

        if (!enabled()) {
            return;
        }

        long now = System.currentTimeMillis();

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            long day = day(world);
            MobTier newest = TierSchedule.newestUnlocked(day);

            if (newest == null) {
                continue;
            }

            int last = LAST_PHASE.getOrDefault(world.getName(), newest.index);

            if (newest.index > last) {
                LAST_PHASE.put(world.getName(), newest.index);
                Deque<MobTier> queue = PENDING.computeIfAbsent(world.getName(),
                        key -> new ArrayDeque<>());

                for (MobTier tier : PhaseAssaultSchedule.tiersToQueue(day, last)) {
                    queue.addLast(tier);
                }
            }

            processPending(world, now);
        }
    }

    private void processPending(World world, long now) {
        Deque<MobTier> queue = PENDING.get(world.getName());

        if (queue == null || queue.isEmpty()) {
            return;
        }

        if (now < NEXT_PENDING_AT.getOrDefault(world.getName(), 0L)) {
            return;
        }

        if (!canAssaultNow(world)) {
            return;
        }

        int cap = Math.max(1, Main.balance.getInt("caps.max-monsters", 90));

        if (liveRaidMobs(world) >= cap) {
            return;
        }

        MobTier tier = queue.peekFirst();

        if (tier == null || !trigger(world, tier)) {
            return;
        }

        queue.pollFirst();
        NEXT_PENDING_AT.put(world.getName(),
                now + Math.max(30000L, Math.max(0, Main.balance.getInt("phase-assault.warning-seconds", 60)) * 1000L));

        if (queue.isEmpty()) {
            PENDING.remove(world.getName());
        }
    }

    private boolean canAssaultNow(World world) {
        boolean any = false;

        for (Player player : world.getPlayers()) {
            if (!MobTargetingService.isTargetable(player)) {
                continue;
            }

            any = true;

            if (isOnSurface(player.getLocation()) && !isNight(world)) {
                return false;
            }
        }

        return any;
    }

    private boolean isNight(World world) {
        return PhaseAssaultSchedule.isNightTime(world.getTime());
    }

    private boolean trigger(World world, MobTier tier) {
        int cap = Math.max(1, Main.balance.getInt("caps.max-monsters", 90));

        if (liveRaidMobs(world) >= cap) {
            return false;
        }

        boolean warned = false;

        for (Player player : world.getPlayers()) {
            if (!MobTargetingService.isTargetable(player)) {
                continue;
            }

            warnAndSchedule(player, tier);
            warned = true;
        }

        return warned;
    }

    private void warnAndSchedule(Player player, MobTier tier) {
        int warningSeconds = Math.max(0, Main.balance.getInt("phase-assault.warning-seconds", 60));
        int waves = Math.max(1, Main.balance.getInt("phase-assault.waves", 2));
        int waveDelaySeconds = Math.max(1, Main.balance.getInt("phase-assault.wave-delay-seconds", 45));
        int baseSize = Math.max(1, Main.balance.getInt("phase-assault.horde-base-size", 8));
        int perTier = Math.max(0, Main.balance.getInt("phase-assault.horde-per-tier", 2));
        int maxSize = Math.max(1, Main.balance.getInt("phase-assault.horde-max-size", 30));
        int total = Math.min(maxSize, baseSize + tier.index * perTier);
        World expectedWorld = player.getWorld();

        player.sendTitle(ChatColor.translateAlternateColorCodes('&', com.nightmare.lang.LangService.format(
                        "assault.title", "&4&lPHASE %tier%", "%tier%", tier.name())),
                ChatColor.translateAlternateColorCodes('&', com.nightmare.lang.LangService.format(
                        "assault.subtitle", "&cThe nightmare is coming for you.", "%tier%", tier.name(),
                        "%player%", player.getName())),
                10, 50, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8F, 0.6F);

        for (int wave = 0; wave < waves; wave++) {
            int waveSize = total / waves + (wave < total % waves ? 1 : 0);
            long delay = warningSeconds * 20L + (long) wave * waveDelaySeconds * 20L;

            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnWave(player, tier, waveSize, expectedWorld),
                    delay);
        }
    }

    private void spawnWave(Player player, MobTier tier, int size, World expectedWorld) {
        if (size <= 0 || !player.isOnline() || player.isDead()
                || !MobTargetingService.isTargetable(player)
                || expectedWorld.getEnvironment() != World.Environment.NORMAL
                || !player.getWorld().equals(expectedWorld)) {
            return;
        }

        World world = player.getWorld();
        int cap = Math.max(1, Main.balance.getInt("caps.max-monsters", 90));

        if (liveRaidMobs(world) >= cap) {
            return;
        }

        boolean surface = isOnSurface(player.getLocation());
        Location center = surface ? raidCenter(player) : player.getLocation();
        List<Location> artificial = surface ? collectArtificial(world, center) : List.of();
        Location pack = surface
                ? findSurfacePackCenter(world, center, artificial)
                : findUndergroundPackCenter(world, player);

        if (pack == null) {
            return;
        }

        for (int index = 0; index < size; index++) {
            if (liveRaidMobs(world) >= cap) {
                break;
            }

            Location spawn = surface
                    ? findSurfacePackSpawn(world, pack, artificial)
                    : findUndergroundPackSpawn(world, pack);

            if (spawn == null) {
                continue;
            }

            EntityType type = randomHordeType(tier);
            Class<? extends Entity> entityClass = type.getEntityClass();

            if (entityClass == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<LivingEntity> livingClass = (Class<LivingEntity>) entityClass;
            LivingEntity spawned;

            try {
                spawned = world.spawn(spawn, livingClass, CreatureSpawnEvent.SpawnReason.RAID, false, entity -> {
                    entity.getPersistentDataContainer().set(MobKeys.PHASE_RAID, PersistentDataType.BOOLEAN, true);
                    entity.getPersistentDataContainer().set(MobKeys.PHASE_RAID_TIER, PersistentDataType.STRING, tier.name());
                });
            } catch (RuntimeException exception) {
                continue;
            }

            if (spawned == null || !spawned.isValid()) {
                continue;
            }

            spawned.setPersistent(false);

            if (spawned instanceof Mob mob) {
                mob.setRemoveWhenFarAway(false);

                if (MobTargetingService.isTargetable(player)) {
                    mob.setTarget(player);
                }
            }

            long now = System.currentTimeMillis();
            RAID_MOBS.put(spawned.getUniqueId(), now);
            RAID_SPAWNED.put(spawned.getUniqueId(), now);
        }
    }

    private Location raidCenter(Player player) {
        Location base = baseCenter(player);

        return base == null ? player.getLocation() : base;
    }

    private Location baseCenter(Player player) {
        World world = player.getWorld();
        int radius = Math.max(8, Main.balance.getInt("phase-assault.base-detection-radius",
                Main.balance.getInt("phase-assault.bed-raid-radius", 32)));
        Location respawn = player.getBedSpawnLocation();

        if (respawn != null && respawn.getWorld() != null
                && respawn.getWorld().equals(world)
                && respawn.distanceSquared(player.getLocation()) <= (double) radius * radius
                && world.isChunkLoaded(respawn.getBlockX() >> 4, respawn.getBlockZ() >> 4)
                && isOnSurface(respawn)) {
            return respawn;
        }

        Location best = null;
        double bestDistance = (double) radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -4; dy <= 4; dy++) {
                    Location at = player.getLocation().clone().add(dx, dy, dz);

                    if (!world.isChunkLoaded(at.getBlockX() >> 4, at.getBlockZ() >> 4)) {
                        continue;
                    }

                    if (!isBed(at.getBlock().getType()) || !isOnSurface(at)) {
                        continue;
                    }

                    double distance = at.distanceSquared(player.getLocation());

                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = at;
                    }
                }
            }
        }

        return best;
    }

    private boolean isBed(Material material) {
        return material.name().endsWith("_BED");
    }

    private List<Location> collectArtificial(World world, Location center) {
        int radius = Math.max(8, Main.balance.getInt("phase-assault.artificial-scan-radius", 32));
        int minY = Math.max(world.getMinHeight(), center.getBlockY() - 8);
        int maxY = Math.min(world.getMaxHeight() - 1, center.getBlockY() + 16);
        List<Location> result = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int x = center.getBlockX() + dx;
                int z = center.getBlockZ() + dz;

                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }

                for (int y = minY; y <= maxY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType();

                    if (type.isAir() || block.isLiquid()) {
                        continue;
                    }

                    if (com.nightmare.world.AltarPlacement.isArtificialName(type.name())) {
                        result.add(block.getLocation());
                    }
                }
            }
        }

        return result;
    }

    private Location findSurfacePackCenter(World world, Location center, List<Location> artificial) {
        int minRadius = Math.max(1, Main.balance.getInt("phase-assault.surface-spawn-min-radius",
                Main.balance.getInt("phase-assault.spawn-radius-min", 12)));
        int maxRadius = Math.max(minRadius,
                Main.balance.getInt("phase-assault.surface-spawn-max-radius",
                        Main.balance.getInt("phase-assault.spawn-radius-max", 32)));

        for (int attempt = 0; attempt < 60; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0D;
            double distance = minRadius + ThreadLocalRandom.current().nextDouble() * (maxRadius - minRadius);
            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            Location spawn = safeSurface(world, x, z);

            if (spawn == null || !isDarkEnough(spawn) || !artificialDistanceOk(spawn, artificial)) {
                continue;
            }

            return spawn;
        }

        return null;
    }

    private Location findSurfacePackSpawn(World world, Location pack, List<Location> artificial) {
        int radius = Math.max(1, Main.balance.getInt("phase-assault.pack-radius", 4));

        for (int attempt = 0; attempt < 24; attempt++) {
            int x = pack.getBlockX() + ThreadLocalRandom.current().nextInt(radius * 2 + 1) - radius;
            int z = pack.getBlockZ() + ThreadLocalRandom.current().nextInt(radius * 2 + 1) - radius;

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            Location spawn = safeSurface(world, x, z);

            if (spawn == null || !isDarkEnough(spawn) || !artificialDistanceOk(spawn, artificial)) {
                continue;
            }

            return spawn;
        }

        return null;
    }

    private Location findUndergroundPackCenter(World world, Player player) {
        int minRadius = Math.max(1, Main.balance.getInt("phase-assault.underground-spawn-min-radius", 6));
        int maxRadius = Math.max(minRadius,
                Main.balance.getInt("phase-assault.underground-spawn-max-radius", 16));
        Location origin = player.getLocation();

        for (int attempt = 0; attempt < 60; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0D;
            double distance = minRadius + ThreadLocalRandom.current().nextDouble() * (maxRadius - minRadius);
            int x = origin.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = origin.getBlockY() + ThreadLocalRandom.current().nextInt(13) - 6;

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            Location spawn = safeUnderground(world, x, y, z);

            if (spawn != null && isDarkEnough(spawn)) {
                return spawn;
            }
        }

        return null;
    }

    private Location findUndergroundPackSpawn(World world, Location pack) {
        int radius = Math.max(1, Main.balance.getInt("phase-assault.pack-radius", 4));

        for (int attempt = 0; attempt < 24; attempt++) {
            int x = pack.getBlockX() + ThreadLocalRandom.current().nextInt(radius * 2 + 1) - radius;
            int z = pack.getBlockZ() + ThreadLocalRandom.current().nextInt(radius * 2 + 1) - radius;
            int y = pack.getBlockY() + ThreadLocalRandom.current().nextInt(5) - 2;

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            Location spawn = safeUnderground(world, x, y, z);

            if (spawn != null && isDarkEnough(spawn)) {
                return spawn;
            }
        }

        return null;
    }

    private Location safeSurface(World world, int x, int z) {
        int top = world.getHighestBlockYAt(x, z);

        for (int y = top; y >= Math.max(world.getMinHeight() + 1, top - 8); y--) {
            Block ground = world.getBlockAt(x, y, z);

            if (!ground.getType().isSolid() || ground.isLiquid()) {
                continue;
            }

            if (hasClearance(world, x, y + 1, z)) {
                return new Location(world, x + 0.5D, y + 1.0D, z + 0.5D);
            }
        }

        return null;
    }

    private Location safeUnderground(World world, int x, int y, int z) {
        if (y <= world.getMinHeight() || y + 3 >= world.getMaxHeight()) {
            return null;
        }

        if (world.getHighestBlockYAt(x, z) <= y + 3) {
            return null;
        }

        Block ground = world.getBlockAt(x, y - 1, z);

        if (!ground.getType().isSolid() || ground.isLiquid() || !hasClearance(world, x, y, z)) {
            return null;
        }

        return new Location(world, x + 0.5D, y, z + 0.5D);
    }

    private boolean hasClearance(World world, int x, int y, int z) {
        return isOpen(world.getBlockAt(x, y, z))
                && isOpen(world.getBlockAt(x, y + 1, z))
                && isOpen(world.getBlockAt(x, y + 2, z));
    }

    private boolean isOpen(Block block) {
        return !block.getType().isSolid() && !block.isLiquid();
    }

    private boolean isDarkEnough(Location location) {
        int global = Main.balance == null ? 7 : Main.balance.getInt("mob-spawn.max-light-level", 7);
        int phase = Main.balance == null ? global
                : Main.balance.getInt("phase-assault.max-light-level", global);
        int max = Math.min(global, phase);
        Block feet = location.getBlock();
        Block head = location.getWorld().getBlockAt(
                location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());

        return feet.getLightLevel() <= max && head.getLightLevel() <= max;
    }

    private boolean artificialDistanceOk(Location candidate, List<Location> artificial) {
        if (artificial.isEmpty()) {
            return true;
        }

        double min = Math.max(0, Main.balance.getInt("phase-assault.artificial-spawn-min-distance", 10));
        double max = Math.max(min, Main.balance.getInt("phase-assault.artificial-spawn-max-distance", 20));
        double nearest = Double.MAX_VALUE;

        for (Location block : artificial) {
            double dx = block.getBlockX() + 0.5D - candidate.getX();
            double dy = block.getBlockY() + 0.5D - candidate.getY();
            double dz = block.getBlockZ() + 0.5D - candidate.getZ();
            nearest = Math.min(nearest, dx * dx + dy * dy + dz * dz);
        }

        return nearest >= min * min && nearest <= max * max;
    }

    private EntityType randomHordeType(MobTier tier) {
        List<EntityType> types = new ArrayList<>();

        for (Map.Entry<EntityType, MobProfile> entry : MobProfileLoader.PROFILES.entrySet()) {
            MobProfile profile = entry.getValue();

            if (profile.category != MobCategory.HOSTILE || profile.maxTierIndex < tier.index
                    || EXCLUDED_TYPES.contains(entry.getKey())) {
                continue;
            }

            types.add(entry.getKey());
        }

        return types.isEmpty() ? EntityType.ZOMBIE
                : types.get(ThreadLocalRandom.current().nextInt(types.size()));
    }

    private boolean isOnSurface(Location location) {
        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        int tolerance = Math.max(0, Main.balance.getInt("phase-assault.surface-depth-tolerance", 6));

        return location.getY() >= world.getHighestBlockYAt(location) - tolerance;
    }

    private int liveRaidMobs(World world) {
        int count = 0;

        for (UUID uuid : new ArrayList<>(RAID_MOBS.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);

            if (entity instanceof LivingEntity living && entity.isValid() && !entity.isDead()
                    && world.equals(living.getWorld())) {
                count++;
            }
        }

        return count;
    }

    private void cleanupRaidMobs() {
        long now = System.currentTimeMillis();
        long despawnAfter = Math.max(1000L, Main.balance.getLong("phase-assault.despawn-after-ms", 300000L));
        int radius = Math.max(16, Main.balance.getInt("phase-assault.despawn-radius", 64));

        for (UUID uuid : new ArrayList<>(RAID_MOBS.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);

            if (!(entity instanceof LivingEntity living) || entity.isDead() || !entity.isValid()) {
                RAID_MOBS.remove(uuid);
                RAID_SPAWNED.remove(uuid);
                continue;
            }

            long spawnedAt = RAID_SPAWNED.getOrDefault(uuid, now);

            if (now - spawnedAt >= MAX_RAID_LIFETIME_MS) {
                living.remove();
                RAID_MOBS.remove(uuid);
                RAID_SPAWNED.remove(uuid);
                continue;
            }

            boolean nearPlayer = false;

            for (Player player : living.getWorld().getPlayers()) {
                if (MobTargetingService.isTargetable(player)
                        && player.getLocation().distanceSquared(living.getLocation()) <= radius * radius) {
                    nearPlayer = true;
                    break;
                }
            }

            if (nearPlayer) {
                RAID_MOBS.put(uuid, now);
                continue;
            }

            if (now - RAID_MOBS.getOrDefault(uuid, now) >= despawnAfter) {
                entity.remove();
                RAID_MOBS.remove(uuid);
                RAID_SPAWNED.remove(uuid);
            }
        }
    }

    private static boolean enabled() {
        return Main.balance == null || Main.balance.getBoolean("phase-assault.enabled", true);
    }

    private static long day(World world) {
        return world.getFullTime() / 24000L;
    }

    public static void cleanup() {
        for (UUID uuid : new ArrayList<>(RAID_MOBS.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);

            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }

        RAID_MOBS.clear();
        RAID_SPAWNED.clear();
        LAST_PHASE.clear();
        PENDING.clear();
        NEXT_PENDING_AT.clear();
    }

}
