package RavenMC.ravenPets;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public class RavenPets extends JavaPlugin {
    private static RavenPets instance;
    private Economy economy;
    private RavenManager ravenManager;
    private AbilityManager abilityManager;
    private RavenCoinManager coinManager;
    private RavenShopGUI shopGUI;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Set instance
        instance = this;

        // Setup economy
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        this.ravenManager = new RavenManager(this);
        this.abilityManager = new AbilityManager(this);
        this.coinManager = new RavenCoinManager(this);

        // Initialize shop GUI
        this.shopGUI = new RavenShopGUI(this);

        // Register commands
        RavenCommand ravenCommand = new RavenCommand(this);
        getCommand("raven").setExecutor(ravenCommand);
        getCommand("raven").setTabCompleter(ravenCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new RavenListener(this), this);

        // Schedule tasks for raven followers
        startRavenFollowTask();

        getLogger().info("RavenPets has been enabled for Minecraft 1.21!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (ravenManager != null) {
            ravenManager.saveAllRavens();
        }

        if (coinManager != null) {
            coinManager.saveAllCoins();
        }

        getLogger().info("RavenPets has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    private void startRavenFollowTask() {
        double followDistance = getConfig().getDouble("follow-distance", 3.0);
        double teleportDistance = getConfig().getDouble("follow-teleport-distance", 20.0);

        // Run task every 5 ticks (1/4 second)
        getServer().getScheduler().runTaskTimer(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                if (ravenManager.hasRaven(player)) {
                    PlayerRaven raven = ravenManager.getRaven(player);
                    // Logic to make ravens follow players would go here
                    // This would involve updating the raven entity's position
                }
            });
        }, 20L, 5L);
    }

    public static RavenPets getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public RavenManager getRavenManager() {
        return ravenManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public RavenCoinManager getCoinManager() {
        return coinManager;
    }

    public RavenShopGUI getShopGUI() {
        return shopGUI;
    }
}