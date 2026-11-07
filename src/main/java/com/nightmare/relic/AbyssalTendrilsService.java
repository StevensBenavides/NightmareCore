package com.nightmare.relic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.nightmare.Main;

public final class AbyssalTendrilsService implements Listener {

    private static final int LOCKED_SLOT = 17;
    private static final java.util.Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final java.util.Map<UUID, Long> PENDING_FALL = new ConcurrentHashMap<>();
    private static final java.util.Set<Block> TEMP_STALAGMITES = ConcurrentHashMap.newKeySet();
    private static final java.util.Set<BlockDisplay> TEMP_DISPLAYS = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() || !isSprinting(player)) {
            return;
        }

        if (!AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(LOCKED_SLOT))) {
            ensureBound(player);
        }

        if (!AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(LOCKED_SLOT))) {
            return;
        }

        long now = System.currentTimeMillis();

        long remainingCooldown = COOLDOWNS.getOrDefault(player.getUniqueId(), 0L);

        if (remainingCooldown > now) {
            event.setCancelled(true);
            sendCooldownDenial(player, remainingCooldown - now);

            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 0.8F);

            return;
        }

        if (isFallingCast(player)) {
            event.setCancelled(true);
            PENDING_FALL.put(player.getUniqueId(), now + 15000L);
            TendrilsEffects.cast(player);

            spawnBlackAura(player);
            player.playSound(player.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.6F, 0.7F);

            return;
        }

        int range = Main.balance.getInt("abyssal-tendrils.range", 50);
        Block target = player.getTargetBlockExact(range);

        if (target == null || target.getType() == Material.AIR || !target.getType().isSolid()) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 0.8F);

            return;
        }

        event.setCancelled(true);
        COOLDOWNS.put(player.getUniqueId(), now + Main.balance.getLong("abyssal-tendrils.cooldown-ms", 60000L));

        TendrilsEffects.cast(player);
        spawnBlackAura(player);

        Location center = target.getLocation().add(0.5, 0.0, 0.5);

        castPatch(player, center, Main.balance.getDouble("abyssal-tendrils.damage", 75.0), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Long expiry = PENDING_FALL.get(player.getUniqueId());

        if (expiry == null) {
            return;
        }

        if (expiry <= System.currentTimeMillis()) {
            PENDING_FALL.remove(player.getUniqueId());
            return;
        }

        Material feet = player.getLocation().getBlock().getType();

        if (!player.isOnGround() && !player.isInWater() && feet != Material.LAVA) {
            return;
        }

        PENDING_FALL.remove(player.getUniqueId());
        COOLDOWNS.put(player.getUniqueId(),
                System.currentTimeMillis() + Main.balance.getLong("abyssal-tendrils.cooldown-ms", 60000L));

        Location center = player.getLocation().clone();

        castPatch(player, center, Main.balance.getDouble("abyssal-tendrils.falling-damage", 100.0), true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        PENDING_FALL.remove(event.getEntity().getUniqueId());
        RelicInputs.clear(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        RelicInputs.clear(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> ensureBound(event.getPlayer()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        RelicInputs.clear(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> ensureBound(event.getPlayer()));
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        RelicInputs.recordSprint(event.getPlayer().getUniqueId(), event.getInput().isSprint());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PENDING_FALL.remove(event.getPlayer().getUniqueId());
        RelicInputs.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        RelicInputs.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        RelicInputs.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        PENDING_FALL.remove(event.getPlayer().getUniqueId());
        RelicInputs.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (AbyssalTendrilsItem.isTendrils(event.getItemDrop().getItemStack())) {
            resetCooldowns(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (AbyssalTendrilsItem.isTendrils(event.getItem().getItemStack())) {
            resetCooldowns(player.getUniqueId());
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> ensureBound(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == player.getInventory()
                && event.getSlot() == LOCKED_SLOT
                && AbyssalTendrilsItem.isTendrils(event.getCurrentItem())) {
            resetCooldowns(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !AbyssalTendrilsItem.isTendrils(event.getOldCursor())) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= topSize && rawSlot - topSize == LOCKED_SLOT) {
                resetCooldowns(player.getUniqueId());
                return;
            }
        }
    }

    public static void tick() {
        long now = System.currentTimeMillis();

        for (UUID uuid : new ArrayList<>(PENDING_FALL.keySet())) {
            Long expiry = PENDING_FALL.get(uuid);

            if (expiry != null && expiry <= now) {
                PENDING_FALL.remove(uuid);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ensureBound(player);
        }
    }

    public static boolean hasTendrilsInInventory(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(slot))) {
                return true;
            }
        }

        return AbyssalTendrilsItem.isTendrils(player.getItemOnCursor());
    }

    public static boolean remove(Player player) {
        ItemStack item = player.getInventory().getItem(LOCKED_SLOT);

        if (!AbyssalTendrilsItem.isTendrils(item)) {
            return false;
        }

        player.getInventory().setItem(LOCKED_SLOT, null);
        PENDING_FALL.remove(player.getUniqueId());
        resetCooldowns(player.getUniqueId());
        player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.updateInventory();

        return true;
    }

    public static void give(Player player) {
        if (AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(LOCKED_SLOT))) {
            return;
        }

        ItemStack item = AbyssalTendrilsItem.create();

        ensureBound(player);

        if (!AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(LOCKED_SLOT))) {
            place(player, item);
        }
    }

    public static void ensureBound(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack slotItem = inventory.getItem(LOCKED_SLOT);

        if (AbyssalTendrilsItem.isTendrils(slotItem)) {
            AbyssalTendrilsItem.refreshLore(slotItem);
            return;
        }

        ItemStack cursor = player.getItemOnCursor();

        if (AbyssalTendrilsItem.isTendrils(cursor)) {
            player.setItemOnCursor(null);
            place(player, cursor);

            return;
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot == LOCKED_SLOT) {
                continue;
            }

            ItemStack item = inventory.getItem(slot);

            if (AbyssalTendrilsItem.isTendrils(item)) {
                inventory.setItem(slot, null);
                place(player, item);

                return;
            }
        }
    }

    private static void place(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        ItemStack occupant = inventory.getItem(LOCKED_SLOT);

        if (occupant != null && !occupant.getType().isAir()) {
            if (!inventory.addItem(occupant).isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), occupant);
            }
        }

        inventory.setItem(LOCKED_SLOT, item);
        AbyssalTendrilsItem.refreshLore(item);

        player.updateInventory();
    }

    private static boolean isSprinting(Player player) {
        return RelicInputs.isSprinting(player);
    }

    private static boolean isFallingCast(Player player) {
        Location location = player.getLocation();

        if (location.getPitch() < 60.0F || player.getVelocity().getY() > -0.1) {
            return false;
        }

        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        return location.getY() - world.getHighestBlockYAt(location) >= 10.0;
    }

    private static void castPatch(Player player, Location center, double damage, boolean falling) {
        World world = center.getWorld();

        if (world == null) {
            return;
        }

        double radius = Main.balance.getDouble("abyssal-tendrils.patch-radius", 7.5);
        int maxStalagmites = Main.balance.getInt("abyssal-tendrils.stalagmite-count", 40);
        int minHeight = Math.max(1, Main.balance.getInt("abyssal-tendrils.stalagmite-min-height", 1));
        int maxHeight = Math.max(minHeight, Main.balance.getInt("abyssal-tendrils.stalagmite-max-height", 3));
        int safeSize = Math.max(0, Main.balance.getInt("abyssal-tendrils.safe-zone", 4));
        int duration = Math.max(20, Main.balance.getInt("abyssal-tendrils.duration-ticks", 40));
        int blockRadius = (int) Math.ceil(radius);

        List<Location> grounds = new ArrayList<>();

        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int z = -blockRadius; z <= blockRadius; z++) {
                if (x * x + z * z > radius * radius) {
                    continue;
                }

                if (falling && inSafeZone(x, z, safeSize)) {
                    continue;
                }

                int worldX = center.getBlockX() + x;
                int worldZ = center.getBlockZ() + z;
                int groundY = groundY(world, worldX, worldZ);

                if (groundY == Integer.MIN_VALUE) {
                    continue;
                }

                grounds.add(new Location(world, worldX, groundY, worldZ));
            }
        }

        Collections.shuffle(grounds);
        List<Block> stalagmites = new ArrayList<>();

        int count = Math.min(maxStalagmites, grounds.size());

        for (int index = 0; index < count; index++) {
            Location ground = grounds.get(index);
            createStalagmite(world, ground.getBlockX(), ground.getBlockY(), ground.getBlockZ(), minHeight, maxHeight, stalagmites);
        }

        List<BlockDisplay> displays = spawnFlyingBlocks(world, center, radius, falling);

        damagePatch(world, center, radius, damage, player, falling, safeSize);
        spawnBlackImpact(world, center, radius, falling);

        TendrilsEffects.impact(center, radius, falling);

        world.playSound(center, Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.6F, 0.6F);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.7F);

        new org.bukkit.scheduler.BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    finishPatch(world, center, radius, falling, stalagmites, displays);
                    cancel();
                    return;
                }

                ticks++;

                if (ticks % 8 == 0) {
                    spawnBlackAmbient(world, center, radius);
                }
            }
        }.runTaskTimer(Main.getInstance(), 1L, 1L);
    }

    private static void createStalagmite(World world, int x, int groundY, int z, int minHeight, int maxHeight,
            List<Block> stalagmites
    ) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        int height;

        if (random.nextDouble() < 0.12) {
            int tallMin = Math.max(maxHeight + 1, Main.balance.getInt("abyssal-tendrils.stalagmite-tall-min-height", 4));
            int tallMax = Math.max(tallMin, Main.balance.getInt("abyssal-tendrils.stalagmite-tall-max-height", 7));
            height = tallMin + (tallMax > tallMin ? random.nextInt(tallMax - tallMin + 1) : 0);
        } else {
            height = random.nextInt(minHeight, maxHeight + 1);
        }

        for (int index = 0; index < height; index++) {
            int y = groundY + 1 + index;

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                return;
            }

            Block block = world.getBlockAt(x, y, z);

            if (block.getType() != Material.AIR) {
                return;
            }

            PointedDripstone data = (PointedDripstone) Material.POINTED_DRIPSTONE.createBlockData();

            data.setVerticalDirection(BlockFace.UP);
            data.setThickness(index == height - 1 ? PointedDripstone.Thickness.TIP
                    : index == 0 ? PointedDripstone.Thickness.BASE : PointedDripstone.Thickness.MIDDLE);
            block.setBlockData(data, false);

            stalagmites.add(block);
            TEMP_STALAGMITES.add(block);

            spawnBlackSmall(world, block.getLocation().add(0.5, 0.5, 0.5));
        }
    }

    private static int groundY(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        Block block = world.getBlockAt(x, y, z);

        if (block.getType().isSolid()) {
            return y;
        }

        for (int depth = 1; depth <= 4; depth++) {
            Block below = world.getBlockAt(x, y - depth, z);

            if (below.getType().isSolid()) {
                return y - depth;
            }
        }

        return Integer.MIN_VALUE;
    }

    private static void damagePatch(World world, Location center, double radius, double damage, Player caster,
            boolean falling, int safeSize
    ) {

        for (Entity entity : world.getNearbyEntities(center, radius, 6.0, radius)) {
            if (!(entity instanceof LivingEntity living) || living.getUniqueId().equals(caster.getUniqueId())) {
                continue;
            }

            double dx = living.getLocation().getX() - center.getX();
            double dz = living.getLocation().getZ() - center.getZ();

            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            if (falling && inSafeZone((int) Math.round(dx), (int) Math.round(dz), safeSize)) {
                continue;
            }

            living.damage(damage, DamageSource.builder(DamageType.STALAGMITE)
                    .withCausingEntity(caster)
                    .withDirectEntity(caster)
                    .build());
        }
    }

    private static boolean inSafeZone(int dx, int dz, int size) {
        if (size <= 0) {
            return false;
        }

        int half = size / 2;

        return dx >= -half && dx < half && dz >= -half && dz < half;
    }

    private static void finishPatch(World world, Location center, double radius, boolean falling,
            List<Block> stalagmites, List<BlockDisplay> displays
    ) {

        int strikes = Main.balance.getInt("abyssal-tendrils.lightning-strikes", 8);

        for (int index = 0; index < strikes; index++) {
            Location point = randomHorizontal(center, radius);
            point.setY(world.getHighestBlockYAt(point) + 1.0);
            world.strikeLightningEffect(point);
        }

        for (Block block : stalagmites) {
            if (!world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                TEMP_STALAGMITES.remove(block);
                continue;
            }

            if (block.getType() == Material.POINTED_DRIPSTONE) {
                block.setType(Material.AIR, false);
                spawnBlackSmall(world, block.getLocation().add(0.5, 0.5, 0.5));
            }

            TEMP_STALAGMITES.remove(block);
        }

        for (BlockDisplay display : displays) {
            if (display.isValid()) {
                display.remove();
            }

            TEMP_DISPLAYS.remove(display);
        }

        TendrilsEffects.finish(center, radius, falling);

        spawnBlackFinish(world, center, radius, falling);

        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6F, 0.7F);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.5F);
    }

    private static List<BlockDisplay> spawnFlyingBlocks(World world, Location center, double radius, boolean falling) {
        int count = Math.max(0, Main.balance.getInt("abyssal-tendrils.flying-blocks", 24));
        int lifetime = Math.max(10, Main.balance.getInt("abyssal-tendrils.flying-block-lifetime-ticks", 30));

        List<BlockDisplay> displays = new ArrayList<>();
        Material[] materials = { Material.BLACKSTONE, Material.DEEPSLATE, Material.SCULK, Material.OBSIDIAN, Material.BASALT };

        for (int index = 0; index < count; index++) {
            Location location = randomHorizontal(center, radius).add(0.0, 1.0 + Math.random(), 0.0);

            if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                continue;
            }

            BlockData data = materials[ThreadLocalRandom.current().nextInt(materials.length)].createBlockData();
            BlockDisplay display;

            try {
                display = world.spawn(location, BlockDisplay.class, entity -> {
                    entity.setBlock(data);
                    entity.setGravity(false);
                    entity.setInvulnerable(true);
                    entity.setPersistent(false);
                    entity.setSilent(true);
                    entity.setBillboard(Display.Billboard.FIXED);
                    entity.setInterpolationDuration(2);
                    entity.setTeleportDuration(2);
                });
            } catch (RuntimeException exception) {
                continue;
            }

            displays.add(display);
            TEMP_DISPLAYS.add(display);
            Location current = display.getLocation().clone();
            Vector velocity = new Vector((Math.random() - 0.5) * 0.45, 0.25 + Math.random() * 0.3,
                    (Math.random() - 0.5) * 0.45);

            new org.bukkit.scheduler.BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    if (!display.isValid() || ticks++ >= lifetime) {
                        if (display.isValid()) {
                            display.remove();
                        }

                        TEMP_DISPLAYS.remove(display);
                        cancel();
                        return;
                    }

                    velocity.setY(velocity.getY() - 0.035);
                    current.add(velocity);
                    display.teleport(current);
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L);
        }

        return displays;
    }

    private static void spawnBlackAura(Player player) {
        World world = player.getWorld();
        Location body = player.getLocation().add(0.0, 1.0, 0.0);

        int density = density();

        world.spawnParticle(Particle.DUST, body, 60 * density, 0.7, 1.0, 0.7, 0.02,
                new Particle.DustOptions(Color.BLACK, 1.4F));
        world.spawnParticle(Particle.SQUID_INK, body, 60 * density, 0.7, 1.0, 0.7, 0.03);
        world.spawnParticle(Particle.SCULK_SOUL, body, 24 * density, 0.6, 0.9, 0.6, 0.02);
    }

    private static void spawnBlackImpact(World world, Location center, double radius, boolean falling) {
        int density = density() * (falling ? 2 : 1);

        for (int index = 0; index < 6; index++) {
            Location point = randomHorizontal(center, radius).add(0.0, 1.0, 0.0);

            world.spawnParticle(Particle.DUST, point, 36 * density, 0.9, 0.9, 0.9, 0.02,
                    new Particle.DustOptions(Color.BLACK, 1.5F));
            world.spawnParticle(Particle.SQUID_INK, point, 36 * density, 0.9, 0.9, 0.9, 0.03);
            world.spawnParticle(Particle.SCULK_SOUL, point, 14 * density, 0.7, 0.9, 0.7, 0.02);
        }
    }

    private static void spawnBlackAmbient(World world, Location center, double radius) {
        int density = density();
        Location point = randomHorizontal(center, radius).add(0.0, 1.0, 0.0);

        world.spawnParticle(Particle.DUST, point, 24 * density, 1.2, 0.8, 1.2, 0.01,
                new Particle.DustOptions(Color.BLACK, 1.3F));
        world.spawnParticle(Particle.SQUID_INK, point, 18 * density, 1.0, 0.8, 1.0, 0.02);
        world.spawnParticle(Particle.SCULK_SOUL, point, 8 * density, 0.8, 0.8, 0.8, 0.01);
    }

    private static void spawnBlackFinish(World world, Location center, double radius, boolean falling) {
        int density = density() * (falling ? 2 : 1);

        for (int index = 0; index < 8; index++) {
            Location point = randomHorizontal(center, radius).add(0.0, 1.0, 0.0);

            world.spawnParticle(Particle.DUST, point, 42 * density, 1.0, 1.0, 1.0, 0.02,
                    new Particle.DustOptions(Color.BLACK, 1.5F));
            world.spawnParticle(Particle.SQUID_INK, point, 42 * density, 1.0, 1.0, 1.0, 0.03);
            world.spawnParticle(Particle.SCULK_SOUL, point, 16 * density, 0.8, 1.0, 0.8, 0.02);
        }
    }

    private static void spawnBlackSmall(World world, Location location) {
        int density = density();

        world.spawnParticle(Particle.DUST, location, 8 * density, 0.3, 0.3, 0.3, 0.01,
                new Particle.DustOptions(Color.BLACK, 1.2F));
        world.spawnParticle(Particle.SQUID_INK, location, 6 * density, 0.3, 0.3, 0.3, 0.01);
        world.spawnParticle(Particle.SCULK_SOUL, location, 2 * density, 0.3, 0.3, 0.3, 0.01);
    }

    private static int density() {
        return Math.max(1, (int) Math.round(Main.balance.getDouble("abyssal-tendrils.particle-multiplier", 2.0)));
    }

    private static Location randomHorizontal(Location center, double radius) {
        double angle = Math.random() * Math.PI * 2.0;
        double distance = Math.sqrt(Math.random()) * radius;

        return center.clone().add(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
    }

    public static void cleanup() {
        for (BlockDisplay display : new ArrayList<>(TEMP_DISPLAYS)) {
            if (display.isValid()) {
                display.remove();
            }
        }

        TEMP_DISPLAYS.clear();

        for (Block block : new ArrayList<>(TEMP_STALAGMITES)) {
            World world = block.getWorld();

            if (world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
                    && block.getType() == Material.POINTED_DRIPSTONE) {
                block.setType(Material.AIR, false);
            }
        }

        TEMP_STALAGMITES.clear();
        PENDING_FALL.clear();
        COOLDOWNS.clear();
        RelicInputs.clearAll();
    }



    private static void sendCooldownDenial(org.bukkit.entity.Player player, long remainingMs) {
        long seconds = Math.max(1L, (remainingMs + 999L) / 1000L);
        String message = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                com.nightmare.lang.LangService.format("cooldown.wait", "&cWait %seconds%s.", "%seconds%",
                        String.valueOf(seconds)));
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
    }

    public static void resetCooldowns(java.util.UUID uuid) {
        COOLDOWNS.remove(uuid);
        PENDING_FALL.remove(uuid);
    }
}
