package com.nightmare.mob;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import com.nightmare.tier.MobTier;

public final class MobChunkService implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }

            if (MobTaskService.ACTIVE.containsKey(mob.getUniqueId())) {
                continue;
            }

            if (mob.getPersistentDataContainer().has(MobKeys.MOB_SUMMON, PersistentDataType.BOOLEAN)) {
                continue;
            }

            String tierName = mob.getPersistentDataContainer().get(MobKeys.MOB_TIER, PersistentDataType.STRING);

            if (tierName == null) {
                continue;
            }

            MobProfile profile = MobProfileLoader.PROFILES.get(mob.getType());

            if (profile == null) {
                continue;
            }

            MobTier tier;

            try {
                tier = MobTier.valueOf(tierName);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            Boolean aggressive = mob.getPersistentDataContainer().get(MobKeys.MOB_AGGRESSIVE, PersistentDataType.BOOLEAN);
            Boolean miner = mob.getPersistentDataContainer().get(MobKeys.MOB_MINER, PersistentDataType.BOOLEAN);
            MobTaskService.ACTIVE.put(mob.getUniqueId(),
                    new MobState(mob.getUniqueId(), tier, profile, mob.getWorld().getName(),
                            aggressive != null ? aggressive : profile.category == MobCategory.HOSTILE,
                            miner != null && miner));
            NametagService.reset(mob);
        }
    }

}
