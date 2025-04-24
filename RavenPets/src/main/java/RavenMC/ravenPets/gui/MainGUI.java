package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Main GUI for the RavenPets plugin
 */
public class MainGUI extends GUI {

    public MainGUI(RavenPets plugin) {
        super(plugin, "&5&lRaven Interface", 45);
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
            return;
        }

        // Raven information in the center
        RavenTier tier = raven.getTier();
        String tierColor = getTierColor(tier);

        // Raven icon in center
        Material ravenMaterial = getRavenMaterial(tier);
        ItemStack ravenItem = createItem(
                ravenMaterial,
                tierColor + "&l" + raven.getName(),
                "&7" + tier.getName() + " (Level " + raven.getLevel() + ")",
                "&7",
                "&7XP: &d" + raven.getXp() + "&7/&d" + (100 + (raven.getLevel() * 50)),
                "&7Status: " + (raven.isActive() ? "&aActive" : "&cInactive")
        );
        inventory.setItem(22, ravenItem);

        // Summon/dismiss button
        if (raven.isActive()) {
            inventory.setItem(31, createItem(
                    Material.RED_CONCRETE,
                    "&c&lDismiss Raven",
                    "&7Click to dismiss your raven"
            ));
        } else {
            inventory.setItem(31, createItem(
                    Material.LIME_CONCRETE,
                    "&a&lSummon Raven",
                    "&7Click to summon your raven"
            ));
        }

        // Ability section
        inventory.setItem(11, createItem(
                Material.BLAZE_POWDER,
                "&d&lAbilities",
                "&7View and manage your",
                "&7raven's abilities"
        ));

        // Inventory section
        inventory.setItem(13, createItem(
                Material.CHEST,
                "&d&lInventory",
                "&7Access your raven's inventory",
                "&7Current slots: &d" + raven.getInventorySlots()
        ));

        // Home locations section
        inventory.setItem(15, createItem(
                Material.COMPASS,
                "&d&lHome Locations",
                "&7Manage your raven's home locations",
                "&7Current homes: &d" + raven.getHomeLocations().size()
        ));

        // Upgrade section
        if (canUpgrade(raven)) {
            inventory.setItem(29, createItem(
                    Material.NETHER_STAR,
                    "&d&lUpgrade Available!",
                    "&7Your raven can evolve to the next tier!",
                    "&7Click to view upgrade requirements"
            ));
        } else {
            inventory.setItem(29, createItem(
                    Material.NETHER_STAR,
                    "&d&lUpgrades",
                    "&7View upgrade requirements for",
                    "&7your raven's next evolution"
            ));
        }

        // Settings section
        inventory.setItem(33, createItem(
                Material.REDSTONE_TORCH,
                "&d&lSettings",
                "&7Configure your raven's settings",
                "&7and appearance"
        ));

        // Close button
        inventory.setItem(44, createItem(
                Material.BARRIER,
                "&c&lClose",
                "&7Close this menu"
        ));
    }

    @Override
    public boolean handleClick(Player player, int slot) {
        Raven raven = getRaven(player);

        // If player doesn't have a raven, only handle close button
        if (raven == null) {
            if (slot == 44) {
                player.closeInventory();
                return true;
            }
            return false;
        }

        switch (slot) {
            case 11: // Abilities
                plugin.getGUIManager().openAbilitiesGUI(player);
                return true;

            case 13: // Inventory
                plugin.getGUIManager().openInventoryGUI(player);
                return true;

            case 15: // Home locations
                plugin.getGUIManager().openHomeLocationsGUI(player);
                return true;

            case 29: // Upgrades
                plugin.getGUIManager().openUpgradeGUI(player);
                return true;

            case 31: // Summon/dismiss
                if (raven.isActive()) {
                    plugin.getRavenManager().despawnRaven(player);
                } else {
                    plugin.getRavenManager().spawnRaven(player);
                }
                update(player);
                return true;

            case 33: // Settings
                plugin.getGUIManager().openSettingsGUI(player);
                return true;

            case 44: // Close
                player.closeInventory();
                return true;
        }

        return false;
    }

    private String getTierColor(RavenTier tier) {
        switch (tier) {
            case NOVICE:
                return "&d"; // Light Purple
            case ADEPT:
                return "&5"; // Dark Purple
            case EXPERT:
                return "&5"; // Bold Dark Purple
            case MASTER:
                return "&5"; // Bold Dark Purple
            case LEGENDARY:
                return "&5"; // Bold Italic Dark Purple
            default:
                return "&d";
        }
    }

    private Material getRavenMaterial(RavenTier tier) {
        switch (tier) {
            case NOVICE:
                return Material.FEATHER;
            case ADEPT:
                return Material.DRAGON_BREATH;
            case EXPERT:
                return Material.ENDER_EYE;
            case MASTER:
                return Material.END_CRYSTAL;
            case LEGENDARY:
                return Material.NETHER_STAR;
            default:
                return Material.FEATHER;
        }
    }

    private boolean canUpgrade(Raven raven) {
        RavenTier currentTier = raven.getTier();
        int level = raven.getLevel();

        // Check if player has reached the level requirement for the next tier
        switch (currentTier) {
            case NOVICE:
                return level >= 10;
            case ADEPT:
                return level >= 25;
            case EXPERT:
                return level >= 50;
            case MASTER:
                return level >= 75;
            case LEGENDARY:
                return false; // Can't upgrade beyond legendary
            default:
                return false;
        }
    }
}