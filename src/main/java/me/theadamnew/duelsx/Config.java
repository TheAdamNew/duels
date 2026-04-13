package me.theadamnew.duelsx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Config {

    private static FileConfiguration config;
    private static final Random random = new Random();

    public static void init(FileConfiguration cfg) {
        config = cfg;
    }

    public static List<String> getMaps() {
        List<String> maps = new ArrayList<>();
        if (config.contains("maps")) {
            maps.addAll(config.getConfigurationSection("maps").getKeys(false));
        }
        return maps;
    }

    public static String getRandomMap() {
        List<String> maps = getMaps();
        if (maps.isEmpty()) {
            return null;
        }
        return maps.get(random.nextInt(maps.size()));
    }

    public static String getMapWorld(String mapName) {
        return config.getString("maps." + mapName + ".world", "");
    }

    public static World getMapWorld(String mapName, boolean autoLoad) {
        String worldName = getMapWorld(mapName);
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }
        
        World world = Bukkit.getWorld(worldName);
        return world;
    }

    public static Location getMapSpawn(String mapName, String playerNum) {
        String path = "maps." + mapName + ".spawn" + playerNum;
        
        if (!config.contains(path)) {
            return null;
        }
        
        String worldName = config.getString("maps." + mapName + ".world", "");
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return null;
        }
        
        double x = config.getDouble(path + ".x", 0);
        double y = config.getDouble(path + ".y", 64);
        double z = config.getDouble(path + ".z", 0);
        float yaw = (float) config.getDouble(path + ".yaw", 0);
        float pitch = (float) config.getDouble(path + ".pitch", 0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static boolean isMapValid(String mapName) {
        String worldName = getMapWorld(mapName);
        if (worldName == null || worldName.isEmpty()) {
            return false;
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }
        
        return getMapSpawn(mapName, "1") != null && getMapSpawn(mapName, "2") != null;
    }

    public static int getRequestTimeout() {
        return config.getInt("settings.request-timeout", 30);
    }

    public static boolean shouldClearInventory() {
        return config.getBoolean("settings.clear-inventory", true);
    }

    public static boolean shouldGiveDefaultItems() {
        return config.getBoolean("settings.give-default-items", true);
    }

    public static int getHealth() {
        return config.getInt("settings.health", 20);
    }

    public static int getFood() {
        return config.getInt("settings.food", 20);
    }

    public static double getGodModeDuration() {
        return config.getDouble("settings.god-mode-duration", 2.5);
    }

    public static boolean isGodModeFlyEnabled() {
        return config.getBoolean("settings.god-mode-fly", true);
    }

    public static boolean isDatabaseEnabled() {
        return config.getBoolean("database.enabled", true);
    }

    public static String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public static int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }

    public static String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }

    public static String getDatabasePassword() {
        return config.getString("database.password", "password");
    }

    public static String getDatabaseName() {
        return config.getString("database.database", "duel_plugin");
    }

    public static boolean isRewardEnabled() {
        return config.getBoolean("rewards.win.enabled", true);
    }

    public static int getWinRewardMoney() {
        return config.getInt("rewards.win.money", 100);
    }

    public static int getWinRewardExp() {
        return config.getInt("rewards.win.experience", 50);
    }

    public static List<String> getWinRewardCommands() {
        return config.getStringList("rewards.win.commands");
    }

    public static boolean isLoseRewardEnabled() {
        return config.getBoolean("rewards.lose.enabled", true);
    }

    public static int getLoseRewardMoney() {
        return config.getInt("rewards.lose.money", 25);
    }

    public static int getLoseRewardExp() {
        return config.getInt("rewards.lose.experience", 10);
    }

    public static boolean isStatsEnabled() {
        return config.getBoolean("stats.enabled", true);
    }

    public static String getStatsResetPermission() {
        return config.getString("stats.reset-permission", "duel.admin.reset");
    }

    public static int getLeaderboardSize() {
        return config.getInt("leaderboard.size", 10);
    }

    public static int getLeaderboardUpdateInterval() {
        return config.getInt("leaderboard.update-interval", 60);
    }

    public static String getLeaderboardDefaultType() {
        return config.getString("leaderboard.default-type", "wins");
    }

    public static String getPluginPrefix() {
        return config.getString("settings.prefix", "<gradient:#ffd700:#ff8c00>[Duels]</gradient>");
    }

    public static boolean isMiniMessageEnabled() {
        return config.getBoolean("settings.mini-message", true);
    }

    public static boolean isRgbSupportEnabled() {
        return config.getBoolean("settings.rgb-support", true);
    }

    public static Map<String, DuelMode> getDuelModes() {
        Map<String, DuelMode> modes = new HashMap<>();
        if (config.contains("modes")) {
            for (String key : config.getConfigurationSection("modes").getKeys(false)) {
                String path = "modes." + key;
                String name = config.getString(path + ".name", key);
                String description = config.getString(path + ".description", "");
                int rounds = config.getInt(path + ".rounds", 1);
                modes.put(key, new DuelMode(key, name, description, rounds));
            }
        }
        return modes;
    }

    public static DuelMode getDefaultMode() {
        return new DuelMode("classic", "Classic", "First to kill wins", 1);
    }

    public static record DuelMode(String id, String name, String description, int rounds) {}
}