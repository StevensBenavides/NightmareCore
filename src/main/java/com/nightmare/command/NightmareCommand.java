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
import com.nightmare.mob.MobProfileLoader;
import com.nightmare.relic.AbyssalTendrilsItem;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class NightmareCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ROOT = List.of("help", "reload", "time", "creator", "system", "story");
    private static final List<String> RELOAD_TARGETS = List.of(
            "messages.yml", "settings.yml", "time", "enchants.yml", "balance.yml", "mobs.yml", "enchant-lore.yml", "story.yml", "all");
    private static final List<String> SYSTEM = List.of("day", "enchant", "item", "relic", "cooldown", "reload", "altar");
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
            default -> send(sender, "&cUnknown subcommand. Use &f/nightmare help commands&c.");
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
            sender.sendMessage(ChatColor.YELLOW + "The Nightmare help GUI is only available to players.");
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
            send(sender, "&cUnknown enchant or help category '&f" + args[1] + "&c'.");
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
                case "settings.yml" -> IO.reloadSettings(plugin);
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
                case "all" -> {
                    IO.reloadMessages(plugin);
                    IO.reloadSettings(plugin);
                    TimeManagement.updatePlayerTimes();
                    TimeManagement.updateWorldsTimes();
                    Main.enchants = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchants.yml"));
                    Main.balance = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "balance.yml"));
                    MobProfileLoader.load(plugin);
                    Main.enchantLore = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchant-lore.yml"));
                    com.nightmare.gui.StoryService.load(plugin);
                    EnchantBookService.registerRecipes(plugin);
                    com.nightmare.relic.RelicRecipeService.register(plugin);
                }
                default -> {
                    send(sender, "&cUnknown reload target '&f" + target + "&c'.");
                    return;
                }
            }

            send(sender, "&aReloaded &f" + target + "&a.");
        } catch (Exception exception) {
            send(sender, "&cCould not reload &f" + target + "&c: " + exception.getMessage());
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
            send(sender, "&cInvalid number of days.");
            return;
        }

        if (days <= 0) {
            send(sender, "&cDays must be a positive number.");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            long ticks = days * 24000L;

            for (World world : Bukkit.getWorlds()) {
                world.setFullTime(world.getFullTime() + ticks);
            }

            TimeManagement.updateWorldsTimes();
            send(sender, "&aAdded &6" + days + "&a day(s) to all worlds.");
            return;
        }

        if (args[1].equalsIgnoreCase("remove")) {
            long ticks = days * 24000L;

            for (World world : Bukkit.getWorlds()) {
                if (world.getFullTime() - ticks < 24000L) {
                    send(sender, "&cYou cannot set any world below day 1.");
                    return;
                }
            }

            for (World world : Bukkit.getWorlds()) {
                world.setFullTime(world.getFullTime() - ticks);
            }

            TimeManagement.updateWorldsTimes();
            send(sender, "&aRemoved &6" + days + "&a day(s) from all worlds.");
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
            sendUsage(sender, "/nightmare system <day|enchant|item|relic|cooldown|reload|altar>");
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
                    send(sender, "&cInvalid day.");
                    return;
                }

                if (day < 0) {
                    send(sender, "&cDay must be 0 or higher.");
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

                send(sender, "&aDay set to &6" + day + "&a.");
            }
            case "enchant", "ench" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, "&cThe enchant command can only be used by a player with an item in hand.");
                    return;
                }

                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system enchant <ench> <level>");
                    return;
                }

                String id = normalizeEnchant(args[2]);

                if (id == null) {
                    send(sender, "&cUnknown enchant '&f" + args[2] + "&c'. Use &f/nightmare help enchants&c.");
                    return;
                }

                CustomEnchant enchant = EnchantRegistry.ENCHANTS.get(id);
                int level;

                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    send(sender, "&cInvalid level.");
                    return;
                }

                if (level < 1 || level > enchant.maxLevel) {
                    send(sender, "&cLevel must be between 1 and " + enchant.maxLevel + ".");
                    return;
                }

                if (id.equals("nightmare_mark") && level != 5) {
                    send(sender, "&cNightmare's Mark is only available at level V.");
                    return;
                }

                if (id.equals("abyssal_howl") && level != 1) {
                    send(sender, "&cAbyssal Howl is a single legendary level.");
                    return;
                }

                ItemStack item = player.getInventory().getItemInMainHand();

                if (item == null || item.getType() == Material.AIR) {
                    send(sender, "&cYou must hold an item.");
                    return;
                }

                if (!enchant.applicable.contains(item.getType())) {
                    send(sender, "&c" + enchant.displayName + " cannot be applied to this item. Applicable on: "
                            + EnchantLoreService.applicabilitySummary(enchant) + ".");
                    return;
                }

                CustomItemService.applyEnchant(item, id, level);
                send(sender, "&aApplied &6" + enchant.displayName + " " + ROMAN[level - 1] + "&a.");
            }
            case "item" -> {
                if (args.length == 4 && args[2].equalsIgnoreCase("info")) {
                    if (!(sender instanceof Player player)) {
                        send(sender, "&cOnly players can inspect the held item.");
                        return;
                    }

                    ItemStack held = player.getInventory().getItemInMainHand();
                    org.bukkit.inventory.meta.ItemMeta meta = held.getItemMeta();
                    String model = meta != null && meta.hasItemModel() && meta.getItemModel() != null
                            ? meta.getItemModel().toString()
                            : "none";
                    send(sender, "&7Held: &f" + held.getType()
                            + " &8| &7item_model: &f" + model);
                    return;
                }

                if (args.length != 5 || !args[2].equalsIgnoreCase("give")) {
                    sendUsage(sender, "/nightmare system item give <player> <item>");
                    sendUsage(sender, "/nightmare system item info");
                    return;
                }

                Player target = Bukkit.getPlayerExact(args[3]);

                if (target == null) {
                    send(sender, "&cPlayer '&f" + args[3] + "&c' is not online.");
                    return;
                }

                String itemId = normalizeItem(args[4]);

                if (itemId == null) {
                    send(sender, "&cUnknown item '&f" + args[4] + "&c'. Use &f/nightmare system item give <player> <item>&c.");
                    return;
                }

                if (itemId.equals("soul_heart")) {
                    com.nightmare.relic.SoulHeartService.give(target);
                    send(sender, "&aGave &fSoul Heart&a to &f" + target.getName() + "&a.");
                    return;
                }

                if (itemId.equals("abyssal_tendrils")) {
                    com.nightmare.relic.AbyssalTendrilsService.give(target);
                    send(sender, "&aGave &5Abyssal Tendrils&a to &f" + target.getName() + "&a.");
                    return;
                }

                ItemStack item = switch (itemId) {
                    case "abyssal_ink" -> EnchantBookService.createAbyssalInk();
                    case "abyssal_nucleus" -> EnchantBookService.createAbyssalNucleus();
                    case "abyssal_howl", "abyssal_core" -> EnchantBookService.createBook(itemId, 1);
                    default -> null;
                };

                if (item == null) {
                    send(sender, "&cCould not create '&f" + args[4] + "&c'.");
                    return;
                }

                if (!target.getInventory().addItem(item).isEmpty()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), item);
                }

                send(sender, "&aGave &f" + itemId + "&a to &f" + target.getName() + "&a.");
            }
            case "relic" -> {
                if (args.length < 4 || args.length > 5) {
                    sendUsage(sender, "/nightmare system relic <give|remove|restore|info> <player> [relic]");
                    return;
                }

                String action = args[2].toLowerCase();
                Player target = Bukkit.getPlayerExact(args[3]);

                if (target == null) {
                    send(sender, "&cPlayer '&f" + args[3] + "&c' is not online.");
                    return;
                }

                if (action.equals("info")) {
                    send(sender, "&f" + target.getName() + "&7:");
                    send(sender, "&7Soul Heart: &f" + relicState(target, true));
                    send(sender, "&7Abyssal Tendrils: &f" + relicState(target, false));
                    return;
                }

                if (args.length != 5) {
                    sendUsage(sender, "/nightmare system relic " + action + " <player> <soul_heart|abyssal_tendrils>");
                    return;
                }

                String relic = normalizeRelic(args[4]);

                if (relic == null) {
                    send(sender, "&cUnknown relic '&f" + args[4] + "&c'.");
                    return;
                }

                if (action.equals("give")) {
                    if (relic.equals("soul_heart")) {
                        if (com.nightmare.relic.SoulHeartService.hasHeart(target)) {
                            send(sender, "&c" + target.getName() + " already has Soul Heart equipped.");
                            return;
                        }

                        com.nightmare.relic.SoulHeartService.give(target);
                    } else {
                        if (AbyssalTendrilsItem.isTendrils(target.getInventory().getItem(17))) {
                            send(sender, "&c" + target.getName() + " already has Abyssal Tendrils equipped.");
                            return;
                        }

                        com.nightmare.relic.AbyssalTendrilsService.give(target);
                    }

                    send(sender, "&aGave &f" + relicName(relic) + "&a to &f" + target.getName() + "&a.");
                    return;
                }

                if (action.equals("remove")) {
                    boolean removed = relic.equals("soul_heart")
                            ? com.nightmare.relic.SoulHeartService.remove(target)
                            : com.nightmare.relic.AbyssalTendrilsService.remove(target);

                    if (!removed) {
                        send(sender, "&c" + target.getName() + " does not have " + relicName(relic) + " equipped.");
                        return;
                    }

                    send(sender, "&aRemoved &f" + relicName(relic) + "&a from &f" + target.getName() + "&a.");
                    return;
                }

                if (action.equals("restore")) {
                    if (relic.equals("soul_heart")) {
                        com.nightmare.relic.SoulHeartService.ensureBound(target);

                        if (!com.nightmare.relic.SoulHeartService.hasHeart(target)) {
                            send(sender, "&cSoul Heart is not in " + target.getName() + "'s inventory.");
                            return;
                        }
                    } else {
                        com.nightmare.relic.AbyssalTendrilsService.ensureBound(target);

                        if (!AbyssalTendrilsItem.isTendrils(target.getInventory().getItem(17))) {
                            send(sender, "&cAbyssal Tendrils is not in " + target.getName() + "'s inventory.");
                            return;
                        }
                    }

                    send(sender, "&aRestored &f" + relicName(relic) + "&a for &f" + target.getName() + "&a.");
                    return;
                }

                sendUsage(sender, "/nightmare system relic <give|remove|restore|info> <player> [relic]");
            }
            case "cooldown" -> {
                if (args.length != 4 || !args[3].equalsIgnoreCase("reset")) {
                    sendUsage(sender, "/nightmare system cooldown <jugador|@player|@s|@a> reset");
                    return;
                }

                if (args[2].equalsIgnoreCase("@a")) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        com.nightmare.core.CooldownResetService.reset(target.getUniqueId());
                    }

                    send(sender, "&aReset all cooldowns for &f" + Bukkit.getOnlinePlayers().size() + "&a players.");
                    return;
                }

                Player target;

                if (args[2].equalsIgnoreCase("@player") || args[2].equalsIgnoreCase("@s")) {
                    if (!(sender instanceof Player player)) {
                        send(sender, "&cUse a player name or @a from the console.");
                        return;
                    }

                    target = player;
                } else {
                    target = Bukkit.getPlayerExact(args[2]);
                }

                if (target == null) {
                    org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(args[2]);

                    if (!offline.hasPlayedBefore()) {
                        send(sender, "&cPlayer '&f" + args[2] + "&c' has never joined this server.");
                        return;
                    }

                    com.nightmare.core.CooldownResetService.reset(offline.getUniqueId());
                    send(sender, "&aReset all cooldowns for &f" + args[2] + "&a.");
                    return;
                }

                com.nightmare.core.CooldownResetService.reset(target.getUniqueId());
                send(sender, "&aReset all cooldowns for &f" + target.getName() + "&a.");
            }
            case "reload" -> {
                if (args.length != 2) {
                    sendUsage(sender, "/nightmare system reload");
                    return;
                }

                reloadTarget(sender, "all");
            }
            case "altar" -> handleAltar(sender, args);
            default -> send(sender, "&cUnknown system subcommand '&f" + args[1] + "&c'.");
        }
    }

    private void handleAltar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "/nightmare system altar <place|list|teleport|remove> [theme|id]");
            return;
        }

        switch (args[2].toLowerCase()) {
            case "place" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, "&cOnly players can place altars.");
                    return;
                }

                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system altar place <forge|soul|abyss|nightmare|relics>");
                    return;
                }

                com.nightmare.world.StructureTheme theme = com.nightmare.world.StructureTheme.byName(args[3]);

                if (theme == null) {
                    send(sender, "&cUnknown altar theme '&f" + args[3] + "&c'.");
                    return;
                }

                com.nightmare.world.StructureService.Altar altar =
                        com.nightmare.world.StructureService.buildAt(player, theme);

                send(sender, "&aBuilt " + theme.name().toLowerCase() + " altar &f" + altar.id() + "&a.");
            }
            case "list" -> {
                java.util.List<com.nightmare.world.StructureService.Altar> altars =
                        com.nightmare.world.StructureService.list();

                if (altars.isEmpty()) {
                    send(sender, "&7No altars registered.");
                    return;
                }

                for (com.nightmare.world.StructureService.Altar altar : altars) {
                    send(sender, "&7- &f" + altar.id() + " &7(" + altar.theme().toLowerCase() + ")");
                }
            }
            case "teleport" -> {
                if (!(sender instanceof Player player)) {
                    send(sender, "&cOnly players can teleport to altars.");
                    return;
                }

                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system altar teleport <id>");
                    return;
                }

                com.nightmare.world.StructureService.Altar altar =
                        com.nightmare.world.StructureService.get(args[3]);

                if (altar == null) {
                    send(sender, "&cUnknown altar '&f" + args[3] + "&c'.");
                    return;
                }
                org.bukkit.World world = Bukkit.getWorld(altar.world());

                if (world == null) {
                    send(sender, "&cThat world is not loaded.");
                    return;
                }

                player.teleport(new org.bukkit.Location(world, altar.x() + 0.5, altar.y() + 1, altar.z() + 0.5));
                send(sender, "&aTeleported to altar &f" + altar.id() + "&a.");
            }
            case "remove" -> {
                if (args.length != 4) {
                    sendUsage(sender, "/nightmare system altar remove <id>");
                    return;
                }

                if (com.nightmare.world.StructureService.remove(args[3])) {
                    send(sender, "&aRemoved altar &f" + args[3] + "&a.");
                } else {
                    send(sender, "&cUnknown altar '&f" + args[3] + "&c'.");
                }
            }
            default -> sendUsage(sender, "/nightmare system altar <place|list|teleport|remove> [theme|id]");
        }
    }

    private void handleStory(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "&cThe chronicle can only be opened by a player.");
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
                    new Text(ChatColor.translateAlternateColorCodes('&', "&fOpen the creator's GitHub."))));

            TextComponent discord = new TextComponent("Discord ⚡");
            discord.setBold(true);
            discord.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/invite/DWfuQRsxwb"));
            discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.translateAlternateColorCodes('&', "&fOpen the creator's Discord."))));

            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix));
            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Support the creator through &0Github &7or &bDiscord&7."));
            player.sendMessage("");
            player.spigot().sendMessage(github, discord);
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);

            return;
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Support the creator through &0Github &7or &bDiscord&7."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&0&lGithub: &7https://github.com/StevensBenavides/NightmareCore"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lDiscord: &7https://discord.com/invite/DWfuQRsxwb"));
        sender.sendMessage("");
    }

    private void sendHelpText(CommandSender sender) {
        send(sender, "&c&lNightmare Core &7| &fCommands");
        send(sender, "&7/nightmare help &8- &fOpen the help GUI or command list.");
        send(sender, "&7/nightmare story &8- &fOpen Nightmare Origin, the story of the gods.");
        send(sender, "&7/nightmare help relics &8- &fOpen the relic guide.");
        send(sender, "&7/nightmare reload <target> &8- &fReload a config file.");
        send(sender, "&7/nightmare time <add|remove> <days> &8- &fChange world time.");
        send(sender, "&7/nightmare creator &8- &fShow creator links.");
        send(sender, "&7/nightmare system day <day> &8- &fSet the world day.");
        send(sender, "&7/nightmare system enchant <ench> <level> &8- &fEnchant the held item.");
        send(sender, "&7/nightmare system item give <player> <item> &8- &fGive a custom item.");
        send(sender, "&7/nightmare system item info &8- &fShow the held item's model.");
        send(sender, "&7/nightmare system relic <give|remove|restore|info> <player> [relic] &8- &fManage relics.");
        send(sender, "&7/nightmare system cooldown <player|@player|@s|@a> reset &8- &fReset attack cooldowns.");
        send(sender, "&7/nightmare system reload &8- &fReload all plugin configs.");
        send(sender, "&7/nightmare system altar <place|list|teleport|remove> &8- &fManage lore altars.");
    }

    private void sendCommandHelp(CommandSender sender) {
        send(sender, "&c&lNightmare Core &7| &fCommand Reference");
        send(sender, "&f/nightmare help &7- Open the guide GUI.");
        send(sender, "&f/nightmare story &7- Open Nightmare Origin, the story of the gods.");
        send(sender, "&f/nightmare help enchants &7- Browse enchantments.");
        send(sender, "&f/nightmare help relics &7- Browse relics.");
        send(sender, "&f/nightmare reload <messages.yml|settings.yml|time|enchants.yml|balance.yml|mobs.yml|all>");
        send(sender, "&f/nightmare time <add|remove> <days>");
        send(sender, "&f/nightmare creator");
        send(sender, "&f/nightmare system day <day>");
        send(sender, "&f/nightmare system enchant <ench> <level>");
        send(sender, "&f/nightmare system item give <player> <item>");
        send(sender, "&f/nightmare system relic <give|remove|restore|info> <player> [relic]");
        send(sender, "&f/nightmare system cooldown <player|@player|@s|@a> reset");
        send(sender, "&f/nightmare system reload");
        send(sender, "&f/nightmare system altar <place|list|teleport|remove> [theme|id]");
    }

    private static void send(CommandSender sender, String message) {
        String prefix = IO.HashMapOfSettings.getOrDefault("prefix", "");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    private static void sendUsage(CommandSender sender, String usage) {
        send(sender, "&cUsage: &f" + usage);
    }

    private static void sendPermission(CommandSender sender) {
        String message = IO.HashMapOfMessages.getOrDefault("permission", "&cYou do not have permission.");
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
        if (soulHeart) {
            if (com.nightmare.relic.SoulHeartService.hasHeart(player)) {
                return "active";
            }

            return com.nightmare.relic.SoulHeartService.hasHeartInInventory(player) ? "in inventory" : "not found";
        }

        if (AbyssalTendrilsItem.isTendrils(player.getInventory().getItem(17))) {
            return "active";
        }

        return com.nightmare.relic.AbyssalTendrilsService.hasTendrilsInInventory(player) ? "in inventory" : "not found";
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
