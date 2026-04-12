package me.theadamnew.duelsx;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duel.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /duel admin create <mapname>");
                    return true;
                }
                String mapName = args[1];
                createMap(sender, mapName);
            }
            case "setspawn1" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /duel admin setspawn1 <mapname>");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command must be used as a player!");
                    return true;
                }
                String mapName = args[1];
                setSpawn1(sender, player, mapName);
            }
            case "setspawn2" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /duel admin setspawn2 <mapname>");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command must be used as a player!");
                    return true;
                }
                String mapName = args[1];
                setSpawn2(sender, player, mapName);
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /duel admin delete <mapname>");
                    return true;
                }
                deleteMap(sender, args[1]);
            }
            case "list" -> {
                listMaps(sender);
            }
            default -> {
                sendHelp(sender);
            }
        }

        return true;
    }

    private void createMap(CommandSender sender, String mapName) {
        var config = DuelPlugin.getInstance().getConfig();
        
        if (!config.contains("maps")) {
            config.createSection("maps");
        }
        
        ConfigurationSection maps = config.getConfigurationSection("maps");
        
        if (maps.contains(mapName)) {
            sender.sendMessage(ChatColor.RED + "Map '" + mapName + "' already exists!");
            return;
        }
        
        String path = "maps." + mapName;
        config.set(path + ".world", "");
        config.set(path + ".spawn1.x", 0);
        config.set(path + ".spawn1.y", 64);
        config.set(path + ".spawn1.z", 0);
        config.set(path + ".spawn1.yaw", 0);
        config.set(path + ".spawn1.pitch", 0);
        config.set(path + ".spawn2.x", 10);
        config.set(path + ".spawn2.y", 64);
        config.set(path + ".spawn2.z", 0);
        config.set(path + ".spawn2.yaw", 180);
        config.set(path + ".spawn2.pitch", 0);
        
        DuelPlugin.getInstance().saveConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Map '" + mapName + "' created!");
        sender.sendMessage(ChatColor.YELLOW + "Stand where you want spawn1 and use: /duel admin setspawn1 " + mapName);
        sender.sendMessage(ChatColor.YELLOW + "Stand where you want spawn2 and use: /duel admin setspawn2 " + mapName);
    }

    private void setSpawn1(CommandSender sender, Player player, String mapName) {
        var config = DuelPlugin.getInstance().getConfig();
        String path = "maps." + mapName;
        
        if (!config.contains(path)) {
            sender.sendMessage(ChatColor.RED + "Map '" + mapName + "' does not exist! Use /duel admin create " + mapName);
            return;
        }
        
        Location loc = player.getLocation();
        
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".spawn1.x", loc.getX());
        config.set(path + ".spawn1.y", loc.getY());
        config.set(path + ".spawn1.z", loc.getZ());
        config.set(path + ".spawn1.yaw", loc.getYaw());
        config.set(path + ".spawn1.pitch", loc.getPitch());
        
        DuelPlugin.getInstance().saveConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Spawn 1 for map '" + mapName + "' set to:");
        sender.sendMessage(ChatColor.WHITE + "World: " + loc.getWorld().getName());
        sender.sendMessage(ChatColor.WHITE + "X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
    }

    private void setSpawn2(CommandSender sender, Player player, String mapName) {
        var config = DuelPlugin.getInstance().getConfig();
        String path = "maps." + mapName;
        
        if (!config.contains(path)) {
            sender.sendMessage(ChatColor.RED + "Map '" + mapName + "' does not exist! Use /duel admin create " + mapName);
            return;
        }
        
        Location loc = player.getLocation();
        
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".spawn2.x", loc.getX());
        config.set(path + ".spawn2.y", loc.getY());
        config.set(path + ".spawn2.z", loc.getZ());
        config.set(path + ".spawn2.yaw", loc.getYaw());
        config.set(path + ".spawn2.pitch", loc.getPitch());
        
        DuelPlugin.getInstance().saveConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Spawn 2 for map '" + mapName + "' set to:");
        sender.sendMessage(ChatColor.WHITE + "World: " + loc.getWorld().getName());
        sender.sendMessage(ChatColor.WHITE + "X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
    }

    private void deleteMap(CommandSender sender, String mapName) {
        var config = DuelPlugin.getInstance().getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        config.set("maps." + mapName, null);
        DuelPlugin.getInstance().saveConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Map '" + mapName + "' deleted!");
    }

    private void listMaps(CommandSender sender) {
        var config = DuelPlugin.getInstance().getConfig();
        
        sender.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "       Duel Maps");
        sender.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
        
        if (!config.contains("maps") || config.getConfigurationSection("maps").getKeys(false).isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No maps created yet!");
            sender.sendMessage(ChatColor.WHITE + "Use /duel admin create <name> to create one");
        } else {
            for (String mapName : config.getConfigurationSection("maps").getKeys(false)) {
                String world = config.getString("maps." + mapName + ".world", "Not set");
                sender.sendMessage(ChatColor.AQUA + mapName + ChatColor.GRAY + " | World: " + world);
            }
        }
        
        sender.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + "╔══════════════════════════════════╗");
        sender.sendMessage(ChatColor.GOLD + "       ⚔ Admin Commands ⚔");
        sender.sendMessage(ChatColor.DARK_GRAY + "╚══════════════════════════════════╝");
        sender.sendMessage(ChatColor.AQUA + "/duel admin create <map>" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Create new arena");
        sender.sendMessage(ChatColor.AQUA + "/duel admin setspawn1 <map>" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Set player 1 spawn");
        sender.sendMessage(ChatColor.AQUA + "/duel admin setspawn2 <map>" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Set player 2 spawn");
        sender.sendMessage(ChatColor.AQUA + "/duel admin delete <map>" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Delete arena");
        sender.sendMessage(ChatColor.AQUA + "/duel admin list" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "List all arenas");
        sender.sendMessage(ChatColor.DARK_GRAY + "═══════════════════════════════════");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("create");
            completions.add("setspawn1");
            completions.add("setspawn2");
            completions.add("delete");
            completions.add("list");
        } else if (args.length == 2) {
            if ("delete".equals(args[0])) {
                var config = DuelPlugin.getInstance().getConfig();
                if (config.contains("maps")) {
                    completions.addAll(config.getConfigurationSection("maps").getKeys(false));
                }
            }
        }
        
        return completions;
    }
}