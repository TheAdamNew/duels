package me.theadamnew.duelsx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

    private static FileConfiguration config;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static void init(FileConfiguration cfg) {
        config = cfg;
    }

    public static String get(String path) {
        return config.getString("messages." + path, "");
    }

    public static String get(String path, Map<String, String> placeholders) {
        String message = get(path);
        if (message.isEmpty()) {
            return "";
        }
        for (var entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public static void send(Player player, String path) {
        String message = get(path);
        if (!message.isEmpty() && player != null) {
            player.sendMessage(parse(message));
        }
    }

    public static void send(Player player, String path, Map<String, String> placeholders) {
        String message = get(path, placeholders);
        if (!message.isEmpty() && player != null) {
            player.sendMessage(parse(message));
        }
    }

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        if (Config.isMiniMessageEnabled()) {
            message = convertLegacyColors(message);
            try {
                return MINI_MESSAGE.deserialize(message);
            } catch (Exception e) {
                return SERIALIZER.deserialize(message);
            }
        } else {
            return SERIALIZER.deserialize(message);
        }
    }

    public static Component withPrefix(String path) {
        return parse(get("prefix") + get(path));
    }

    public static Component withPrefix(String path, Map<String, String> placeholders) {
        return parse(get("prefix") + get(path, placeholders));
    }

    private static String convertLegacyColors(String message) {
        Map<String, String> colorMap = Map.ofEntries(
            Map.entry("&0", "<black>"),
            Map.entry("&1", "<dark_blue>"),
            Map.entry("&2", "<dark_green>"),
            Map.entry("&3", "<dark_aqua>"),
            Map.entry("&4", "<dark_red>"),
            Map.entry("&5", "<dark_purple>"),
            Map.entry("&6", "<gold>"),
            Map.entry("&7", "<gray>"),
            Map.entry("&8", "<dark_gray>"),
            Map.entry("&9", "<blue>"),
            Map.entry("&a", "<green>"),
            Map.entry("&b", "<aqua>"),
            Map.entry("&c", "<red>"),
            Map.entry("&d", "<light_purple>"),
            Map.entry("&e", "<yellow>"),
            Map.entry("&f", "<white>"),
            Map.entry("&k", "<obfuscated>"),
            Map.entry("&l", "<bold>"),
            Map.entry("&m", "<strikethrough>"),
            Map.entry("&n", "<underline>"),
            Map.entry("&o", "<italic>"),
            Map.entry("&r", "<reset>")
        );

        for (var entry : colorMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        if (Config.isRgbSupportEnabled()) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String hex = matcher.group(1);
                matcher.appendReplacement(sb, "<#" + hex + ">");
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }

        return message;
    }

    public static String getPrefix() {
        return SERIALIZER.serialize(parse(get("prefix")));
    }
}
