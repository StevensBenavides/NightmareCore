package com.nightmare.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import com.nightmare.IO;
import com.nightmare.Main;
import com.nightmare.TimeManagement;
import com.nightmare.core.DifficultyService;
import com.nightmare.enchant.CustomEnchant;
import com.nightmare.enchant.EnchantBookService;
import com.nightmare.enchant.EnchantLoreService;
import com.nightmare.enchant.EnchantRegistry;
import com.nightmare.gui.GuiManager;
import com.nightmare.item.CustomItemService;
import com.nightmare.lang.LangService;
import com.nightmare.mob.MobProfileLoader;
import com.nightmare.relic.AbyssalTendrilsItem;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class NightmareCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ROOT = List.of("help", "reload", "time", "creator", "system", "story");
    private static final List<String> RELOAD_TARGETS = List.of(
            "messages.yml", "settings.yml", "language", "time", "enchants.yml", "balance.yml", "mobs.yml", "enchant-lore.yml", "story.yml", "structures.yml", "all");
    private static final List<String> SYSTEM = List.of("day", "enchant", "item", "relic", "cooldown", "reload", "altar", "revive");
    private static final List<String> ITEM_IDS = List.of(
            "soul_heart", "abyssal_tendrils", "abyssal_ink", "abyssal_nucleus", "abyssal_howl", "abyssal_core");
    private static final List<String> TIME = List.of("add", "remove");
    private static final List<String> HELP = List.of("enchants", "relics", "commands");
    private static final String[] ROMAN = { "I", "II", "III", "IV", "V" };

    private final Plugin plugin = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                GuiManager.openMain(player);
            } else {
                sendHelpText(sender);
            }

            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help", "h" -> handleHelp(sender, args);
            case "reload" -> handleReload(sender, args);
            case "time" -> handleTime(sender, args);
            case "creator", "creators" -> handleCreator(sender, args);
            case "system", "sys" -> handleSystem(sender, args);
            case "story", "chronicle" -> handleStory(sender, args);
            default -> send(sender, LangService.msg("commands.generic.unknown_subcommand",
                    "&cUnknown subcommand. Use &f/nightmare help commands&c."));
        }

        return true;
    }

    private void handleHelp(CommandSender sender, String[] args) {
        if (args.length > 2) {
            sendUsage(sender, "/nightmare help [enchants|relics|commands|<ench>]");
            return;
        }

        if (args.length == 1) {
            if (sender instanceof Player player) {
                GuiManager.openMain(player);
            } else {
                sendHelpText(sender);
            }

            return;
        }

        String target = args[1].toLowerCase();

        if (target.equals("commands")) {
            sendCommandHelp(sender);
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.YELLOW + LangService.msg("commands.generic.gui_only_players",
                    "The Nightmare help GUI is only available to players."));
            sendHelpText(sender);
            return;
        }

        if (target.equals("enchants")) {
            GuiManager.openEnchantList(player);
            return;
        }

        if (target.equals("relics")) {
            GuiManager.openRelics(player);
            return;
        }

        String id = normalizeEnchant(args[1]);

        if (id == null) {
            send(sender, LangService.format("commands.generic.unknown_enchant",
                    "&cUnknown enchant or help category '&f%input%&c'.", "%input%", args[1]));
            return;
        }

        GuiManager.openEnchantDetail(player, id, id.equals("nightmare_mark") ? 5 : 1);
    }

    private void handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nightmare.reload")) {
            sendPermission(sender);
            return;
        }

        if (args.length != 2) {
            sendUsage(sender, "/nightmare reload <" + String.join("|", RELOAD_TARGETS) + ">");
            return;
        }

        reloadTarget(sender, args[1].toLowerCase());
    }

    private void reloadTarget(CommandSender sender, String target) {
        try {
            switch (target) {
                case "messages.yml" -> IO.reloadMessages(plugin);
                case "settings.yml" -> {
                    IO.reloadSettings(plugin);
                    LangService.reload(plugin);
                }
                case "language" -> LangService.reload(plugin);
                case "time" -> {
                    TimeManagement.updatePlayerTimes();
                    TimeManagement.updateWorldsTimes();
                }
                case "enchants.yml" -> {
                    Main.enchants = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchants.yml"));
                    EnchantBookService.registerRecipes(plugin);
                }
                case "balance.yml" -> {
                    Main.balance = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "balance.yml"));
                    com.nightmare.relic.RelicRecipeService.register(plugin);
                }
                case "mobs.yml" -> MobProfileLoader.load(plugin);
                case "enchant-lore.yml" -> Main.enchantLore = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchant-lore.yml"));
                case "story.yml" -> com.nightmare.gui.StoryService.load(plugin);
                case "structures.yml" -> com.nightmare.world.StructureService.load(plugin);
                case "all" -> {
                    IO.reloadMessages(plugin);
                    IO.reloadSettings(plugin);
                    LangService.reload(plugin);
                    TimeManagement.updatePlayerTimes();
                    TimeManagement.updateWorldsTimes();
                    Main.enchants = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchants.yml"));
                    Main.balance = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "balance.yml"));
                    MobProfileLoader.load(plugin);
                    Main.enchantLore = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchant-lore.yml"));
                    com.nightmare.gui.StoryService.load(plugin);
                    com.nightmare.world.StructureService.load(plugin);
                    EnchantBookService.registerRecipes(plugin);
                    com.nightmare.relic.RelicRecipeService.register(plugin);
                }
                default -> {
                send(sender, LangService.format("commands.generic.unknown_reload_target",
                    "&cUnknown reload target '&f%target%&c'.", "%target%", target));
                    return;
                }
            }

            send(sender, LangService.format("commands.reload.done",
                    "&aReloaded &f%target%&a.", "%target%", target));
        } catch (Exception exception) {
            send(sender, LangService.format("commands.reload.failed",
                    "&cCould not reload &f%target%&c: %error%", "%target%", target, "%error%", exception.getMessage()));
        }
    }

    private void handleTime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nightmare.system")) {
            sendPermission(sender);
            return;
        }

        if (args.length != 3) {
            sendUsage(sender, "/nightmare time <add|remove> <days>");
            return;
        }

        int days;

        try {
            days = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            send(sender, LangService.msg("commands.time.invalid_days",
                    "&cInvalid number of days."));
            return;
        }

        if (days <= 0) {
            send(sender, LangService.msg("commands.time.days_positive",
                    "&cDays must be a positive number."));
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            long ticks = days * 24000L;

            for (World world : Bukkit.getWorlds()) {
                world.setFullTime(world.getFullTime() + ticks);
            }

            TimeManagement.updateWorldsTimes();
            send(sender, LangService.format("commands.time.added_days",
                    "&aAdded &6%days%&a day(s) to all worlds.", "%days%", String.valueOf(days)));
            return;
        }

        if (args[1].equalsIgnoreCase("remove")) {
            long ticks = days * 24000L;

            for (World world : Bukkit.getWorlds()) {
                if (world.getFullTime() - ticks < 24000L) {
                    send(sender, LangService.msg("commands.time.cannot_below_day1",
                            "&cYou cannot set any world below day 1."));
                    return;
                }
            }

            for (World world : Bukkit.getWorlds()) {
                world.setFullTime(world.getFullTime() - ticks);
            }

            TimeManagement.updateWorldsTimes();
            send(sender, LangService.format("commands.time.removed_days",
                    "&aRemoved &6%days%&a day(s) from all worlds.", "%days%", String.valueOf(days)));
            return;
        }

        sendUsage(sender, "/nightmare time <add|remove> <days>");
    }

    private void handleSystem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nightmare.system")) {
            sendPermission(sender);
            return;
        }

        if (args.length < 2) {
            sendUsage(sender, "/nightmare system <day|enchant|item|relic|cooldown|reload|altar|revive>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "day" -> {
                if (args.length != 3) {
                    sendUsage(sender, "/nightmare system day <day>");
                    return;
                }

                long day;

                try {
                    day = Long.parseLong(args[2]);
                } catch (NumberFormatException exception) {
                send(sender, LangService.msg("commands.system.invalid_day",
                        "&cInvalid day."));
                    return;
                }

                if (day < 0) {
                send(sender, LangService.msg("commands.system.day_positive",
                        "&cDay must be 0 or higher."));
                    return;
                }

                if (sender instanceof Player player) {
                    player.getWorld().setFullTime(day * 24000L);
                    TimeManagement.updateWorldsTimes();
                    DifficultyService.apply(player.getWorld());
                } else {
                    for (World world : Bukkit.getWorlds()) {
                        world.setFullTime(day * 24000L);
                        DifficultyService.apply(world);
                    }

                    TimeManagement.updateWorldsTimes();
                }

                send(sender, LangService.format("commands.system.day_set",
                        "&aDay set to &6%day%&a.", "%day%", String.valueOf(day)));
            }
            case "enchant", "ench" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, LangService.msg("commands.system.enchant_only_player",
                            "&cThe enchant command can only be used by a player with an item in hand."));
                    return;
                }

                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system enchant <ench> <level>");
                    return;
                }

                String id = normalizeEnchant(args[2]);

                if (id == null) {
                    send(sender, LangService.format("commands.system.unknown_enchant",
                        "&cUnknown enchant '&f%input%&c'. Use &f/nightmare help enchants&c.", "%input%", args[2]));
                    return;
                }

                CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
                int level;

                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    send(sender, LangService.msg("commands.system.invalid_level",
                            "&cInvalid level."));
                    return;
                }

                if (level < 1 || level > enchant.maxLevel) {
                    send(sender, LangService.format("commands.system.level_range",
                            "&cLevel must be between 1 and %max%.", "%max%", String.valueOf(enchant.maxLevel)));
                    return;
                }

                if (id.equals("nightmare_mark") && level != 5) {
                    send(sender, LangService.msg("commands.system.mark_only_v",
                            "&cNightmare's Mark is only available at level V."));
                    return;
                }

                if (id.equals("abyssal_howl") && level != 1) {
                    send(sender, LangService.msg("commands.system.howl_single",
                            "&cAbyssal Howl is a single legendary level."));
                    return;
                }

                ItemStack item = player.getInventory().getItemInMainHand();

                if (item == null || item.getType() == Material.AIR) {
                    send(sender, LangService.msg("commands.system.must_hold_item",
                            "&cYou must hold an item."));
                    return;
                }

                if (!enchant.applicable.contains(item.getType())) {
                    send(sender, LangService.format("commands.system.cannot_apply",
                            "&c%enchant% cannot be applied to this item. Applicable on: %applicable%.",
                            "%enchant%", enchant.displayName,
                            "%applicable%", EnchantLoreService.applicabilitySummary(enchant)));
                    return;
                }

                CustomItemService.applyEnchant(item, id, level);
                send(sender, LangService.format("commands.system.applied",
                        "&aApplied &6%enchant% %level%&a.",
                        "%enchant%", enchant.displayName, "%level%", ROMAN[level - 1]));
            }
            case "item" -> {
                if (args.length == 4 && args[2].equalsIgnoreCase("info")) {
                    if (!(sender instanceof Player player)) {
                        send(sender, LangService.msg("commands.system.only_players_inspect",
                            "&cOnly players can inspect the held item."));
                        return;
                    }

                    ItemStack held = player.getInventory().getItemInMainHand();
                    org.bukkit.inventory.meta.ItemMeta meta = held.getItemMeta();
                    String model = meta != null && meta.hasItemModel() && meta.getItemModel() != null
                            ? meta.getItemModel().toString()
                            : "none";
                    send(sender, LangService.format("commands.system.held_info",
                            "&7Held: &f%material% &8| &7item_model: &f%model%",
                            "%material%", held.getType().name(),
                            "%model%", model));
                    return;
                }

                if (args.length != 5 || !args[2].equalsIgnoreCase("give")) {
                    sendUsage(sender, "/nightmare system item give <player> <item>");
                    sendUsage(sender, "/nightmare system item info");
                    return;
                }

                Player target = Bukkit.getPlayerExact(args[3]);

                if (target == null) {
                    send(sender, LangService.format("commands.generic.player_offline",
                            "&cPlayer '&f%player%&c' is not online.", "%player%", args[3]));
                    return;
                }

                String itemId = normalizeItem(args[4]);

                if (itemId == null) {
                    send(sender, LangService.format("commands.system.unknown_item",
                            "&cUnknown item '&f%input%&c'. Use &f/nightmare system item give <player> <item>&c.",
                            "%input%", args[4]));
                    return;
                }

                if (itemId.equals("soul_heart")) {
                    com.nightmare.relic.SoulHeartService.give(target);
                    send(sender, LangService.format("commands.system.gave_soul_heart",
                            "&aGave &fSoul Heart&a to &f%player%&a.", "%player%", target.getName()));
                    return;
                }

                if (itemId.equals("abyssal_tendrils")) {
                    com.nightmare.relic.AbyssalTendrilsService.give(target);
                    send(sender, LangService.format("commands.system.gave_tendrils",
                            "&aGave &5Abyssal Tendrils&a to &f%player%&a.", "%player%", target.getName()));
                    return;
                }

                ItemStack item = switch (itemId) {
                    case "abyssal_ink" -> EnchantBookService.createAbyssalInk();
                    case "abyssal_nucleus" -> EnchantBookService.createAbyssalNucleus();
                    case "abyssal_howl", "abyssal_core" -> EnchantBookService.createBook(itemId, 1);
                    default -> null;
                };

                if (item == null) {
                    send(sender, LangService.format("commands.system.could_not_create",
                            "&cCould not create '&f%input%&c'.", "%input%", args[4]));
                    return;
                }

                if (!target.getInventory().addItem(item).isEmpty()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), item);
                }

                send(sender, LangService.format("commands.system.gave_item",
                        "&aGave &f%item%&a to &f%player%&a.",
                        "%item%", itemId, "%player%", target.getName()));
            }
            case "relic" -> {
                if (args.length < 4 || args.length > 5) {
                    sendUsage(sender, "/nightmare system relic <give|remove|restore|info> <player> [relic]");
                    return;
                }

                String action = args[2].toLowerCase();
                Player target = Bukkit.getPlayerExact(args[3]);

                if (target == null) {
                    send(sender, LangService.format("commands.generic.player_offline",
                            "&cPlayer '&f%player%&c' is not online.", "%player%", args[3]));
                    return;
                }

                if (action.equals("info")) {
                    send(sender, "&f" + target.getName() + "&7:");
                    send(sender, LangService.format("commands.system.relic_state",
                            "&7%label%: &f%state%",
                            "%label%", "Soul Heart", "%state%", relicState(target, true)));
                    send(sender, LangService.format("commands.system.relic_state",
                            "&7%label%: &f%state%",
                            "%label%", "Abyssal Tendrils", "%state%", relicState(target, false)));
                    return;
                }

                if (args.length != 5) {
                    sendUsage(sender, "/nightmare system relic " + action + " <player> <soul_heart|abyssal_tendrils>");
                    return;
                }

                String relic = normalizeRelic(args[4]);

                if (relic == null) {
                    send(sender, LangService.format("commands.system.unknown_relic",
                            "&cUnknown relic '&f%input%&c'.", "%input%", args[4]));
                    return;
                }

                if (action.equals("give")) {
                    if (relic.equals("soul_heart")) {
                        if (com.nightmare.relic.SoulHeartService.hasHeart(target)) {
                            send(sender, LangService.format("commands.system.already_has",
                                    "&c%player% already has %relic% equipped.",
                                    "%player%", target.getName(), "%relic%", "Soul Heart"));
                            return;
                        }

                        com.nightmare.relic.SoulHeartService.give(target);
                    } else {
                        if (AbyssalTendrilsItem.isTendrils(target.getInventory().getItem(17))) {
                            send(sender, LangService.format("commands.system.already_has",
                                    "&c%player% already has %relic% equipped.",
                                    "%player%", target.getName(), "%relic%", "Abyssal Tendrils"));
                            return;
                        }

                        com.nightmare.relic.AbyssalTendrilsService.give(target);
                    }

                    send(sender, LangService.format("commands.system.gave_relic",
                            "&aGave &f%relic%&a to &f%player%&a.",
                            "%relic%", relicName(relic), "%player%", target.getName()));
                    return;
                }

                if (action.equals("remove")) {
                    boolean removed = relic.equals("soul_heart")
                            ? com.nightmare.relic.SoulHeartService.remove(target)
                            : com.nightmare.relic.AbyssalTendrilsService.remove(target);

                    if (!removed) {
                        send(sender, LangService.format("commands.system.does_not_have",
                                "&c%player% does not have %relic% equipped.",
                                "%player%", target.getName(), "%relic%", relicName(relic)));
                        return;
                    }

                    send(sender, LangService.format("commands.system.removed_relic",
                            "&aRemoved &f%relic%&a from &f%player%&a.",
                            "%relic%", relicName(relic), "%player%", target.getName()));
                    return;
                }

                if (action.equals("restore")) {
                    if (relic.equals("soul_heart")) {
                        com.nightmare.relic.SoulHeartService.ensureBound(target);

                        if (!com.nightmare.relic.SoulHeartService.hasHeart(target)) {
                            send(sender, LangService.format("commands.system.not_in_inventory",
                                    "&c%relic% is not in %player%'s inventory.",
                                    "%relic%", "Soul Heart", "%player%", target.getName()));
                            return;
                        }
                    } else {
                        com.nightmare.relic.AbyssalTendrilsService.ensureBound(target);

                        if (!AbyssalTendrilsItem.isTendrils(target.getInventory().getItem(17))) {
                            send(sender, LangService.format("commands.system.not_in_inventory",
                                    "&c%relic% is not in %player%'s inventory.",
                                    "%relic%", "Abyssal Tendrils", "%player%", target.getName()));
                            return;
                        }
                    }

                    send(sender, LangService.format("commands.system.restored_relic",
                            "&aRestored &f%relic%&a for &f%player%&a.",
                            "%relic%", relicName(relic), "%player%", target.getName()));
                    return;
                }

                sendUsage(sender, "/nightmare system relic <give|remove|restore|info> <player> [relic]");
            }
            case "cooldown" -> {
                if (args.length != 4 || !args[3].equalsIgnoreCase("reset")) {
                    sendUsage(sender, "/nightmare system cooldown <player|@player|@s|@a> reset");
                    return;
                }

                if (args[2].equalsIgnoreCase("@a")) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        com.nightmare.core.CooldownResetService.reset(target.getUniqueId());
                    }

                    send(sender, LangService.format("commands.system.reset_all_players",
                            "&aReset all cooldowns for &f%count%&a players.",
                            "%count%", String.valueOf(Bukkit.getOnlinePlayers().size())));
                    return;
                }

                Player target;

                if (args[2].equalsIgnoreCase("@player") || args[2].equalsIgnoreCase("@s")) {
                    if (!(sender instanceof Player player)) {
                        send(sender, LangService.msg("commands.generic.use_name_or_console",
                                "&cUse a player name or @a from the console."));
                        return;
                    }

                    target = player;
                } else {
                    target = Bukkit.getPlayerExact(args[2]);
                }

                if (target == null) {
                    org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(args[2]);

                    if (!offline.hasPlayedBefore()) {
                        send(sender, LangService.format("commands.generic.never_joined",
                                "&cPlayer '&f%player%&c' has never joined this server.", "%player%", args[2]));
                        return;
                    }

                    com.nightmare.core.CooldownResetService.reset(offline.getUniqueId());
                    send(sender, LangService.format("commands.system.reset_all_named",
                            "&aReset all cooldowns for &f%player%&a.", "%player%", args[2]));
                    return;
                }

                com.nightmare.core.CooldownResetService.reset(target.getUniqueId());
                send(sender, LangService.format("commands.system.reset_all_named",
                        "&aReset all cooldowns for &f%player%&a.", "%player%", target.getName()));
            }
            case "reload" -> {
                if (args.length != 2) {
                    sendUsage(sender, "/nightmare system reload");
                    return;
                }

                reloadTarget(sender, "all");
            }
            case "altar" -> handleAltar(sender, args);

            case "revive" -> handleRevive(sender, args);

            default -> send(sender, LangService.format("commands.system.unknown_command",
                "&cUnknown system subcommand '&f%input%&c'.", "%input%", args[1]));
        }
    }

    private void handleRevive(CommandSender sender, String[] args) {

        if (args.length != 3) {

            sendUsage(sender, "/nightmare system revive <player|@s|@a>");

            return;
        }

        String query = args[2];

        if (query.equalsIgnoreCase("@a")) {

            int revived = 0;

            for (Player target : Bukkit.getOnlinePlayers()) {

                if (pardonBan(target.getName())) {

                    revived++;
                }
            }

            for (org.bukkit.OfflinePlayer banned : Bukkit.getBannedPlayers()) {

                String name = banned.getName();

                if (name != null && isNightmareBan(name) && pardonBan(name)) {

                    revived++;
                }
            }

            send(sender, LangService.format("commands.system.revived_many",
                    "&aRevived &f%count%&a players.", "%count%", String.valueOf(revived)));

            return;
        }

        if (query.equalsIgnoreCase("@s") || query.equalsIgnoreCase("@player")) {

            if (!(sender instanceof Player self)) {

                send(sender, LangService.msg("commands.generic.use_name_or_console",
                        "&cUse a player name or @a from the console."));

                return;
            }

            query = self.getName();
        }

        Player online = Bukkit.getPlayerExact(query);

        String targetName = online != null ? online.getName() : null;

        if (targetName == null) {

            org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(query);

            if (!offline.hasPlayedBefore() && !offline.isOnline()) {

                send(sender, LangService.format("commands.generic.never_joined",
                        "&cPlayer '&f%player%&c' has never joined this server.", "%player%", query));

                return;
            }

            targetName = offline.getName() != null ? offline.getName() : query;
        }

        if (!Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(targetName)) {

            send(sender, LangService.format("commands.system.not_banned",
                    "&7%player% is not banned.", "%player%", targetName));

            return;
        }

        pardonBan(targetName);

        send(sender, LangService.format("commands.system.revived_one",
                "&aRevived &f%player%&a.", "%player%", targetName));
    }

    private static boolean pardonBan(String name) {

        boolean pardoned = false;

        org.bukkit.BanList nameBan = Bukkit.getBanList(org.bukkit.BanList.Type.NAME);

        if (nameBan.isBanned(name)) {

            nameBan.pardon(name);

            pardoned = true;
        }

        try {

            org.bukkit.BanList profileBan = Bukkit.getBanList(org.bukkit.BanList.Type.PROFILE);

            if (profileBan.isBanned(name)) {

                profileBan.pardon(name);

                pardoned = true;
            }

        } catch (Exception ignored) {

            // Paper < 1.21 or Bukkit without PROFILE: NAME only applies.
        }

        return pardoned;
    }

    private static boolean isNightmareBan(String name) {

        org.bukkit.BanEntry entry = Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntry(name);

        return entry != null && "NightmareCore".equals(entry.getSource());
    }

    private void handleAltar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "/nightmare system altar <place|list|teleport|remove> [theme|id]");
            return;
        }

        switch (args[2].toLowerCase()) {
            case "place" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, LangService.msg("commands.system.only_players_place",
                            "&cOnly players can place altars."));
                    return;
                }

                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system altar place <forge|soul|abyss|nightmare|relics>");
                    return;
                }

                com.nightmare.world.StructureTheme theme = com.nightmare.world.StructureTheme.byName(args[3]);

                if (theme == null) {
                    send(sender, LangService.format("commands.system.unknown_theme",
                            "&cUnknown altar theme '&f%input%&c'.", "%input%", args[3]));
                    return;
                }

                com.nightmare.world.StructureService.Altar altar =
                        com.nightmare.world.StructureService.buildAt(player, theme);

                send(sender, LangService.format("commands.system.built_altar",
                        "&aBuilt %theme% altar &f%id%&a.",
                        "%theme%", theme.name().toLowerCase(), "%id%", altar.id()));
            }
            case "list" -> {
                java.util.List<com.nightmare.world.StructureService.Altar> altars =
                        com.nightmare.world.StructureService.list();

                if (altars.isEmpty()) {
                    send(sender, LangService.msg("commands.system.no_altars",
                            "&7No altars registered."));
                    return;
                }

                for (com.nightmare.world.StructureService.Altar altar : altars) {
                    send(sender, "&7- &f" + altar.id() + " &7(" + altar.theme().toLowerCase() + ")");
                }
            }
            case "teleport" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, LangService.msg("commands.system.only_players_teleport",
                            "&cOnly players can teleport to altars."));
                    return;
                }

                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system altar teleport <id>");
                    return;
                }

                com.nightmare.world.StructureService.Altar altar =
                        com.nightmare.world.StructureService.get(args[3]);

                if (altar == null) {
                    send(sender, LangService.format("commands.system.unknown_altar", "&cUnknown altar '&f%input%&c'.", "%input%", args[3]));
                    return;
                }
                org.bukkit.World world = Bukkit.getWorld(altar.world());

                if (world == null) {
                    send(sender, LangService.msg("commands.system.world_not_loaded",
                            "&cThat world is not loaded."));
                    return;
                }

                player.teleport(new org.bukkit.Location(world, altar.x() + 0.5, altar.y() + 1, altar.z() + 0.5));
                send(sender, LangService.format("commands.system.teleported_altar",
                        "&aTeleported to altar &f%id%&a.", "%id%", altar.id()));
            }
            case "remove" -> {
                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system altar remove <id>");
                    return;
                }

                if (com.nightmare.world.StructureService.remove(args[3])) {
                    send(sender, LangService.format("commands.system.removed_altar",
                            "&aRemoved altar &f%id%&a.", "%id%", args[3]));
                } else {
                    send(sender, LangService.format("commands.system.unknown_altar", "&cUnknown altar '&f%input%&c'.", "%input%", args[3]));
                }
            }
            default -> sendUsage(sender, "/nightmare system altar <place|list|teleport|remove> [theme|id]");
        }
    }

    private void handleStory(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, LangService.msg("commands.story.chronicle_only_player",
                "&cThe chronicle can only be opened by a player."));
            return;
        }

        if (!sender.hasPermission("nightmare.story")) {
            sendPermission(sender);
            return;
        }

        if (args.length != 1) {
            sendUsage(sender, "/nightmare story");
            return;
        }

        com.nightmare.gui.StoryService.open(player, 0);
    }

    private void handleCreator(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendUsage(sender, "/nightmare creator");
            return;
        }

        String prefix = IO.HashMapOfSettings.getOrDefault("prefix", "");

        if (sender instanceof Player player) {
            TextComponent github = new TextComponent("Github 🔱  ");

            github.setBold(true);
            github.setColor(net.md_5.bungee.api.ChatColor.BLACK);
            github.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StevensBenavides/NightmareCore"));
            github.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.translateAlternateColorCodes('&', LangService.msg("commands.creator.github_hover",
                            "&fOpen the creator's GitHub.")))));

            TextComponent discord = new TextComponent("Discord ⚡");
            discord.setBold(true);
            discord.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/invite/DWfuQRsxwb"));
            discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.translateAlternateColorCodes('&', LangService.msg("commands.creator.discord_hover",
                            "&fOpen the creator's Discord.")))));

            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix));
            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', LangService.msg("commands.creator.support",
                    "&7Support the creator through &0Github &7or &bDiscord&7.")));
            player.sendMessage("");
            player.spigot().sendMessage(github, discord);
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);

            return;
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LangService.msg("commands.creator.support",
                "&7Support the creator through &0Github &7or &bDiscord&7.")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LangService.msg("commands.creator.github_line",
                "&0&lGithub: &7https://github.com/StevensBenavides/NightmareCore")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LangService.msg("commands.creator.discord_line",
                "&b&lDiscord: &7https://discord.com/invite/DWfuQRsxwb")));
        sender.sendMessage("");
    }

    private void sendHelpText(CommandSender sender) {
        for (String line : LangService.list("commands.help.help_text")) {
            send(sender, line);
        }
    }

    private void sendCommandHelp(CommandSender sender) {
        for (String line : LangService.list("commands.help.command_help")) {
            send(sender, line);
        }
    }

    private static void send(CommandSender sender, String message) {
        String prefix = IO.HashMapOfSettings.getOrDefault("prefix", "");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    private static void sendUsage(CommandSender sender, String usage) {
        send(sender, LangService.msg("commands.generic.usage_prefix", "&cUsage: &f") + usage);
    }

    private static void sendPermission(CommandSender sender) {
        String message = IO.HashMapOfMessages.getOrDefault("permission", null);

        if (message == null) {
            message = LangService.msg("commands.generic.no_permission", "&cYou do not have permission.");
        }

        send(sender, message);
    }

    private static String normalizeEnchant(String input) {
        String normalized = input.toLowerCase().replace('-', '_').replace(' ', '_').replace("'", "");

        if (EnchantRegistry.ENCHANTS.containsKey(normalized)) {
            return normalized;
        }

        for (CustomEnchant enchant : EnchantRegistry.ALL) {
            String display = enchant.displayName.toLowerCase().replace("'", "").replace(' ', '_');

            if (display.equals(normalized)) {
                return enchant.id;
            }
        }

        return null;
    }

    private static String normalizeRelic(String input) {
        String normalized = input.toLowerCase().replace('-', '_').replace(' ', '_');

        return switch (normalized) {
            case "soulheart", "soul_heart", "soul" -> "soul_heart";
            case "tendrils", "abyssal_tendrils", "tendril" -> "abyssal_tendrils";
            default -> null;
        };
    }

    private static String relicName(String id) {
        return id.equals("soul_heart") ? "Soul Heart" : "Abyssal Tendrils";
    }

    private static String relicState(Player player, boolean soulHeart) {
        String active = LangService.msg("commands.system.relic_state_active", "active");
        String inInventory = LangService.msg("commands.system.relic_state_in_inventory", "in inventory");
        String notFound = LangService.msg("commands.system.relic_state_not_found", "not found");

        if (soulHeart) {
            if (com.nightmare.relic.SoulHeartService.hasHeart(player)) {
                return active;
            }

            return com.nightmare.relic.SoulHeartService.hasHeartInInventory(player) ? inInventory : notFound;
        }

        if (AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(17))) {
            return active;
        }

        return com.nightmare.relic.AbyssalTendrilsService.hasTendrilsInInventory(player) ? inInventory : notFound;
    }

    private static String normalizeItem(String input) {
        String normalized = input.toLowerCase().replace('-', '_').replace(' ', '_').replace("'", "");

        return switch (normalized) {
            case "soulheart", "soul_heart", "soul" -> "soul_heart";
            case "tendrils", "amulet", "abyssal_tendrils_amulet", "abyssal_tendrils" -> "abyssal_tendrils";
            case "ink", "abyssal_ink" -> "abyssal_ink";
            case "nucleus", "abyssal_nucleus" -> "abyssal_nucleus";
            case "howl", "abyssal_howl" -> "abyssal_howl";
            case "core", "abyssal_core" -> "abyssal_core";
            default -> null;
        };
    }

    private static List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();

        if (options == null || options.isEmpty()) {
            return result;
        }

        String lower = prefix.toLowerCase();

        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }

        Collections.sort(result);
        return result;
    }

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("help");
            options.add("creator");

            if (sender.hasPermission("nightmare.story")) {
                options.add("story");
            }

            if (sender.hasPermission("nightmare.reload")) {
                options.add("reload");
            }

            if (sender.hasPermission("nightmare.system")) {
                options.add("time");
                options.add("system");
            }

            return filter(options, args[0]);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "help" -> filter(helpOptions(), args[1]);
                case "reload" -> sender.hasPermission("nightmare.reload") ? filter(RELOAD_TARGETS, args[1]) : List.of();
                case "time" -> sender.hasPermission("nightmare.system") ? filter(TIME, args[1]) : List.of();
                case "system", "sys" -> sender.hasPermission("nightmare.system") ? filter(SYSTEM, args[1]) : List.of();
                default -> List.of();
            };
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys")) {
                if (args[1].equalsIgnoreCase("day")) {
                    return filter(List.of("0", "50", "100"), args[2]);
                }

                if (args[1].equalsIgnoreCase("enchant") || args[1].equalsIgnoreCase("ench")) {
                    return filter(new ArrayList<>(EnchantRegistry.ENCHANTS.keySet()), args[2]);
                }

                if (args[1].equalsIgnoreCase("item")) {
                    return filter(List.of("give", "info"), args[2]);
                }

                if (args[1].equalsIgnoreCase("relic")) {
                    return filter(List.of("give", "remove", "restore", "info"), args[2]);
                }

                if (args[1].equalsIgnoreCase("cooldown")) {
                    List<String> players = new ArrayList<>();
                    players.add("@player");
                    players.add("@s");
                    players.add("@a");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        players.add(player.getName());
                    }

                    return filter(players, args[2]);
                }

                if (args[1].equalsIgnoreCase("altar")) {
                    return filter(List.of("place", "list", "teleport", "remove"), args[2]);
                }

                if (args[1].equalsIgnoreCase("revive")) {

                    List<String> suggestions = new ArrayList<>();

                    suggestions.add("@s");

                    suggestions.add("@a");

                    for (Player player : Bukkit.getOnlinePlayers()) {

                        suggestions.add(player.getName());
                    }

                    for (org.bukkit.BanEntry entry : Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntries()) {

                        if ("NightmareCore".equals(entry.getSource())) {

                            suggestions.add(entry.getTarget());
                        }
                    }

                    return filter(suggestions, args[2]);
                }
            }

                if (args[1].equalsIgnoreCase("altar")) {
                    return filter(List.of("place", "list", "teleport", "remove"), args[2]);
                }

                if (args[0].equalsIgnoreCase("time")) {
                    return filter(List.of("1", "5", "10"), args[2]);
                }

                return List.of();
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("system") && args[1].equalsIgnoreCase("altar")) {
            if (args[2].equalsIgnoreCase("place")) {
                return filter(List.of("forge", "soul", "abyss", "nightmare", "relics"), args[3]);
            }

            if (args[2].equalsIgnoreCase("teleport") || args[2].equalsIgnoreCase("remove")) {
                List<String> ids = new ArrayList<>();
                for (com.nightmare.world.StructureService.Altar altar : com.nightmare.world.StructureService.list()) {
                    ids.add(altar.id());
                }
                return filter(ids, args[3]);
            }

            return List.of();
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys"))
                && args[1].equalsIgnoreCase("relic")) {
            List<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }

            return filter(players, args[3]);
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys"))
                && args[1].equalsIgnoreCase("cooldown")) {
            return filter(List.of("reset"), args[3]);
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys"))
                && args[1].equalsIgnoreCase("item") && args[2].equalsIgnoreCase("give")) {
            List<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }

            return filter(players, args[3]);
        }

        if (args.length == 5 && (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys"))
                && args[1].equalsIgnoreCase("relic")) {
            return filter(List.of("soul_heart", "abyssal_tendrils"), args[4]);
        }

        if (args.length == 5 && (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys"))
                && args[1].equalsIgnoreCase("item") && args[2].equalsIgnoreCase("give")) {
            return filter(ITEM_IDS, args[4]);
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys"))
                && (args[1].equalsIgnoreCase("enchant") || args[1].equalsIgnoreCase("ench"))) {
            String id = normalizeEnchant(args[2]);

            if (id == null) {
                return List.of();
            }

            CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
            List<String> levels = new ArrayList<>();

            for (int level = 1; level <= enchant.maxLevel; level++) {
                if (id.equals("nightmare_mark") && level != 5) {
                    continue;
                }

                if (id.equals("abyssal_howl") && level != 1) {
                    continue;
                }

                levels.add(String.valueOf(level));
            }

            return filter(levels, args[3]);
        }

        return List.of();
    }

    private static List<String> helpOptions() {
        List<String> options = new ArrayList<>(HELP);
        options.addAll(EnchantRegistry.ENCHANTS.keySet());
        return options;
    }

}
