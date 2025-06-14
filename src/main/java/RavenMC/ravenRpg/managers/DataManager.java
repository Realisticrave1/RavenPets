package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private RavenRpg plugin;
    private Map<UUID, PlayerData> playerDataMap;
    private File dataFolder;

    public DataManager(RavenRpg plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData getPlayerData(Player player) {
        if (player == null) {
            plugin.getLogger().warning("Attempted to get player data for null player");
            return null;
        }
        return getPlayerData(player.getUniqueId(), player.getName());
    }

    public PlayerData getPlayerData(UUID uuid, String name) {
        if (uuid == null) {
            plugin.getLogger().warning("Attempted to get player data for null UUID");
            return null;
        }

        try {
            PlayerData data = playerDataMap.get(uuid);
            if (data == null) {
                data = loadPlayerData(uuid, name != null ? name : "Unknown");
                if (data != null) {
                    playerDataMap.put(uuid, data);
                }
            }
            return data;
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting player data for " + (name != null ? name : uuid.toString()) + ": " + e.getMessage());
            e.printStackTrace();

            // Return a new PlayerData as fallback
            PlayerData fallbackData = new PlayerData(uuid, name != null ? name : "Unknown");
            playerDataMap.put(uuid, fallbackData);
            return fallbackData;
        }
    }

    private PlayerData loadPlayerData(UUID uuid, String name) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerData(uuid, name);
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            PlayerData data = new PlayerData(uuid, name);

            // Load basic data with null safety
            data.setPlayerName(config.getString("playerName", name != null ? name : "Unknown"));
            data.setCurrentGuild(config.getString("currentGuild"));

            // Load race and bloodline with error handling
            if (config.contains("selectedRace")) {
                try {
                    String raceName = config.getString("selectedRace");
                    if (raceName != null) {
                        Race race = Race.valueOf(raceName);
                        data.setSelectedRace(race);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid race for player " + name + ": " + config.getString("selectedRace"));
                }
            }

            if (config.contains("selectedBloodline")) {
                try {
                    String bloodlineName = config.getString("selectedBloodline");
                    if (bloodlineName != null) {
                        Bloodline bloodline = Bloodline.valueOf(bloodlineName);
                        data.setSelectedBloodline(bloodline);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid bloodline for player " + name + ": " + config.getString("selectedBloodline"));
                }
            }

            // Load stats with defaults
            if (config.contains("stats")) {
                try {
                    for (String stat : config.getConfigurationSection("stats").getKeys(false)) {
                        int value = config.getInt("stats." + stat, 10);
                        data.setStat(stat, Math.max(0, value)); // Ensure non-negative
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading stats for player " + name + ": " + e.getMessage());
                }
            }

            // Load skills with defaults
            if (config.contains("skills")) {
                try {
                    for (String skill : config.getConfigurationSection("skills").getKeys(false)) {
                        int level = config.getInt("skills." + skill, 1);
                        data.setSkill(skill, Math.max(1, level)); // Ensure at least level 1
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading skills for player " + name + ": " + e.getMessage());
                }
            }

            // Load owned shops
            if (config.contains("ownedShops")) {
                try {
                    data.getOwnedShops().addAll(config.getStringList("ownedShops"));
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading owned shops for player " + name + ": " + e.getMessage());
                }
            }

            // Load custom data
            if (config.contains("customData")) {
                try {
                    for (String key : config.getConfigurationSection("customData").getKeys(false)) {
                        Object value = config.get("customData." + key);
                        if (value != null) {
                            data.getCustomData().put(key, value);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading custom data for player " + name + ": " + e.getMessage());
                }
            }

            // Load join date
            long joinDate = config.getLong("joinDate", System.currentTimeMillis());
            data.getCustomData().put("joinDate", joinDate);

            return data;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player data for " + name + ": " + e.getMessage());
            e.printStackTrace();
            return new PlayerData(uuid, name);
        }
    }

    public void savePlayerData(PlayerData data) {
        if (data == null) {
            plugin.getLogger().warning("Attempted to save null player data");
            return;
        }

        try {
            File file = new File(dataFolder, data.getPlayerUUID().toString() + ".yml");
            FileConfiguration config = new YamlConfiguration();

            config.set("playerName", data.getPlayerName());
            config.set("currentGuild", data.getCurrentGuild());
            config.set("joinDate", data.getJoinDate());

            // Save race and bloodline
            if (data.getSelectedRace() != null) {
                config.set("selectedRace", data.getSelectedRace().name());
            }
            if (data.getSelectedBloodline() != null) {
                config.set("selectedBloodline", data.getSelectedBloodline().name());
            }

            // Save stats
            for (Map.Entry<String, Integer> entry : data.getStats().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    config.set("stats." + entry.getKey(), entry.getValue());
                }
            }

            // Save skills
            for (Map.Entry<String, Integer> entry : data.getSkills().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    config.set("skills." + entry.getKey(), entry.getValue());
                }
            }

            // Save owned shops
            config.set("ownedShops", data.getOwnedShops());

            // Save custom data
            for (Map.Entry<String, Object> entry : data.getCustomData().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    config.set("customData." + entry.getKey(), entry.getValue());
                }
            }

            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + data.getPlayerName() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            plugin.getLogger().severe("Unexpected error saving player data for " + data.getPlayerName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAllData() {
        try {
            int saved = 0;
            for (PlayerData data : playerDataMap.values()) {
                if (data != null) {
                    savePlayerData(data);
                    saved++;
                }
            }
            plugin.getLogger().info("Saved data for " + saved + " players");
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving all player data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removePlayerData(UUID uuid) {
        if (uuid != null) {
            playerDataMap.remove(uuid);
        }
    }

    public void clearCache() {
        playerDataMap.clear();
    }

    public int getCachedPlayerCount() {
        return playerDataMap.size();
    }
}