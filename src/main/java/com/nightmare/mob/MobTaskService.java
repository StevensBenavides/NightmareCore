package com.nightmare.mob;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.nightmare.Main;
import com.nightmare.tier.MobTier;

public final class MobTaskService {

    public static final Map<UUID, MobState> ACTIVE = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> SUMMON_EXPIRY = new ConcurrentHashMap<>();

    private static final Set<String> OFFENSIVE_POWERS = Set.of(
            "AURA", "SUMMON", "MOBILITY", "VOLLEY", "CHARGE", "LIGHTNING", "EXPLOSION", "DEBUFF", "DIG");

    private static final Set<EntityType> VOLLEY_TYPES = EnumSet.of(
            EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.PARCHED,
            EntityType.PILLAGER, EntityType.BLAZE, EntityType.WITCH,
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);

    private MobTaskService() {
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        NametagService.tick();

        for (Map.Entry<UUID, Long> entry : new ArrayList<>(SUMMON_EXPIRY.entrySet())) {
            if (entry.getValue() > now) {
                continue;
            }

            Entity summoned = Bukkit.getEntity(entry.getKey());

            if (summoned != null) {
                summoned.remove();
            }

            SUMMON_EXPIRY.remove(entry.getKey());
        }

        for (MobState state : new ArrayList<>(ACTIVE.values())) {
            Entity entity = Bukkit.getEntity(state.uuid);

            if (entity == null || entity.isDead() || !(entity instanceof Mob mob)) {
                ACTIVE.remove(state.uuid);
                continue;
            }

            List<String> powers = new ArrayList<>(state.profile.powers);

            if (state.tier == MobTier.K) {
                for (String elitePower : List.of("AURA", "SUMMON", "MOBILITY", "PHASE")) {
                    if (!powers.contains(elitePower)) {
                        powers.add(elitePower);
                    }
                }
            }

            if (state.miner && !powers.contains("DIG")) {
                powers.add("DIG");
            }

            boolean canAttack = state.profile.category == MobCategory.HOSTILE || state.aggressive;

            if (state.miner) {
                updateMinerTarget(mob);
            }

            for (String power : powers) {
                long next = state.cooldowns.getOrDefault(power, 0L);

                if (now < next) {
                    continue;
                }

                if (!canAttack && OFFENSIVE_POWERS.contains(power)) {
                    state.cooldowns.put(power, now + 30000L);
                    continue;
                }

                LivingEntity target = mob.getTarget();

                if (target instanceof Player targetPlayer && !MobTargetingService.isTargetable(targetPlayer)) {
                    mob.setTarget(null);
                    target = null;
                }

                if (canAttack && (target == null || target.isDead())) {
                    for (Entity nearby : mob.getNearbyEntities(16.0, 8.0, 16.0)) {
                        if (nearby instanceof Player player && !player.isDead()
                                && MobTargetingService.isTargetable(player)) {
                            target = player;
                            break;
                        }
                    }
                }

                if (target instanceof Player targetPlayer && !MobTargetingService.isTargetable(targetPlayer)) {
                    target = null;
                }

                if (state.profile.category != MobCategory.HOSTILE && state.aggressive
                        && target != null && mob.getTarget() == null) {
                    mob.setTarget(target);
                }

                if (target instanceof Player targetPlayer && !MobTargetingService.isTargetable(targetPlayer)) {
                    continue;
                }

                switch (power) {
                    case "AURA" -> {
                        int radius = 5 + state.tier.index / 3;
                        int duration = Main.balance.getInt("mob-effects.aura-duration-ticks", 60);
                        int amplifier = Math.min(1, Main.balance.getInt("mob-effects.aura-amplifier", 0));
                        PotionEffectType effect = state.profile.category == MobCategory.HOSTILE
                                ? PotionEffectType.WEAKNESS
                                : PotionEffectType.SLOWNESS;

                        for (Entity nearby : mob.getNearbyEntities(radius, radius, radius)) {
                            if (nearby instanceof Player player && !player.isDead()
                                    && MobTargetingService.isTargetable(player)) {
                                player.addPotionEffect(new PotionEffect(effect, duration, amplifier, true, true, true));
                            }
                        }

                        mob.getWorld().spawnParticle(Particle.SCULK_SOUL, mob.getLocation().add(0.0, 1.0, 0.0), 10, 0.8, 0.8, 0.8, 0.01);
                        state.cooldowns.put(power, now + Main.balance.getLong("mob-effects.aura-cooldown-ms", 5000L));
                    }
                    case "SUMMON" -> {
                        if (SUMMON_EXPIRY.size() >= 40) {
                            continue;
                        }

                        int amount = state.tier == MobTier.K ? 3 : 1;

                        for (int i = 0; i < amount; i++) {
                            Location location = mob.getLocation().clone().add(Math.random() * 2.0 - 1.0, 0.0, Math.random() * 2.0 - 1.0);
                            @SuppressWarnings("unchecked")
                            Class<LivingEntity> entityClass = (Class<LivingEntity>) state.profile.type.getEntityClass();
                            LivingEntity summon = mob.getWorld().spawn(location, entityClass,
                                    CreatureSpawnEvent.SpawnReason.CUSTOM, false, spawned -> {
                                        spawned.getPersistentDataContainer().set(MobKeys.MOB_SUMMON,
                                                PersistentDataType.BOOLEAN, true);
                                        spawned.getPersistentDataContainer().set(MobKeys.MOB_PROCESSED,
                                                PersistentDataType.BOOLEAN, true);

                                        if (spawned instanceof Mob summonMob) {
                                            summonMob.setPersistent(false);
                                            summonMob.setRemoveWhenFarAway(true);
                                        }
                                    });

                            SUMMON_EXPIRY.put(summon.getUniqueId(), now + 15000L);
                        }

                        state.cooldowns.put(power, now + 20000L);
                    }
                    case "MOBILITY" -> {
                        if (target == null) {
                            continue;
                        }

                        Vector direction = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(1.4);

                        mob.setVelocity(direction.setY(0.35));
                        mob.getWorld().spawnParticle(Particle.PORTAL, mob.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.6, 0.4, 0.1);

                        state.cooldowns.put(power, now + 8000L);
                    }
                    case "VOLLEY" -> {
                        if (target == null) {
                            continue;
                        }

                        if (!VOLLEY_TYPES.contains(mob.getType())) {
                            state.cooldowns.put(power, now + 30000L);
                            continue;
                        }

                        Vector direction = target.getEyeLocation().toVector().subtract(mob.getEyeLocation().toVector()).normalize().multiply(1.6);
                        int arrows = 1 + state.tier.index / 5;

                        for (int i = 0; i < arrows; i++) {
                            Vector shot = direction.clone();

                            shot.add(new Vector(Math.random() * 0.12 - 0.06, Math.random() * 0.08 - 0.04,
                                    Math.random() * 0.12 - 0.06));

                            mob.launchProjectile(Arrow.class, shot);
                        }

                        state.cooldowns.put(power, now + 4000L);
                    }
                    case "CHARGE" -> {
                        if (target == null) {
                            continue;
                        }

                        Vector direction = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(1.2);

                        mob.setVelocity(direction.setY(0.4));

                        state.cooldowns.put(power, now + 6000L);
                    }
                    case "HEAL" -> {
                        mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + 1.0 + state.tier.index / 2.0));
                        mob.getWorld().spawnParticle(Particle.HEART, mob.getLocation().add(0.0, 1.5, 0.0), 6);

                        state.cooldowns.put(power, now + 8000L);
                    }
                    case "LIGHTNING" -> {
                        if (target == null) {
                            continue;
                        }

                        if (mob.getLocation().distanceSquared(target.getLocation()) > 36.0) {
                            continue;
                        }

                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(2.0 + state.tier.index / 2.0, DamageSource.builder(DamageType.LIGHTNING_BOLT)
                                .withCausingEntity(mob)
                                .withDirectEntity(mob)
                                .build());

                        state.cooldowns.put(power, now + 6000L);
                    }
                    case "EXPLOSION" -> {
                        if (target == null) {
                            continue;
                        }

                        if (state.tier.index < 2) {
                            continue;
                        }

                        if (mob.getLocation().distanceSquared(target.getLocation()) > 36.0) {
                            continue;
                        }

                        target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 8);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6F, 0.8F);

                        for (Entity nearby : target.getWorld().getNearbyEntities(target.getLocation(), 4.0, 4.0, 4.0)) {
                            if (!(nearby instanceof Player nearbyPlayer)
                                    || nearbyPlayer.getUniqueId().equals(mob.getUniqueId())
                                    || !MobTargetingService.isTargetable(nearbyPlayer)) {
                                continue;
                            }

                            nearbyPlayer.damage(2.0 + state.tier.index / 2.0, DamageSource.builder(DamageType.EXPLOSION)
                                    .withCausingEntity(mob)
                                    .withDirectEntity(mob)
                                    .build());
                        }

                        state.cooldowns.put(power, now + 6000L);
                    }
                    case "BUFF" -> {
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, Math.min(2, state.tier.index / 4), true, true, true));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, true, true, true));

                        state.cooldowns.put(power, now + 10000L);
                    }
                    case "DEBUFF" -> {
                        if (!(target instanceof LivingEntity living)) {
                            continue;
                        }

                        if (mob.getLocation().distanceSquared(target.getLocation()) > 100.0) {
                            continue;
                        }

                        int duration = Main.balance.getInt("mob-effects.debuff-duration-ticks", 60);
                        int amplifier = Math.min(1, Main.balance.getInt("mob-effects.debuff-amplifier", 0));

                        living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, amplifier, true, true, true));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 0, true, true, true));

                        state.cooldowns.put(power, now + Main.balance.getLong("mob-effects.debuff-cooldown-ms", 8000L));
                    }
                    case "PHASE" -> {
                        if (mob.getHealth() / mob.getMaxHealth() > 0.5) {
                            continue;
                        }

                        mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, true, false, false));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true, false, false));
                        mob.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, mob.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.6, 0.4, 0.1);

                        state.cooldowns.put(power, now + 20000L);
                    }
                    case "DIG" -> {
                        if (!(target instanceof LivingEntity living) || !isValidMinerTarget(living)) {
                            continue;
                        }

                        double range = Math.max(1.0, Main.balance == null ? 6.0
                                : Main.balance.getDouble("miner.dig-range", 6.0));

                        if (mob.getLocation().distanceSquared(target.getLocation()) > range * range) {
                            continue;
                        }

                        if (state.digCharges <= 0) {
                            breakPickaxe(mob, state);
                            continue;
                        }

                        int broken = digTowardTarget(mob, state, living, power, now);

                        if (broken > 0) {
                            mob.swingMainHand();
                            mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_STONE_BREAK, 0.9F, 0.9F);

                            if (state.digCharges <= 0) {
                                breakPickaxe(mob, state);
                            }
                        }
                    }
                    default -> state.cooldowns.put(power, now + 10000L);
                }
            }
        }
    }

    private static void updateMinerTarget(Mob mob) {
        double range = Math.max(1.0, Main.balance == null ? 16.0
                : Main.balance.getDouble("miner.follow-range", 16.0));
        double rangeSq = range * range;
        Player bestPlayer = null;
        double bestPlayerDist = Double.MAX_VALUE;
        LivingEntity bestPrey = null;
        double bestPreyDist = Double.MAX_VALUE;

        for (Entity nearby : mob.getNearbyEntities(range, range, range)) {
            if (!(nearby instanceof LivingEntity living) || living.equals(mob)
                    || living.isDead() || !living.isValid()) {
                continue;
            }

            double distance = mob.getLocation().distanceSquared(living.getLocation());

            if (distance > rangeSq) {
                continue;
            }

            if (living instanceof Player player) {
                if (MobTargetingService.isTargetable(player) && distance < bestPlayerDist) {
                    bestPlayer = player;
                    bestPlayerDist = distance;
                }
            } else if (isMinerPrey(living) && distance < bestPreyDist) {
                bestPrey = living;
                bestPreyDist = distance;
            }
        }

        LivingEntity chosen = bestPlayer != null ? bestPlayer : bestPrey;

        if (chosen != null) {
            if (mob.getTarget() == null || !mob.getTarget().equals(chosen)) {
                mob.setTarget(chosen);
            }

            return;
        }

        LivingEntity current = mob.getTarget();

        if (current == null) {
            return;
        }

        if (current.isDead() || !current.isValid()
                || mob.getLocation().distanceSquared(current.getLocation()) > rangeSq
                || (current instanceof Player player && !MobTargetingService.isTargetable(player))) {
            mob.setTarget(null);
        }
    }

    private static boolean isMinerPrey(LivingEntity entity) {
        return entity instanceof org.bukkit.entity.AbstractVillager
                || entity instanceof org.bukkit.entity.IronGolem
                || entity instanceof org.bukkit.entity.Snowman;
    }

    private static boolean isValidMinerTarget(LivingEntity target) {
        if (target instanceof Player player) {
            return MobTargetingService.isTargetable(player);
        }

        return isMinerPrey(target);
    }

    private static int digTowardTarget(Mob mob, MobState state, LivingEntity target, String power, long now) {
        if (state.digCharges <= 0) {
            return 0;
        }

        boolean onlyNatural = Main.balance != null
                && Main.balance.getBoolean("miner.only-natural-blocks", false);
        double range = Math.max(1.0, Main.balance == null ? 6.0
                : Main.balance.getDouble("miner.dig-range", 6.0));
        Location from = mob.getLocation();
        Location to = target.getLocation();
        MinerDig.BlockPos hit = MinerDig.find(
                from.getX(), from.getY() + 0.5D, from.getZ(),
                to.getX(), to.getY() + 0.5D, to.getZ(),
                range,
                (x, y, z) -> {
                    org.bukkit.block.Block candidate = mob.getWorld().getBlockAt(x, y, z);
                    return candidate.getType().isSolid() && !candidate.isLiquid();
                });

        if (hit == null) {
            state.cooldowns.put(power, now + 2000L);
            return 0;
        }

        if (!isDiggable(mob.getWorld().getBlockAt(hit.x(), hit.y(), hit.z()), onlyNatural)) {
            state.cooldowns.put(power, now + 2000L);
            return 0;
        }

        int baseY = Math.min(hit.y(), from.getBlockY());
        int broken = 0;

        for (int dy = 0; dy <= 1 && state.digCharges > 0; dy++) {
            org.bukkit.block.Block block = mob.getWorld().getBlockAt(hit.x(), baseY + dy, hit.z());

            if (!isDiggable(block, onlyNatural)) {
                continue;
            }

            org.bukkit.block.data.BlockData data = block.getBlockData();
            Location brokenAt = block.getLocation().add(0.5D, 0.5D, 0.5D);
            block.setType(org.bukkit.Material.AIR, false);
            mob.getWorld().spawnParticle(Particle.BLOCK, brokenAt, 14, 0.3D, 0.3D, 0.3D, 0.05D, data);
            state.digCharges--;
            broken++;
        }

        if (broken == 0) {
            state.cooldowns.put(power, now + 2000L);
            return 0;
        }

        mob.getPersistentDataContainer().set(MobKeys.MOB_DIG_CHARGES, PersistentDataType.INTEGER,
                state.digCharges);
        state.cooldowns.put(power, now + digCooldownMs(mob));
        return broken;
    }

    private static void breakPickaxe(Mob mob, MobState state) {
        state.digCharges = 0;
        org.bukkit.inventory.EntityEquipment equipment = mob.getEquipment();

        if (equipment == null) {
            return;
        }

        org.bukkit.inventory.ItemStack held = equipment.getItemInMainHand();

        if (held == null || held.getType().isAir()) {
            return;
        }

        org.bukkit.Material brokenType = held.getType();
        equipment.setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        mob.getWorld().spawnParticle(Particle.ITEM, mob.getLocation().add(0.0D, 1.0D, 0.0D),
                12, 0.3D, 0.3D, 0.3D, 0.05D, new org.bukkit.inventory.ItemStack(brokenType));
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8F, 0.8F);
        mob.getPersistentDataContainer().set(MobKeys.MOB_DIG_CHARGES, PersistentDataType.INTEGER, 0);
    }

    private static boolean isDiggable(org.bukkit.block.Block block, boolean onlyNatural) {
        org.bukkit.Material type = block.getType();

        if (type.isAir() || block.isLiquid()) {
            return false;
        }

        if (type.getHardness() < 0.0F) {
            return false;
        }

        switch (type) {
            case OBSIDIAN:
            case CRYING_OBSIDIAN:
            case REINFORCED_DEEPSLATE:
            case END_PORTAL:
            case END_PORTAL_FRAME:
            case END_GATEWAY:
            case SPAWNER:
            case TRIAL_SPAWNER:
            case VAULT:
            case BEDROCK:
            case BARRIER:
            case COMMAND_BLOCK:
            case CHAIN_COMMAND_BLOCK:
            case REPEATING_COMMAND_BLOCK:
            case STRUCTURE_BLOCK:
            case JIGSAW:
                return false;
            default:
                break;
        }

        return !onlyNatural || !com.nightmare.world.AltarPlacement.isArtificialName(type.name());
    }

    private static long digCooldownMs(Mob mob) {
        org.bukkit.Material held = mob.getEquipment() == null
                ? org.bukkit.Material.AIR
                : mob.getEquipment().getItemInMainHand().getType();
        String key;
        long fallback;

        if (held == org.bukkit.Material.IRON_PICKAXE) {
            key = "miner.dig-cooldown-iron-ms";
            fallback = 4000L;
        } else if (held == org.bukkit.Material.STONE_PICKAXE) {
            key = "miner.dig-cooldown-stone-ms";
            fallback = 6000L;
        } else {
            key = "miner.dig-cooldown-wood-ms";
            fallback = 8000L;
        }

        return Math.max(1000L, Main.balance == null ? fallback : Main.balance.getLong(key, fallback));
    }
}
