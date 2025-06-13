package RavenMC.ravenRpg;

import RavenMC.ravenRpg.commands.RpgCommand;
import RavenMC.ravenRpg.commands.RpgAdminCommand;
import RavenMC.ravenRpg.commands.ShopCommand;
import RavenMC.ravenRpg.commands.GuildCommand;
import RavenMC.ravenRpg.listeners.*;
import RavenMC.ravenRpg.managers.*;
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
    private Economy economy = null;
    private boolean ravenPetsEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

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

        // Initialize managers
        try {
            dataManager = new DataManager(this);
            raceManager = new RaceManager(this);
            guildManager = new GuildManager(this);
            shopManager = new ShopManager(this);
            bloodlineManager = new BloodlineManager(this);
            getLogger().info("All managers initialized successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize managers: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        try {
            registerCommands();
            getLogger().info("Commands registered successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }

        // Register listeners
        try {
            registerListeners();
            getLogger().info("All listeners registered successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to register listeners: " + e.getMessage());
            e.printStackTrace();
        }

        // Register PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new RavenRpgPlaceholderExpansion(this).register();
                getLogger().info("PlaceholderAPI integration enabled!");
            } catch (Exception e) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
            }
        }

        getLogger().info("RavenRpg has been enabled successfully!" +
                (ravenPetsEnabled ? " (RavenPets integration active)" : ""));
    }

    @Override
    public void onDisable() {
        try {
            if (dataManager != null) {
                dataManager.saveAllData();
                getLogger().info("All player data saved successfully!");
            }
        } catch (Exception e) {
            getLogger().severe("Error saving player data: " + e.getMessage());
        }

        try {
            if (shopManager != null) {
                shopManager.saveAllShops();
                getLogger().info("All shops saved successfully!");
            }
        } catch (Exception e) {
            getLogger().severe("Error saving shops: " + e.getMessage());
        }

        getLogger().info("RavenRpg has been disabled!");
    }

    private boolean setupEconomy() {
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
        return economy != null;
    }

    private void checkRavenPetsIntegration() {
        if (getServer().getPluginManager().getPlugin("RavenPets") != null) {
            ravenPetsEnabled = true;
            getLogger().info("RavenPets detected - enabling integration features!");
        } else {
            getLogger().info("RavenPets not found - running in standalone mode");
        }
    }

    private void registerCommands() {
        // Main RPG command
        RpgCommand rpgCommand = new RpgCommand(this);
        getCommand("rpg").setExecutor(rpgCommand);
        getCommand("rpg").setTabCompleter(rpgCommand);

        // Admin command
        RpgAdminCommand adminCommand = new RpgAdminCommand(this);
        getCommand("rpgadmin").setExecutor(adminCommand);
        getCommand("rpgadmin").setTabCompleter(adminCommand);

        // Shop command
        ShopCommand shopCommand = new ShopCommand(this);
        getCommand("shop").setExecutor(shopCommand);
        getCommand("shop").setTabCompleter(shopCommand);

        // Guild command
        GuildCommand guildCommand = new GuildCommand(this);
        getCommand("guild").setExecutor(guildCommand);
        getCommand("guild").setTabCompleter(guildCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
    }

    // Getters
    public static RavenRpg getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public RaceManager getRaceManager() { return raceManager; }
    public GuildManager getGuildManager() { return guildManager; }
    public ShopManager getShopManager() { return shopManager; }
    public BloodlineManager getBloodlineManager() { return bloodlineManager; }
    public Economy getEconomy() { return economy; }
    public boolean isRavenPetsEnabled() { return ravenPetsEnabled; }
}