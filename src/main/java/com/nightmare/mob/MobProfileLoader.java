package com.nightmare.mob;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public final class MobProfileLoader {

    public static final Map<EntityType, MobProfile> PROFILES = new HashMap<>();

    private MobProfileLoader() {
    }

    public static void load(Plugin plugin) {
        PROFILES.clear();
        File file = new File(plugin.getDataFolder(), "mobs.yml");

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection groups = config.getConfigurationSection("groups");

        if (groups == null) {
            return;
        }

        for (String groupName : groups.getKeys(false)) {
            ConfigurationSection group = groups.getConfigurationSection(groupName);

            if (group == null) {
                continue;
            }

            MobCategory category;

            try {
                category = MobCategory.valueOf(group.getString("category", "HOSTILE").toUpperCase());
            } catch (IllegalArgumentException exception) {
                category = MobCategory.HOSTILE;
            }

            int maxTier = group.getInt("max-tier", category == MobCategory.HOSTILE ? 10 : 9);
            maxTier = Math.min(10, Math.max(0, maxTier));

            if (category != MobCategory.HOSTILE) {
                maxTier = Math.min(maxTier, 9);
            }

            boolean gear = group.getBoolean("gear", category == MobCategory.HOSTILE);
            
            List<String> powers = new ArrayList<>(group.getStringList("powers"));

            for (String typeName : group.getStringList("types")) {
                EntityType type;

                try {
                    type = EntityType.valueOf(typeName.toUpperCase());
                } catch (IllegalArgumentException exception) {
                    continue;
                }

                PROFILES.put(type, new MobProfile(type, category, maxTier, gear, powers));
            }
        }
    }

}
