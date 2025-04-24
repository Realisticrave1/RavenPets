package RavenMC.ravenPets.manager;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import RavenMC.ravenPets.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RavenManager {

    private final RavenPets plugin;
    private final Map<UUID, Raven> playerRavens; // Player UUID -> Raven
    private final Map<UUID, UUID> entityToOwner; // Entity UUID -> Player UUID

    public RavenManager(RavenPets plugin) {
        this.plugin = plugin;
        this.playerRavens = new ConcurrentHashMap<>();
        this.entityToOwner = new ConcurrentHashMap<>();

        // Start the raven behavior task
        startRavenBehaviorTask();

        // Load all online players' ravens
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadRaven(player.getUniqueId());
        }
    }

    public void loadRaven(UUID playerId) {
        // Check if already loaded
        if (playerRavens.containsKey(playerId)) {
            return;
        }

        // Load from database or create new
        Raven raven = plugin.getDatabaseManager().loadRaven(playerId);
        if (raven == null) {
            raven = new Raven(playerId);
            plugin.getDatabaseManager().saveRaven(raven);
        }

        playerRavens.put(playerId, raven);

        // If entity exists, add to entity map
        if (raven.getEntityId() != null) {
            entityToOwner.put(raven.getEntityId(), playerId);
        }
    }

    public void unloadRaven(UUID playerId) {
        Raven raven = playerRavens.get(playerId);
        if (raven != null) {
            // Save to database
            plugin.getDatabaseManager().saveRaven(raven);

            // Despawn
            raven.despawn();

            // Remove from maps
            if (raven.getEntityId() != null) {
                entityToOwner.remove(raven.getEntityId());
            }
            playerRavens.remove(playerId);
        }
    }

    public void spawnRaven(Player player) {
        UUID playerId = player.getUniqueId();
        Location loc = player.getLocation().add(0, 1, 0);
        Parrot parrot = (Parrot) player.getWorld().spawnEntity(loc, EntityType.PARROT);
        parrot.setVariant(Parrot.Variant.BLUE); // Closest to purple
        parrot.setCustomName(ChatColor.LIGHT_PURPLE + "Raven");
        parrot.setCustomNameVisible(true);
        parrot.setOwner(player);
        player.sendMessage(ChatColor.GREEN + "Raven spawned!");
        parrot.setTamed(true);
        parrot.setOwner(player);

        // Load raven if not loaded
        if (!playerRavens.containsKey(playerId)) {
            loadRaven(playerId);
        }

        Raven raven = playerRavens.get(playerId);
        if (raven != null) {
            raven.spawn(player);

            // Update entity mapping
            if (raven.getEntityId() != null) {
                entityToOwner.put(raven.getEntityId(), playerId);
            }

            MessageUtil.sendMessage(player, "Your " + raven.getTier().getName() + " has been summoned!");
        }
    }

    public void despawnRaven(Player player) {
        UUID playerId = player.getUniqueId();
        Raven raven = playerRavens.get(playerId);

        if (raven != null && raven.isActive()) {
            // Update entity mapping
            if (raven.getEntityId() != null) {
                entityToOwner.remove(raven.getEntityId());
            }

            raven.despawn();
            MessageUtil.sendMessage(player, "Your raven has been dismissed.");
        }
    }

    public Raven getRavenByPlayer(UUID playerId) {
        return playerRavens.get(playerId);
    }

    public Raven getRavenByEntity(UUID entityId) {
        UUID ownerId = entityToOwner.get(entityId);
        if (ownerId != null) {
            return playerRavens.get(ownerId);
        }
        return null;
    }

    public void saveAllRavens() {
        for (Raven raven : playerRavens.values()) {
            plugin.getDatabaseManager().saveRaven(raven);
        }
    }

    private void startRavenBehaviorTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Process all active ravens
            for (Raven raven : playerRavens.values()) {
                if (raven.isActive()) {
                    // Get owner
                    Player owner = Bukkit.getPlayer(raven.getOwnerId());
                    if (owner == null || !owner.isOnline()) {
                        raven.despawn();
                        continue;
                    }

                    // Check if entity is still valid
                    Entity entity = raven.getEntity();
                    if (entity == null || entity.isDead()) {
                        // Respawn
                        raven.spawn(owner);
                        continue;
                    }

                    // Check distance to owner
                    if (entity.getLocation().distance(owner.getLocation()) > 20) {
                        raven.teleportToOwner(owner);
                    }

                    // Execute raven abilities
                    executeRavenAbilities(raven, owner);
                }
            }
        }, 20L, 20L); // Run every second
    }

    private void executeRavenAbilities(Raven raven, Player owner) {
        // This would contain the logic for all the various raven abilities
        // For example:

        // Item retrieval ability
        if (raven.hasAbility("item_retrieval")) {
            int radius = raven.getDetectionRadius();

            // This would get nearby items and pick them up for the player
            owner.getNearbyEntities(radius, radius, radius).stream()
                    .filter(e -> e.getType().name().contains("ITEM"))
                    .forEach(item -> {
                        // Logic to pick up item
                    });
        }

        // Enemy detection
        if (raven.hasAbility("enemy_detection")) {
            int radius = raven.getDetectionRadius();

            // Check for nearby hostile mobs
            owner.getNearbyEntities(radius, radius, radius).stream()
                    .filter(e -> isHostile(e))
                    .forEach(mob -> {
                        // Alert owner about nearby hostiles
                    });
        }

        // Resource highlighting
        if (raven.hasAbility("resource_highlighting")) {
            // Logic to highlight nearby ores to the player
        }

        // Combat assistance
        if (raven.hasAbility("combat_assistance")) {
            // Logic to boost player damage or apply effects during combat
        }

        // Auto-repair
        if (raven.hasAbility("auto_repair")) {
            // Logic to repair damaged items in player inventory
        }

        // Resource auto-collection
        if (raven.hasAbility("resource_auto_collection")) {
            // Logic to automatically collect nearby resources
        }
    }

    private boolean isHostile(Entity entity) {
        String type = entity.getType().name();
        return type.contains("ZOMBIE") || type.contains("SKELETON") ||
                type.contains("CREEPER") || type.contains("SPIDER") ||
                type.contains("ENDERMAN") || type.contains("WITCH") ||
                type.contains("SLIME") || type.contains("PHANTOM");
    }
}