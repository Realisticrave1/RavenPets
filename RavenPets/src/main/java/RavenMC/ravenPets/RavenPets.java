package RavenMC.ravenPets;

import RavenMC.ravenPets.commands.RavenBlockCommand;
import RavenMC.ravenPets.commands.RavenCommand;
import RavenMC.ravenPets.config.ConfigManager;
import RavenMC.ravenPets.database.DatabaseManager;
import RavenMC.ravenPets.gui.GUIManager;
import RavenMC.ravenPets.gui.InventoryGUI;
import RavenMC.ravenPets.integrations.CitizensIntegration;
import RavenMC.ravenPets.integrations.LuckPermsIntegration;
import RavenMC.ravenPets.integrations.PlaceholderIntegration;
import RavenMC.ravenPets.listeners.GUIListener;
import RavenMC.ravenPets.listeners.PlayerListener;
import RavenMC.ravenPets.listeners.RavenBlockListener;
import RavenMC.ravenPets.listeners.RavenListener;
import RavenMC.ravenPets.manager.RavenBlockManager;
import RavenMC.ravenPets.manager.RavenManager;
import RavenMC.ravenPets.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class RavenPets extends JavaPlugin {

    private static RavenPets instance;
    private ConfigManager configManager;
    private RavenManager ravenManager;
    private DatabaseManager databaseManager;
    private GUIManager guiManager;
    private RavenBlockManager ravenBlockManager;
    private CitizensIntegration citizensIntegration;
    private LuckPermsIntegration luckPermsIntegration;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Initializing RavenPets plugin for Minecraft 1.21.4...");

        // Initialize message utility
        MessageUtil.init(this);

        // Initialize config
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Initialize database
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.initialize();
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            getLogger().severe("Disabling plugin due to database initialization failure!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        ravenManager = new RavenManager(this);
        guiManager = new GUIManager(this);
        ravenBlockManager = new RavenBlockManager(this);

        // Register commands
        getCommand("ravenpet").setExecutor(new RavenCommand(this));
        getCommand("ravenblock").setExecutor(new RavenBlockCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RavenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RavenBlockListener(this), this);

        // Initialize integrations
        setupIntegrations();

        getLogger().info("RavenPets has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling RavenPets plugin...");

        if (ravenManager != null) {
            getLogger().info("Saving all ravens...");
            ravenManager.saveAllRavens();
        }

        // Save all raven inventories
        getLogger().info("Saving raven inventories...");
        InventoryGUI.saveAllInventories();

        if (databaseManager != null) {
            getLogger().info("Closing database connection...");
            databaseManager.close();
        }

        getLogger().info("RavenPets has been disabled!");
    }

    private void setupIntegrations() {
        // Citizens integration
        if (Bukkit.getPluginManager().getPlugin("Citizens") != null) {
            try {
                getLogger().info("Initializing Citizens integration...");
                citizensIntegration = new CitizensIntegration(this);
                getLogger().info("Citizens integration enabled!");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to initialize Citizens integration: " + e.getMessage(), e);
            }
        } else {
            getLogger().info("Citizens plugin not found, integration disabled.");
        }

        // LuckPerms integration
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                getLogger().info("Initializing LuckPerms integration...");
                luckPermsIntegration = new LuckPermsIntegration(this);
                getLogger().info("LuckPerms integration enabled!");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to initialize LuckPerms integration: " + e.getMessage(), e);
            }
        } else {
            getLogger().info("LuckPerms plugin not found, integration disabled.");
        }

        // PlaceholderAPI integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                getLogger().info("Initializing PlaceholderAPI integration...");
                new PlaceholderIntegration(this).register();
                getLogger().info("PlaceholderAPI integration enabled!");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to initialize PlaceholderAPI integration: " + e.getMessage(), e);
            }
        } else {
            getLogger().info("PlaceholderAPI plugin not found, integration disabled.");
        }
    }

    public static RavenPets getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RavenManager getRavenManager() {
        return ravenManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public RavenBlockManager getRavenBlockManager() {
        return ravenBlockManager;
    }

    public CitizensIntegration getCitizensIntegration() {
        return citizensIntegration;
    }

    public LuckPermsIntegration getLuckPermsIntegration() {
        return luckPermsIntegration;
    }
}