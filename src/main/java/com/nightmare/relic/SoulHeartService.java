package com.nightmare.relic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.nightmare.Main;

public final class SoulHeartService implements Listener {

    private static final int LOCKED_SLOT = 16;
    private static final Map<UUID, Long> BURST_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> DIVINE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> DIVINE_WINDUP = new ConcurrentHashMap<>();

    public static void start(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, SoulHeartService::tick, 20L, 20L);
        SoulHeartAuraEffect.start(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        Player player = event.getPlayer();

        if (!hasHeart(player) || player.isSneaking() || !isSprinting(player)) {
            return;
        }

        long now = System.currentTimeMillis();

        if (BURST_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) > now) {
            return;
        }

        BURST_COOLDOWNS.put(player.getUniqueId(), now + Main.balance.getLong("soul-heart.burst-cooldown-ms", 45000L));
        castSoulBurst(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!hasHeart(player)) {
            ensureBound(player);
        }

        if (!hasHeart(player) || !player.isSneaking() || isSprinting(player)) {
            if (hasHeart(player) && player.isSneaking() && isSprinting(player)) {
                player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 0.8F);
            }
            return;
        }

        long now = System.currentTimeMillis();

        if (DIVINE_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) > now) {
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 0.8F);
            return;
        }

        event.setCancelled(true);
        DIVINE_COOLDOWNS.put(player.getUniqueId(), now + Main.balance.getLong("soul-heart.divine-cooldown-ms", 3600000L));
        DivineCutEffect.cast(player);
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        RelicInputs.recordSprint(event.getPlayer().getUniqueId(), event.getInput().isSprint());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        clearTransientState(event.getEntity().getUniqueId());
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
    public void onQuit(PlayerQuitEvent event) {
        clearTransientState(event.getPlayer().getUniqueId());
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
        clearTransientState(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (SoulHeartItem.isSoulHeart(event.getItemDrop().getItemStack())) {
            resetCooldowns(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && SoulHeartItem.isSoulHeart(event.getItem().getItemStack())) {
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
                && SoulHeartItem.isSoulHeart(event.getCurrentItem())) {
            resetCooldowns(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !SoulHeartItem.isSoulHeart(event.getOldCursor())) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= topSize && rawSlot - topSize == LOCKED_SLOT && SoulHeartItem.isSoulHeart(event.getOldCursor())) {
                resetCooldowns(player.getUniqueId());
                return;
            }
        }
    }

    public static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ensureBound(player);
        }
    }

    public static void give(Player player) {
        if (hasHeart(player)) {
            return;
        }

        ItemStack item = SoulHeartItem.create();
        ensureBound(player);

        if (!hasHeart(player)) {
            place(player, item);
        }
    }

    public static boolean hasHeart(Player player) {
        return SoulHeartItem.isSoulHeart(player.getInventory().getItem(LOCKED_SLOT));
    }

    public static boolean hasHeartInInventory(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (SoulHeartItem.isSoulHeart(player.getInventory().getItem(slot))) {
                return true;
            }
        }

        return SoulHeartItem.isSoulHeart(player.getItemOnCursor());
    }

    public static boolean remove(Player player) {
        ItemStack item = player.getInventory().getItem(LOCKED_SLOT);

        if (!SoulHeartItem.isSoulHeart(item)) {
            return false;
        }

        player.getInventory().setItem(LOCKED_SLOT, null);
        resetCooldowns(player.getUniqueId());
        player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.updateInventory();
        
        return true;
    }

    public static void resetCooldowns(UUID uuid) {
        BURST_COOLDOWNS.remove(uuid);
        DIVINE_COOLDOWNS.remove(uuid);
        cancelDivineWindup(uuid);
    }

    public static void ensureBound(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack slotItem = inventory.getItem(LOCKED_SLOT);

        if (SoulHeartItem.isSoulHeart(slotItem)) {
            SoulHeartItem.refreshLore(slotItem);
            return;
        }

        ItemStack cursor = player.getItemOnCursor();

        if (SoulHeartItem.isSoulHeart(cursor)) {
            player.setItemOnCursor(null);
            place(player, cursor);
            return;
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot == LOCKED_SLOT) {
                continue;
            }

            ItemStack item = inventory.getItem(slot);

            if (SoulHeartItem.isSoulHeart(item)) {
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

        SoulHeartItem.refreshLore(item);

        player.updateInventory();
    }

    private static boolean isSprinting(Player player) {
        return RelicInputs.isSprinting(player);
    }

    private static void clearTransientState(UUID uuid) {
        RelicInputs.clear(uuid);
        cancelDivineWindup(uuid);
    }

    static void trackDivineWindup(UUID uuid, BukkitTask task) {
        BukkitTask previous = DIVINE_WINDUP.put(uuid, task);

        if (previous != null) {
            previous.cancel();
        }
    }

    static void finishDivineWindup(UUID uuid, BukkitTask task) {
        DIVINE_WINDUP.remove(uuid, task);
    }

    static void cancelDivineWindup(UUID uuid) {
        BukkitTask task = DIVINE_WINDUP.remove(uuid);

        if (task != null) {
            task.cancel();
        }

    }

    static void refundDivineWindup(UUID uuid) {
        cancelDivineWindup(uuid);
        DIVINE_COOLDOWNS.remove(uuid);
    }

    private static void castSoulBurst(Player player) {
        World world = player.getWorld();

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 0.9F);
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.1F);

        Location start = player.getEyeLocation().clone();

        int range = Math.min(25, Main.balance.getInt("soul-heart.burst-range", 25));
        double speed = Main.balance.getDouble("soul-heart.burst-speed", 2.0);
        double radius = Main.balance.getDouble("soul-heart.burst-radius", 5.0);
        double damage = Main.balance.getDouble("soul-heart.burst-damage", 200.0);
        int maxBlocks = Main.balance.getInt("soul-heart.burst-max-blocks", 2048);
        int maxSteps = (int) Math.ceil(range / speed);

        Vector direction = player.getLocation().getDirection().normalize().multiply(speed);
        Set<UUID> hit = new HashSet<>();

        new org.bukkit.scheduler.BukkitRunnable() {
            private int steps = 0;
            private int broken = 0;

            @Override
            public void run() {
                if (!player.isOnline() || steps >= maxSteps) {
                    cancel();
                    return;
                }

                start.add(direction);
                steps++;

                if (start.getWorld() == null
                        || !start.getWorld().isChunkLoaded(start.getBlockX() >> 4, start.getBlockZ() >> 4)) {
                    cancel();
                    return;
                }

                Location center = start.clone();
                SoulHeartEffects.soulBurst(world, center, radius);

                if (steps % 5 == 0) {
                    Vector right = direction.clone().normalize().crossProduct(new Vector(0, 1, 0)).normalize();

                    if (right.lengthSquared() < 0.001) {
                        right = new Vector(1, 0, 0);
                    }

                    PentagramEffect.draw(world, center, right, new Vector(0, 1, 0), 1.5, 0.0, true);
                }

                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(entity instanceof LivingEntity living) || living instanceof Player
                            || living.isDead() || living.getLocation().distanceSquared(center) > radius * radius) {
                        continue;
                    }

                    if (!hit.add(living.getUniqueId())) {
                        continue;
                    }

                    living.damage(damage, DamageSource.builder(DamageType.MAGIC)
                            .withCausingEntity(player)
                            .withDirectEntity(player)
                            .build());
                }

                broken = breakSphere(world, center, player, broken, maxBlocks, 3);
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    private static int breakSphere(World world, Location center, Player player, int broken, int max, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (broken >= max || x * x + y * y + z * z > radius * radius) {
                        continue;
                    }

                    int blockX = center.getBlockX() + x;
                    int blockZ = center.getBlockZ() + z;

                    if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
                        continue;
                    }

                    Block block = world.getBlockAt(blockX, center.getBlockY() + y, blockZ);

                    if (block.getType().isAir() || block.getType().getHardness() < 0.0F) {
                        continue;
                    }

                    BlockBreakEvent event = new BlockBreakEvent(block, player);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        continue;
                    }

                    block.setType(Material.AIR, false);
                    broken++;
                }
            }
        }

        return broken;
    }

    public static void cleanup() {
        for (BukkitTask task : DIVINE_WINDUP.values()) {
            task.cancel();
        }

        DIVINE_WINDUP.clear();
        BURST_COOLDOWNS.clear();
        DIVINE_COOLDOWNS.clear();

        RelicInputs.clearAll();
    }

}
