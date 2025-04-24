package RavenMC.ravenPets.listeners;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class RavenListener implements Listener {

    private final RavenPets plugin;

    public RavenListener(RavenPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        // Check if entity is a raven
        if (entity instanceof Parrot) {
            PersistentDataContainer container = entity.getPersistentDataContainer();

            if (container.has(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING)) {
                // Entity is a raven, make it invulnerable
                event.setCancelled(true);

                // If it's void damage, teleport raven to owner
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    String ownerIdStr = container.get(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING);
                    if (ownerIdStr != null) {
                        UUID ownerId = UUID.fromString(ownerIdStr);
                        Player owner = plugin.getServer().getPlayer(ownerId);

                        if (owner != null && owner.isOnline()) {
                            Raven raven = plugin.getRavenManager().getRavenByPlayer(ownerId);
                            if (raven != null) {
                                raven.teleportToOwner(owner);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Check if entity is a raven
        if (entity instanceof Parrot) {
            PersistentDataContainer container = entity.getPersistentDataContainer();

            if (container.has(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING)) {
                // Entity is a raven, cancel drops
                event.getDrops().clear();
                event.setDroppedExp(0);

                // Respawn raven later
                String ownerIdStr = container.get(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING);
                if (ownerIdStr != null) {
                    UUID ownerId = UUID.fromString(ownerIdStr);
                    Player owner = plugin.getServer().getPlayer(ownerId);

                    if (owner != null && owner.isOnline()) {
                        // Respawn after 5 seconds
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            Raven raven = plugin.getRavenManager().getRavenByPlayer(ownerId);
                            if (raven != null) {
                                raven.spawn(owner);
                                owner.sendMessage("§5Your raven has been respawned!");
                            }
                        }, 100L); // 5 seconds
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        // Check if entity is a raven
        if (entity instanceof Parrot) {
            PersistentDataContainer container = entity.getPersistentDataContainer();

            if (container.has(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING)) {
                // Entity is a raven
                String ownerIdStr = container.get(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING);
                if (ownerIdStr != null) {
                    UUID ownerId = UUID.fromString(ownerIdStr);

                    // Check if player is the owner
                    if (event.getPlayer().getUniqueId().equals(ownerId)) {
                        // Player is interacting with their own raven
                        event.setCancelled(true);

                        // Open raven interface
                        openRavenInterface(event.getPlayer());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        Entity entity = event.getEntity();

        // Check if entity is a parrot
        if (entity instanceof Parrot) {
            // Check if this parrot is already a raven
            PersistentDataContainer container = entity.getPersistentDataContainer();

            if (!container.has(plugin.getConfigManager().getRavenKey(), PersistentDataType.STRING)) {
                // Not a raven, check if player has one already
                Player player = (Player) event.getOwner();
                Raven existingRaven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());

                if (existingRaven == null || !existingRaven.isActive()) {
                    // Ask player if they want to convert this parrot to a raven
                    player.sendMessage("§5You've tamed a parrot! Would you like to convert it to your raven companion?");
                    player.sendMessage("§5Type §d/ravenpet convert §5to transform it.");

                    // Store the entity ID for conversion
                    // This would typically use a data structure to track conversion candidates
                }
            }
        }
    }

    private void openRavenInterface(Player player) {
        // This would open a GUI for the player to interact with their raven
        // For demonstration purposes, just send a message
        player.sendMessage("§5Raven Interface would open here.");
        player.sendMessage("§5- §dInventory");
        player.sendMessage("§5- §dAbilities");
        player.sendMessage("§5- §dUpgrades");
        player.sendMessage("§5- §dSettings");

        // In a real implementation, this would create an inventory GUI
    }
}