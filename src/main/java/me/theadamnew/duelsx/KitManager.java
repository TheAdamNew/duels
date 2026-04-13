package me.theadamnew.duelsx;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public class KitManager {

    private static final Map<String, KitData> kits = new HashMap<>();
    private static final Map<Player, String> playerKits = new HashMap<>();
    private static File kitsFile;

    public static void loadKits(DuelPlugin plugin) {
        kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        loadKitsFromFile();
    }

    private static void loadKitsFromFile() {
        if (!kitsFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(kitsFile);
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) {
            return;
        }
        for (String key : kitsSection.getKeys(false)) {
            String path = "kits." + key;
            String permission = config.getString(path + ".permission", "");
            String displayName = config.getString(path + ".displayname", key);
            String icon = config.getString(path + ".icon", "IRON_SWORD");
            List<String> lore = config.getStringList(path + ".lore");
            List<String> items = config.getStringList(path + ".items");
            kits.put(key.toLowerCase(), new KitData(key, permission, displayName, icon, lore, items));
        }
    }

    public static void reloadKits() {
        loadKitsFromFile();
    }

    public static boolean giveKit(Player player, String kitName) {
        KitData kit = kits.get(kitName.toLowerCase());
        if (kit == null) {
            kit = kits.get("default");
        }
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return false;
        }
        boolean hasPermission = kit.permission().isEmpty() || player.hasPermission(kit.permission());
        if (!hasPermission) {
            player.sendMessage(ChatColor.RED + "You don't have permission for the " + kitName + " kit!");
            return false;
        }
        giveKitItems(player, kit.items());
        playerKits.put(player, kitName.toLowerCase());
        return true;
    }

    private static void giveKitItems(Player player, List<String> items) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        for (String itemStr : items) {
            try {
                if (itemStr.contains("&effect:")) {
                    applyPotionEffect(player, itemStr);
                } else {
                    ItemStack item = parseItemString(itemStr);
                    if (item != null) {
                        player.getInventory().addItem(item);
                    }
                }
            } catch (Exception e) {
                DuelPlugin.getInstance().getLogger().warning("Failed to parse item: " + itemStr);
            }
        }
    }

    private static void applyPotionEffect(Player player, String itemStr) {
        String[] parts = itemStr.split("&effect:");
        String potionDef = parts[0];
        if (!potionDef.contains("POTION:")) {
            return;
        }
        String[] effectParts = parts[1].split("&");
        String effectName = "";
        int duration = 180;
        int level = 1;
        for (String prop : effectParts) {
            if (prop.startsWith("effect:")) {
                effectName = prop.split(":")[1].toUpperCase();
            } else if (prop.startsWith("duration:")) {
                duration = Integer.parseInt(prop.split(":")[1]);
            } else if (prop.startsWith("level:")) {
                level = Integer.parseInt(prop.split(":")[1]);
            }
        }
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType != null) {
            player.addPotionEffect(new PotionEffect(effectType, duration * 20, level - 1));
        }
    }

    private static ItemStack parseItemString(String itemStr) {
        if (itemStr.contains("|")) {
            String[] parts = itemStr.split("\\|");
            itemStr = parts[0].trim();
        }
        String[] parts = itemStr.split(":");
        if (parts.length < 2) return null;
        String materialName = parts[0].toUpperCase();
        int amount = 1;
        try {
            amount = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            amount = 1;
        }
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            return null;
        }
        ItemStack item = new ItemStack(material, amount);
        if (itemStr.contains("&enchant:")) {
            String[] enchantParts = itemStr.split("&enchant:");
            if (enchantParts.length > 1) {
                String[] enchParts = enchantParts[1].split(":");
                if (enchParts.length >= 2) {
                    String enchantName = enchParts[0].toUpperCase();
                    int enchantLevel = 1;
                    try {
                        enchantLevel = Integer.parseInt(enchParts[1]);
                    } catch (NumberFormatException ignored) {}
                    Enchantment enchant = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchantName.toLowerCase()));
                    if (enchant != null) {
                        item.addUnsafeEnchantment(enchant, enchantLevel);
                        ItemMeta meta = item.getItemMeta();
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
        return item;
    }

    public static String getPlayerKit(Player player) {
        return playerKits.getOrDefault(player, "default");
    }

    public static void clearPlayerKit(Player player) {
        playerKits.remove(player);
    }

    public static List<String> getAvailableKits() {
        return new ArrayList<>(kits.keySet());
    }

    public static List<String> getKitsWithAccess(Player player) {
        return kits.entrySet().stream()
            .filter(e -> e.getValue().permission().isEmpty() || player.hasPermission(e.getValue().permission()))
            .map(Map.Entry::getKey)
            .toList();
    }

    public static KitData getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public static Collection<KitData> getAllKits() {
        return kits.values();
    }

    public record KitData(String name, String permission, String displayName, String icon, List<String> lore, List<String> items) {
        public ItemStack getIcon() {
            Material mat = Material.getMaterial(icon());
            if (mat == null) mat = Material.IRON_SWORD;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName()));
                meta.setLore(lore().stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList());
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
            return item;
        }
    }
}