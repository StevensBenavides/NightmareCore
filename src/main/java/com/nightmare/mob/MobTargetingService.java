package com.nightmare.mob;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public final class MobTargetingService implements Listener {

    public static boolean isTargetable(Player player) {
        return isTargetable(player.getGameMode());
    }

    public static boolean isTargetable(GameMode gameMode) {
        return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && !isTargetable(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (isTargetable(event.getNewGameMode())) {
            return;
        }

        Player player = event.getPlayer();

        for (MobState state : MobTaskService.ACTIVE.values()) {
            if (Bukkit.getEntity(state.uuid) instanceof Mob mob
                    && player.equals(mob.getTarget())) {
                mob.setTarget(null);
            }
        }
    }

}
