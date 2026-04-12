package me.theadamnew.duelsx;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    private static final Map<UUID, SavedInventory> savedInventories = new HashMap<>();

    public static void saveInventory(UUID playerId, ItemStack[] inventory, ItemStack[] armor, 
                                    ItemStack offHand, double health, double maxHealth, int food, 
                                    float exhaustion, java.util.Collection<org.bukkit.potion.PotionEffect> effects,
                                    org.bukkit.Location lastLocation) {
        savedInventories.put(playerId, new SavedInventory(
            inventory.clone(),
            armor != null ? armor.clone() : new ItemStack[4],
            offHand,
            health,
            maxHealth,
            food,
            exhaustion,
            new java.util.ArrayList<>(effects),
            lastLocation.clone()
        ));
    }

    public static ItemStack[] getInventory(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.inventory() : null;
    }

    public static ItemStack[] getArmor(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.armor() : null;
    }

    public static ItemStack getOffHand(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.offHand() : null;
    }

    public static double getHealth(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.health() : 20.0;
    }

    public static double getMaxHealth(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.maxHealth() : 20.0;
    }

    public static int getFood(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.food() : 20;
    }

    public static float getExhaustion(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.exhaustion() : 0.0f;
    }

    public static java.util.List<org.bukkit.potion.PotionEffect> getPotionEffects(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.effects() : java.util.List.of();
    }

    public static org.bukkit.Location getLastLocation(UUID playerId) {
        SavedInventory saved = savedInventories.get(playerId);
        return saved != null ? saved.lastLocation() : null;
    }

    public static void removeInventory(UUID playerId) {
        savedInventories.remove(playerId);
    }

    public static boolean hasInventory(UUID playerId) {
        return savedInventories.containsKey(playerId);
    }

    public static void restoreAllInventories() {
        for (UUID playerId : savedInventories.keySet()) {
        }
    }

    public static Map<UUID, SavedInventory> getSavedInventories() {
        return savedInventories;
    }

    public record SavedInventory(
        ItemStack[] inventory,
        ItemStack[] armor,
        ItemStack offHand,
        double health,
        double maxHealth,
        int food,
        float exhaustion,
        java.util.List<org.bukkit.potion.PotionEffect> effects,
        org.bukkit.Location lastLocation
    ) {}
}