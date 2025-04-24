package RavenMC.ravenPets.listeners;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listener for GUI-related events
 */
public class GUIListener implements Listener {

    private final RavenPets plugin;

    public GUIListener(RavenPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();

        // Check if the player has an open GUI inventory
        if (GUI.hasOpenInventory(player.getUniqueId())) {
            // Cancel all shift-clicks by default for GUI safety
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }

            // Cancel clicks on player inventory while GUI is open (except for inventory GUI)
            if (event.getClickedInventory() != null &&
                    event.getClickedInventory().getType() == InventoryType.PLAYER &&
                    !inventoryTitle.equals(plugin.getGUIManager().getInventoryGUI().getTitle())) {
                event.setCancelled(true);
                return;
            }

            // Cancel all hotbar swap/number key actions
            if (event.getHotbarButton() != -1) {
                event.setCancelled(true);
                return;
            }

            // Handle Main GUI
            if (inventoryTitle.equals(plugin.getGUIManager().getMainGUI().getTitle())) {
                event.setCancelled(true);
                if (event.getClickedInventory() != null &&
                        event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    plugin.getGUIManager().getMainGUI().handleClick(player, event.getRawSlot());
                }
                return;
            }

            // Handle Abilities GUI
            if (inventoryTitle.equals(plugin.getGUIManager().getAbilitiesGUI().getTitle())) {
                event.setCancelled(true);
                if (event.getClickedInventory() != null &&
                        event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    plugin.getGUIManager().getAbilitiesGUI().handleClick(player, event.getRawSlot());
                }
                return;
            }

            // Handle Inventory GUI - Special case as we want some item movement
            if (inventoryTitle.equals(plugin.getGUIManager().getInventoryGUI().getTitle())) {
                // Only cancel if our handler returns true (meaning it handled the click)
                if (event.getClickedInventory() != null &&
                        event.getClickedInventory().getType() != InventoryType.PLAYER &&
                        plugin.getGUIManager().getInventoryGUI().handleClick(player, event.getRawSlot())) {
                    event.setCancelled(true);
                }
                return;
            }

            // Handle Home Locations GUI
            if (inventoryTitle.equals(plugin.getGUIManager().getHomeLocationsGUI().getTitle())) {
                event.setCancelled(true);
                if (event.getClickedInventory() != null &&
                        event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    plugin.getGUIManager().getHomeLocationsGUI().handleClick(player, event.getRawSlot());
                }
                return;
            }

            // Handle Upgrade GUI
            if (inventoryTitle.equals(plugin.getGUIManager().getUpgradeGUI().getTitle())) {
                event.setCancelled(true);
                if (event.getClickedInventory() != null &&
                        event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    plugin.getGUIManager().getUpgradeGUI().handleClick(player, event.getRawSlot());
                }
                return;
            }

            // Handle Settings GUI
            if (inventoryTitle.equals(plugin.getGUIManager().getSettingsGUI().getTitle())) {
                event.setCancelled(true);
                if (event.getClickedInventory() != null &&
                        event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    plugin.getGUIManager().getSettingsGUI().handleClick(player, event.getRawSlot());
                }
                return;
            }

            // For safety, cancel all interactions with unknown GUIs
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Check if the player has an open GUI
        if (GUI.hasOpenInventory(player.getUniqueId())) {
            // Cancel all drag events in any GUI
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Remove player from open inventories list
        GUI.removePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove player from open inventories list
        GUI.removePlayer(player.getUniqueId());
    }
}