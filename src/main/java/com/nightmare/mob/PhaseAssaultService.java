package com.nightmare.mob;

import java.util.ArrayList;
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
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;
import com.nightmare.tier.MobTier;
import com.nightmare.tier.TierSchedule;

public final class PhaseAssaultService {

    private static final Map<String, Integer> LAST_PHASE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RAID_MOBS = new ConcurrentHashMap<>();
    private static final Set<EntityType> EXCLUDED_TYPES = EnumSet.of(
            EntityType.WARDEN, EntityType.CREAKING, EntityType.BREEZE);

    private Plugin plugin;

    public void start(Plugin plugin) {
        this.plugin = plugin;
        LAST_PHASE.clear();
        RAID_MOBS.clear();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(MobKeys.PHASE_RAID, PersistentDataType.BOOLEAN)) {
                    entity.remove();
                }
            }

            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            MobTier newest = TierSchedule.newestUnlocked(world.getFullTime() / 24000L);

            if (newest != null) {
                LAST_PHASE.put(world.getName(), newest.index);
            }
        }

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (!Main.balance.getBoolean("phase-assault.enabled", true)) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            MobTier newest = TierSchedule.newestUnlocked(world.getFullTime() / 24000L);

            if (newest == null) {
                continue;
            }

            int last = LAST_PHASE.getOrDefault(world.getName(), newest.index);

            if (newest.index > last) {
                LAST_PHASE.put(world.getName(), newest.index);

                trigger(world, newest);
            } else if (newest.index < last) {
                LAST_PHASE.put(world.getName(), newest.index);
            }
        }

        cleanupRaidMobs();
    }

    private void trigger(World world, MobTier tier) {
        int cap = Math.max(1, Main.balance.getInt("caps.max-monsters", 300));

        if (liveRaidMobs(world) >= cap) {
            return;
        }

        for (Player player : world.getPlayers()) {
            if (!MobTargetingService.isTargetable(player) || !isOnSurface(player.getLocation())) {
                continue;
            }

            warnAndSchedule(player, tier);
        }
    }

    private void warnAndSchedule(Player player, MobTier tier) {
        int warningSeconds = Math.max(0, Main.balance.getInt("phase-assault.warning-seconds", 60));
        int waves = Math.max(1, Main.balance.getInt("phase-assault.waves", 2));
        int waveDelaySeconds = Math.max(1, Main.balance.getInt("phase-assault.wave-delay-seconds", 45));
        int baseSize = Math.max(1, Main.balance.getInt("phase-assault.horde-base-size", 8));
        int perTier = Math.max(0, Main.balance.getInt("phase-assault.horde-per-tier", 2));
        int maxSize = Math.max(1, Main.balance.getInt("phase-assault.horde-max-size", 30));
        int total = Math.min(maxSize, baseSize + tier.index * perTier);

        player.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "PHASE " + tier.name(),
                ChatColor.RED + com.nightmare.lang.LangService.msg("assault.subtitle",
                        "The nightmare is coming for you."), 10, 50, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8F, 0.6F);

        for (int wave = 0; wave < waves; wave++) {
            int waveSize = total / waves + (wave < total % waves ? 1 : 0);
            long delay = warningSeconds * 20L + (long) wave * waveDelaySeconds * 20L;

            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnWave(player, tier, waveSize), delay);
        }
    }

    private void spawnWave(Player player, MobTier tier, int size) {
        if (size <= 0 || !player.isOnline() || !MobTargetingService.isTargetable(player)
                || !isOnSurface(player.getLocation())) {
            return;
        }

        World playerWorld = player.getWorld();
        int cap = Math.max(1, Main.balance.getInt("caps.max-monsters", 300));

        if (liveRaidMobs(playerWorld) >= cap) {
            return;
        }

        Location center = raidCenter(player);

        if (center == null) {
            center = player.getLocation();
        }

        World world = center.getWorld();

        if (world == null) {
            return;
        }

        int minRadius = Main.balance.getInt("phase-assault.spawn-radius-min", 24);
        int maxRadius = Math.max(minRadius, Main.balance.getInt("phase-assault.spawn-radius-max", 40));

        for (int index = 0; index < size; index++) {
            Location spawn = findSpawn(world, center, minRadius, maxRadius);

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

            spawned.setPersistent(true);

            if (spawned instanceof Mob mob) {
                mob.setRemoveWhenFarAway(false);

                if (MobTargetingService.isTargetable(player)) {
                    mob.setTarget(player);
                }
            }

            RAID_MOBS.put(spawned.getUniqueId(), System.currentTimeMillis());
        }
    }

    private Location raidCenter(Player player) {
        Location respawn = player.getRespawnLocation();
        int radius = Main.balance.getInt("phase-assault.bed-raid-radius", 50);

        if (respawn != null && respawn.getWorld() != null
                && respawn.getWorld().equals(player.getWorld())
                && respawn.distanceSquared(player.getLocation()) <= radius * radius
                && respawn.getWorld().isChunkLoaded(respawn.getBlockX() >> 4, respawn.getBlockZ() >> 4)
                && isOnSurface(respawn)) {
            return respawn;
        }

        return player.getLocation();
    }

    private Location findSpawn(World world, Location center, int minRadius, int maxRadius) {
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
            double distance = minRadius + ThreadLocalRandom.current().nextDouble() * (maxRadius - minRadius);
            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            int y = world.getHighestBlockYAt(x, z);

            Block ground = world.getBlockAt(x, y, z);

            if (!ground.getType().isSolid() || ground.isLiquid()) {
                continue;
            }

            Location spawn = new Location(world, x + 0.5, y + 1.0, z + 0.5);

            if (!spawn.getBlock().getType().isAir() || !spawn.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                continue;
            }

            return spawn;
        }

        return null;
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
            }
        }
    }

    public static void cleanup() {
        for (UUID uuid : new ArrayList<>(RAID_MOBS.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);

            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }

        RAID_MOBS.clear();
        LAST_PHASE.clear();
    }

}
