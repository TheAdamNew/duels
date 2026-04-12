package me.theadamnew.duelsx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DuelCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "accept" -> {
                if (!DuelManager.acceptDuel(player)) {
                    player.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
                    player.sendMessage(ChatColor.RED + "No pending duel request found.");
                    player.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
                    return true;
                }
            }
            case "deny" -> {
                DuelManager.denyDuel(player);
                player.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
                player.sendMessage(ChatColor.RED + "Duel request denied.");
                player.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
            }
            case "end" -> {
                DuelManager.endDuelByForfeit(player);
            }
            case "stats" -> {
                LeaderboardManager.showMyStats(player);
            }
            case "leaderboard", "top" -> {
                String sortBy = "wins";
                if (args.length > 1) {
                    sortBy = args[1].toLowerCase();
                }
                LeaderboardManager.sendLeaderboard(player, sortBy);
            }
            case "kits", "kit" -> {
                openKitSelector(player);
            }
            case "help" -> {
                showHelp(player);
            }
            default -> {
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /duel <player>");
                    return true;
                }

                String targetName = args[0];
                Player target = Bukkit.getPlayer(targetName);

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                    return true;
                }

                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "You can't duel yourself!");
                    return true;
                }

                DuelGUI.openDuelSetup(player, target);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return completions;
        }

        if (args.length == 1) {
            for (Player p : sender.getServer().getOnlinePlayers()) {
                if (!p.equals(player)) {
                    completions.add(p.getName());
                }
            }
        }

        return completions;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "╔══════════════════════════════════╗");
        player.sendMessage(ChatColor.GOLD + "         ⚔ Duel Commands ⚔");
        player.sendMessage(ChatColor.DARK_GRAY + "╚══════════════════════════════════╝");
        player.sendMessage(ChatColor.AQUA + "/duel <player>" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Challenge player");
        player.sendMessage(ChatColor.AQUA + "/duel accept" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Accept duel");
        player.sendMessage(ChatColor.AQUA + "/duel deny" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Deny duel");
        player.sendMessage(ChatColor.AQUA + "/duel end" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Forfeit duel");
        player.sendMessage(ChatColor.AQUA + "/duel stats" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "View your stats");
        player.sendMessage(ChatColor.AQUA + "/duel top" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Leaderboard");
        player.sendMessage(ChatColor.AQUA + "/duel kits" + ChatColor.GRAY + " ▸ " + ChatColor.WHITE + "Kit selector");
        player.sendMessage(ChatColor.DARK_GRAY + "═══════════════════════════════════");
    }

    private void openKitSelector(Player player) {
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "⚔ Kit Selector");

        for (KitManager.KitData kit : KitManager.getAllKits()) {
            ItemStack item = kit.getIcon();
            
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                boolean hasAccess = kit.permission().isEmpty() || player.hasPermission(kit.permission());
                
                List<String> lore = new ArrayList<>();
                for (String line : kit.lore()) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                if (hasAccess) {
                    lore.add("");
                    lore.add(ChatColor.GREEN + "» Click to select!");
                } else {
                    lore.add("");
                    lore.add(ChatColor.RED + "» Locked!");
                }
                meta.setLore(lore);
                
                if (!hasAccess) {
                    meta.setDisplayName(ChatColor.GRAY + kit.displayName());
                }
                
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
            
            inv.addItem(item);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "✕ Close");
        closeMeta.setLore(List.of(ChatColor.GRAY + "», Close this menu"));
        closeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        close.setItemMeta(closeMeta);
        inv.setItem(26, close);

        player.openInventory(inv);
    }
}