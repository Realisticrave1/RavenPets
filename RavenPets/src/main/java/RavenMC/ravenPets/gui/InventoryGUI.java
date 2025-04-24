package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI for managing a raven's inventory
 */
public class InventoryGUI extends GUI {

    private static final Map<UUID, Inventory> ravenInventories = new HashMap<>();
    private static final NamespacedKey SLOT_KEY = new NamespacedKey("ravenpets", "slot_index");

    public InventoryGUI(RavenPets plugin) {
        super(plugin, "&5&lRaven Inventory", 54);
    }

    @Override
    public void update(Player player) {
        // Clear the inventory
        inventory.clear();

        // Create border
        createBorder(Material.BLACK_STAINED_GLASS_PANE);

        Raven raven = getRaven(player);
        if (raven == null) {
            // Player doesn't have a raven
            inventory.setItem(22, createItem(
                    Material.BARRIER,
                    "&c&lNo Raven Found",
                    "&7Complete the welcome tutorial",
                    "&7to claim your Raven Egg."
            ));

            // Add back button
            inventory.setItem(49, createItem(
                    Material.ARROW,
                    "&c&l← Back",
                    "&7Return to the main menu"
            ));

            return;
        }

        // Get or create the raven's internal inventory
        Inventory ravenInventory = getRavenInventory(raven);

        // Add title
        inventory.setItem(4, createItem(
                Material.CHEST,
                "&d&lRaven Inventory",
                "&7Storage slots: &d" + raven.getInventorySlots()
        ));

        // Add slots based on raven's inventory size
        int slots = raven.getInventorySlots();
        int startRow = 2;
        int endRow = 4;

        for (int row = startRow; row <= endRow; row++) {
            for (int col = 1; col <= 7; col++) {
                int index = (row - startRow) * 7 + (col - 1);
                int slot = row * 9 + col;

                if (index < slots) {
                    // This is an available slot

                    // Check if there's an item in this slot in the raven's inventory
                    ItemStack content = ravenInventory.getItem(index);
                    if (content != null) {
                        // There's an item - display it
                        inventory.setItem(slot, tagSlotItem(content, index));
                    } else {
                        // Empty slot - show empty slot marker
                        inventory.setItem(slot, createSlotMarker(index));
                    }
                } else {
                    // This slot is not available
                    inventory.setItem(slot, createItem(
                            Material.RED_STAINED_GLASS_PANE,
                            "&c&lLocked Slot",
                            "&7Upgrade your raven to",
                            "&7unlock more inventory slots"
                    ));
                }
            }
        }

        // Add navigation buttons
        inventory.setItem(49, createItem(
                Material.ARROW,
                "&c&l← Back",
                "&7Return to the main menu"
        ));
    }

    @Override
    public boolean handleClick(Player player, int slot) {
        // Handle back button
        if (slot == 49) {
            plugin.getGUIManager().openMainGUI(player);
            return true;
        }

        Raven raven = getRaven(player);
        if (raven == null) {
            return false;
        }

        // Check if the click is in a valid inventory slot
        int row = slot / 9;
        int col = slot % 9;

        if (row >= 2 && row <= 4 && col >= 1 && col <= 7) {
            int startRow = 2;
            int index = (row - startRow) * 7 + (col - 1);

            // Check if this slot is within the raven's inventory size
            if (index < raven.getInventorySlots()) {
                handleInventoryClick(player, raven, slot, index);
                return true;
            }
        }

        return false;
    }

    /**
     * Handles a click on an inventory slot
     *
     * @param player The player who clicked
     * @param raven The player's raven
     * @param guiSlot The slot in the GUI that was clicked
     * @param inventoryIndex The index in the raven's inventory
     */
    private void handleInventoryClick(Player player, Raven raven, int guiSlot, int inventoryIndex) {
        Inventory ravenInventory = getRavenInventory(raven);
        ItemStack cursorItem = player.getItemOnCursor();
        ItemStack slotItem = inventory.getItem(guiSlot);

        // If the cursor is empty and the slot has an item, take the item
        if (cursorItem.getType() == Material.AIR && slotItem != null && !isSlotMarker(slotItem)) {
            // Get the stored inventory index from the item
            ItemMeta meta = slotItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            int storedIndex = container.getOrDefault(SLOT_KEY, PersistentDataType.INTEGER, -1);

            if (storedIndex >= 0) {
                // Remove from raven inventory
                ravenInventory.setItem(storedIndex, null);

                // Give to player
                player.setItemOnCursor(removeSlotTag(slotItem.clone()));

                // Update the GUI
                update(player);
            }
        }
        // If the cursor has an item and the slot is empty or has a slot marker, place the item
        else if (cursorItem.getType() != Material.AIR && (slotItem == null || isSlotMarker(slotItem))) {
            // Add to raven inventory
            ravenInventory.setItem(inventoryIndex, cursorItem.clone());

            // Clear cursor
            player.setItemOnCursor(null);

            // Update the GUI
            update(player);
        }
        // If both cursor and slot have items, swap them
        else if (cursorItem.getType() != Material.AIR && slotItem != null && !isSlotMarker(slotItem)) {
            // Get the stored inventory index from the item
            ItemMeta meta = slotItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            int storedIndex = container.getOrDefault(SLOT_KEY, PersistentDataType.INTEGER, -1);

            if (storedIndex >= 0) {
                // Remove from raven inventory
                ravenInventory.setItem(storedIndex, cursorItem.clone());

                // Give to player
                player.setItemOnCursor(removeSlotTag(slotItem.clone()));

                // Update the GUI
                update(player);
            }
        }
    }

    /**
     * Gets or creates a raven's inventory
     *
     * @param raven The raven to get the inventory for
     * @return The raven's inventory
     */
    private Inventory getRavenInventory(Raven raven) {
        UUID ownerId = raven.getOwnerId();

        if (!ravenInventories.containsKey(ownerId)) {
            // Create a new inventory for this raven
            int size = Math.max(9, (raven.getInventorySlots() / 9 + 1) * 9);
            Inventory newInventory = Bukkit.createInventory(null, size, "Raven Storage");
            ravenInventories.put(ownerId, newInventory);
        }

        return ravenInventories.get(ownerId);
    }

    /**
     * Creates an empty slot marker item
     *
     * @param index The slot index
     * @return The slot marker item
     */
    private ItemStack createSlotMarker(int index) {
        ItemStack marker = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = marker.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Empty Slot");

        // Store the slot index in the item
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(SLOT_KEY, PersistentDataType.INTEGER, index);

        marker.setItemMeta(meta);
        return marker;
    }

    /**
     * Tags an item with its slot index
     *
     * @param item The item to tag
     * @param index The slot index
     * @return The tagged item
     */
    private ItemStack tagSlotItem(ItemStack item, int index) {
        ItemStack tagged = item.clone();
        ItemMeta meta = tagged.getItemMeta();

        if (meta != null) {
            // Store the slot index in the item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(SLOT_KEY, PersistentDataType.INTEGER, index);

            tagged.setItemMeta(meta);
        }

        return tagged;
    }

    /**
     * Removes the slot tag from an item
     *
     * @param item The item to remove the tag from
     * @return The untagged item
     */
    private ItemStack removeSlotTag(ItemStack item) {
        ItemStack untagged = item.clone();
        ItemMeta meta = untagged.getItemMeta();

        if (meta != null) {
            // Remove the slot index from the item
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.remove(SLOT_KEY);

            untagged.setItemMeta(meta);
        }

        return untagged;
    }

    /**
     * Checks if an item is a slot marker
     *
     * @param item The item to check
     * @return Whether the item is a slot marker
     */
    private boolean isSlotMarker(ItemStack item) {
        return item != null && item.getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Saves all raven inventories to persistent storage
     * This would be called when the plugin is disabled
     */
    public static void saveAllInventories() {
        // In a real implementation, this would save all inventories to a database or file
        // For now, it just prints a message
        System.out.println("Saving " + ravenInventories.size() + " raven inventories...");
    }
}