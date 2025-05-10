package RavenMC.ravenPets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simpler, more direct cooldown management system
 */
public class DirectCooldownManager {
    private final RavenPets plugin;

    // Structure: playerUUID -> elementType -> [primaryCooldownEnd, secondaryCooldownEnd]
    private final Map<UUID, Map<String, long[]>> cooldowns = new HashMap<>();

    public DirectCooldownManager(RavenPets plugin) {
        this.plugin = plugin;

        // Start periodic task to update cooldown displays
        new BukkitRunnable() {
            @Override
            public void run() {
                updateCooldownDisplays();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    /**
     * Get the cooldown time for a specific ability
     * @param elementType The element type
     * @param isSecondary True for secondary ability, false for primary
     * @return The cooldown time in seconds
     */
    public int getCooldownTime(String elementType, boolean isSecondary) {
        String configPath = "abilities." + elementType.toLowerCase() +
                (isSecondary ? ".secondary-cooldown" : ".cooldown");

        int defaultCooldown = isSecondary ? 120 : 60; // Default cooldowns in seconds
        return plugin.getConfig().getInt(configPath, defaultCooldown);
    }

    /**
     * Apply a tier-based reduction to cooldown time
     * @param player The player
     * @param cooldownSeconds The base cooldown time in seconds
     * @return The reduced cooldown time
     */
    public int getReducedCooldown(Player player, int cooldownSeconds) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        RavenTier tier = raven.getTier();

        // 10% reduction per tier level
        double tierMultiplier = 1.0 - (tier.ordinal() * 0.1);
        return Math.max(5, (int)(cooldownSeconds * tierMultiplier)); // Minimum 5 second cooldown
    }

    /**
     * Set a cooldown for a player
     * @param player The player
     * @param elementType The element type
     * @param isSecondary True for secondary ability, false for primary
     * @return True if the cooldown was set, false if the player is on cooldown
     */
    public boolean useCooldown(Player player, String elementType, boolean isSecondary) {
        if (player.hasPermission("ravenpets.bypasscooldown")) {
            // Log bypass if debug mode is on
            if (plugin.getConfig().getBoolean("debug-mode", false)) {
                plugin.getLogger().info(player.getName() + " bypassed cooldown for " +
                        elementType + (isSecondary ? " secondary" : " primary") + " ability");
            }
            return true; // Allow usage with bypass permission
        }

        UUID playerId = player.getUniqueId();

        // Get or create player's cooldowns
        Map<String, long[]> playerCooldowns = cooldowns.computeIfAbsent(playerId, k -> new HashMap<>());

        // Get or create element cooldowns
        long[] abilityCooldowns = playerCooldowns.computeIfAbsent(elementType, k -> new long[2]);

        int cooldownIndex = isSecondary ? 1 : 0;
        long now = System.currentTimeMillis();

        // Check if on cooldown
        if (abilityCooldowns[cooldownIndex] > now) {
            // Calculate remaining time
            int remainingSeconds = (int)((abilityCooldowns[cooldownIndex] - now) / 1000);

            // Notify player
            player.sendMessage("§c§l" + (isSecondary ? "Secondary ability" : "Ability") +
                    " on cooldown! §7(§f" + remainingSeconds + "s §7remaining)");

            // Play sound
            player.playSound(player.getLocation(), "block.note_block.bass", 1.0f, 0.5f);

            return false; // Still on cooldown
        }

        // Set new cooldown
        int cooldownSeconds = getCooldownTime(elementType, isSecondary);
        cooldownSeconds = getReducedCooldown(player, cooldownSeconds);

        // Set cooldown end time
        abilityCooldowns[cooldownIndex] = now + (cooldownSeconds * 1000L);

        // Update player's cooldowns in the map
        playerCooldowns.put(elementType, abilityCooldowns);
        cooldowns.put(playerId, playerCooldowns);

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Set " + cooldownSeconds + "s cooldown for " + player.getName() +
                    "'s " + elementType + (isSecondary ? " secondary" : " primary") + " ability");
        }

        return true; // Cooldown set, ability can be used
    }

    /**
     * Check if a player is on cooldown
     * @param player The player
     * @param elementType The element type
     * @param isSecondary True for secondary ability, false for primary
     * @return True if on cooldown, false if ready
     */
    public boolean isOnCooldown(Player player, String elementType, boolean isSecondary) {
        if (player.hasPermission("ravenpets.bypasscooldown")) {
            return false;
        }

        UUID playerId = player.getUniqueId();

        // Check if player has cooldowns
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }

        Map<String, long[]> playerCooldowns = cooldowns.get(playerId);

        // Check if element has cooldowns
        if (!playerCooldowns.containsKey(elementType)) {
            return false;
        }

        long[] abilityCooldowns = playerCooldowns.get(elementType);
        int cooldownIndex = isSecondary ? 1 : 0;

        // Check if still on cooldown
        return abilityCooldowns[cooldownIndex] > System.currentTimeMillis();
    }

    /**
     * Get the remaining cooldown time in seconds
     * @param player The player
     * @param elementType The element type
     * @param isSecondary True for secondary ability, false for primary
     * @return Remaining cooldown in seconds, 0 if not on cooldown
     */
    public int getRemainingCooldown(Player player, String elementType, boolean isSecondary) {
        UUID playerId = player.getUniqueId();

        // Check if player has cooldowns
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }

        Map<String, long[]> playerCooldowns = cooldowns.get(playerId);

        // Check if element has cooldowns
        if (!playerCooldowns.containsKey(elementType)) {
            return 0;
        }

        long[] abilityCooldowns = playerCooldowns.get(elementType);
        int cooldownIndex = isSecondary ? 1 : 0;

        long remainingMillis = abilityCooldowns[cooldownIndex] - System.currentTimeMillis();
        return remainingMillis > 0 ? (int)(remainingMillis / 1000) : 0;
    }

    /**
     * Clear all cooldowns for a player
     * @param playerId The player's UUID
     */
    public void clearCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }

    /**
     * Clear a specific cooldown for a player
     * @param player The player
     * @param elementType The element type
     * @param isSecondary True for secondary ability, false for primary
     */
    public void clearCooldown(Player player, String elementType, boolean isSecondary) {
        UUID playerId = player.getUniqueId();

        if (!cooldowns.containsKey(playerId)) {
            return;
        }

        Map<String, long[]> playerCooldowns = cooldowns.get(playerId);

        if (!playerCooldowns.containsKey(elementType)) {
            return;
        }

        long[] abilityCooldowns = playerCooldowns.get(elementType);
        int cooldownIndex = isSecondary ? 1 : 0;

        // Reset cooldown
        abilityCooldowns[cooldownIndex] = 0;
        playerCooldowns.put(elementType, abilityCooldowns);

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Cleared " + elementType + (isSecondary ? " secondary" : " primary") +
                    " ability cooldown for " + player.getName());
        }
    }

    /**
     * Update the cooldown displays for all online players
     */
    private void updateCooldownDisplays() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            // Skip if no cooldowns
            if (!cooldowns.containsKey(playerId)) {
                continue;
            }

            // Get player's raven
            PlayerRaven raven = plugin.getRavenManager().getRaven(player);
            String elementType = raven.getElementType().name();

            // Get cooldowns for this element
            Map<String, long[]> playerCooldowns = cooldowns.get(playerId);

            if (playerCooldowns.containsKey(elementType)) {
                long[] abilityCooldowns = playerCooldowns.get(elementType);

                // Clean up expired cooldowns
                long now = System.currentTimeMillis();
                if (abilityCooldowns[0] < now && abilityCooldowns[1] < now) {
                    playerCooldowns.remove(elementType);

                    // Remove player from map if they have no cooldowns
                    if (playerCooldowns.isEmpty()) {
                        cooldowns.remove(playerId);
                    }
                }
            }
        }
    }

    /**
     * Print all active cooldowns to the console (for debugging)
     */
    public void printActiveCooldowns() {
        if (!plugin.getConfig().getBoolean("debug-mode", false)) {
            return;
        }

        plugin.getLogger().info("=== ACTIVE COOLDOWNS ===");
        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Map<String, long[]>> entry : cooldowns.entrySet()) {
            UUID playerId = entry.getKey();
            Player player = Bukkit.getPlayer(playerId);
            String playerName = player != null ? player.getName() : playerId.toString();

            for (Map.Entry<String, long[]> elementEntry : entry.getValue().entrySet()) {
                String elementType = elementEntry.getKey();
                long[] cooldownTimes = elementEntry.getValue();

                // Primary cooldown
                if (cooldownTimes[0] > now) {
                    int remainingSeconds = (int)((cooldownTimes[0] - now) / 1000);
                    plugin.getLogger().info(playerName + "'s " + elementType + " primary: " +
                            remainingSeconds + "s remaining");
                }

                // Secondary cooldown
                if (cooldownTimes[1] > now) {
                    int remainingSeconds = (int)((cooldownTimes[1] - now) / 1000);
                    plugin.getLogger().info(playerName + "'s " + elementType + " secondary: " +
                            remainingSeconds + "s remaining");
                }
            }
        }

        plugin.getLogger().info("=======================");
    }
}