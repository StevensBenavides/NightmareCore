package com.nightmare.world;

import org.bukkit.Material;
import org.bukkit.Particle;

public enum StructureTheme {
    FORGE(0, Material.FURNACE, Particle.FLAME),
    SOUL(4, Material.SOUL_LANTERN, Particle.SOUL_FIRE_FLAME),
    ABYSS(5, Material.OBSIDIAN, Particle.SCULK_SOUL),
    NIGHTMARE(5, Material.WITHER_SKELETON_SKULL, Particle.ASH),
    RELICS(8, Material.ENCHANTED_BOOK, Particle.ENCHANT);

    public final int storyChapter;
    public final Material centerpiece;
    public final Particle ambience;

    StructureTheme(int storyChapter, Material centerpiece, Particle ambience) {
        this.storyChapter = storyChapter;
        this.centerpiece = centerpiece;
        this.ambience = ambience;
    }

    public static StructureTheme byName(String name) {
        if (name == null) {
            return null;
        }

        for (StructureTheme theme : values()) {
            if (theme.name().equalsIgnoreCase(name)) {
                return theme;
            }
        }

        return null;
    }
}
