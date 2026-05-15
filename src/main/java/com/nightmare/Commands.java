package com.nightmare;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class Commands implements CommandExecutor, TabCompleter{

    private final Plugin plugin = Main.getInstance();

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            return List.of("time", "reload", "creator");
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("reload")) {
            return List.of("time", "messages.yml", "settings.yml");
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("time")) {
            return List.of("add", "remove");
        }

        return null;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player) && args.length > 1 && args[0].equalsIgnoreCase("reload")) {
            
            if (args[1].equalsIgnoreCase("messages.yml")) {
                
                try {
                    IO.reloadMessages(plugin);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + IO.HashMapOfMessages.get("reload-messages")));
                } catch (IOException e) {
                    sender.getServer().getPluginManager().disablePlugin(plugin);
                    e.printStackTrace();
                }


            } else if (args[1].equalsIgnoreCase("settings.yml")) {

                try {
                    IO.reloadSettings(plugin);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + IO.HashMapOfMessages.get("reload-settings")));
                } catch (IOException e) {
                    sender.getServer().getPluginManager().disablePlugin(plugin);
                    e.printStackTrace();
                }

            } else if (args[1].equalsIgnoreCase("time")) {
                TimeManagement.updatePlayerTimes();
                TimeManagement.updateWorldsTimes();

                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&fWorld and Player time reloaded."));
            }

            return true;

        } else if (sender instanceof Player && args.length > 1 && args[0].equalsIgnoreCase("reload")) {

            if (!(sender.hasPermission("nightmare.reload")) || !sender.isOp()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + IO.HashMapOfMessages.get("permission")));
                return true;
            }

            Player p = (Player) sender;
            
            if (args[1].equalsIgnoreCase("messages.yml")) {
                
                try {
                    IO.reloadMessages(plugin);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + IO.HashMapOfMessages.get("reload-messages")));
                } catch (IOException e) {
                    sender.getServer().getPluginManager().disablePlugin(plugin);
                    e.printStackTrace();
                }

            } else if (args[1].equalsIgnoreCase("settings.yml")) {

                try {
                    IO.reloadSettings(plugin);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + IO.HashMapOfMessages.get("reload-settings")));
                } catch (IOException e) {
                    sender.getServer().getPluginManager().disablePlugin(plugin);
                    e.printStackTrace();
                }

            } else if (args[1].equalsIgnoreCase("time")) {
                TimeManagement.updatePlayerTimes();
                TimeManagement.updateWorldsTimes();

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&fWorld and Player time reloaded."));
            }

        } else if (!(sender instanceof Player) && args.length == 1 && args[0].equalsIgnoreCase("creator")) {

            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("%s", plugin.getConfig().getString("prefix"))));
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Support the creator through &0Github &7or &bDiscord&7."));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&0&lGithub: &7https://github.com/DevCheckOG/NightmareCore-Dev"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lDiscord: &7https://discord.com/invite/DWfuQRsxwb"));
            Bukkit.getConsoleSender().sendMessage("");

        } else if (sender instanceof Player && args.length == 1 && args[0].equalsIgnoreCase("creators")) {
            
            Player p = (Player) sender;

            TextComponent github = new TextComponent();
            github.setText("Github 🔱  ");
            github.setBold(true);
            github.setColor(ChatColor.BLACK);
            github.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StevensBenavides/NightmareCore"));
            github.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', "&fOpen a browser window with the creator github."))));

            TextComponent discord = new TextComponent();
            discord.setText("Discord ⚡");
            discord.setBold(true);
            discord.setColor(ChatColor.AQUA);
            discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/invite/DWfuQRsxwb"));
            discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', "&fOpen a browser window with the creator's &bdiscord server&f."))));

            p.sendMessage("");
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix")));
            p.sendMessage("");
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Support the creator through &0Github &7or &bDiscord&7."));
            p.sendMessage("");
            p.spigot().sendMessage(github, discord);
            p.sendMessage("");

            p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 10, 10);

        } else if (sender instanceof Player && args.length > 2 && args[0].equalsIgnoreCase("time")) {

           Player p = (Player) sender;

           if (args[1].equalsIgnoreCase("add")) {
                try {
                    int days = Integer.parseInt(args[2]);
                    long ticksToAdd = days * 24000L;

                    for (World world : p.getServer().getWorlds()) {
                        world.setFullTime(world.getFullTime() + ticksToAdd);
                    }

                    TimeManagement.updateWorldsTimes();
 
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&c" + days + " &fdays have been added to the server."));
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&cNumber of days invalid."));
                }
            }


            if (args[1].equalsIgnoreCase("remove")) {
                try {
                    int days = Integer.parseInt(args[2]);
                    long ticksToRemove = days * 24000L;

                    boolean cannotRemoveDays = false;

                    for (World world : p.getServer().getWorlds()) {

                        if ((world.getFullTime() - ticksToRemove) < 24000L ) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&cYou cannot set days to less than 1."));
                            cannotRemoveDays = true;
                            break;
                        }

                    }

                    if (!cannotRemoveDays) {
                        for (World world : p.getServer().getWorlds()) {
                            world.setFullTime(world.getFullTime() - ticksToRemove);
                        }
                        
                        TimeManagement.updateWorldsTimes();
     
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&c" + days + " &fdays have been removed to the server."));
                    }

                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', IO.HashMapOfSettings.get("prefix") + "&cNumber of days invalid."));
                }
            }

        }

        return true;

    }
    
}
