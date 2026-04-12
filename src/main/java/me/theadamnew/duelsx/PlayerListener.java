package me.theadamnew.duelsx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        DuelManager.onPlayerQuit(event.getPlayer());
        DuelGUI.removeSetupData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        
        if (title.contains("Kit Selector")) {
            event.setCancelled(true);
            handleKitSelectorClick(player, event.getCurrentItem());
            return;
        }
        
        if (title.contains("Duel Setup") || title.contains("Select Mode") || title.contains("Select Rounds")) {
            event.setCancelled(true);
            handleDuelGUIClick(player, event.getCurrentItem(), title);
        }
    }

    private void handleKitSelectorClick(Player player, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        for (KitManager.KitData kit : KitManager.getAllKits()) {
            if (clicked.getType() == kit.getIcon().getType()) {
                boolean hasAccess = kit.permission().isEmpty() || player.hasPermission(kit.permission());
                if (!hasAccess) {
                    player.sendMessage(ChatColor.RED + "You don't have permission for this kit!");
                    return;
                }

                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Kit selected: " + kit.displayName());
                player.sendMessage(ChatColor.YELLOW + "Now use /duel <player> to start a duel!");
                break;
            }
        }
    }

    private void handleDuelGUIClick(Player player, ItemStack clicked, String title) {
        if (clicked == null || clicked.getType() == Material.AIR) return;

        UUID playerId = player.getUniqueId();
        DuelGUI.DuelSetupData setup = DuelGUI.getSetupData(playerId);

        if (clicked.getType() == Material.RED_CONCRETE) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Duel request cancelled.");
            DuelGUI.removeSetupData(playerId);
            return;
        }

        if (clicked.getType() == Material.ARROW && title.contains("Select")) {
            DuelGUI.openDuelSetup(player, setup != null ? setup.target() : null);
            return;
        }

        if (title.contains("Duel Setup")) {
            if (clicked.getType() == Material.COMPASS) {
                if (setup != null) DuelGUI.openModeSelector(player, setup);
            } else if (clicked.getType() == Material.BOOK) {
                if (setup != null) DuelGUI.openRoundsSelector(player, setup);
            } else if (clicked.getType() == Material.LIME_CONCRETE) {
                if (setup != null && setup.target() != null && setup.target().isOnline()) {
                    player.closeInventory();
                    DuelManager.sendDuelRequestWithSetup(player, setup.target(), setup.mode(), setup.rounds());
                    DuelGUI.removeSetupData(playerId);
                }
            }
        } else if (title.contains("Select Mode")) {
            if (clicked.getType() == Material.LIME_WOOL || clicked.getType() == Material.GRAY_WOOL) {
                String name = clicked.getItemMeta().getDisplayName().toLowerCase();
                String mode;
                if (name.contains("sword")) {
                    mode = "sword";
                } else if (name.contains("archer")) {
                    mode = "archer";
                } else {
                    mode = "classic";
                }
                
                if (setup != null) {
                    DuelGUI.updateSetupData(playerId, mode, setup.rounds());
                }
                DuelGUI.openDuelSetup(player, setup != null ? setup.target() : null);
            }
        }
    }
}