package com.nightmare.enchant;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.nightmare.Main;
import com.nightmare.relic.TendrilsEffects;

public final class AbyssalCoreService implements Listener {

    private static final java.util.Map<java.util.UUID, Long> BEAM_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Long> AREA_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Set<java.util.UUID> BEAM_READY = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final java.util.Map<java.util.UUID, Long> RIGHT_CLICK_AT = new java.util.concurrent.ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClickMark(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            RIGHT_CLICK_AT.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking() || !hasAbyssalCore(player)) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now - RIGHT_CLICK_AT.getOrDefault(player.getUniqueId(), 0L) < 250L) {
            return;
        }

        if (BEAM_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) > now) {
            return;
        }

        BEAM_COOLDOWNS.put(player.getUniqueId(), now + Main.balance.getLong("abyssal-core.burst-cooldown-ms", 60000L));
        BEAM_READY.remove(player.getUniqueId());
        castBeam(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking() || !hasAbyssalCore(player)) {
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            return;
        }

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK
                && player.getInventory().getItemInMainHand().getType().isBlock()) {
            return;
        }

        long now = System.currentTimeMillis();

        long remainingCooldown = AREA_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L);

        if (remainingCooldown > now) {
            sendCooldownDenial(player, remainingCooldown - now);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 0.8F);
            return;
        }

        event.setCancelled(true);
        AREA_COOLDOWNS.put(player.getUniqueId(), now + Main.balance.getLong("abyssal-core.area-cooldown-ms", 120000L));

        castArea(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking() || !hasAbyssalCore(player)) {
            return;
        }

        long now = System.currentTimeMillis();

        long remainingCooldown = AREA_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L);

        if (remainingCooldown > now) {
            sendCooldownDenial(player, remainingCooldown - now);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 0.8F);
            return;
        }

        event.setCancelled(true);
        AREA_COOLDOWNS.put(player.getUniqueId(), now + Main.balance.getLong("abyssal-core.area-cooldown-ms", 120000L));

        castArea(player);
    }


    private static void sendCooldownDenial(org.bukkit.entity.Player player, long remainingMs) {
        long seconds = Math.max(1L, (remainingMs + 999L) / 1000L);
        String message = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                com.nightmare.lang.LangService.format("cooldown.wait", "&cWait %seconds%s.", "%seconds%",
                        String.valueOf(seconds)));
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
    }

    public static void tick() {
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hasAbyssalCore(player)) {
                continue;
            }

            if (BEAM_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) <= now) {
                if (BEAM_READY.add(player.getUniqueId())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.8F, 0.6F);
                    player.getWorld().spawnParticle(Particle.SCULK_SOUL,
                            player.getLocation().add(0.0, 1.0, 0.0), 18, 0.5, 0.8, 0.5, 0.02);
                }
            } else {
                BEAM_READY.remove(player.getUniqueId());
            }
        }
    }

    private static boolean hasAbyssalCore(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();

        if (chestplate == null || chestplate.getType() == Material.AIR || chestplate.getItemMeta() == null) {
            return false;
        }

        Integer level = chestplate.getItemMeta().getPersistentDataContainer()
                .get(EnchantKeys.KEYS.get("abyssal_core"), PersistentDataType.INTEGER);

        return level != null && level > 0;
    }

    private static void castBeam(Player player) {
        World world = player.getWorld();
        Location start = player.getEyeLocation().clone();

        int range = Math.min(19, Main.balance.getInt("abyssal-core.burst-range", 19));
        double speed = Main.balance.getDouble("abyssal-core.burst-speed", 2.0);
        double radius = Main.balance.getDouble("abyssal-core.burst-radius", 4.0);
        double damage = Main.balance.getDouble("abyssal-core.burst-damage", 80.0);
        int maxBlocks = Main.balance.getInt("abyssal-core.burst-max-blocks", 4096);
        boolean drops = Main.balance.getBoolean("abyssal-core.burst-drop-blocks", false);
        boolean respect = Main.balance.getBoolean("abyssal-core.burst-respect-protections", true);
        int maxSteps = (int) Math.ceil(range / speed);

        Vector direction = player.getLocation().getDirection().normalize().multiply(speed);

        java.util.Set<java.util.UUID> hit = new java.util.HashSet<>();

        Location eye = player.getEyeLocation().clone();

        Block target = player.getTargetBlockExact(range);

        Location end = target != null ? target.getLocation().add(0.5, 0.5, 0.5)
                : eye.clone().add(player.getLocation().getDirection().normalize().multiply(range));

        TendrilsEffects.burstLine(eye, end);

        new org.bukkit.scheduler.BukkitRunnable() {
            private int steps = 0;
            private int broken = 0;
            private boolean impacted = false;

            @Override
            public void run() {
                if (!player.isOnline() || steps >= maxSteps) {
                    if (steps >= maxSteps && !impacted) {
                        TendrilsEffects.burstImpact(start.clone());
                    }

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

                world.spawnParticle(Particle.EXPLOSION, center, 1);
                world.spawnParticle(Particle.DUST, center, 14, 0.6, 0.8, 0.6, 0.01,
                        new Particle.DustOptions(org.bukkit.Color.BLACK, 1.2F));
                world.spawnParticle(Particle.SQUID_INK, center, 40, 0.5, 0.7, 0.5, 0.01);
                world.spawnParticle(Particle.SCULK_SOUL, center, 4, 0.4, 0.6, 0.4, 0.01);

                for (int point = 0; point < 3; point++) {
                    Location burst = center.clone().add(
                            (Math.random() * 2.0 - 1.0) * radius,
                            (Math.random() * 2.0 - 1.0) * radius,
                            (Math.random() * 2.0 - 1.0) * radius);

                    if (burst.distanceSquared(center) > radius * radius) {
                        continue;
                    }

                    world.spawnParticle(Particle.EXPLOSION, burst, 1);
                    world.spawnParticle(Particle.DUST, burst, 14, 0.6, 0.8, 0.6, 0.01,
                            new Particle.DustOptions(org.bukkit.Color.BLACK, 1.2F));
                    world.spawnParticle(Particle.SQUID_INK, burst, 40, 0.5, 0.7, 0.5, 0.01);
                    world.spawnParticle(Particle.SCULK_SOUL, burst, 4, 0.4, 0.6, 0.4, 0.01);
                }

                if (steps % 5 == 0) {
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.7F);
                }

                if (steps % 3 == 0) {
                    world.playSound(center, Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.5F, 0.8F);
                }

                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(entity instanceof LivingEntity living) || living.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }

                    if (living.getLocation().distanceSquared(center) > radius * radius) {
                        continue;
                    }

                    if (!hit.add(living.getUniqueId())) {
                        continue;
                    }

                    living.damage(damage, DamageSource.builder(DamageType.SONIC_BOOM)
                            .withCausingEntity(player)
                            .withDirectEntity(player)
                            .build());
                }

                if (!impacted && !hit.isEmpty()) {
                    impacted = true;
                    TendrilsEffects.burstImpact(center);
                }

                int before = broken;

                broken = breakBlocks(world, center, player, broken, maxBlocks, drops, respect, radius);

                if (!impacted && broken > before) {
                    impacted = true;
                    TendrilsEffects.burstImpact(center);
                }

            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    private static void castArea(Player player) {
        World world = player.getWorld();
        Location center = player.getLocation().clone();

        double radius = Main.balance.getDouble("abyssal-core.area-radius", 7.5);
        double damage = Main.balance.getDouble("abyssal-core.area-damage", 100.0);

        int strikes = Main.balance.getInt("abyssal-core.area-lightning-strikes", 12);
        int maxBlocks = Main.balance.getInt("abyssal-core.area-max-blocks", 1024);
        int heightAbove = Main.balance.getInt("abyssal-core.area-height-above", 2);
        int heightBelow = Main.balance.getInt("abyssal-core.area-height-below", 1);

        boolean drops = Main.balance.getBoolean("abyssal-core.area-drop-blocks", false);
        boolean respect = Main.balance.getBoolean("abyssal-core.area-respect-protections", true);

        java.util.Set<java.util.UUID> hit = new java.util.HashSet<>();
        java.util.List<Block> blocks = new java.util.ArrayList<>();

        int blockRadius = (int) Math.ceil(radius);

        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int z = -blockRadius; z <= blockRadius; z++) {
                if (x * x + z * z > radius * radius) {
                    continue;
                }

                for (int y = -heightBelow; y <= heightAbove; y++) {
                    if (blocks.size() >= maxBlocks) {
                        break;
                    }

                    blocks.add(world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z));
                }
            }
        }

        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.7F, 0.8F);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.6F);
        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6F, 0.8F);

        TendrilsEffects.cataclysmCast(center);

        new org.bukkit.scheduler.BukkitRunnable() {
            private int ticks = 0;
            private int blockIndex = 0;
            private int broken = 0;

            @Override
            public void run() {
                if (ticks >= 30 || !player.isOnline()) {
                    TendrilsEffects.cataclysmFinish(center);
                    cancel();
                    return;
                }

                ticks++;

                for (int point = 0; point < 3; point++) {
                    Location location = randomPoint(center, radius);

                    world.spawnParticle(Particle.EXPLOSION, location, 2);
                    world.spawnParticle(Particle.SQUID_INK, location, 14, 0.8, 0.8, 0.8, 0.03);
                    world.spawnParticle(Particle.SCULK_SOUL, location, 8, 0.6, 0.6, 0.6, 0.02);
                    world.spawnParticle(Particle.DUST, location, 12, 0.6, 0.6, 0.6, 0.02,
                            new Particle.DustOptions(org.bukkit.Color.BLACK, 1.4F));
                }

                if (ticks <= strikes) {
                    Location strike = randomPoint(center, radius);

                    strike.setY(world.getHighestBlockYAt(strike) + 1.0);
                    world.strikeLightningEffect(strike);
                }

                if (ticks == 1) {
                    for (Entity entity : world.getNearbyEntities(center, radius, heightAbove + 3, radius)) {
                        if (!(entity instanceof LivingEntity living) || living.getUniqueId().equals(player.getUniqueId())) {
                            continue;
                        }

                        if (!hit.add(living.getUniqueId())) {
                            continue;
                        }

                        living.damage(damage, DamageSource.builder(DamageType.SONIC_BOOM)
                                .withCausingEntity(player)
                                .withDirectEntity(player)
                                .build());
                    }
                }

                int batch = Math.max(1, maxBlocks / 20);

                while (blockIndex < blocks.size() && batch > 0) {
                    Block block = blocks.get(blockIndex++);
                    batch--;

                    if (block.getType() == Material.AIR || block.getType().getHardness() < 0.0F) {
                        continue;
                    }

                    if (respect) {
                        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
                        Bukkit.getPluginManager().callEvent(breakEvent);

                        if (breakEvent.isCancelled()) {
                            continue;
                        }
                    }

                    if (drops) {
                        block.breakNaturally();
                    } else {
                        block.setType(Material.AIR, false);
                    }

                    broken++;

                    if (broken >= maxBlocks) {
                        break;
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    private static Location randomPoint(Location center, double radius) {
        double angle = Math.random() * Math.PI * 2.0;
        double distance = 3.0 + Math.random() * Math.max(0.1, radius - 3.0);

        return center.clone().add(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
    }

    private static int breakBlocks(World world, Location center, Player player, int broken, int max,
            boolean drops, boolean respect, double radius
    ) {
        int blockRadius = (int) Math.ceil(radius);

        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    if (broken >= max) {
                        return broken;
                    }

                    if (x * x + y * y + z * z > radius * radius) {
                        continue;
                    }

                    int blockX = center.getBlockX() + x;
                    int blockZ = center.getBlockZ() + z;

                    if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
                        continue;
                    }

                    Block block = world.getBlockAt(blockX, center.getBlockY() + y, blockZ);

                    if (block.getType() == Material.AIR || block.getType().getHardness() < 0.0F) {
                        continue;
                    }

                    if (respect) {
                        BlockBreakEvent event = new BlockBreakEvent(block, player);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            continue;
                        }
                    }

                    if (drops) {
                        block.breakNaturally();
                    } else {
                        block.setType(Material.AIR, false);
                    }

                    broken++;
                }
            }
        }

        return broken;
    }


    public static void resetCooldowns(java.util.UUID uuid) {
        BEAM_COOLDOWNS.remove(uuid);
        AREA_COOLDOWNS.remove(uuid);
        BEAM_READY.remove(uuid);
    }
}
