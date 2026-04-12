package me.theadamnew.duelsx;

import org.bukkit.plugin.java.JavaPlugin;

public final class DuelPlugin extends JavaPlugin {

    private static DuelPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("DuelPlugin v1.0.0 enabled!");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        Config.init(getConfig());

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