package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RavenChatListener implements Listener {
    private final RavenPets plugin;
    private final Map<UUID, String> playerPendingAction;

    public RavenChatListener(RavenPets plugin) {
        this.plugin = plugin;
        this.playerPendingAction = new HashMap<>();
    }

    /**
     * Register a player as waiting for chat input
     * @param player The player
     * @param action The action type
     */
    public void registerPendingInput(Player player, String action) {
        playerPendingAction.put(player.getUniqueId(), action);
    }

    /**
     * Unregister a player from waiting for chat input
     * @param player The player
     */
    public void unregisterPendingInput(Player player) {
        playerPendingAction.remove(player.getUniqueId());
    }

    /**
     * Check if a player is waiting for chat input
     * @param player The player
     * @return True if the player has a pending action
     */
    public boolean hasPendingInput(Player player) {
        return playerPendingAction.containsKey(player.getUniqueId());
    }

    /**
     * Get the pending action for a player
     * @param player The player
     * @return The action type or null if none
     */
    public String getPendingAction(Player player) {
        return playerPendingAction.get(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if player has a pending action
        if (playerPendingAction.containsKey(playerId)) {
            event.setCancelled(true);
            String action = playerPendingAction.get(playerId);
            String message = event.getMessage();

            // Run task synchronously since we'll be modifying game state
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    handleChatInput(player, action, message));

            // Remove the pending action
            playerPendingAction.remove(playerId);
        }

        // Check for admin actions
        if (plugin.getAdminCommandHandler() != null &&
                plugin.getAdminCommandHandler().hasAction(player)) {
            event.setCancelled(true);
            String message = event.getMessage();

            // Run task synchronously
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    plugin.getAdminCommandHandler().handleChatInput(player, message));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up when player leaves
        Player player = event.getPlayer();
        playerPendingAction.remove(player.getUniqueId());
    }

    private void handleChatInput(Player player, String action, String message) {
        switch (action) {
            case "rename_raven":
                handleRavenRename(player, message);
                break;
            // Add more cases as needed
        }
    }

    private void handleRavenRename(Player player, String newName) {
        // Check name length
        if (newName.length() > 16) {
            player.sendMessage("§c§lRaven name cannot be longer than 16 characters!");
            return;
        }

        // Check for color codes
        if (!player.hasPermission("ravenpets.colornames") && newName.contains("§")) {
            player.sendMessage("§c§lYou don't have permission to use color codes in your raven's name!");
            return;
        }

        // Apply the new name
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        raven.setName(newName);

        player.sendMessage("§d§lYour raven has been renamed to §r" + newName + "§d§l!");
    }
}