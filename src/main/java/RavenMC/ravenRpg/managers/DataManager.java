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
        return getPlayerData(player.getUniqueId(), player.getName());
    }

    public PlayerData getPlayerData(UUID uuid, String name) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) {
            data = loadPlayerData(uuid, name);
            playerDataMap.put(uuid, data);
        }
        return data;
    }

    private PlayerData loadPlayerData(UUID uuid, String name) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerData(uuid, name);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData(uuid, name);

        // Load basic data
        data.setPlayerName(config.getString("playerName", name));
        data.setCurrentGuild(config.getString("currentGuild"));

        // Load race and bloodline
        if (config.contains("selectedRace")) {
            try {
                Race race = Race.valueOf(config.getString("selectedRace"));
                data.setSelectedRace(race);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid race for player " + name + ": " + config.getString("selectedRace"));
            }
        }

        if (config.contains("selectedBloodline")) {
            try {
                Bloodline bloodline = Bloodline.valueOf(config.getString("selectedBloodline"));
                data.setSelectedBloodline(bloodline);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid bloodline for player " + name + ": " + config.getString("selectedBloodline"));
            }
        }

        // Load stats
        if (config.contains("stats")) {
            for (String stat : config.getConfigurationSection("stats").getKeys(false)) {
                data.setStat(stat, config.getInt("stats." + stat));
            }
        }

        // Load skills
        if (config.contains("skills")) {
            for (String skill : config.getConfigurationSection("skills").getKeys(false)) {
                data.setSkill(skill, config.getInt("skills." + skill));
            }
        }

        // Load owned shops
        if (config.contains("ownedShops")) {
            data.getOwnedShops().addAll(config.getStringList("ownedShops"));
        }

        // Load custom data
        if (config.contains("customData")) {
            for (String key : config.getConfigurationSection("customData").getKeys(false)) {
                data.getCustomData().put(key, config.get("customData." + key));
            }
        }

        return data;
    }

    public void savePlayerData(PlayerData data) {
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
            config.set("stats." + entry.getKey(), entry.getValue());
        }

        // Save skills
        for (Map.Entry<String, Integer> entry : data.getSkills().entrySet()) {
            config.set("skills." + entry.getKey(), entry.getValue());
        }

        // Save owned shops
        config.set("ownedShops", data.getOwnedShops());

        // Save custom data
        for (Map.Entry<String, Object> entry : data.getCustomData().entrySet()) {
            config.set("customData." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + data.getPlayerName());
            e.printStackTrace();
        }
    }

    public void saveAllData() {
        for (PlayerData data : playerDataMap.values()) {
            savePlayerData(data);
        }
    }
}