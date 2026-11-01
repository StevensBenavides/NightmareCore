package com.nightmare.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import com.nightmare.Main;

public final class NightmarePackService implements Listener {

    private static byte[] packBytes;
    private static byte[] sha1;
    private static HttpServer server;
    private static String packUrl;

    private NightmarePackService() {
    }

    public static NightmarePackService register(Plugin plugin) {
        NightmarePackService service = new NightmarePackService();
        init(plugin);
        plugin.getServer().getPluginManager().registerEvents(service, plugin);
        return service;
    }

    private static void init(Plugin plugin) {
        if (!Main.getSettings().getBoolean("resource_pack.enable", true)) {
            return;
        }

        byte[] resource = readResource(plugin);

        if (resource == null || resource.length == 0) {
            plugin.getLogger().warning("NightmareCoreTexturePack.zip not found; skipping pack auto-apply.");
            return;
        }

        packBytes = resource;
        sha1 = sha1(packBytes);

        int port = Integer.parseInt(Main.getSettings().getString("resource_pack.port", "8274"));

        String host = Main.getSettings().getString("resource_pack.host", "localhost");
String path = Main.getSettings().getString("resource_pack.path", "/NightmareCoreTexturePack.zip");
        packUrl = "http://" + host + ":" + port + path;

        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext(path, NightmarePackService::serve);
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("NightmareCoreTexturePack served on " + packUrl);
        } catch (Exception exception) {
            plugin.getLogger().warning("NightmareCoreTexturePack HTTP server failed: " + exception.getMessage());
            server = null;
        }
    }

    private static byte[] readResource(Plugin plugin) {
        try (InputStream input = plugin.getResource("pack/NightmareCoreTexturePack.zip"); ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int length;

            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            return output.toByteArray();

        } catch (Exception exception) {
            Bukkit.getLogger().warning("NightmareCoreTexturePack read failed: " + exception.getMessage());
            return null;
        }
    }

    private static byte[] sha1(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(bytes);
        } catch (Exception exception) {
            return new byte[20];
        }
    }

    private static void serve(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, packBytes.length);
            exchange.getResponseBody().write(packBytes);
            exchange.getResponseBody().close();
        } catch (Exception exception) {
            Bukkit.getLogger().warning("NightmareCoreTexturePack serve failed: " + exception.getMessage());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (packBytes == null || server == null) {
            return;
        }

        boolean force = Main.getSettings().getBoolean("resource_pack.force", true);
        event.getPlayer().setResourcePack(UUID.randomUUID(), packUrl, sha1, "NightmareCore", force);
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

    public static void dispose() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        packBytes = null;
        sha1 = null;
        packUrl = null;
    }

}
