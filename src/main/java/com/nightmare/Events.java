package com.nightmare;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nightmare.FastBoard.FastBoard;
import com.nightmare.lang.LangService;
import com.nightmare.player.StarterKitService;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;


import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public final class Events implements Listener {

    private final Plugin plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {

        final YamlConfiguration config = Main.getSettings();

        try {

            StarterKitService.give(event.getPlayer());

            if (config.getBoolean("scoreboard.enable")) {

                FastBoard board = new FastBoard(event.getPlayer());

                board.updateTitle(ChatColor.translateAlternateColorCodes('&',
                        PlaceholderAPI.setPlaceholders(event.getPlayer(),
                                config.getString("scoreboard.name", ""))));

                NightmareScoreboard.boards.put(event.getPlayer().getUniqueId(), board);

            }

            if (config.getBoolean("join_and_leave.enable")) {

                String message = config.getString("prefix", "")
                        + config.getString("join_and_leave.join", "");

                message = PlaceholderAPI.setPlaceholders(event.getPlayer(), message);

                event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', message));

            }

            if (config.getBoolean("message_join.enable")) {

                ArrayList<String> tempLines = new ArrayList<String>();

                for (String line : config.getStringList("message_join.lines")) {
                    tempLines.add(ChatColor.translateAlternateColorCodes('&',
                            PlaceholderAPI.setPlaceholders(event.getPlayer(), line)));
                }

                for (String line : tempLines) {
                    event.getPlayer().sendMessage(line);
                }

                String joinTitle = ChatColor.translateAlternateColorCodes('&',
                        PlaceholderAPI.setPlaceholders(event.getPlayer(),
                                config.getString("message_join.title", "")));

                if (!joinTitle.isBlank()) {
                    event.getPlayer().sendTitle(joinTitle, null, 20, 20, 10);
                }
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.8F, 1.0F);

            }

            if (config.getBoolean("join_guide.enable")) {
                long delay = Math.max(0L, config.getLong("join_guide.delay_ticks", 100L));
                UUID joinedUuid = event.getPlayer().getUniqueId();

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Player joined = plugin.getServer().getPlayer(joinedUuid);

                    if (joined == null || !joined.isOnline()) {
                        return;
                    }

                    if (!Main.getSettings().getBoolean("join_guide.enable", true)) {
                        return;
                    }

                    sendJoinGuide(joined);
                }, delay);
            }

            if (config.getBoolean("config.player.display_health")) {

                ScoreboardManager manager = event.getPlayer().getServer().getScoreboardManager();
                Scoreboard score = manager.getNewScoreboard();

                Objective obj_health_below = score.registerNewObjective("player_health", Criteria.HEALTH, ChatColor.translateAlternateColorCodes('&', "&c♥"));

                obj_health_below.setDisplaySlot(DisplaySlot.BELOW_NAME);

                event.getPlayer().setScoreboard(score);
                event.getPlayer().damage(0.001F);

                Objective obj_health_tab = score.registerNewObjective("player_health_tab", Criteria.HEALTH, ChatColor.translateAlternateColorCodes('&', "&c♥"));

                obj_health_tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                obj_health_tab.setRenderType(RenderType.HEARTS);

                event.getPlayer().setScoreboard(score);
                event.getPlayer().damage(0.001F);

            }

            {
                Player player = event.getPlayer();
                UUID playerUUID = player.getUniqueId();

                TimeManagement.addPlayerTimeIfAbsent(playerUUID);

                TimeManagement.getSpecificPlayerTime(playerUUID).ifPresent(playerTime -> {

                    if (player.getGameMode() == GameMode.SURVIVAL && playerTime.getThirstyMinutes() >= 15) {

                        player.setMetadata("thristy", new FixedMetadataValue(plugin, true));

                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().warning("Nightmare event error: " + e.getMessage());
        }

    }

    private record GuideEntry(String command, String descriptionKey, String fallback, String permission) {
    }

    private static final List<GuideEntry> JOIN_GUIDE = List.of(
            new GuideEntry("/nightmare help", "guide.help", "Open the main guide menu.", null),
            new GuideEntry("/nightmare story", "guide.story", "Open the nightmare story.", null));

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {

        Player player = event.getEntity();

        player.setMetadata("thristy", new FixedMetadataValue(plugin, false));

        {
            UUID uuid = player.getUniqueId();
            Optional<PlayerTime> currentPlayerTime = TimeManagement.getSpecificPlayerTime(uuid);

            if (currentPlayerTime.isPresent()) {
                PlayerTime playerTime = currentPlayerTime.get();
                playerTime.resetThirstyMinutes();
            }
        }

        {

            try {

                if (player.getGameMode() == GameMode.SURVIVAL) {
                    final YamlConfiguration config = Main.getSettings();
                    final boolean isDeathMessageEnabled = config.getBoolean("message_death.enable");

                    if (isDeathMessageEnabled) {
                        String baseTitle = config.getString("message_death.title", "")
                                .replace("%player_killed%", player.getName());
                        String baseSubtitle = config.getString("message_death.subtitle", "")
                                .replace("%player_killed%", player.getName());

                        for (Player outSidePlayer : player.getServer().getOnlinePlayers()) {
                            if (outSidePlayer == null) {
                                continue;
                            }

                            String titleMessage = ChatColor.translateAlternateColorCodes('&',
                                    PlaceholderAPI.setPlaceholders(outSidePlayer, baseTitle));
                            String subtitleMessage = ChatColor.translateAlternateColorCodes('&',
                                    PlaceholderAPI.setPlaceholders(outSidePlayer, baseSubtitle));

                            outSidePlayer.sendTitle(titleMessage, subtitleMessage, 10, 40, 10);

                            outSidePlayer.playSound(
                                outSidePlayer.getLocation(),
                                Sound.ITEM_TRIDENT_RETURN,
                                0.9f,
                                1.0f
                            );

                            outSidePlayer.playSound(
                                outSidePlayer.getLocation(),
                                Sound.ITEM_TOTEM_USE,
                                0.8f,
                                1.0f
                            );

                        }

                    }

                    final String worldName = player.getWorld().getName();
                    final Optional<WorldTime> worldTimeOpt = TimeManagement.getSpecificWorldTime(worldName);

                    if (worldTimeOpt.isPresent()) {
                        WorldTime worldTime = worldTimeOpt.get();
                        long day = worldTime.getDay();
                        int minutes;

                        if (day < 27) {
                            minutes = Main.balance.getInt("death-ban.days-0-26", 5);
                        } else if (day < 54) {
                            minutes = Main.balance.getInt("death-ban.days-27-53", 10);
                        } else if (day < 81) {
                            minutes = Main.balance.getInt("death-ban.days-54-80", 15);
                        } else {
                            minutes = Main.balance.getInt("death-ban.days-81-100", 20);
                        }

                        Date expirationDate = new Date(System.currentTimeMillis() + minutes * 60L * 1000L);

                        if (!player.hasPermission("nightmare.bypass.deathban")) {
                            player.ban(ChatColor.translateAlternateColorCodes('&',
                                    LangService.msg("death.ban_reason",
                                            "You fall into the nightmare.")),
                                    expirationDate, "NightmareCore", true);
                        }
                    }
                                    }

            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().warning("Nightmare event error: " + e.getMessage());
            }

        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemConsumeEvent(PlayerItemConsumeEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        {
            if (player.getGameMode() == GameMode.SURVIVAL) {

                final Optional<WorldTime> optionalWorldTime = TimeManagement.getSpecificWorldTime(player.getWorld().getName());

                if (optionalWorldTime.isPresent()) {
                    final WorldTime worldTime = optionalWorldTime.get();
                    final Randomness random = new Randomness();

                    if (worldTime.isDayBelow(20)) {
                        if (random.is5percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    } else if (worldTime.isDayBelow(40)) {
                        if (random.is15percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    }  else if (worldTime.isDayBelow50()) {
                        if (random.is15percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    } else {
                        if (random.is15percent()) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 0.7f);
                        }
                    }

                }


            }

            if (event.isCancelled()) {
                return;
            }

            if (player.getGameMode() == GameMode.SURVIVAL) {

                if (item.getType() == Material.POTION || item.getType() == Material.GLASS_BOTTLE) {

                    if (item.getItemMeta() instanceof PotionMeta meta) {

                    if (meta.getBasePotionType() == PotionType.WATER) {

                        if (player.hasMetadata("thristy")) {

                            boolean isThristy = false;

                            for (MetadataValue metadataValue : player.getMetadata("thristy")) {
                                if (metadataValue.getOwningPlugin() == plugin) {
                                    isThristy = metadataValue.asBoolean();
                                    break;
                                }
                            }

                            if (isThristy) {
                                UUID uuid = player.getUniqueId();

                                Optional<PlayerTime> currentPlayerTime = TimeManagement.getSpecificPlayerTime(uuid);

                                if (currentPlayerTime.isPresent()) {
                                    PlayerTime playerTime = currentPlayerTime.get();
                                    playerTime.resetThirstyMinutes();

                                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                                    player.removePotionEffect(PotionEffectType.SLOWNESS);

                                    player.setMetadata("thristy", new FixedMetadataValue(plugin, false));
                                }

                            }
                        }

                    }

                    }

                }

            }

        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeave(PlayerQuitEvent event) {

        try {

            final YamlConfiguration config = Main.getSettings();

            if (config.getBoolean("scoreboard.enable")) {
                FastBoard board =
                        NightmareScoreboard.boards.remove(event.getPlayer().getUniqueId());

                if (board != null) {
                    board.delete();
                }
            }

            if (config.getBoolean("join_and_leave.enable")) {

                String message = config.getString("prefix", "")
                        + config.getString("join_and_leave.leave", "");

                message = PlaceholderAPI.setPlaceholders(event.getPlayer(), message);

                event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', message));

            }

        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().warning("Nightmare event error: " + e.getMessage());
        }

    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerPing(ServerListPingEvent event) {

        try {
            final YamlConfiguration config = Main.getSettings();

            if (config.getBoolean("motd.enable")) {

                java.util.List<String> motdLines = new java.util.ArrayList<>();

                for (String line : config.getStringList("motd.lines")) {
                    motdLines.add(ChatColor.translateAlternateColorCodes('&', line));
                }

                event.setMotd(String.join("\n", motdLines));

            }

        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().warning("Nightmare event error: " + e.getMessage());
        }

    }

    private static void sendJoinGuide(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                LangService.msg("guide.header",
                        "&6&lNightmare Guide &7| &fUseful commands:")));

        for (GuideEntry entry : JOIN_GUIDE) {
            if (entry.permission() != null && !player.hasPermission(entry.permission())) {
                continue;
            }

            String description = LangService.msg(entry.descriptionKey(), entry.fallback());
            String line = "&f" + entry.command() + " &7- " + description;

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    PlaceholderAPI.setPlaceholders(player, line)));
        }
    }

}
