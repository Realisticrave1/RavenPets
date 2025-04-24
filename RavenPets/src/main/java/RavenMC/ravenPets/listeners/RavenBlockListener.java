package RavenMC.ravenPets.listeners;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for handling raven block interactions
 */
public class RavenBlockListener implements Listener {

    private final RavenPets plugin;

    public RavenBlockListener(RavenPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        plugin.getLogger().info("Block placed by " + player.getName() + ": " + item.getType());
        plugin.getLogger().info("Is raven block: " + plugin.getRavenBlockManager().isRavenBlock(item));

        // Check if the player is placing a raven block
        if (plugin.getRavenBlockManager().isRavenBlock(item)) {
            // Cancel the block placement
            event.setCancelled(true);

            // Get the tier of the raven block
            int tier = plugin.getRavenBlockManager().getRavenBlockTier(item);

            // Check if the player has permission for this tier
            if (!hasPermissionForTier(player, tier)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this tier of raven!");
                return;
            }

            // Get the player's current raven
            Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());

            // If the player already has a raven
            if (raven != null) {
                // Check if the player is trying to use a higher tier than they have
                if (tier > getRavenTierLevel(raven.getTier())) {
                    upgradeRaven(player, raven, tier);
                } else {
                    // Just spawn the existing raven
                    if (!raven.isActive()) {
                        plugin.getRavenManager().spawnRaven(player);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your " + raven.getTier().getName() + " Raven has been summoned!");
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your raven is already active!");
                    }
                }
            } else {
                // Create a new raven for the player at the specified tier
                createNewRaven(player, tier);
            }

            // Consume one item from the player's hand
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }

    /**
     * Creates a new raven for a player
     *
     * @param player The player to create a raven for
     * @param tier The tier of the raven
     */
    private void createNewRaven(Player player, int tier) {
        // This would create a new raven of the specified tier
        // In a real implementation, this would involve creating a Raven object
        // with the appropriate tier and abilities

        // For now, just simulate it
        player.sendMessage(ChatColor.GREEN + "You've obtained a new " + getTierName(tier) + " Raven companion!");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your raven has been summoned!");

        // Play effects
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
    }

    /**
     * Upgrades a player's raven to a higher tier
     *
     * @param player The player
     * @param raven The player's current raven
     * @param newTier The new tier to upgrade to
     */
    private void upgradeRaven(Player player, Raven raven, int newTier) {
        // This would upgrade the player's raven to a higher tier
        // In a real implementation, this would update the Raven object's tier
        // and unlock new abilities

        // For now, just simulate it
        player.sendMessage(ChatColor.GREEN + "Your raven has been upgraded to " + getTierName(newTier) + " tier!");

        // Play effects
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }

    /**
     * Checks if a player has permission to use a certain tier of raven
     *
     * @param player The player to check
     * @param tier The tier to check permission for
     * @return Whether the player has permission
     */
    private boolean hasPermissionForTier(Player player, int tier) {
        return player.hasPermission("ravenpets.tier." + tier) || player.hasPermission("ravenpets.admin");
    }

    /**
     * Gets the numeric level of a raven tier
     *
     * @param tier The raven tier
     * @return The tier level (1-5)
     */
    private int getRavenTierLevel(RavenTier tier) {
        switch (tier) {
            case NOVICE: return 1;
            case ADEPT: return 2;
            case EXPERT: return 3;
            case MASTER: return 4;
            case LEGENDARY: return 5;
            default: return 0;
        }
    }

    /**
     * Gets the name of a raven tier
     *
     * @param tier The tier level (1-5)
     * @return The tier name
     */
    private String getTierName(int tier) {
        switch (tier) {
            case 1: return "Novice";
            case 2: return "Adept";
            case 3: return "Expert";
            case 4: return "Master";
            case 5: return "Legendary";
            default: return "Unknown";
        }
    }
}