package me.theadamnew.duelsx;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private static HikariDataSource dataSource;
    private static boolean isEnabled = false;

    public static void initialize() {
        if (!Config.isDatabaseEnabled()) {
            Bukkit.getLogger().info("[DuelPlugin] Database disabled in config.");
            isEnabled = false;
            return;
        }

        try {
            dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:mysql://" + Config.getDatabaseHost() + ":" + Config.getDatabasePort() + 
                "/" + Config.getDatabaseName() + "?useSSL=false&allowPublicKeyRetrieval=true");
            dataSource.setUsername(Config.getDatabaseUsername());
            dataSource.setPassword(Config.getDatabasePassword());
            dataSource.setMaximumPoolSize(10);
            dataSource.setMinimumIdle(2);

            createTables();
            isEnabled = true;
            Bukkit.getLogger().info("[DuelPlugin] MySQL connection established!");
        } catch (Exception e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to connect to MySQL: " + e.getMessage());
            isEnabled = false;
        }
    }

    private static void createTables() {
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS duel_stats (
                player_uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                wins INT DEFAULT 0,
                losses INT DEFAULT 0,
                plays INT DEFAULT 0,
                winstreak INT DEFAULT 0,
                best_winstreak INT DEFAULT 0,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(createStatsTable)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to create stats table: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || !isEnabled) {
            throw new SQLException("Database not enabled");
        }
        return dataSource.getConnection();
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static CompletableFuture<Void> updateStatsAsync(UUID playerId, String playerName, boolean isWin) {
        return CompletableFuture.runAsync(() -> updateStats(playerId, playerName, isWin));
    }

    public static void updateStats(UUID playerId, String playerName, boolean isWin) {
        if (!isEnabled) return;

        String sql = """
            INSERT INTO duel_stats (player_uuid, player_name, wins, losses, plays, winstreak, best_winstreak)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                player_name = VALUES(player_name),
                wins = wins + VALUES(wins),
                losses = losses + VALUES(losses),
                plays = plays + VALUES(plays),
                winstreak = VALUES(winstreak),
                best_winstreak = GREATEST(best_winstreak, VALUES(best_winstreak)),
                last_updated = CURRENT_TIMESTAMP
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int wins = isWin ? 1 : 0;
            int losses = isWin ? 0 : 1;
            int plays = 1;
            int winstreak = isWin ? 1 : 0;
            int bestWinstreak = isWin ? 1 : 0;

            stmt.setString(1, playerId.toString());
            stmt.setString(2, playerName);
            stmt.setInt(3, wins);
            stmt.setInt(4, losses);
            stmt.setInt(5, plays);
            stmt.setInt(6, winstreak);
            stmt.setInt(7, bestWinstreak);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to update stats: " + e.getMessage());
        }
    }

    public static PlayerStats getStats(UUID playerId) {
        if (!isEnabled) return new PlayerStats(0, "", 0, 0, 0, 0);

        String sql = "SELECT * FROM duel_stats WHERE player_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerStats(
                    rs.getInt("wins"),
                    rs.getString("player_name"),
                    rs.getInt("losses"),
                    rs.getInt("plays"),
                    rs.getInt("winstreak"),
                    rs.getInt("best_winstreak")
                );
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to get stats: " + e.getMessage());
        }
        return new PlayerStats(0, "", 0, 0, 0, 0);
    }

    public static List<PlayerStats> getLeaderboard(String sortBy, int limit) {
        List<PlayerStats> stats = new ArrayList<>();
        if (!isEnabled) return stats;

        String column = switch (sortBy) {
            case "wins" -> "wins";
            case "losses" -> "losses";
            case "plays" -> "plays";
            case "winstreak", "best_winstreak" -> "best_winstreak";
            default -> "wins";
        };

        String sql = "SELECT * FROM duel_stats ORDER BY " + column + " DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.add(new PlayerStats(
                    rs.getInt("wins"),
                    rs.getString("player_name"),
                    rs.getInt("losses"),
                    rs.getInt("plays"),
                    rs.getInt("winstreak"),
                    rs.getInt("best_winstreak")
                ));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to get leaderboard: " + e.getMessage());
        }
        return stats;
    }

    public static void resetStats(UUID playerId) {
        if (!isEnabled) return;

        String sql = "DELETE FROM duel_stats WHERE player_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to reset stats: " + e.getMessage());
        }
    }

    public static void resetAllStats() {
        if (!isEnabled) return;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM duel_stats")) {
            stmt.executeUpdate();
            Bukkit.getLogger().info("[DuelPlugin] All stats have been reset.");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[DuelPlugin] Failed to reset all stats: " + e.getMessage());
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public record PlayerStats(int wins, String playerName, int losses, int plays, int winstreak, int bestWinstreak) {
        public float getWinRate() {
            return plays > 0 ? (float) wins / plays * 100 : 0;
        }
    }
}