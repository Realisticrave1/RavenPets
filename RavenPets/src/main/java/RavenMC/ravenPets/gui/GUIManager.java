package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import org.bukkit.entity.Player;

/**
 * Manages all GUIs in the RavenPets plugin
 */
public class GUIManager {

    private final RavenPets plugin;
    private final MainGUI mainGUI;
    private final AbilitiesGUI abilitiesGUI;
    private final InventoryGUI inventoryGUI;
    private final HomeLocationsGUI homeLocationsGUI;
    private final UpgradeGUI upgradeGUI;
    private final SettingsGUI settingsGUI;

    public GUIManager(RavenPets plugin) {
        this.plugin = plugin;
        this.mainGUI = new MainGUI(plugin);
        this.abilitiesGUI = new AbilitiesGUI(plugin);
        this.inventoryGUI = new InventoryGUI(plugin);
        this.homeLocationsGUI = new HomeLocationsGUI(plugin);
        this.upgradeGUI = new UpgradeGUI(plugin);
        this.settingsGUI = new SettingsGUI(plugin);
    }

    /**
     * Opens the main raven interface for a player
     *
     * @param player The player to open the GUI for
     */
    public void openMainGUI(Player player) {
        mainGUI.open(player);
    }

    /**
     * Opens the abilities interface for a player
     *
     * @param player The player to open the GUI for
     */
    public void openAbilitiesGUI(Player player) {
        abilitiesGUI.open(player);
    }

    /**
     * Opens the inventory interface for a player
     *
     * @param player The player to open the GUI for
     */
    public void openInventoryGUI(Player player) {
        inventoryGUI.open(player);
    }

    /**
     * Opens the home locations interface for a player
     *
     * @param player The player to open the GUI for
     */
    public void openHomeLocationsGUI(Player player) {
        homeLocationsGUI.open(player);
    }

    /**
     * Opens the upgrade interface for a player
     *
     * @param player The player to open the GUI for
     */
    public void openUpgradeGUI(Player player) {
        upgradeGUI.open(player);
    }

    /**
     * Opens the settings interface for a player
     *
     * @param player The player to open the GUI for
     */
    public void openSettingsGUI(Player player) {
        settingsGUI.open(player);
    }

    /**
     * Gets the main GUI instance
     *
     * @return The main GUI instance
     */
    public MainGUI getMainGUI() {
        return mainGUI;
    }

    /**
     * Gets the abilities GUI instance
     *
     * @return The abilities GUI instance
     */
    public AbilitiesGUI getAbilitiesGUI() {
        return abilitiesGUI;
    }

    /**
     * Gets the inventory GUI instance
     *
     * @return The inventory GUI instance
     */
    public InventoryGUI getInventoryGUI() {
        return inventoryGUI;
    }

    /**
     * Gets the home locations GUI instance
     *
     * @return The home locations GUI instance
     */
    public HomeLocationsGUI getHomeLocationsGUI() {
        return homeLocationsGUI;
    }

    /**
     * Gets the upgrade GUI instance
     *
     * @return The upgrade GUI instance
     */
    public UpgradeGUI getUpgradeGUI() {
        return upgradeGUI;
    }

    /**
     * Gets the settings GUI instance
     *
     * @return The settings GUI instance
     */
    public SettingsGUI getSettingsGUI() {
        return settingsGUI;
    }
}