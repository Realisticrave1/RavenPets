package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Base GUI class that all RavenPets GUIs extend from
 */
public abstract class GUI {

    protected final RavenPets plugin;
    protected Inventory inventory;
    protected final String title;
    protected final int size;

    // Keep track of open inventories for each player
    private static final List<UUID> openInventories = new ArrayList<>();

    /**
     * Constructor for the base GUI
     *
     * @param plugin The main plugin instance
     * @param title  The title of the GUI
     * @param size   The size of the GUI (must be a multiple of 9)
     */
    public GUI(RavenPets plugin, String title, int size) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, this.title);
    }

    /**
     * Gets the GUI title
     *
     * @return The GUI title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the GUI size
     *
     * @return The GUI size
     */
    public int getSize() {
        return size;
    }

    /**
     * Opens the GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void open(Player player) {
        // Add player to open inventories list
        openInventories.add(player.getUniqueId());

        // Update the GUI contents
        update(player);

        // Open the inventory
        player.openInventory(inventory);
    }

    /**
     * Updates the GUI contents
     *
     * @param player The player to update the GUI for
     */
    public abstract void update(Player player);

    /**
     * Handles a click in the GUI
     *
     * @param player The player who clicked
     * @param slot   The slot that was clicked
     * @return Whether the click was handled
     */
    public abstract boolean handleClick(Player player, int slot);

    /**
     * Creates an item for the GUI
     *
     * @param material The material of the item
     * @param name     The name of the item
     * @param lore     The lore of the item
     * @return The created item
     */
    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (lore != null && lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreList);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a border around the GUI using a specified material
     *
     * @param material The material to use for the border
     */
    protected void createBorder(Material material) {
        ItemStack borderItem = new ItemStack(material);
        ItemMeta meta = borderItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            borderItem.setItemMeta(meta);
        }

        // Top row
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
        }

        // Bottom row
        for (int i = size - 9; i < size; i++) {
            inventory.setItem(i, borderItem);
        }

        // Left and right sides
        for (int i = 0; i < size / 9; i++) {
            inventory.setItem(i * 9, borderItem);           // Left side
            inventory.setItem(i * 9 + 8, borderItem);       // Right side
        }
    }

    /**
     * Creates navigation buttons for the GUI
     */
    protected void createNavigationButtons() {
        // Back button
        inventory.setItem(size - 9, createItem(Material.ARROW, "&c← Back", "&7Return to the previous menu"));

        // Close button
        inventory.setItem(size - 1, createItem(Material.BARRIER, "&cClose", "&7Close this menu"));
    }

    /**
     * Gets the raven for a player
     *
     * @param player The player to get the raven for
     * @return The player's raven, or null if they don't have one
     */
    protected Raven getRaven(Player player) {
        return plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
    }

    /**
     * Checks if a player has an open inventory
     *
     * @param playerId The UUID of the player to check
     * @return Whether the player has an open inventory
     */
    public static boolean hasOpenInventory(UUID playerId) {
        return openInventories.contains(playerId);
    }

    /**
     * Removes a player from the open inventories list
     *
     * @param playerId The UUID of the player to remove
     */
    public static void removePlayer(UUID playerId) {
        openInventories.remove(playerId);
    }
}