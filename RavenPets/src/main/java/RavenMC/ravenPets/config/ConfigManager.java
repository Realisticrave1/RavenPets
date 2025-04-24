package RavenMC.ravenPets.config;

import RavenMC.ravenPets.RavenPets;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    private final RavenPets plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final NamespacedKey ravenKey;

    public ConfigManager(RavenPets plugin) {
        this.plugin = plugin;
        this.ravenKey = new NamespacedKey(plugin, "raven_owner");
    }

    public void loadConfigs() {
        createConfig("config.yml");
        createConfig("messages.yml");

        reloadConfigs();
    }

    public void reloadConfigs() {
        config = loadConfig("config.yml");
        messages = loadConfig("messages.yml");
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            createConfig(fileName);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    private void createConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            // Create parent directories
            file.getParentFile().mkdirs();

            // Save default config
            plugin.saveResource(fileName, false);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfigs();
        }
        return config;
    }

    public FileConfiguration getMessages() {
        if (messages == null) {
            reloadConfigs();
        }
        return messages;
    }

    public void saveConfig() {
        try {
            getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config to " + "config.yml" + ": " + e.getMessage());
        }
    }

    public void saveMessages() {
        try {
            getMessages().save(new File(plugin.getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages to " + "messages.yml" + ": " + e.getMessage());
        }
    }

    public NamespacedKey getRavenKey() {
        return ravenKey;
    }
}