package me.theadamnew.duelsx;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager implements Listener {

    private static final Map<UUID, Duel> activeDuels = new HashMap<>();
    private static final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();

    public static void sendDuelRequest(Player sender, Player target) {
        DuelGUI.openDuelSetup(sender, target);
    }

    public static void sendDuelRequestWithSetup(Player sender, Player target, String mode, int rounds) {
        if (isInDuel(sender) || isInDuel(target)) {
            sender.sendMessage(ChatColor.RED + "One of the players is already in a duel!");
            return;
        }

        if (isPendingRequest(target)) {
            sender.sendMessage(ChatColor.RED + target.getName() + " already has a pending duel request!");
            return;
        }

        DuelRequest request = new DuelRequest(sender, target, mode, rounds);
        pendingRequests.put(target.getUniqueId(), request);

        sender.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
        sender.sendMessage(ChatColor.GREEN + "Duel request sent to " + target.getName());
        sender.sendMessage(ChatColor.YELLOW + "Mode: " + ChatColor.AQUA + mode + ChatColor.YELLOW + " | Rounds: " + ChatColor.AQUA + rounds);
        sender.sendMessage(ChatColor.GRAY + "═══════════════════════════════");

        target.sendMessage(ChatColor.GRAY + "═══════════════════════════════");
        target.sendMessage(ChatColor.GOLD + "⚔ " + sender.getName() + ChatColor.YELLOW + " wants to duel you!");
        target.sendMessage(ChatColor.YELLOW + "Mode: " + ChatColor.AQUA + mode + ChatColor.YELLOW + " | Rounds: " + ChatColor.AQUA + rounds);
        target.sendMessage(ChatColor.YELLOW + " ");
        target.sendMessage(ChatColor.GREEN + "► " + ChatColor.BOLD + "ACCEPT" + ChatColor.RESET + ChatColor.GREEN + " - /duel accept");
        target.sendMessage(ChatColor.RED + "► " + ChatColor.BOLD + "DENY" + ChatColor.RESET + ChatColor.RED + " - /duel deny");
        target.sendMessage(ChatColor.GRAY + "═══════════════════════════════");

        Bukkit.getScheduler().runTaskLater(DuelPlugin.getInstance(), () -> {
            pendingRequests.remove(target.getUniqueId());
        }, Config.getRequestTimeout() * 20L);
    }

    public static boolean hasPendingRequest(UUID playerId) {
        return pendingRequests.containsKey(playerId);
    }

    public static DuelRequest getPendingRequest(UUID playerId) {
        return pendingRequests.get(playerId);
    }

    public static boolean acceptDuel(Player player) {
        DuelRequest request = pendingRequests.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage(ChatColor.RED + "No pending duel request!");
            return false;
        }

        Player sender = request.sender();
        if (!sender.isOnline()) {
            player.sendMessage(ChatColor.RED + "The player is no longer online!");
            return false;
        }

        startDuel(sender, player, request.mode(), request.rounds());
        return true;
    }

    public static void denyDuel(Player player) {
        DuelRequest request = pendingRequests.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage(ChatColor.RED + "No pending duel request!");
            return;
        }

        Player sender = request.sender();
        if (sender.isOnline()) {
            sender.sendMessage(ChatColor.RED + player.getName() + " denied your duel request!");
        }
        player.sendMessage(ChatColor.YELLOW + "Duel request denied.");
    }

    public static void startDuel(Player player1, Player player2, String mode, int rounds) {
        String arenaName = Config.getRandomMap();
        
        if (arenaName == null || !Config.isMapValid(arenaName)) {
            player1.sendMessage(ChatColor.RED + "No valid arena found! Please create an arena first.");
            player2.sendMessage(ChatColor.RED + "No valid arena found! Please create an arena first.");
            return;
        }

        Location spawn1 = Config.getMapSpawn(arenaName, "1");
        Location spawn2 = Config.getMapSpawn(arenaName, "2");
        
        if (spawn1 == null || spawn2 == null) {
            player1.sendMessage(ChatColor.RED + "Arena spawns not configured properly!");
            player2.sendMessage(ChatColor.RED + "Arena spawns not configured properly!");
            return;
        }

        UUID p1Id = player1.getUniqueId();
        UUID p2Id = player2.getUniqueId();

        GameMode p1OriginalGM = player1.getGameMode();
        GameMode p2OriginalGM = player2.getGameMode();
        Location p1OriginalLoc = player1.getLocation().clone();
        Location p2OriginalLoc = player2.getLocation().clone();

        InventoryManager.saveInventory(
            p1Id,
            player1.getInventory().getContents(),
            player1.getInventory().getArmorContents(),
            player1.getInventory().getItemInOffHand(),
            player1.getHealth(),
            player1.getMaxHealth(),
            player1.getFoodLevel(),
            player1.getExhaustion(),
            player1.getActivePotionEffects(),
            player1.getLocation().clone()
        );

        InventoryManager.saveInventory(
            p2Id,
            player2.getInventory().getContents(),
            player2.getInventory().getArmorContents(),
            player2.getInventory().getItemInOffHand(),
            player2.getHealth(),
            player2.getMaxHealth(),
            player2.getFoodLevel(),
            player2.getExhaustion(),
            player2.getActivePotionEffects(),
            player2.getLocation().clone()
        );

        if (Config.shouldClearInventory()) {
            player1.getInventory().clear();
            player1.getInventory().setArmorContents(new ItemStack[4]);
            player1.getInventory().setItemInOffHand(null);
            player2.getInventory().clear();
            player2.getInventory().setArmorContents(new ItemStack[4]);
            player2.getInventory().setItemInOffHand(null);
        }

        player1.setHealth(Config.getHealth());
        player1.setFoodLevel(Config.getFood());
        player1.setExhaustion(0f);
        player1.getActivePotionEffects().forEach(e -> player1.removePotionEffect(e.getType()));

        player2.setHealth(Config.getHealth());
        player2.setFoodLevel(Config.getFood());
        player2.setExhaustion(0f);
        player2.getActivePotionEffects().forEach(e -> player2.removePotionEffect(e.getType()));

        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);

        if (Config.shouldGiveDefaultItems()) {
            clearDuelInventory(player1);
            clearDuelInventory(player2);
            String kitName = mode.equals("archer") ? "archer" : mode.equals("sword") ? "sword" : "default";
            KitManager.giveKit(player1, kitName);
            KitManager.giveKit(player2, kitName);
        }

        player1.teleport(spawn1);
        player2.teleport(spawn2);

        spawn1.getWorld().setGameRule(org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS, false);
        spawn2.getWorld().setGameRule(org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS, false);

        player1.setInvulnerable(true);
        player2.setInvulnerable(true);
        
        if (Config.isGodModeFlyEnabled()) {
            player1.setAllowFlight(true);
            player2.setAllowFlight(true);
        }

        Bukkit.getScheduler().runTaskLater(DuelPlugin.getInstance(), () -> {
            if (player1.isOnline()) {
                player1.setInvulnerable(false);
                player1.setAllowFlight(false);
                player1.setFlying(false);
            }
            if (player2.isOnline()) {
                player2.setInvulnerable(false);
                player2.setAllowFlight(false);
                player2.setFlying(false);
            }
        }, (long) (Config.getGodModeDuration() * 20));

        Duel duel = new Duel(player1, player2, spawn1.getWorld(), p1OriginalLoc, p2OriginalLoc, p1OriginalGM, p2OriginalGM, mode, rounds, 0, 0);
        activeDuels.put(p1Id, duel);
        activeDuels.put(p2Id, duel);

        Bukkit.broadcastMessage(ChatColor.AQUA + player1.getName() + ChatColor.WHITE + " vs " + 
            ChatColor.AQUA + player2.getName() + ChatColor.GREEN + " - Duel started!");
        Bukkit.broadcastMessage(ChatColor.GRAY + "Arena: " + arenaName + " | Mode: " + mode);
    }

    public static void endDuel(Duel duel, Player winner, Player loser) {
        boolean isWinnerOnline = winner != null && winner.isOnline();
        boolean isLoserOnline = loser != null && loser.isOnline();

        if (isWinnerOnline) {
            restoreInventory(winner);
            Location loc = duel.p1OriginalLocation();
            if (winner.getUniqueId().equals(duel.player1().getUniqueId())) {
                loc = duel.p1OriginalLocation();
            } else {
                loc = duel.p2OriginalLocation();
            }
            
            if (loc != null) {
                winner.teleport(loc);
            }
            
            winner.setGameMode(duel.player1().getUniqueId().equals(winner.getUniqueId()) ? 
                duel.p1OriginalGM() : duel.p2OriginalGM());
            
            int winsNeeded = (duel.rounds() + 1) / 2;
            winner.sendMessage(ChatColor.GREEN + "You won the duel! " + ChatColor.GRAY + "(" + 
                (winner.getUniqueId().equals(duel.player1().getUniqueId()) ? duel.p1Wins() : duel.p2Wins()) + 
                "/" + winsNeeded + ")");
            giveReward(winner, true);
            
            if (Config.isStatsEnabled() && !"practice".equals(duel.mode())) {
                DatabaseManager.updateStatsAsync(winner.getUniqueId(), winner.getName(), true);
            }
        }

        if (isLoserOnline) {
            Location loc = loser.getUniqueId().equals(duel.player1().getUniqueId()) ? 
                duel.p1OriginalLocation() : duel.p2OriginalLocation();
            
            if (loser.isDead()) {
                loser.setHealth(20);
                loser.setFoodLevel(20);
                loser.spigot().respawn();
            }
            
            if (loc != null) {
                final Location teleportLoc = loc;
                Bukkit.getScheduler().runTaskLater(DuelPlugin.getInstance(), () -> {
                    if (loser.isOnline()) {
                        loser.teleport(teleportLoc);
                        loser.setGameMode(loser.getUniqueId().equals(duel.player1().getUniqueId()) ? 
                            duel.p1OriginalGM() : duel.p2OriginalGM());
                    }
                }, 2L);
            }
            
            restoreInventory(loser);
            loser.setHealth(20);
            loser.setFoodLevel(20);
            
            loser.sendMessage(ChatColor.RED + "You lost the duel!");
            giveReward(loser, false);
            
            if (Config.isStatsEnabled() && !"practice".equals(duel.mode())) {
                DatabaseManager.updateStatsAsync(loser.getUniqueId(), loser.getName(), false);
            }
        }

        if (duel != null) {
            activeDuels.remove(duel.player1().getUniqueId());
            activeDuels.remove(duel.player2().getUniqueId());
            
            KitManager.clearPlayerKit(duel.player1());
            KitManager.clearPlayerKit(duel.player2());
        }

        if (winner != null || loser != null) {
            Player p1 = duel != null ? duel.player1() : null;
            Player p2 = duel != null ? duel.player2() : null;
            if (p1 != null) InventoryManager.removeInventory(p1.getUniqueId());
            if (p2 != null) InventoryManager.removeInventory(p2.getUniqueId());
        }
    }

    private static void giveReward(Player player, boolean isWin) {
        if (!isWin && !Config.isLoseRewardEnabled()) {
            return;
        }
        
        if (isWin && Config.isRewardEnabled()) {
            int money = Config.getWinRewardMoney();
            int exp = Config.getWinRewardExp();
            player.sendMessage(ChatColor.GREEN + "You received " + money + " coins and " + exp + " XP!");
            
            for (String cmd : Config.getWinRewardCommands()) {
                String command = cmd.replace("{player}", player.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
            }
        } else if (!isWin && Config.isLoseRewardEnabled()) {
            int money = Config.getLoseRewardMoney();
            int exp = Config.getLoseRewardExp();
            player.sendMessage(ChatColor.YELLOW + "You received " + money + " coins and " + exp + " XP!");
        }
    }

    public static void endDuelByForfeit(Player player) {
        Duel duel = activeDuels.get(player.getUniqueId());
        if (duel == null) {
            player.sendMessage(ChatColor.RED + "You're not in a duel!");
            return;
        }

        Player other = getOpponent(player, duel);
        endDuel(duel, other, player);
        if (other != null && other.isOnline()) {
            other.sendMessage(ChatColor.GREEN + player.getName() + " forfeited! You win!");
        }
    }

    public static boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    public static boolean isPendingRequest(Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }

    public static Duel getDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }

    public static Player getOpponent(Player player, Duel duel) {
        if (duel == null) return null;
        if (player.getUniqueId().equals(duel.player1().getUniqueId())) {
            return duel.player2();
        }
        return duel.player1();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Duel duel = activeDuels.get(player.getUniqueId());

        if (duel != null) {
            event.setDeathMessage(null);
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            
            Player winner = getOpponent(player, duel);
            boolean player1Won = winner.getUniqueId().equals(duel.player1().getUniqueId());
            
            int newP1Wins = player1Won ? duel.p1Wins() + 1 : duel.p1Wins();
            int newP2Wins = player1Won ? duel.p2Wins() : duel.p2Wins() + 1;
            
            int winsNeeded = (duel.rounds() + 1) / 2;
            
            if (newP1Wins >= winsNeeded || newP2Wins >= winsNeeded) {
                Bukkit.broadcastMessage(ChatColor.GOLD + winner.getName() + ChatColor.GREEN + " wins the match!");
                endDuel(duel, winner, player);
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " died! " + 
                    ChatColor.GREEN + winner.getName() + ChatColor.YELLOW + " wins the round! " +
                    ChatColor.AQUA + "(" + newP1Wins + " - " + newP2Wins + ")");
                
                activeDuels.remove(player.getUniqueId());
                activeDuels.remove(winner.getUniqueId());
                
                Duel newDuel = new Duel(duel.player1(), duel.player2(), duel.arenaWorld(),
                    duel.p1OriginalLocation(), duel.p2OriginalLocation(),
                    duel.p1OriginalGM(), duel.p2OriginalGM(),
                    duel.mode(), duel.rounds(), newP1Wins, newP2Wins);
                
                activeDuels.put(duel.player1().getUniqueId(), newDuel);
                activeDuels.put(duel.player2().getUniqueId(), newDuel);
                
                respawnPlayersForNextRound(duel.player1(), duel.player2(), duel.arenaWorld());
            }
        }
    }
    
    private static void respawnPlayersForNextRound(Player p1, Player p2, World arenaWorld) {
        String mapName = Config.getRandomMap();
        if (mapName == null) {
            mapName = Config.getMaps().get(0);
        }
        if (mapName == null) {
            endDuel(null, null, null);
            return;
        }
        
        Location spawn1 = Config.getMapSpawn(mapName, "1");
        Location spawn2 = Config.getMapSpawn(mapName, "2");
        
        p1.setHealth(20);
        p1.setFoodLevel(20);
        p1.setInvulnerable(true);
        
        p2.setHealth(20);
        p2.setFoodLevel(20);
        p2.setInvulnerable(true);
        
        p1.teleport(spawn1);
        p2.teleport(spawn2);
        
        Bukkit.getScheduler().runTaskLater(DuelPlugin.getInstance(), () -> {
            if (p1.isOnline()) p1.setInvulnerable(false);
            if (p2.isOnline()) p2.setInvulnerable(false);
        }, (long) (Config.getGodModeDuration() * 20));
    }

    public static void onPlayerQuit(Player player) {
        Duel duel = activeDuels.get(player.getUniqueId());
        if (duel != null) {
            Player other = getOpponent(player, duel);
            endDuel(duel, other, player);
            if (other != null && other.isOnline()) {
                other.sendMessage(ChatColor.RED + "Your opponent left the duel!");
            }
        }

        pendingRequests.remove(player.getUniqueId());
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().sender().getUniqueId().equals(player.getUniqueId()));
    }

    public static void endAllDuels() {
        for (UUID playerId : activeDuels.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                restoreInventory(player);
            }
            InventoryManager.removeInventory(playerId);
        }
        activeDuels.clear();
    }

    private static void clearDuelInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
    }

    private static void restoreInventory(Player player) {
        UUID playerId = player.getUniqueId();

        if (InventoryManager.hasInventory(playerId)) {
            ItemStack[] inventory = InventoryManager.getInventory(playerId);
            ItemStack[] armor = InventoryManager.getArmor(playerId);
            ItemStack offHand = InventoryManager.getOffHand(playerId);
            double health = InventoryManager.getHealth(playerId);
            double maxHealth = InventoryManager.getMaxHealth(playerId);
            int food = InventoryManager.getFood(playerId);
            float exhaustion = InventoryManager.getExhaustion(playerId);

            player.getInventory().setContents(inventory);
            player.getInventory().setArmorContents(armor);
            player.getInventory().setItemInOffHand(offHand);

            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            for (org.bukkit.potion.PotionEffect effect : InventoryManager.getPotionEffects(playerId)) {
                player.addPotionEffect(effect);
            }

            player.setHealth(health);
            player.setMaxHealth(maxHealth);
            player.setFoodLevel(food);
            player.setExhaustion(exhaustion);

            InventoryManager.removeInventory(playerId);
        }
    }

    private static World getOrCreateArenaWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.environment(World.Environment.NORMAL);
            world = creator.createWorld();
        }

        return world;
    }

    public record Duel(Player player1, Player player2, World arenaWorld, 
                       Location p1OriginalLocation, Location p2OriginalLocation,
                       GameMode p1OriginalGM, GameMode p2OriginalGM,
                       String mode, int rounds, int p1Wins, int p2Wins) {}

    public record DuelRequest(Player sender, Player target, String mode, int rounds) {}
}