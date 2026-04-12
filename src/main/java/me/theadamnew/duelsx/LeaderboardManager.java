package me.theadamnew.duelsx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardManager {

    private static final List<DatabaseManager.PlayerStats> cachedLeaderboard = new ArrayList<>();
    private static String currentSortBy = "wins";
    private static long lastUpdate = 0;

    public static void updateCache() {
        if (!DatabaseManager.isEnabled()) return;

        int size = Config.getLeaderboardSize();
        currentSortBy = Config.getLeaderboardDefaultType();
        cachedLeaderboard.clear();
        cachedLeaderboard.addAll(DatabaseManager.getLeaderboard(currentSortBy, size));
        lastUpdate = System.currentTimeMillis();
    }

    public static void sendLeaderboard(Player player, String sortBy) {
        if (!DatabaseManager.isEnabled()) {
            player.sendMessage(ChatColor.RED + "Leaderboard is currently disabled.");
            return;
        }

        updateLeaderboardForPlayer(player, sortBy);
    }

    public static void sendLeaderboard(CommandSender sender, String sortBy) {
        if (!DatabaseManager.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Leaderboard is currently disabled.");
            return;
        }

        updateLeaderboardForSender(sender, sortBy);
    }

    private static void updateLeaderboardForPlayer(Player player, String sortBy) {
        List<DatabaseManager.PlayerStats> stats = DatabaseManager.getLeaderboard(sortBy, Config.getLeaderboardSize());

        String title = switch (sortBy) {
            case "wins" -> "Top Wins";
            case "losses" -> "Top Losses";
            case "plays" -> "Most Plays";
            case "winstreak" -> "Best Winstreak";
            default -> "Top Wins";
        };

        player.sendMessage(ChatColor.GRAY + "╔══════════════════════════════════════╗");
        player.sendMessage(ChatColor.AQUA + "         " + title + " Leaderboard");
        player.sendMessage(ChatColor.GRAY + "╠══════════════════════════════════════╣");

        if (stats.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No stats yet!");
        } else {
            for (int i = 0; i < stats.size(); i++) {
                DatabaseManager.PlayerStats s = stats.get(i);
                ChatColor rankColor = switch (i) {
                    case 0 -> ChatColor.GOLD;
                    case 1 -> ChatColor.GRAY;
                    case 2 -> ChatColor.YELLOW;
                    default -> ChatColor.WHITE;
                };
                String rankIcon = switch (i) {
                    case 0 -> "⚔";
                    case 1 -> "⚔";
                    case 2 -> "⚔";
                    default -> " ";
                };
                player.sendMessage(rankColor + rankIcon + " #" + (i + 1) + " " + s.playerName() + 
                    ChatColor.GRAY + " | " + ChatColor.WHITE + "W:" + ChatColor.GREEN + s.wins() + 
                    ChatColor.WHITE + " L:" + ChatColor.RED + s.losses() + 
                    ChatColor.WHITE + " P:" + s.plays());
            }
        }

        player.sendMessage(ChatColor.GRAY + "╚════════════════════��═════════════════╝");
    }

    private static void updateLeaderboardForSender(CommandSender sender, String sortBy) {
        List<DatabaseManager.PlayerStats> stats = DatabaseManager.getLeaderboard(sortBy, Config.getLeaderboardSize());

        String title = switch (sortBy) {
            case "wins" -> "Top Wins";
            case "losses" -> "Top Losses";
            case "plays" -> "Most Plays";
            case "winstreak" -> "Best Winstreak";
            default -> "Top Wins";
        };

        sender.sendMessage(ChatColor.GRAY + "╔══════════════════════════════════════╗");
        sender.sendMessage(ChatColor.AQUA + "         " + title + " Leaderboard");
        sender.sendMessage(ChatColor.GRAY + "╠══════════════════════════════════════╣");

        if (stats.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No stats yet!");
        } else {
            for (int i = 0; i < stats.size(); i++) {
                DatabaseManager.PlayerStats s = stats.get(i);
                ChatColor rankColor = switch (i) {
                    case 0 -> ChatColor.GOLD;
                    case 1 -> ChatColor.GRAY;
                    case 2 -> ChatColor.YELLOW;
                    default -> ChatColor.WHITE;
                };
                String rankIcon = switch (i) {
                    case 0 -> "⚔";
                    case 1 -> "⚔";
                    case 2 -> "⚔";
                    default -> " ";
                };
                sender.sendMessage(rankColor + rankIcon + " #" + (i + 1) + " " + s.playerName() + 
                    ChatColor.GRAY + " | " + ChatColor.WHITE + "W:" + ChatColor.GREEN + s.wins() + 
                    ChatColor.WHITE + " L:" + ChatColor.RED + s.losses() + 
                    ChatColor.WHITE + " P:" + s.plays());
            }
        }

        sender.sendMessage(ChatColor.GRAY + "╚══════════════════════════════════════╝");
    }

    public static void showMyStats(Player player) {
        if (!DatabaseManager.isEnabled()) {
            player.sendMessage(ChatColor.RED + "Stats are currently disabled.");
            return;
        }

        DatabaseManager.PlayerStats stats = DatabaseManager.getStats(player.getUniqueId());
        float winRate = stats.getWinRate();

        player.sendMessage(ChatColor.GRAY + "══" + "════════════════════════════════════");
        player.sendMessage(ChatColor.AQUA + "           Your Duel Stats");
        player.sendMessage(ChatColor.GRAY + "══" + "════════════════════════════════════");
        player.sendMessage(ChatColor.WHITE + "Player: " + ChatColor.YELLOW + player.getName());
        player.sendMessage(ChatColor.WHITE + "Wins: " + ChatColor.GREEN + stats.wins());
        player.sendMessage(ChatColor.WHITE + "Losses: " + ChatColor.RED + stats.losses());
        player.sendMessage(ChatColor.WHITE + "Plays: " + ChatColor.AQUA + stats.plays());
        player.sendMessage(ChatColor.WHITE + "Win Rate: " + (winRate >= 50 ? ChatColor.GREEN : ChatColor.RED) + 
            String.format("%.1f%%", winRate));
        player.sendMessage(ChatColor.WHITE + "Win Streak: " + ChatColor.GOLD + stats.winstreak());
        player.sendMessage(ChatColor.WHITE + "Best Win Streak: " + ChatColor.GOLD + stats.bestWinstreak());
        player.sendMessage(ChatColor.GRAY + "══" + "════════════════════════════════════");
    }
}