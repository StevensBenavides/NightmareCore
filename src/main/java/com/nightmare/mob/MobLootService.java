package com.nightmare.mob;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public final class MobLootService implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        String tierName = entity.getPersistentDataContainer().get(MobKeys.MOB_TIER, PersistentDataType.STRING);

        if (tierName == null) {
            return;
        }

        if (entity.getPersistentDataContainer().has(MobKeys.MOB_SUMMON, PersistentDataType.BOOLEAN)) {
            event.getDrops().clear();

            if (entity instanceof Mob mob) {
                MobTaskService.ACTIVE.remove(mob.getUniqueId());
                MobTaskService.SUMMON_EXPIRY.remove(mob.getUniqueId());
            }

            return;
        }

        if (entity instanceof Creeper) {
            event.getDrops().clear();
        }

        if (entity instanceof Mob mob) {
            MobTaskService.ACTIVE.remove(mob.getUniqueId());
            MobTaskService.SUMMON_EXPIRY.remove(mob.getUniqueId());
            NametagService.clear(mob.getUniqueId());
        }
    }

}
