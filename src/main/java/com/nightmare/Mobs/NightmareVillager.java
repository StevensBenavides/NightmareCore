package com.nightmare.Mobs;

import java.util.Optional;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;

import com.nightmare.Main;
import com.nightmare.Randomness;
import com.nightmare.TimeManagement;
import com.nightmare.WorldTime;

public class NightmareVillager {
    NightmareVillager(Entity entity, YamlConfiguration config) {
        final Randomness randomness = new Randomness();
        final Villager villager = (Villager) entity;
    
        final Optional<WorldTime> currentWorldTime = TimeManagement.getSpecificWorldTime(villager.getWorld().getName());
    
        if (currentWorldTime.isPresent()) {
            final WorldTime worldTime = currentWorldTime.get();

            if (villager.isAdult()) {
                
                if (worldTime.isDayBelow(20)) {
                    if (randomness.is5percent()) {
                        villager.setMetadata("AngryVillager", new FixedMetadataValue(Main.getInstance(), true));
                    }
                } 
                
                else if (worldTime.isDayBelow(50)) {
                    if (randomness.is10percent()) {
                        villager.setMetadata("AngryVillager", new FixedMetadataValue(Main.getInstance(), true));
                    }
                } 
                
                else if (worldTime.isDayAbove(50)) {
                    if (randomness.is15percent()) {
                        villager.setMetadata("AngryVillager", new FixedMetadataValue(Main.getInstance(), true));
                    }
                }
            }
        }
    }
    
}
