package me.theadamnew.duelsx;

import org.bukkit.ChatColor;

public class Util {

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getPrefix() {
        return Config.getPluginPrefix();
    }

    public static String getPrefixedMessage(String message) {
        return getPrefix() + " " + message;
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(message);
    }
}