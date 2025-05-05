package RavenMC.ravenPets;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;

public class RavenPets extends JavaPlugin implements Listener {
    private static RavenPets instance;
    private Economy economy;
    private RavenManager ravenManager;
    private AbilityManager abilityManager;
    private RavenCoinManager coinManager;
    private RavenShopGUI shopGUI;
    private RavenMainGUI mainGUI;
    private RavenChatListener chatListener;
    private RavenAdminCommand adminCommandHandler;
    private boolean placeholderAPIEnabled = false;

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

        // Initialize GUIs
        this.shopGUI = new RavenShopGUI(this);
        this.mainGUI = new RavenMainGUI(this);

        // Initialize chat listener
        this.chatListener = new RavenChatListener(this);

        // Register commands
        RavenCommand ravenCommand = new RavenCommand(this);
        getCommand("raven").setExecutor(ravenCommand);
        getCommand("raven").setTabCompleter(ravenCommand);

        // Register admin command
        this.adminCommandHandler = new RavenAdminCommand(this);
        getCommand("radmin").setExecutor(adminCommandHandler);
        getCommand("radmin").setTabCompleter(adminCommandHandler);

        // Register listeners
        getServer().getPluginManager().registerEvents(new RavenListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(mainGUI, this);
        getServer().getPluginManager().registerEvents(shopGUI, this);
        getServer().getPluginManager().registerEvents(adminCommandHandler, this);

        // Schedule tasks for raven followers
        startRavenFollowTask();

        // Start auto-save for raven data
        ravenManager.startAutoSave();

        // Check for PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            // Use proper registration method for PlaceholderAPI
            new RavenPlaceholders(this).register();
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        getLogger().info("RavenPets has been enabled for Minecraft 1.21!");
    }

    @Override
    public void onDisable() {
        // Despawn all ravens when server stops
        getServer().getOnlinePlayers().forEach(player -> {
            if (ravenManager.hasRaven(player)) {
                ravenManager.despawnRaven(player);
            }
        });

        // Save all data
        if (ravenManager != null) {
            getLogger().info("Saving all raven data...");
            ravenManager.saveAllRavens();
        }

        if (coinManager != null) {
            getLogger().info("Saving all coin data...");
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

                    if (raven.isSpawned() && raven.getRavenEntity() != null && !raven.getRavenEntity().isDead()) {
                        // Calculate distance between player and raven
                        double distance = player.getLocation().distance(raven.getRavenEntity().getLocation());

                        // If raven is too far, teleport it
                        if (distance > teleportDistance) {
                            raven.getRavenEntity().teleport(player.getLocation().add(
                                    (Math.random() - 0.5) * 2,
                                    1.5 + (Math.random() * 0.5),
                                    (Math.random() - 0.5) * 2));
                        }
                        // Otherwise, move smoothly towards player if beyond follow distance
                        else if (distance > followDistance) {
                            // Get direction vector
                            org.bukkit.util.Vector direction = player.getLocation().toVector()
                                    .subtract(raven.getRavenEntity().getLocation().toVector())
                                    .normalize()
                                    .multiply(0.2); // Speed factor

                            // Add slight Y offset to make it fly above player
                            direction.setY(direction.getY() + 0.05);

                            // Move the entity
                            raven.getRavenEntity().teleport(raven.getRavenEntity().getLocation().add(direction.toLocation(player.getWorld())));

                            // Add flying particles based on element type
                            if (Math.random() < 0.3) {
                                Particle particleType = Particle.WITCH; // Default

                                switch (raven.getElementType()) {
                                    case FIRE:
                                        particleType = Particle.FLAME;
                                        break;
                                    case WATER:
                                        particleType = Particle.DRIPPING_WATER;
                                        break;
                                    case EARTH:
                                        particleType = Particle.FALLING_DUST;
                                        break;
                                    case AIR:
                                        particleType = Particle.CLOUD;
                                        break;
                                    case LIGHTNING:
                                        particleType = Particle.ELECTRIC_SPARK;
                                        break;
                                    case ICE:
                                        particleType = Particle.SNOWFLAKE;
                                        break;
                                    case NATURE:
                                        particleType = Particle.COMPOSTER;
                                        break;
                                    case DARKNESS:
                                        particleType = Particle.SMOKE;
                                        break;
                                    case LIGHT:
                                        particleType = Particle.END_ROD;
                                        break;
                                }

                                raven.getRavenEntity().getWorld().spawnParticle(
                                        particleType,
                                        raven.getRavenEntity().getLocation(),
                                        1, 0.1, 0.1, 0.1, 0);
                            }
                        }
                    }
                }
            });
        }, 20L, 5L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();

        // Check if the item is a raven core
        if (displayName.startsWith("§5§lRaven Core:") &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);

            // Check if player is sneaking for secondary ability
            if (player.isSneaking()) {
                abilityManager.executeSecondaryAbility(player);
            } else {
                abilityManager.executeAbility(player);
            }
        }
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

    public RavenMainGUI getMainGUI() {
        return mainGUI;
    }

    public RavenChatListener getChatListener() {
        return chatListener;
    }

    public RavenAdminCommand getAdminCommandHandler() {
        return adminCommandHandler;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}