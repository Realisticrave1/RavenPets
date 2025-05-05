package RavenMC.ravenPets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class RavenManager {
    private final RavenPets plugin;
    private final Map<UUID, PlayerRaven> playerRavens;
    private File dataFile;
    private FileConfiguration data;

    public RavenManager(RavenPets plugin) {
        this.plugin = plugin;
        this.playerRavens = new HashMap<>();

        // Setup data file
        setupDataFile();

        // Load all ravens from database/config
        loadAllRavens();
    }

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "ravendata.yml");

        // Create file if it doesn't exist
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdir();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create ravendata.yml file!");
                e.printStackTrace();
            }
        }

        // Load the data
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadAllRavens() {
        // Clear current data
        playerRavens.clear();

        // Get existing data from online players
        if (data.contains("players")) {
            for (String uuidString : data.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidString);

                    // Create new raven
                    PlayerRaven raven = new PlayerRaven(playerId);

                    // Load saved data
                    String path = "players." + uuidString + ".";

                    // Load name
                    if (data.contains(path + "name")) {
                        raven.setName(data.getString(path + "name"));
                    }

                    // Load level
                    if (data.contains(path + "level")) {
                        raven.setLevel(data.getInt(path + "level"));
                    }

                    // Load experience
                    if (data.contains(path + "experience")) {
                        raven.setExperience(data.getInt(path + "experience"));
                    }

                    // Load element type - This is the critical part
                    if (data.contains(path + "element")) {
                        try {
                            String elementName = data.getString(path + "element");
                            RavenElementType element = RavenElementType.valueOf(elementName);
                            raven.setElementType(element);
                        } catch (IllegalArgumentException e) {
                            // If the element type is invalid, use a random one
                            plugin.getLogger().warning("Invalid element type for player " + playerId + ". Using random element.");
                        }
                    }

                    // Load custom appearance flags
                    if (data.contains(path + "customColors")) {
                        raven.setCustomColors(data.getBoolean(path + "customColors"));
                    }

                    if (data.contains(path + "customParticles")) {
                        raven.setCustomParticles(data.getBoolean(path + "customParticles"));
                    }

                    // Add to map
                    playerRavens.put(playerId, raven);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in ravendata.yml: " + uuidString);
                }
            }
        }

        plugin.getLogger().info("Loaded " + playerRavens.size() + " ravens from data file.");
    }

    public void saveAllRavens() {
        // Clear existing data
        data.set("players", null);

        // Save all ravens
        for (Map.Entry<UUID, PlayerRaven> entry : playerRavens.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerRaven raven = entry.getValue();

            String path = "players." + playerId.toString() + ".";

            // Save basic data
            data.set(path + "name", raven.getName());
            data.set(path + "level", raven.getLevel());
            data.set(path + "experience", raven.getExperience());
            data.set(path + "element", raven.getElementType().name());
            data.set(path + "customColors", raven.hasCustomColors());
            data.set(path + "customParticles", raven.hasCustomParticles());
        }

        // Save to file
        try {
            data.save(dataFile);
            plugin.getLogger().info("Saved " + playerRavens.size() + " ravens to data file.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ravendata.yml file!");
            e.printStackTrace();
        }
    }

    public PlayerRaven getRaven(Player player) {
        return getRaven(player.getUniqueId());
    }

    public PlayerRaven getRaven(UUID playerId) {
        // Check if player already has a raven
        if (playerRavens.containsKey(playerId)) {
            return playerRavens.get(playerId);
        }

        // Create new raven if not
        PlayerRaven raven = new PlayerRaven(playerId);
        playerRavens.put(playerId, raven);

        // Save immediately to ensure element type persistence
        saveRaven(playerId, raven);

        return raven;
    }

    private void saveRaven(UUID playerId, PlayerRaven raven) {
        String path = "players." + playerId.toString() + ".";

        // Save basic data
        data.set(path + "name", raven.getName());
        data.set(path + "level", raven.getLevel());
        data.set(path + "experience", raven.getExperience());
        data.set(path + "element", raven.getElementType().name());
        data.set(path + "customColors", raven.hasCustomColors());
        data.set(path + "customParticles", raven.hasCustomParticles());

        // Save to file
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ravendata.yml file!");
            e.printStackTrace();
        }
    }

    public void spawnRaven(Player player) {
        PlayerRaven raven = getRaven(player);
        raven.spawn(player.getLocation());

        // Notify nearby players (for multiplayer effect)
        Collection<Player> nearbyPlayers = player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(player.getLocation()) < 30)
                .filter(p -> p != player)
                .collect(Collectors.toList());

        for (Player nearbyPlayer : nearbyPlayers) {
            nearbyPlayer.sendMessage("§5§l* §d" + player.getName() + "'s raven has appeared");
        }
    }

    public void despawnRaven(Player player) {
        PlayerRaven raven = getRaven(player);
        raven.despawn();
    }

    public boolean hasRaven(Player player) {
        return playerRavens.containsKey(player.getUniqueId()) &&
                getRaven(player).isSpawned();
    }

    /**
     * Resets a player's raven to default values
     * @param player The player whose raven should be reset
     */
    public void resetRaven(Player player) {
        UUID playerId = player.getUniqueId();

        // First despawn if active
        if (hasRaven(player)) {
            despawnRaven(player);
        }

        // Remove existing raven
        playerRavens.remove(playerId);

        // Create a new default raven
        PlayerRaven newRaven = new PlayerRaven(playerId);
        playerRavens.put(playerId, newRaven);

        // Save the new raven
        saveRaven(playerId, newRaven);
    }

    /**
     * Sets a specific element type for a player's raven
     * @param player The player
     * @param elementType The element type to set
     */
    public void setRavenElement(Player player, RavenElementType elementType) {
        PlayerRaven raven = getRaven(player);
        raven.setElementType(elementType);

        // Save the change
        saveRaven(player.getUniqueId(), raven);
    }

    /**
     * Auto-save data every 5 minutes
     */
    public void startAutoSave() {
        int saveInterval = plugin.getConfig().getInt("auto-save-interval", 6000); // Default 5 minutes

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                this::saveAllRavens,
                saveInterval,
                saveInterval
        );

        plugin.getLogger().info("Raven data auto-save enabled (every " + (saveInterval/20/60) + " minutes)");
    }

    /**
     * Sets experience for a player's raven
     * This will trigger level-ups as needed
     * @param player The player
     * @param experience The experience points to set
     */
    public void setExperience(Player player, int experience) {
        PlayerRaven raven = getRaven(player);

        // Reset experience to 0
        raven.setExperience(0);

        // Add the new amount
        raven.addExperience(experience);

        // Save the change
        saveRaven(player.getUniqueId(), raven);
    }

    /**
     * Despawns all active ravens on the server
     * @return The number of ravens despawned
     */
    public int despawnAllRavens() {
        int count = 0;

        for (PlayerRaven raven : playerRavens.values()) {
            if (raven.isSpawned()) {
                raven.despawn();
                count++;
            }
        }

        return count;
    }

    /**
     * Gets the total number of ravens
     * @return The total count of ravens (active and inactive)
     */
    public int getTotalRavenCount() {
        return playerRavens.size();
    }

    /**
     * Gets the count of active (spawned) ravens
     * @return The number of active ravens
     */
    public int getActiveRavenCount() {
        int count = 0;

        for (PlayerRaven raven : playerRavens.values()) {
            if (raven.isSpawned()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Gets stats about raven element types
     * @return A map of element types to their counts
     */
    public Map<RavenElementType, Integer> getElementStats() {
        Map<RavenElementType, Integer> stats = new HashMap<>();

        // Initialize with zeros
        for (RavenElementType type : RavenElementType.values()) {
            stats.put(type, 0);
        }

        // Count each element
        for (PlayerRaven raven : playerRavens.values()) {
            RavenElementType type = raven.getElementType();
            stats.put(type, stats.get(type) + 1);
        }

        return stats;
    }

    /**
     * Gets stats about raven tiers
     * @return A map of tier types to their counts
     */
    public Map<RavenTier, Integer> getTierStats() {
        Map<RavenTier, Integer> stats = new HashMap<>();

        // Initialize with zeros
        for (RavenTier tier : RavenTier.values()) {
            stats.put(tier, 0);
        }

        // Count each tier
        for (PlayerRaven raven : playerRavens.values()) {
            RavenTier tier = raven.getTier();
            stats.put(tier, stats.get(tier) + 1);
        }

        return stats;
    }
}