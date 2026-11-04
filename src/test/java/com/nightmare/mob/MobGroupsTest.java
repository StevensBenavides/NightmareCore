package com.nightmare.mob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class MobGroupsTest {

    private static YamlConfiguration mobs;

    @BeforeClass
    public static void loadGroups() {
        mobs = YamlConfiguration.loadConfiguration(new File("src/main/resources/mobs.yml"));
    }

    @Test
    public void allGroupsAreHostile() {
        ConfigurationSection groups = mobs.getConfigurationSection("groups");

        assertTrue("mobs.yml must define groups", groups != null);

        for (String name : groups.getKeys(false)) {
            String category = groups.getString(name + ".category", "HOSTILE");
            assertEquals("group " + name + " must be HOSTILE (no tiers for passive/neutral mobs)",
                    "HOSTILE", category.toUpperCase());
        }
    }

    @Test
    public void passiveAndNeutralTypesHaveNoGroup() {
        Set<String> grouped = new HashSet<>();
        ConfigurationSection groups = mobs.getConfigurationSection("groups");

        assertTrue("mobs.yml must define groups", groups != null);

        for (String name : groups.getKeys(false)) {
            List<String> types = groups.getStringList(name + ".types");
            grouped.addAll(types);
        }

        String[] vanillaOnly = {
                "COW", "MOOSHROOM", "SHEEP", "PIG", "CHICKEN", "RABBIT",
                "HORSE", "DONKEY", "MULE", "CAMEL", "CAT", "OCELOT", "PARROT",
                "VILLAGER", "WANDERING_TRADER", "FROG", "SNIFFER",
                "WOLF", "IRON_GOLEM", "SNOW_GOLEM", "BEE", "LLAMA",
                "POLAR_BEAR", "PANDA", "GOAT", "FOX",
                "DOLPHIN", "SQUID", "TURTLE", "COD", "SALMON"
        };

        for (String type : vanillaOnly) {
            assertFalse(type + " must stay vanilla (no tier group)", grouped.contains(type));
        }
    }

    @Test
    public void undeadSplitKeepsMeleeAndArchers() {
        ConfigurationSection groups = mobs.getConfigurationSection("groups");

        assertTrue("mobs.yml must define groups", groups != null);

        List<String> melee = groups.getStringList("zombie_melee.types");
        assertTrue("zombie_melee must keep ZOMBIE", melee.contains("ZOMBIE"));

        List<String> meleePowers = groups.getStringList("zombie_melee.powers");
        assertFalse("zombie_melee must not shoot arrows", meleePowers.contains("VOLLEY"));

        List<String> archers = groups.getStringList("skeleton_archers.types");
        assertTrue("skeleton_archers must keep SKELETON", archers.contains("SKELETON"));
        assertTrue("skeleton_archers keeps VOLLEY", groups.getStringList("skeleton_archers.powers").contains("VOLLEY"));
    }
}
