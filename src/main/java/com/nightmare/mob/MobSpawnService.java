package com.nightmare.mob;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.Main;
import com.nightmare.item.CustomItemService;
import com.nightmare.tier.MobTier;
import com.nightmare.tier.TierSchedule;

public final class MobSpawnService implements Listener {

    @SuppressWarnings("deprecation")
    private static final Set<CreatureSpawnEvent.SpawnReason> ALLOWED_REASONS = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.CHUNK_GEN,
            CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER,
            CreatureSpawnEvent.SpawnReason.RAID,
            CreatureSpawnEvent.SpawnReason.PATROL,
            CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
            CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE,
            CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
            CreatureSpawnEvent.SpawnReason.BREEDING,
            CreatureSpawnEvent.SpawnReason.JOCKEY,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN);

    private static final Map<String, Integer> LAST_GUARANTEED = new HashMap<>();
    private static final Map<String, Integer> K_PITY = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!ALLOWED_REASONS.contains(event.getSpawnReason())) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (mob.getPersistentDataContainer().has(MobKeys.MOB_PROCESSED, PersistentDataType.BOOLEAN)) {
            return;
        }

        MobProfile profile = MobProfileLoader.PROFILES.get(mob.getType());

        if (profile == null) {
            return;
        }

        if (profile.category != MobCategory.HOSTILE) {
            return;
        }

        if (isTooBright(mob)) {
            event.setCancelled(true);
            return;
        }

        long day = mob.getWorld().getFullTime() / 24000L;

        String worldName = mob.getWorld().getName();
        MobTier newest = TierSchedule.newestUnlocked(day);

        int guaranteedTier = LAST_GUARANTEED.getOrDefault(worldName, -1);
        boolean canTakeNewest = newest != null && profile.maxTierIndex >= newest.index
                && (newest != MobTier.K || profile.category == MobCategory.HOSTILE);

        MobTier forcedTier = forcedTier(mob);
        MobTier tier;

        if (forcedTier != null) {
            tier = forcedTier;
        } else if (canTakeNewest && guaranteedTier < newest.index) {
            tier = newest;
            LAST_GUARANTEED.put(worldName, newest.index);
        } else {
            tier = TierSchedule.roll(day, ThreadLocalRandom.current());
        }

        mob.getPersistentDataContainer().set(MobKeys.MOB_PROCESSED, PersistentDataType.BOOLEAN, true);

        if (tier == null) {
            return;
        }

        boolean pityTriggered = false;

        if (forcedTier == null && tier != MobTier.K && day >= 90) {
            int pity = K_PITY.getOrDefault(worldName, 0) + 1;

            if (pity >= 250) {
                tier = MobTier.K;
                pityTriggered = true;
            }

            K_PITY.put(worldName, pity);
        }

        if (tier.index > profile.maxTierIndex) {
            tier = MobTier.values()[profile.maxTierIndex];
        }

        if (tier == MobTier.K && profile.category != MobCategory.HOSTILE) {
            tier = MobTier.J;
        }

        if (forcedTier == null && tier == MobTier.K) {
            int maxK = Main.balance.getInt("caps.max-k-per-world", 5);
            int maxPerSpecies = Main.balance.getInt("caps.max-k-per-species", 1);
            long sameSpecies = MobTaskService.ACTIVE.values().stream()
                    .filter(state -> state.tier == MobTier.K && state.profile.type == mob.getType()
                            && state.worldName.equals(worldName))
                    .count();
            long totalK = MobTaskService.ACTIVE.values().stream()
                    .filter(state -> state.tier == MobTier.K && state.worldName.equals(worldName))
                    .count();

            if (sameSpecies >= maxPerSpecies || totalK >= maxK) {
                tier = MobTier.J;
            }
        }

        if (forcedTier == null) {
            if (tier == MobTier.K) {
                K_PITY.put(worldName, 0);
            } else if (pityTriggered) {
                K_PITY.put(worldName, 250);
            }
        }

        boolean miner = isMinerRoll(mob, tier);
        int digCharges = miner ? rollDigCharges() : 0;

        mob.getPersistentDataContainer().set(MobKeys.MOB_TIER, PersistentDataType.STRING, tier.name());
        mob.getPersistentDataContainer().set(MobKeys.MOB_MINER, PersistentDataType.BOOLEAN, miner);
        mob.getPersistentDataContainer().set(MobKeys.MOB_DIG_CHARGES, PersistentDataType.INTEGER, digCharges);
        mob.getPersistentDataContainer().set(MobKeys.MOB_ELITE, PersistentDataType.BOOLEAN, tier.elite);
        mob.getPersistentDataContainer().set(MobKeys.MOB_SPECIES, PersistentDataType.STRING, mob.getType().name());

        boolean aggressive = profile.category == MobCategory.HOSTILE;

        mob.getPersistentDataContainer().set(MobKeys.MOB_AGGRESSIVE, PersistentDataType.BOOLEAN, aggressive);

        String settingKey = "config.mobs." + tier.name().toLowerCase() + "_tier";
        String format = Main.settings.getString(settingKey,
                "&c&lNightmare %mob% &4☠ Tier " + tier.name());

        if (!format.contains("%mob%")) {
            Main.getInstance().getLogger().warning("[mobs] setting '" + settingKey
                    + "' is missing %mob%; appending it to the mob name.");
            format = format + " %mob%";
        }

        String tierPath = "tiers." + tier.name() + ".";
        
        double healthMultiplier = Main.balance.getDouble(tierPath + "health", tier.healthMultiplier);
        double damageMultiplier = Main.balance.getDouble(tierPath + "damage", tier.damageMultiplier);
        int armorBonus = Main.balance.getInt(tierPath + "armor", tier.armorBonus);
        double speedMultiplier = Main.balance.getDouble(tierPath + "speed", tier.speedMultiplier);

        AttributeInstance maxHealth = mob.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(Math.min(1000.0, maxHealth.getBaseValue() * healthMultiplier));
        }

        AttributeInstance attackDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE);

        if (attackDamage != null) {
            attackDamage.setBaseValue(Math.min(2048.0, attackDamage.getBaseValue() * damageMultiplier));
        }

        if (miner && attackDamage != null) {
            double minerDamage = Main.balance == null ? 2.0
                    : Math.max(0.5, Main.balance.getDouble("miner.damage", 2.0));
            attackDamage.setBaseValue(minerDamage);
        }

        AttributeInstance armor = mob.getAttribute(Attribute.ARMOR);

        if (armor != null) {
            armor.setBaseValue(Math.min(30.0, armor.getBaseValue() + armorBonus));
        }

        AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);

        if (speed != null) {
            speed.setBaseValue(Math.min(1024.0, speed.getBaseValue() * speedMultiplier));
        }

        AttributeInstance followRange = mob.getAttribute(Attribute.FOLLOW_RANGE);

        if (followRange != null) {
            if (miner) {
                double minerFollow = Main.balance == null ? 16.0
                        : Math.max(4.0, Main.balance.getDouble("miner.follow-range", 16.0));
                followRange.setBaseValue(Math.min(followRange.getBaseValue(), minerFollow));
            } else {
                followRange.setBaseValue(Math.max(followRange.getBaseValue(), 24.0 + tier.index * 2.0));
            }
        }

        AttributeInstance knockback = mob.getAttribute(Attribute.KNOCKBACK_RESISTANCE);

        if (knockback != null) {
            knockback.setBaseValue(Math.min(1.0, knockback.getBaseValue() + tier.index * 0.03));
        }

        if (mob.getType() == EntityType.GUARDIAN || mob.getType() == EntityType.ELDER_GUARDIAN) {
            if (maxHealth != null) {
                maxHealth.setBaseValue(mob.getType() == EntityType.ELDER_GUARDIAN ? 8.0 : 4.0);
            }
        }

        mob.setHealth(mob.getMaxHealth());

        String mobName = mob.getType().name().toLowerCase().replace('_', ' ');
        mob.setCustomName(ChatColor.translateAlternateColorCodes('&', format.replace("%mob%", mobName)));
        mob.setCustomNameVisible(tier == MobTier.K);
        mob.setCanPickupItems(false);
        mob.setPersistent(tier.elite);

        if (profile.gear) {
            EntityEquipment equipment = mob.getEquipment();

            if (equipment != null) {
                if ((mob instanceof AbstractSkeleton && !(mob instanceof WitherSkeleton)) || mob instanceof Pillager) {
                    equipment.setItemInMainHand(CustomItemService.createBow(tier, tier.elite));
                } else if (miner) {
                    equipment.setItemInMainHand(CustomItemService.createPickaxe(ThreadLocalRandom.current()));
                } else {
                    equipment.setItemInMainHand(CustomItemService.createMeleeWeapon(tier, tier.elite));
                }

                double pieceChance = miner ? 0.0 : 0.30 + 0.06 * tier.index;
                ThreadLocalRandom random = ThreadLocalRandom.current();

                if (random.nextDouble() < pieceChance) {
                    equipment.setHelmet(CustomItemService.createArmor(tier, EquipmentSlot.HEAD, tier.elite));
                }

                if (random.nextDouble() < pieceChance) {
                    equipment.setChestplate(CustomItemService.createArmor(tier, EquipmentSlot.CHEST, tier.elite));
                }

                if (random.nextDouble() < pieceChance) {
                    equipment.setLeggings(CustomItemService.createArmor(tier, EquipmentSlot.LEGS, tier.elite));
                }

                if (random.nextDouble() < pieceChance) {
                    equipment.setBoots(CustomItemService.createArmor(tier, EquipmentSlot.FEET, tier.elite));
                }
                equipment.setItemInMainHandDropChance(0.0F);
                equipment.setHelmetDropChance(0.0F);
                equipment.setChestplateDropChance(0.0F);
                equipment.setLeggingsDropChance(0.0F);
                equipment.setBootsDropChance(0.0F);
            }
        }

        MobTaskService.ACTIVE.put(mob.getUniqueId(),
                new MobState(mob.getUniqueId(), tier, profile, mob.getWorld().getName(), aggressive, miner,
                        digCharges));
    }

    private static boolean isMinerRoll(Mob mob, MobTier tier) {
        if (mob.getType() != EntityType.ZOMBIE) {
            return false;
        }

        if (Main.balance != null && !Main.balance.getBoolean("miner.enabled", true)) {
            return false;
        }

        double base = Main.balance == null ? 0.18 : Main.balance.getDouble("miner.chance", 0.18);
        double perTier = Main.balance == null ? 0.01
                : Main.balance.getDouble("miner.chance-per-tier", 0.01);
        double max = Main.balance == null ? 0.30 : Main.balance.getDouble("miner.chance-max", 0.30);
        double chance = Math.min(max, base + perTier * tier.index);

        if (chance <= 0.0) {
            return false;
        }

        if (chance >= 1.0) {
            return true;
        }

        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    private static MobTier forcedTier(Mob mob) {
        String value = mob.getPersistentDataContainer().get(MobKeys.PHASE_RAID_TIER, PersistentDataType.STRING);

        if (value == null) {
            return null;
        }

        try {
            return MobTier.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean isTooBright(Mob mob) {
        int max = Main.balance == null ? 7 : Main.balance.getInt("mob-spawn.max-light-level", 7);
        Block feet = mob.getLocation().getBlock();
        Block head = feet.getRelative(BlockFace.UP);

        return feet.getLightLevel() > max || head.getLightLevel() > max;
    }

    private static int rollDigCharges() {
        int min = Main.balance == null ? 2 : Math.max(0, Main.balance.getInt("miner.dig-charges-min", 2));
        int max = Main.balance == null ? 6 : Math.max(min, Main.balance.getInt("miner.dig-charges-max", 6));

        return min == max ? min : min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }

}
