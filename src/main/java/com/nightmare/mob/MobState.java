package com.nightmare.mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.nightmare.tier.MobTier;

public final class MobState {

    public final UUID uuid;
    public final MobTier tier;
    public final MobProfile profile;
    public final String worldName;
    public final boolean aggressive;
    public final Map<String, Long> cooldowns = new HashMap<>();

    public MobState(UUID uuid, MobTier tier, MobProfile profile, String worldName, boolean aggressive) {
        this.uuid = uuid;
        this.tier = tier;
        this.profile = profile;
        this.worldName = worldName;
        this.aggressive = aggressive;
    }

}
