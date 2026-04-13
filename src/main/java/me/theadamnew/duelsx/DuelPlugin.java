package me.theadamnew.duelsx;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DuelPlugin extends JavaPlugin {

    private static DuelPlugin instance;
    private static FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("DuelPlugin v1.0.0 enabled!");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        Config.init(getConfig());

        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        Messages.init(messagesConfig);

        KitManager.loadKits(this);
        
        DatabaseManager.initialize();
        
        if (DatabaseManager.isEnabled()) {
            int interval = Config.getLeaderboardUpdateInterval() * 20;
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                LeaderboardManager.updateCache();
            }, interval, interval);
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new DuelManager(), this);
        getServer().getPluginManager().registerEvents(new DuelGUI(), this);

        getCommand("duel").setExecutor(new DuelCommand());
        getCommand("dueladmin").setExecutor(new AdminCommand());
        
        getLogger().info("DuelPlugin fully loaded!");
    }

    @Override
    public void onDisable() {
        DuelManager.endAllDuels();
        DatabaseManager.shutdown();
        getLogger().info("DuelPlugin v1.0.0 disabled!");
    }

    public static DuelPlugin getInstance() {
        return instance;
    }
}