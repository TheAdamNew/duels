package me.theadamnew.duelsx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class Util {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        if (Config.isMiniMessageEnabled()) {
            return SERIALIZER.serialize(Messages.parse(message));
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Component colorizeComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return Messages.parse(message);
    }

    public static String getPrefix() {
        return Messages.getPrefix();
    }

    public static String getPrefixedMessage(String message) {
        return getPrefix() + " " + message;
    }

    public static Component getPrefixedMessageComponent(String message) {
        return Messages.parse(Messages.get("prefix") + " " + message);
    }

    public static void sendMessage(Player player, String path) {
        Messages.send(player, path);
    }

    public static void sendMessage(Player player, String path, Map<String, String> placeholders) {
        Messages.send(player, path, placeholders);
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(message);
    }
}
