package com.nightmare.core;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import com.nightmare.Main;

public final class NightmarePackService implements Listener {

    private static final String PACK_URL =
            "https://raw.githubusercontent.com/StevensBenavides/NightmareCore/refs/heads/master/pack/NightmareCoreTexturePack.zip";

    private static final String PACK_PROMPT = "NightmareCore";

    private static final boolean PACK_FORCE = true;

    private static String packUrl;

    private NightmarePackService() {
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED -> Bukkit.getLogger().info("NightmareCoreTexturePack applied.");
            case DECLINED -> Bukkit.getLogger().warning("NightmareCoreTexturePack declined.");
            case FAILED_DOWNLOAD -> Bukkit.getLogger().warning("NightmareCoreTexturePack download failed.");
            default -> {
            }
        }
    }

    private static void init(Plugin plugin) {
        if (!Main.getSettings().getBoolean("resource_pack.enable", true)) {
            return;
        }

        packUrl = PACK_URL;

        plugin.getLogger().info("NightmareCoreTexturePack URL: " + packUrl);
    }

    public static NightmarePackService register(Plugin plugin) {
        NightmarePackService service = new NightmarePackService();
        init(plugin);
        plugin.getServer().getPluginManager().registerEvents(service, plugin);
        return service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (packUrl == null) {
            return;
        }

        event.getPlayer().setResourcePack(UUID.randomUUID(), packUrl, null, PACK_PROMPT, PACK_FORCE);
    }

    public static void shutdown() {
        packUrl = null;
    }

}
