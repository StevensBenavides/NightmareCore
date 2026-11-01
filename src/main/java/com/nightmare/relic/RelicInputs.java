package com.nightmare.relic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

/**
 * Single source of truth for the client sprint flag used by relic combos.
 * Bukkit's own isSprinting() can lag behind the real input, and raw input
 * packets can go stale (death, teleport, world change), so input is stored
 * with a timestamp and expires shortly after the last sprint packet.
 */
public final class RelicInputs {

    private static final long SPRINT_TTL_MS = 1500L;
    private static final Map<UUID, Long> SPRINT_INPUT_AT = new ConcurrentHashMap<>();

    private RelicInputs() {
    }

    public static void recordSprint(UUID uuid, boolean sprinting) {
        if (sprinting) {
            SPRINT_INPUT_AT.put(uuid, System.currentTimeMillis());
        } else {
            SPRINT_INPUT_AT.remove(uuid);
        }
    }

    public static boolean isSprinting(Player player) {
        if (player.isSprinting()) {
            return true;
        }
        Long lastSprint = SPRINT_INPUT_AT.get(player.getUniqueId());
        return lastSprint != null && System.currentTimeMillis() - lastSprint <= SPRINT_TTL_MS;
    }

    public static void clear(UUID uuid) {
        SPRINT_INPUT_AT.remove(uuid);
    }

    public static void clearAll() {
        SPRINT_INPUT_AT.clear();
    }
}
