package com.nightmare;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Guards against compile-time/runtime API drift between the Spigot API used
 * to build the plugin and the Paper runtime it runs on. Direct references to
 * renamed Bukkit members (e.g. GameRule fields, removed player methods) compile
 * fine but explode at runtime, so they are banned in favor of name-based lookups.
 */
public class ApiSurfaceTest {

    private static final Pattern GAME_RULE_FIELD = Pattern.compile("GameRule\\.([A-Z][A-Z0-9_]*)\\b");
    private static final String[] FORBIDDEN_CALLS = {
            "getBedSpawnLocation(",
    };

    @Test
    public void noDirectGameRuleFieldReferences() {
        List<String> violations = new ArrayList<>();

        for (File file : javaFiles(new File("src/main/java"))) {
            String content = read(file);
            Matcher matcher = GAME_RULE_FIELD.matcher(content);

            while (matcher.find()) {
                if (matcher.group(1).equals("getByName")) {
                    continue;
                }

                violations.add(file.getPath() + ": direct GameRule field '"
                        + matcher.group(1) + "' (use GameRule.getByName instead)");
            }

            for (String forbidden : FORBIDDEN_CALLS) {
                if (content.contains(forbidden)) {
                    violations.add(file.getPath() + ": forbidden call '" + forbidden
                            + "' missing from the Paper runtime API");
                }
            }
        }

        assertTrue("API surface violations: " + violations, violations.isEmpty());
    }

    private static void failRead(String message) {
        throw new AssertionError(message);
    }

    private static List<File> javaFiles(File root) {
        List<File> result = new ArrayList<>();
        File[] children = root.listFiles();

        if (children == null) {
            return result;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                result.addAll(javaFiles(child));
            } else if (child.getName().endsWith(".java")) {
                result.add(child);
            }
        }

        return result;
    }

    private static String read(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException exception) {
            failRead("Could not read " + file.getPath() + ": " + exception.getMessage());
            return "";
        }
    }
}
