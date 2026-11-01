package com.nightmare.mob;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.Main;
import com.nightmare.tier.MobTier;

public final class NametagService implements Listener {

    public static final Map<UUID, Long> VISIBLE_UNTIL = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }

        if (damager instanceof Mob mob) {
            show(mob);
        }

        if (event.getEntity() instanceof Mob mob) {
            show(mob);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(event.getTarget() instanceof Player)) {
            return;
        }

        show(mob);
    }

    public static void show(Mob mob) {
        String tierName = mob.getPersistentDataContainer().get(MobKeys.MOB_TIER, PersistentDataType.STRING);

        if (tierName == null) {
            return;
        }

        mob.setCustomNameVisible(true);

        if (!tierName.equals(MobTier.K.name())) {
            long duration = Main.balance.getLong("nametags.combat-visible-ms", 5000L);

            VISIBLE_UNTIL.put(mob.getUniqueId(), System.currentTimeMillis() + duration);
        }
    }

    public static void reset(Mob mob) {
        String tierName = mob.getPersistentDataContainer().get(MobKeys.MOB_TIER, PersistentDataType.STRING);

        if (tierName == null) {
            return;
        }

        mob.setCustomNameVisible(tierName.equals(MobTier.K.name()));
        VISIBLE_UNTIL.remove(mob.getUniqueId());
    }

    public static void clear(UUID uuid) {
        VISIBLE_UNTIL.remove(uuid);
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        
        for (Map.Entry<UUID, Long> entry : new ArrayList<>(VISIBLE_UNTIL.entrySet())) {
            if (entry.getValue() > now) {
                continue;
            }

            Entity entity = Bukkit.getEntity(entry.getKey());

            if (entity instanceof Mob mob) {
                String tierName = mob.getPersistentDataContainer().get(MobKeys.MOB_TIER, PersistentDataType.STRING);
                mob.setCustomNameVisible(tierName != null && tierName.equals(MobTier.K.name()));
            }

            VISIBLE_UNTIL.remove(entry.getKey());
        }
    }

}
