package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.abilities.RavenAbility;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI for managing raven abilities
 */
public class AbilitiesGUI extends GUI {

    private static final Map<RavenAbility, Material> abilityMaterials = new HashMap<>();

    static {
        // Novice abilities
        abilityMaterials.put(RavenAbility.BASIC_CRAFTING, Material.CRAFTING_TABLE);
        abilityMaterials.put(RavenAbility.SMALL_INVENTORY, Material.CHEST);
        abilityMaterials.put(RavenAbility.ITEM_RETRIEVAL, Material.HOPPER);
        abilityMaterials.put(RavenAbility.ILLUMINATION, Material.GLOWSTONE);

        // Adept abilities
        abilityMaterials.put(RavenAbility.IMPROVED_CRAFTING, Material.SMITHING_TABLE);
        abilityMaterials.put(RavenAbility.MEDIUM_INVENTORY, Material.BARREL);
        abilityMaterials.put(RavenAbility.BASIC_FLIGHT, Material.FEATHER);
        abilityMaterials.put(RavenAbility.ENEMY_DETECTION, Material.ENDER_EYE);
        abilityMaterials.put(RavenAbility.RESOURCE_HIGHLIGHTING, Material.DIAMOND_ORE);
        abilityMaterials.put(RavenAbility.HOME_LOCATIONS, Material.RED_BED);

        // Expert abilities
        abilityMaterials.put(RavenAbility.ADVANCED_CRAFTING, Material.ENCHANTING_TABLE);
        abilityMaterials.put(RavenAbility.LARGE_INVENTORY, Material.SHULKER_BOX);
        abilityMaterials.put(RavenAbility.ENHANCED_FLIGHT, Material.ELYTRA);
        abilityMaterials.put(RavenAbility.COMBAT_ASSISTANCE, Material.IRON_SWORD);
        abilityMaterials.put(RavenAbility.TELEPORTATION, Material.ENDER_PEARL);
        abilityMaterials.put(RavenAbility.RESOURCE_AUTO_COLLECTION, Material.DIAMOND_PICKAXE);
        abilityMaterials.put(RavenAbility.EXPERT_HOME_LOCATIONS, Material.PURPLE_BED);

        // Master abilities
        abilityMaterials.put(RavenAbility.MASTER_CRAFTING, Material.BEACON);
        abilityMaterials.put(RavenAbility.XL_INVENTORY, Material.ENDER_CHEST);
        abilityMaterials.put(RavenAbility.ADVANCED_FLIGHT, Material.DRAGON_HEAD);
        abilityMaterials.put(RavenAbility.CUSTOM_HOME_DIMENSION, Material.END_PORTAL_FRAME);
        abilityMaterials.put(RavenAbility.ADVANCED_MAGIC, Material.BREWING_STAND);
        abilityMaterials.put(RavenAbility.AUTO_REPAIR, Material.ANVIL);
        abilityMaterials.put(RavenAbility.WEATHER_CONTROL, Material.WATER_BUCKET);
        abilityMaterials.put(RavenAbility.MASTER_HOME_LOCATIONS, Material.BLACK_BED);

        // Legendary abilities
        abilityMaterials.put(RavenAbility.LEGENDARY_CRAFTING, Material.DRAGON_EGG);
        abilityMaterials.put(RavenAbility.UNLIMITED_INVENTORY, Material.CHEST_MINECART);
        abilityMaterials.put(RavenAbility.UNLIMITED_FLIGHT, Material.NETHERITE_CHESTPLATE);
        abilityMaterials.put(RavenAbility.WORLD_ALTERING, Material.COMMAND_BLOCK);
        abilityMaterials.put(RavenAbility.CUSTOM_COMMANDS, Material.REPEATING_COMMAND_BLOCK);
        abilityMaterials.put(RavenAbility.SERVER_TITLES, Material.NAME_TAG);
        abilityMaterials.put(RavenAbility.TIME_CONTROL, Material.CLOCK);
        abilityMaterials.put(RavenAbility.VOID_WALKING, Material.NETHERITE_BOOTS);
        abilityMaterials.put(RavenAbility.RESOURCE_GENERATION, Material.NETHERITE_PICKAXE);
        abilityMaterials.put(RavenAbility.UNLIMITED_HOME_LOCATIONS, Material.RESPAWN_ANCHOR);
    }

    private RavenTier currentDisplayTier;

    public AbilitiesGUI(RavenPets plugin) {
        super(plugin, "&5&lRaven Abilities", 54);
        this.currentDisplayTier = RavenTier.NOVICE;
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

        // Add tier selection buttons
        addTierSelectionButtons(raven);

        // Show ability slots based on current display tier
        showAbilities(raven, currentDisplayTier);

        // Add title
        inventory.setItem(4, createItem(
                Material.ENCHANTED_BOOK,
                getTierColor(currentDisplayTier) + "&l" + currentDisplayTier.getName() + " Abilities",
                "&7Level " + currentDisplayTier.getMinLevel() + "-" + currentDisplayTier.getMaxLevel(),
                "&7",
                "&7Click on an ability to use it",
                "&7or see more information"
        ));

        // Add navigation buttons
        inventory.setItem(49, createItem(
                Material.ARROW,
                "&c&l← Back",
                "&7Return to the main menu"
        ));
    }

    private void addTierSelectionButtons(Raven raven) {
        RavenTier playerTier = raven.getTier();
        int currentTierOrdinal = playerTier.ordinal();

        // Add tier buttons on the top row
        for (RavenTier tier : RavenTier.values()) {
            int slot = tier.ordinal();
            boolean unlocked = tier.ordinal() <= currentTierOrdinal;
            boolean selected = tier == currentDisplayTier;

            Material material;
            String name;
            String[] lore;

            if (unlocked) {
                material = selected ? Material.PURPLE_CONCRETE : Material.MAGENTA_CONCRETE;
                name = getTierColor(tier) + "&l" + tier.getName();
                lore = new String[] {
                        "&7Level " + tier.getMinLevel() + "-" + tier.getMaxLevel(),
                        "&7",
                        selected ? "&5&l✓ Currently Selected" : "&7Click to view abilities"
                };
            } else {
                material = Material.GRAY_CONCRETE;
                name = "&8&l" + tier.getName() + " &c&l(Locked)";
                lore = new String[] {
                        "&7Level " + tier.getMinLevel() + "-" + tier.getMaxLevel(),
                        "&7",
                        "&cYou haven't unlocked this tier yet!"
                };
            }

            inventory.setItem(slot, createItem(material, name, lore));
        }
    }

    private void showAbilities(Raven raven, RavenTier tier) {
        RavenTier playerTier = raven.getTier();
        int currentRow = 2; // Start from the third row (after tier selection and title)

        // Get abilities for the current tier
        for (RavenAbility ability : RavenAbility.values()) {
            if (ability.getRequiredTier() == tier) {
                boolean unlocked = raven.hasAbility(ability.getName());
                boolean canUse = playerTier.ordinal() >= tier.ordinal() && unlocked;

                Material material = abilityMaterials.getOrDefault(ability, Material.PAPER);
                String name;
                ArrayList<String> lore = new ArrayList<>();

                // Set name based on unlock status
                if (unlocked) {
                    name = "&a&l" + formatAbilityName(ability.getName());
                    lore.add("&7" + ability.getDescription());
                    lore.add("&7");

                    if (canUse) {
                        lore.add("&a✓ Unlocked");
                        lore.add("&7");
                        lore.add("&eClick to use this ability");
                    } else {
                        lore.add("&c✗ Not available at your tier");
                    }
                } else {
                    name = "&c&l" + formatAbilityName(ability.getName()) + " &c&l(Locked)";
                    lore.add("&7" + ability.getDescription());
                    lore.add("&7");
                    lore.add("&cYou haven't unlocked this ability yet!");

                    // Add unlock requirements
                    lore.add("&7");
                    lore.add("&7Requirements:");
                    lore.add("&7- Raven Tier: " + getTierColor(tier) + tier.getName());

                    // Add custom requirements based on ability
                    switch (ability) {
                        case BASIC_FLIGHT:
                            lore.add("&7- Craft a Raven Amulet");
                            break;
                        case TELEPORTATION:
                            lore.add("&7- Defeat the Enhanced Ender Dragon");
                            break;
                        case WEATHER_CONTROL:
                            lore.add("&7- Complete the Void Citadel dungeon");
                            break;
                        case TIME_CONTROL:
                            lore.add("&7- Defeat the Void Lord raid boss");
                            break;
                    }
                }

                // Find the next available slot
                int column = (ability.ordinal() % 7) + 1; // Spread across columns 1-7
                int slot = currentRow * 9 + column;

                // Move to next row if we've filled this one
                if (column == 7) {
                    currentRow++;
                }

                inventory.setItem(slot, createItem(material, name, lore.toArray(new String[0])));
            }
        }
    }

    @Override
    public boolean handleClick(Player player, int slot) {
        Raven raven = getRaven(player);

        // Handle back button
        if (slot == 49) {
            plugin.getGUIManager().openMainGUI(player);
            return true;
        }

        // If player doesn't have a raven, only handle back button
        if (raven == null) {
            return false;
        }

        // Handle tier selection buttons (top row)
        if (slot >= 0 && slot < 5) {
            RavenTier clickedTier = RavenTier.values()[slot];

            // Only allow clicking tiers player has unlocked
            if (raven.getTier().ordinal() >= clickedTier.ordinal()) {
                currentDisplayTier = clickedTier;
                update(player);
                return true;
            }
            return false;
        }

        // Handle ability clicks
        if (slot > 9 && slot < 45) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

                // Check if the ability is unlocked (green name)
                if (displayName.contains("(Locked)")) {
                    // Ability is locked
                    player.sendMessage(ChatColor.RED + "You haven't unlocked this ability yet!");
                    return true;
                }

                // Try to use the ability
                RavenAbility ability = getAbilityByDisplayName(displayName);
                if (ability != null && raven.hasAbility(ability.getName())) {
                    useAbility(player, raven, ability);
                    return true;
                }
            }
        }

        return false;
    }

    private void useAbility(Player player, Raven raven, RavenAbility ability) {
        // This would contain the logic for using abilities
        // For now, just send a message
        player.sendMessage(ChatColor.GREEN + "Used the " + formatAbilityName(ability.getName()) + " ability!");
        player.closeInventory();

        // Here we'd implement the actual ability functionality
        switch (ability) {
            case ILLUMINATION:
                // Give player night vision
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Your raven illuminates the area around you!");
                break;

            case BASIC_FLIGHT:
            case ENHANCED_FLIGHT:
            case ADVANCED_FLIGHT:
            case UNLIMITED_FLIGHT:
                // Enable flight for the player
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Your raven grants you the ability to fly!");
                break;

            case ENEMY_DETECTION:
                // Detect nearby enemies
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Your raven scans for nearby enemies...");
                break;

            // Additional ability implementations would go here
        }
    }

    private RavenAbility getAbilityByDisplayName(String displayName) {
        for (RavenAbility ability : RavenAbility.values()) {
            if (displayName.equals(formatAbilityName(ability.getName()))) {
                return ability;
            }
        }
        return null;
    }

    private String formatAbilityName(String abilityName) {
        // Convert snake_case to Title Case
        String[] words = abilityName.split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                builder.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return builder.toString().trim();
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
}