package com.nightmare.Mobs;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;

import net.md_5.bungee.api.ChatColor;

public final class Tasks {

    private final YamlConfiguration config = Main.getSettings();
    private final Plugin plugin = Main.getInstance();

    public void setEffectsMobs() {

        File settings = new File(plugin.getDataFolder(), "settings.yml");   

        if (!settings.exists())

            try {
                throw new IOException("settings.yml does not exist.");
            } catch (IOException e) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                e.printStackTrace();
            }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            Predicate<World> filterWorld = world -> world.getEnvironment() == World.Environment.NORMAL;
            Predicate<Entity> filterPlayer = entity -> entity instanceof Player;
            
            for (World world : plugin.getServer().getWorlds().stream().filter(filterWorld).toList()) {

                for (Entity mob : world.getEntitiesByClasses(Zombie.class, Creeper.class, Spider.class, Enderman.class)) {

                    if (mob instanceof Zombie) {

                        final Zombie zombie = (Zombie) mob;
                        final String c = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("config.mobs.c")).replace("%mob%", Zombie.class.getSimpleName()));

                        if (zombie.getCustomName() != null && zombie.getCustomName().equalsIgnoreCase(c) && !zombie.isDead()) {
                            world.spawnParticle(Particle.EXPLOSION, zombie.getLocation(), 60);
                            world.playSound(zombie.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 1);
                        }

                    } else if (mob instanceof Creeper) {

                        final Creeper creeper = (Creeper) mob;

                        final String c = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("config.mobs.c")).replace("%mob%", Creeper.class.getSimpleName()));

                        if (creeper.getCustomName() != null && creeper.getCustomName().equalsIgnoreCase(c) && !creeper.isDead() && creeper.getTarget() == null) {

                            for (Entity entity : creeper.getNearbyEntities(15.0, 15.0, 15.0).stream().filter(filterPlayer).toList()) {
                                Player player = (Player) entity;

                                if (player.getGameMode() == GameMode.SURVIVAL)
                                    creeper.setTarget(player);
                            }

                        } else if (creeper.getCustomName() != null && creeper.getCustomName().equalsIgnoreCase(c) && !creeper.isDead() && creeper.getTarget() == null) {

                            for (Entity entity : creeper.getNearbyEntities(15.0, 15.0, 15.0).stream().filter(filterPlayer).toList()) {
                                Player player = (Player) entity;

                                if (player.getGameMode() == GameMode.SURVIVAL)
                                    creeper.setTarget(player);
                            }

                        }

                    } else if (mob instanceof Spider) {

                        final Spider spider = (Spider) mob;
                        final String c = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("config.mobs.c")).replace("%mob%", Spider.class.getSimpleName()));

                        if (spider.getCustomName() != null && spider.getCustomName().equalsIgnoreCase(c) && !spider.isDead() && spider.getTarget() != null) {
                            if (spider.getTarget() instanceof Player) {

                                Player player = (Player) spider.getTarget();

                                for (int i = 0; i < 3; i++) {
                                    spider.getWorld().spawnEntity(player.getLocation(), EntityType.LIGHTNING_BOLT);
                                }
                            }

                        }

                    } else if (mob instanceof Enderman) {

                        final Enderman enderman = (Enderman) mob;

                        final String c = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("config.mobs.c")).replace("%mob%", Enderman.class.getSimpleName()));

                        if (enderman.getCustomName() != null && enderman.getCustomName().equalsIgnoreCase(c) && !enderman.isDead()) {

                            enderman.getWorld().spawnParticle(Particle.END_ROD, enderman.getLocation(), 60);

                        } else if (enderman.getCustomName() != null && enderman.getCustomName().equalsIgnoreCase(c) && !enderman.isDead()) {

                            enderman.getWorld().spawnParticle(Particle.PORTAL, enderman.getLocation(), 60);

                        }

                    }

                }

            }
            
        }, 0, 30L);

    } 
    
}
