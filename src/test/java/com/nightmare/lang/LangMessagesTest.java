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
                    placeholders(en.getString(key)), placeholders(es.getString(key)));
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
