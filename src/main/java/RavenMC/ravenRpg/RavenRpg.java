package RavenMC.ravenRpg;

import RavenMC.ravenRpg.commands.RpgCommand;
import RavenMC.ravenRpg.commands.RpgAdminCommand;
import RavenMC.ravenRpg.commands.ShopCommand;
import RavenMC.ravenRpg.commands.GuildCommand;
import RavenMC.ravenRpg.listeners.*;
import RavenMC.ravenRpg.managers.*;
import RavenMC.ravenRpg.integration.RavenPetsIntegration;
import RavenMC.ravenRpg.utils.RavenRpgPlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class RavenRpg extends JavaPlugin {

    private static RavenRpg instance;
    private DataManager dataManager;
    private RaceManager raceManager;
    private GuildManager guildManager;
    private ShopManager shopManager;
    private BloodlineManager bloodlineManager;
    private RavenPetsIntegration ravenPetsIntegration;
    private Economy economy = null;
    private boolean ravenPetsEnabled = false;
    private boolean initializationComplete = false;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Starting RavenRpg initialization...");

        // Save default config
        saveDefaultConfig();

        // Setup Vault economy
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check for RavenPets integration
        checkRavenPetsIntegration();

        // Initialize managers with error handling
        if (!initializeManagers()) {
            getLogger().severe("Failed to initialize managers - disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize RavenPets integration
        if (ravenPetsEnabled) {
            initializeRavenPetsIntegration();
        }

        // Register commands
        if (!registerCommands()) {
            getLogger().warning("Some commands failed to register");
        }

        // Register listeners
        if (!registerListeners()) {
            getLogger().warning("Some listeners failed to register");
        }

        // Register PlaceholderAPI expansion
        registerPlaceholderAPI();

        initializationComplete = true;
        getLogger().info("RavenRpg has been enabled successfully!" +
                (ravenPetsEnabled ? " (RavenPets integration active)" : ""));
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down RavenRpg...");

        try {
            if (dataManager != null) {
                dataManager.saveAllData();
                getLogger().info("All player data saved successfully!");
            }
        } catch (Exception e) {
            getLogger().severe("Error saving player data: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (shopManager != null) {
                shopManager.saveAllShops();
                getLogger().info("All shops saved successfully!");
            }
        } catch (Exception e) {
            getLogger().severe("Error saving shops: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (guildManager != null) {
                guildManager.saveGuilds();
                getLogger().info("All guilds saved successfully!");
            }
        } catch (Exception e) {
            getLogger().severe("Error saving guilds: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("RavenRpg has been disabled!");
    }

    private boolean setupEconomy() {
        try {
            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                getLogger().warning("Vault plugin not found!");
                return false;
            }

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().warning("No economy provider found!");
                return false;
            }

            economy = rsp.getProvider();
            if (economy != null) {
                getLogger().info("Economy setup successful with " + economy.getName());
                return true;
            } else {
                getLogger().warning("Economy provider is null!");
                return false;
            }
        } catch (Exception e) {
            getLogger().severe("Error setting up economy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void checkRavenPetsIntegration() {
        try {
            if (getServer().getPluginManager().getPlugin("RavenPets") != null) {
                ravenPetsEnabled = true;
                getLogger().info("RavenPets detected - enabling integration features!");
            } else {
                getLogger().info("RavenPets not found - running in standalone mode");
            }
        } catch (Exception e) {
            getLogger().warning("Error checking RavenPets integration: " + e.getMessage());
            ravenPetsEnabled = false;
        }
    }

    private boolean initializeManagers() {
        try {
            getLogger().info("Initializing data manager...");
            dataManager = new DataManager(this);
            if (dataManager == null) {
                getLogger().severe("Failed to initialize DataManager");
                return false;
            }

            getLogger().info("Initializing race manager...");
            raceManager = new RaceManager(this);
            if (raceManager == null) {
                getLogger().severe("Failed to initialize RaceManager");
                return false;
            }

            getLogger().info("Initializing guild manager...");
            guildManager = new GuildManager(this);
            if (guildManager == null) {
                getLogger().severe("Failed to initialize GuildManager");
                return false;
            }

            getLogger().info("Initializing shop manager...");
            shopManager = new ShopManager(this);
            if (shopManager == null) {
                getLogger().severe("Failed to initialize ShopManager");
                return false;
            }

            getLogger().info("Initializing bloodline manager...");
            bloodlineManager = new BloodlineManager(this);
            if (bloodlineManager == null) {
                getLogger().severe("Failed to initialize BloodlineManager");
                return false;
            }

            getLogger().info("All managers initialized successfully!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to initialize managers: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void initializeRavenPetsIntegration() {
        try {
            ravenPetsIntegration = new RavenPetsIntegration(this);
            if (ravenPetsIntegration != null && ravenPetsIntegration.isEnabled()) {
                getLogger().info("RavenPets integration initialized successfully!");
            } else {
                getLogger().warning("RavenPets integration failed to initialize properly");
                ravenPetsEnabled = false;
            }
        } catch (Exception e) {
            getLogger().warning("Failed to initialize RavenPets integration: " + e.getMessage());
            e.printStackTrace();
            ravenPetsEnabled = false;
        }
    }

    private boolean registerCommands() {
        boolean success = true;

        try {
            // Main RPG command
            RpgCommand rpgCommand = new RpgCommand(this);
            if (getCommand("rpg") != null) {
                getCommand("rpg").setExecutor(rpgCommand);
                getCommand("rpg").setTabCompleter(rpgCommand);
                getLogger().info("Registered /rpg command");
            } else {
                getLogger().warning("Failed to register /rpg command - command not found in plugin.yml");
                success = false;
            }

            // Admin command
            RpgAdminCommand adminCommand = new RpgAdminCommand(this);
            if (getCommand("rpgadmin") != null) {
                getCommand("rpgadmin").setExecutor(adminCommand);
                getCommand("rpgadmin").setTabCompleter(adminCommand);
                getLogger().info("Registered /rpgadmin command");
            } else {
                getLogger().warning("Failed to register /rpgadmin command - command not found in plugin.yml");
                success = false;
            }

            // Shop command
            ShopCommand shopCommand = new ShopCommand(this);
            if (getCommand("shop") != null) {
                getCommand("shop").setExecutor(shopCommand);
                getCommand("shop").setTabCompleter(shopCommand);
                getLogger().info("Registered /shop command");
            } else {
                getLogger().warning("Failed to register /shop command - command not found in plugin.yml");
                success = false;
            }

            // Guild command
            GuildCommand guildCommand = new GuildCommand(this);
            if (getCommand("guild") != null) {
                getCommand("guild").setExecutor(guildCommand);
                getCommand("guild").setTabCompleter(guildCommand);
                getLogger().info("Registered /guild command");
            } else {
                getLogger().warning("Failed to register /guild command - command not found in plugin.yml");
                success = false;
            }

            if (success) {
                getLogger().info("All commands registered successfully!");
            }

        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    private boolean registerListeners() {
        boolean success = true;

        try {
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
            getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
            getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);

            getLogger().info("All listeners registered successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to register listeners: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    private void registerPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new RavenRpgPlaceholderExpansion(this).register();
                getLogger().info("PlaceholderAPI integration enabled!");
            } catch (Exception e) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Getters with null safety
    public static RavenRpg getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        if (dataManager == null) {
            getLogger().warning("DataManager is null! Plugin may not be fully initialized.");
        }
        return dataManager;
    }

    public RaceManager getRaceManager() {
        if (raceManager == null) {
            getLogger().warning("RaceManager is null! Plugin may not be fully initialized.");
        }
        return raceManager;
    }

    public GuildManager getGuildManager() {
        if (guildManager == null) {
            getLogger().warning("GuildManager is null! Plugin may not be fully initialized.");
        }
        return guildManager;
    }

    public ShopManager getShopManager() {
        if (shopManager == null) {
            getLogger().warning("ShopManager is null! Plugin may not be fully initialized.");
        }
        return shopManager;
    }

    public BloodlineManager getBloodlineManager() {
        if (bloodlineManager == null) {
            getLogger().warning("BloodlineManager is null! Plugin may not be fully initialized.");
        }
        return bloodlineManager;
    }

    public RavenPetsIntegration getRavenPetsIntegration() {
        return ravenPetsIntegration;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isRavenPetsEnabled() {
        return ravenPetsEnabled && ravenPetsIntegration != null;
    }

    public boolean isInitializationComplete() {
        return initializationComplete;
    }

    // Utility methods
    public boolean isPluginFullyLoaded() {
        return initializationComplete &&
                dataManager != null &&
                raceManager != null &&
                guildManager != null &&
                shopManager != null &&
                bloodlineManager != null;
    }

    public void safeShutdown() {
        getLogger().info("Performing safe shutdown...");

        try {
            if (dataManager != null) {
                dataManager.saveAllData();
            }
            if (guildManager != null) {
                guildManager.saveGuilds();
            }
            if (shopManager != null) {
                shopManager.saveAllShops();
            }
        } catch (Exception e) {
            getLogger().severe("Error during safe shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}