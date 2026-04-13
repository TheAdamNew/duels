package me.theadamnew.duelsx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuelGUI implements Listener {

    private static final Map<UUID, DuelSetupData> pendingSetup = new HashMap<>();

public static void openDuelSetup(Player sender, Player target) {
        DuelSetupData existing = pendingSetup.get(sender.getUniqueId());
        String currentMode = (existing != null) ? existing.mode() : "classic";
        int currentRounds = (existing != null) ? existing.rounds() : 1;
        
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "⚔ Duel Setup");

        ItemStack modeItem = createItem(Material.COMPASS, 
            ChatColor.AQUA + "➤ Select Mode", 
            List.of(ChatColor.GRAY + "», Current: " + getModeDisplayName(currentMode)));
        inv.setItem(11, modeItem);

        ItemStack roundsItem = createItem(Material.BOOK, 
            ChatColor.GREEN + "➤ Select Rounds", 
            List.of(ChatColor.GRAY + "», Current: " + currentRounds));
        inv.setItem(13, roundsItem);

        ItemStack confirmItem = createItem(Material.LIME_CONCRETE, 
            ChatColor.GREEN + "ᴄᴏɴꜰɪʀᴍ", 
            List.of(ChatColor.GREEN + "», Click to send request"));
        inv.setItem(22, confirmItem);

        ItemStack cancelItem = createItem(Material.RED_CONCRETE, 
            ChatColor.RED + "ᴄᴀɴᴄᴇʟ", 
            List.of(ChatColor.RED + "», Click to cancel"));
        inv.setItem(26, cancelItem);

        ItemStack player1Head = createPlayerHead(sender);
        inv.setItem(3, player1Head);

        ItemStack vs = createItem(Material.BEACON, 
            ChatColor.RED + "⚔ VS ⚔", 
            List.of());
        inv.setItem(4, vs);

        ItemStack player2Head = createPlayerHead(target);
        inv.setItem(5, player2Head);

        pendingSetup.put(sender.getUniqueId(), new DuelSetupData(target, currentMode, currentRounds));

        sender.openInventory(inv);
    }

    private static String getModeDisplayName(String modeId) {
        Config.DuelMode mode = Config.getDuelModes().get(modeId);
        return mode != null ? mode.name() : "Classic";
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPlayerHead(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "⛃ " + player.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Health: " + ChatColor.GREEN + "❤ " + (int) player.getHealth() + "/20");
        lore.add(ChatColor.GRAY + "GameMode: " + ChatColor.AQUA + player.getGameMode().name());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static DuelSetupData getSetupData(UUID playerId) {
        return pendingSetup.get(playerId);
    }

    public static void removeSetupData(UUID playerId) {
        pendingSetup.remove(playerId);
    }

    public static void updateSetupData(UUID playerId, String mode, int rounds) {
        DuelSetupData data = pendingSetup.get(playerId);
        if (data != null) {
            pendingSetup.put(playerId, new DuelSetupData(data.target(), mode, rounds));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.contains("Duel Setup") && !title.contains("Select Mode") && !title.contains("Select Rounds")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        UUID playerId = player.getUniqueId();
        DuelSetupData setup = pendingSetup.get(playerId);
        if (setup == null) {
            player.closeInventory();
            return;
        }

        if (title.contains("Select Mode")) {
            if (clicked.getType() == Material.LIME_WOOL || clicked.getType() == Material.GRAY_WOOL) {
                String name = clicked.getItemMeta().getDisplayName();
                String displayLower = name.toLowerCase();
                String modeId = null;
                for (Config.DuelMode m : Config.getDuelModes().values()) {
                    if (name.equalsIgnoreCase("✦ " + m.name()) || displayLower.contains(m.name().toLowerCase())) {
                        modeId = m.id();
                        break;
                    }
                }
                if (modeId == null) modeId = "classic";
                updateSetupData(playerId, modeId, setup.rounds());
                openDuelSetup(player, setup.target());
            } else if (clicked.getType() == Material.ARROW) {
                openDuelSetup(player, setup.target());
            }
            return;
        }

        if (title.contains("Select Rounds")) {
            if (clicked.getType() == Material.LIME_WOOL || clicked.getType() == Material.GRAY_WOOL) {
                String name = clicked.getItemMeta().getDisplayName();
                int rounds = 1;
                if (name.contains("Best of 3")) {
                    rounds = 3;
                } else if (name.contains("Best of 5")) {
                    rounds = 5;
                }
                updateSetupData(playerId, setup.mode(), rounds);
                openDuelSetup(player, setup.target());
            } else if (clicked.getType() == Material.ARROW) {
                openDuelSetup(player, setup.target());
            }
            return;
        }

        switch (clicked.getType()) {
            case COMPASS -> openModeSelector(player, setup);
            case BOOK -> openRoundsSelector(player, setup);
            case LIME_CONCRETE -> {
                player.closeInventory();
                Player target = setup.target();
                if (target != null && target.isOnline()) {
                    DuelManager.sendDuelRequestWithSetup(player, target, setup.mode(), setup.rounds());
                }
                pendingSetup.remove(playerId);
            }
            case RED_CONCRETE -> {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Duel request cancelled.");
                pendingSetup.remove(playerId);
            }
            default -> {}
        }
    }

    public static void openModeSelector(Player player, DuelSetupData setup) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "⚔ Select Mode");

        Map<String, Config.DuelMode> modes = Config.getDuelModes();
        int slot = 10;
        for (Config.DuelMode mode : modes.values()) {
            boolean selected = mode.id().equals(setup.mode());
            ItemStack item = new ItemStack(selected ? Material.LIME_WOOL : Material.GRAY_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "✦ " + mode.name());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "» " + mode.description());
            if (selected) {
                lore.add(ChatColor.GREEN + "✓ Selected");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
            if (slot == 14) slot = 16;
            if (slot == 20) break;
        }

        ItemStack back = createItem(Material.ARROW, ChatColor.YELLOW + "« Back", List.of(ChatColor.GRAY + "», Return to setup"));
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    public static void openRoundsSelector(Player player, DuelSetupData setup) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "⚔ Select Rounds");

        int[] rounds = {1, 3, 5};
        int slot = 11;
        for (int r : rounds) {
            boolean selected = r == setup.rounds();
            ItemStack item = new ItemStack(selected ? Material.LIME_WOOL : Material.GRAY_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "✦ " + (r == 1 ? "1 Round (Classic)" : "Best of " + r));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "» First to " + ((r + 1) / 2) + " kills wins");
            if (selected) {
                lore.add(ChatColor.GREEN + "✓ Selected");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot += 2;
        }

        ItemStack back = createItem(Material.ARROW, ChatColor.YELLOW + "« Back", List.of(ChatColor.GRAY + "», Return to setup"));
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    public record DuelSetupData(Player target, String mode, int rounds) {}
}