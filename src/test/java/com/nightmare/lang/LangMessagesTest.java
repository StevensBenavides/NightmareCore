package com.nightmare.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class LangMessagesTest {

    private static YamlConfiguration en;
    private static YamlConfiguration es;

    @BeforeClass
    public static void load() {
        en = YamlConfiguration.loadConfiguration(new File("src/main/resources/lang/en/messages.yml"));
        es = YamlConfiguration.loadConfiguration(new File("src/main/resources/lang/es/messages.yml"));
    }

    @Test
    public void englishAndSpanishHaveSameKeys() {
        Set<String> enKeys = flatten(en);
        Set<String> esKeys = flatten(es);

        assertEquals("key sets differ between en and es", enKeys, esKeys);
    }

    @Test
    public void placeholdersMatchBetweenLanguages() {
        Set<String> keys = flatten(en);

        for (String key : keys) {
            assertEquals("placeholder mismatch at " + key,
                    placeholders(textOf(en, key)), placeholders(textOf(es, key)));
        }
    }

    @Test
    public void criticalKeysHaveNoStrayPlaceholdersInText() {
        String[] keys = {
                "commands.reload.done", "commands.reload.failed",
                "commands.system.applied", "commands.system.gave_item",
                "commands.system.revived_one", "commands.generic.player_offline",
                "craft.unlock_day", "gui.unlocks_on_day"};

        for (String key : keys) {
            assertTrue("missing en key " + key, en.contains(key));
            assertTrue("missing es key " + key, es.contains(key));
        }
    }

    @Test
    public void localizedDisplayKeysArePresent() {
        String[] keys = {
                "assault.title", "cooldown.wait",
                "structures.book_title", "structures.book_author",
                "story.soul_god", "story.abyss_god", "story.foe",
                "relics.soul_heart.name", "relics.abyssal_tendrils.name",
                "gui.title_main", "gui.title_enchants", "gui.title_recipe",
                "gui.title_abyssal_chain", "gui.title_relics", "gui.title_story",
                "gui.title_soul_recipe", "gui.title_tendrils_recipe",
                "gui.enchants_button", "gui.relics_button", "gui.info_relic_soul",
                "gui.origin_label", "gui.recipe_crafting", "gui.recipe_tier",
                "gui.recipe_catalyst", "gui.recipe_book", "gui.recipe_ink",
                "gui.recipe_nucleus", "gui.recipe_howl", "gui.recipe_core",
                "gui.recipe_tendrils", "gui.recipe_soul",
                "stats.hits", "stats.anti_heal", "stats.distance", "stats.reduction",
                "stats.blocks", "stats.blocks_diameter", "stats.permanent", "stats.hold",
                "stats.sneak_left", "stats.sneak_right",
                "stats.abyssal_burst", "stats.abyssal_cataclysm"};

        for (String key : keys) {
            assertTrue("missing en key " + key, en.contains(key));
            assertTrue("missing es key " + key, es.contains(key));
            assertEquals("placeholder mismatch at " + key,
                    placeholders(textOf(en, key)), placeholders(textOf(es, key)));
        }
    }

    @Test
    public void relicGuiLoreListsArePresent() {
        String[] keys = {"relic_soul_heart", "relic_tendrils", "recipe_tendrils_book", "recipe_soul_heart_book"};

        for (String key : keys) {
            assertFalse("en gui." + key + " empty", en.getStringList("gui." + key).isEmpty());
            assertFalse("es gui." + key + " empty", es.getStringList("gui." + key).isEmpty());
        }
    }

    private static Set<String> flatten(ConfigurationSection section) {
        Map<String, Object> values = new HashMap<>();
        collect(section, "", values);
        return values.keySet();
    }

    private static void collect(ConfigurationSection section, String prefix, Map<String, Object> out) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(key);
            String path = prefix.isEmpty() ? key : prefix + "." + key;

            if (child != null) {
                collect(child, path, out);
            } else {
                out.put(path, section.get(key));
            }
        }
    }

    private static String textOf(YamlConfiguration messages, String key) {
        String value = messages.getString(key);

        if (value != null) {
            return value;
        }

        return String.join("\n", messages.getStringList(key));
    }

    private static Set<String> placeholders(String text) {
        Set<String> result = new HashSet<>();

        if (text == null) {
            return result;
        }

        Matcher matcher = Pattern.compile("%[a-zA-Z_]+%").matcher(text);

        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }
}
