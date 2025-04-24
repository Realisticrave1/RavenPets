package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for managing raven home locations
 */
public class HomeLocationsGUI extends GUI {

    public HomeLocationsGUI(RavenPets plugin) {
        super(plugin, "&5&lRaven Home Locations", 54);
    }

    @Override
    public void update(Player player) {
        // Clear the inventory
        inventory.clear();

        // Create border
        createBorder(Material.BLACK_STAINED_GLASS_PANE);

        Raven raven = getRaven(player);
        if (raven == null) {
            // Player doesn't have a raven
            inventory.setItem(22, createItem(
                    Material.BARRIER,
                    "&c&lNo Raven Found",
                    "&7Complete the welcome tutorial",
                    "&7to claim your Raven Egg."
            ));

            // Add back button
            inventory.setItem(49, createItem(
                    Material.ARROW,
                    "&c&l← Back",
                    "&7Return to the main menu"
            ));

            return;
        }

        // Add title
        int maxHomes = getMaxHomeLocations(raven.getTier());
        inventory.setItem(4, createItem(
                Material.COMPASS,
                "&d&lRaven Home Locations",
                "&7Current homes: &d" + raven.getHomeLocations().size() + "/" + maxHomes,
                "&7",
                "&7Click on a location to teleport"
        ));

        // Add home locations
        List<Location> homes = raven.getHomeLocations();

        if (homes.isEmpty()) {
            inventory.setItem(22, createItem(
                    Material.RED_BED,
                    "&c&lNo Home Locations",
                    "&7You haven't set any home locations yet.",
                    "&7",
                    "&aClick here to add your current location",
                    "&7as a home location."
            ));
        } else {
            // Display homes as clickable items
            for (int i = 0; i < homes.size(); i++) {
                if (i >= 28) break; // Maximum of 28 visible homes (4 rows * 7 columns)

                Location loc = homes.get(i);
                int row = (i / 7) + 2; // Start from the third row
                int col = (i % 7) + 1; // Spread across columns 1-7
                int slot = row * 9 + col;

                // Create home item
                inventory.setItem(slot, createHomeItem(i + 1, loc));
            }
        }

        // Add action buttons

        // Add home button
        if (raven.getHomeLocations().size() < maxHomes) {
            inventory.setItem(47, createItem(
                    Material.EMERALD,
                    "&a&lAdd Home",
                    "&7Add your current location",
                    "&7as a home location"
            ));
        } else {
            inventory.setItem(47, createItem(
                    Material.BARRIER,
                    "&c&lMaximum Homes Reached",
                    "&7You can't add more home locations!",
                    "&7Maximum: &d" + maxHomes
            ));
        }

        // Delete home button (active only if there are homes)
        if (!homes.isEmpty()) {
            inventory.setItem(51, createItem(
                    Material.REDSTONE,
                    "&c&lDelete Home",
                    "&7Click on a home location",
                    "&7after clicking this button",
                    "&7to delete it"
            ));
        }

        // Add navigation buttons
        inventory.setItem(49, createItem(
                Material.ARROW,
                "&c&l← Back",
                "&7Return to the main menu"
        ));
    }

    @Override
    public boolean handleClick(Player player, int slot) {
        // Handle back button
        if (slot == 49) {
            plugin.getGUIManager().openMainGUI(player);
            return true;
        }

        Raven raven = getRaven(player);
        if (raven == null) {
            return false;
        }

        // Handle add home button
        if (slot == 47) {
            addHomeLocation(player, raven);
            update(player);
            return true;
        }

        // Handle delete home button
        if (slot == 51) {
            // Toggle delete mode
            player.sendMessage(ChatColor.YELLOW + "Click on a home location to delete it.");
            return true;
        }

        // Handle "No Home Locations" button
        if (slot == 22 && raven.getHomeLocations().isEmpty()) {
            addHomeLocation(player, raven);
            update(player);
            return true;
        }

        // Handle home location clicks
        int row = slot / 9;
        int col = slot % 9;

        if (row >= 2 && row <= 5 && col >= 1 && col <= 7) {
            // This is a home location slot
            int index = (row - 2) * 7 + (col - 1);
            List<Location> homes = raven.getHomeLocations();

            if (index < homes.size()) {
                // Clicked on a valid home location

                // Check if in delete mode (checking if the delete button was previously clicked)
                ItemStack deleteButton = inventory.getItem(51);
                if (deleteButton != null && deleteButton.getType() == Material.REDSTONE) {
                    // Delete the home location
                    homes.remove(index);
                    player.sendMessage(ChatColor.RED + "Home location #" + (index + 1) + " deleted!");
                    update(player);
                    return true;
                }

                // Check if teleportation ability is unlocked
                if (!raven.hasAbility("teleportation")) {
                    player.sendMessage(ChatColor.RED + "Your raven needs the teleportation ability!");
                    return true;
                }

                // Teleport to the home location
                player.teleport(homes.get(index));
                player.sendMessage(ChatColor.GREEN + "Teleported to home location #" + (index + 1) + "!");

                // Close inventory after teleporting
                player.closeInventory();
                return true;
            }
        }

        return false;
    }

    /**
     * Creates an item representing a home location
     *
     * @param number The home location number
     * @param location The home location
     * @return The item representing the home location
     */
    private ItemStack createHomeItem(int number, Location location) {
        Material material;
        String dimension = location.getWorld().getEnvironment().name();

        // Choose material based on dimension
        switch (dimension) {
            case "NORMAL":
                material = Material.GREEN_BED;
                break;
            case "NETHER":
                material = Material.RED_BED;
                break;
            case "THE_END":
                material = Material.PURPLE_BED;
                break;
            default:
                material = Material.WHITE_BED;
                break;
        }

        // Create item with location information
        return createItem(
                material,
                "&d&lHome #" + number,
                "&7X: &f" + location.getBlockX(),
                "&7Y: &f" + location.getBlockY(),
                "&7Z: &f" + location.getBlockZ(),
                "&7World: &f" + formatWorldName(location.getWorld()),
                "&7",
                "&aClick to teleport to this location"
        );
    }

    /**
     * Formats a world name for display
     *
     * @param world The world to format the name of
     * @return The formatted world name
     */
    private String formatWorldName(World world) {
        String dimension = world.getEnvironment().name();

        switch (dimension) {
            case "NORMAL":
                return "Overworld";
            case "NETHER":
                return "The Nether";
            case "THE_END":
                return "The End";
            default:
                return world.getName();
        }
    }

    /**
     * Gets the maximum number of home locations for a tier
     *
     * @param tier The tier to get the maximum for
     * @return The maximum number of home locations
     */
    private int getMaxHomeLocations(RavenTier tier) {
        switch (tier) {
            case NOVICE: return 0;
            case ADEPT: return 3;
            case EXPERT: return 5;
            case MASTER: return 8;
            case LEGENDARY: return 28; // Maximum visible in GUI
            default: return 0;
        }
    }

    /**
     * Adds a home location for a player
     *
     * @param player The player to add a home location for
     * @param raven The player's raven
     */
    private void addHomeLocation(Player player, Raven raven) {
        int maxHomes = getMaxHomeLocations(raven.getTier());

        // Check if player can add more homes
        if (raven.getHomeLocations().size() >= maxHomes) {
            player.sendMessage(ChatColor.RED + "You can't add more home locations! Maximum: " + maxHomes);
            return;
        }

        // Add home
        raven.addHomeLocation(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Home location added! You now have " +
                raven.getHomeLocations().size() + "/" + maxHomes + " home locations.");
    }
}