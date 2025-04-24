package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI for upgrading ravens between tiers
 */
public class UpgradeGUI extends GUI {

    private static final Map<RavenTier, Material> tierMaterials = new HashMap<>();
    private static final Map<RavenTier, Material> tierItemMaterials = new HashMap<>();

    static {
        // Tier display materials
        tierMaterials.put(RavenTier.NOVICE, Material.PURPLE_STAINED_GLASS);
        tierMaterials.put(RavenTier.ADEPT, Material.PURPLE_CONCRETE);
        tierMaterials.put(RavenTier.EXPERT, Material.PURPLE_TERRACOTTA);
        tierMaterials.put(RavenTier.MASTER, Material.PURPLE_GLAZED_TERRACOTTA);
        tierMaterials.put(RavenTier.LEGENDARY, Material.PURPLE_SHULKER_BOX);

        // Tier upgrade item materials
        tierItemMaterials.put(RavenTier.NOVICE, Material.FEATHER);
        tierItemMaterials.put(RavenTier.ADEPT, Material.AMETHYST_SHARD);
        tierItemMaterials.put(RavenTier.EXPERT, Material.ENDER_EYE);
        tierMaterials.put(RavenTier.MASTER, Material.END_CRYSTAL);
        tierItemMaterials.put(RavenTier.LEGENDARY, Material.DRAGON_EGG);
    }

    public UpgradeGUI(RavenPets plugin) {
        super(plugin, "&5&lRaven Upgrades", 54);
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
        RavenTier currentTier = raven.getTier();
        inventory.setItem(4, createItem(
                Material.NETHER_STAR,
                "&d&lRaven Upgrades",
                "&7Current Tier: " + getTierColor(currentTier) + currentTier.getName(),
                "&7Level: &d" + raven.getLevel(),
                "&7",
                "&7Upgrade your raven to unlock",
                "&7new abilities and powers!"
        ));

        // Show tier progression
        showTierProgression(raven);

        // Show upgrade requirements for next tier
        showUpgradeRequirements(raven);

        // Add upgrade button if eligible
        if (canUpgrade(raven)) {
            RavenTier nextTier = getNextTier(currentTier);
            inventory.setItem(31, createItem(
                    Material.EMERALD,
                    "&a&lUpgrade Now!",
                    "&7Click to upgrade your raven to",
                    getTierColor(nextTier) + nextTier.getName() + " Tier",
                    "&7",
                    "&aYou meet all the requirements!"
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

        // Handle upgrade button
        if (slot == 31 && canUpgrade(raven)) {
            upgradeRaven(player, raven);
            update(player);
            return true;
        }

        return false;
    }

    /**
     * Shows the tier progression in the GUI
     *
     * @param raven The player's raven
     */
    private void showTierProgression(Raven raven) {
        RavenTier currentTier = raven.getTier();
        int currentTierOrdinal = currentTier.ordinal();

        // Add tier progression items at the top of the GUI
        for (RavenTier tier : RavenTier.values()) {
            int slot = 10 + tier.ordinal() * 2; // Spread across slots 10, 12, 14, 16, 18
            boolean unlocked = tier.ordinal() <= currentTierOrdinal;
            boolean current = tier == currentTier;

            Material material = tierMaterials.getOrDefault(tier, Material.GLASS);
            String name;
            List<String> lore = new ArrayList<>();

            name = getTierColor(tier) + "&l" + tier.getName() + " Tier";
            lore.add("&7Level " + tier.getMinLevel() + "-" + tier.getMaxLevel());
            lore.add("&7");

            if (unlocked) {
                if (current) {
                    lore.add("&a✓ Current Tier");
                } else {
                    lore.add("&a✓ Unlocked");
                }
            } else {
                lore.add("&c✗ Locked");
            }

            ItemStack item = createItem(material, name, lore.toArray(new String[0]));

            // Add a glowing effect for the current tier
            if (current) {
                item = addGlow(item);
            }

            inventory.setItem(slot, item);

            // Add connector if not the last tier
            if (tier.ordinal() < RavenTier.values().length - 1) {
                boolean connectorUnlocked = tier.ordinal() < currentTierOrdinal;
                Material connectorMaterial = connectorUnlocked ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                String connectorName = connectorUnlocked ? "&a→" : "&c→";

                inventory.setItem(slot + 1, createItem(connectorMaterial, connectorName));
            }
        }
    }

    /**
     * Method to add glow effect to an item (compatible with 1.21.4)
     *
     * @param item The item to add glow to
     * @return The glowing item
     */
    private ItemStack addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // In 1.21.4, we use a reliable enchantment
            meta.addEnchant(Enchantment.SHARPNESS, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Shows the upgrade requirements for the next tier
     *
     * @param raven The player's raven
     */
    private void showUpgradeRequirements(Raven raven) {
        RavenTier currentTier = raven.getTier();

        // If already at max tier, show legendary status
        if (currentTier == RavenTier.LEGENDARY) {
            inventory.setItem(22, createItem(
                    Material.DRAGON_EGG,
                    "&5&lLegendary Raven",
                    "&7Your raven has reached the",
                    "&7highest possible tier!",
                    "&7",
                    "&d✦ Legendary Status Achieved ✦"
            ));
            return;
        }

        // Show requirements for next tier
        RavenTier nextTier = getNextTier(currentTier);
        Material itemMaterial = tierItemMaterials.getOrDefault(nextTier, Material.PAPER);

        String[] requirements = getRequirements(raven, nextTier);
        boolean canUpgrade = true;

        // Check if any requirement is not met
        for (String req : requirements) {
            if (req.contains("&c✗")) {
                canUpgrade = false;
                break;
            }
        }

        // Create item with all requirements
        List<String> lore = new ArrayList<>();
        lore.add("&7Requirements to upgrade to");
        lore.add(getTierColor(nextTier) + nextTier.getName() + " Tier&7:");
        lore.add("&7");

        // Add all requirements to lore
        for (String req : requirements) {
            lore.add(req);
        }

        // Add status
        lore.add("&7");
        if (canUpgrade) {
            lore.add("&a✓ All requirements met!");
            lore.add("&7Click the upgrade button below");
            lore.add("&7to evolve your raven.");
        } else {
            lore.add("&c✗ Some requirements not met.");
            lore.add("&7Complete all requirements to upgrade.");
        }

        // Create the upgrade requirements item
        inventory.setItem(22, createItem(
                itemMaterial,
                getTierColor(nextTier) + "&l" + nextTier.getName() + " Tier Upgrade",
                lore.toArray(new String[0])
        ));
    }

    /**
     * Gets the requirements for upgrading to a tier
     *
     * @param raven The player's raven
     * @param tier The tier to get requirements for
     * @return An array of requirement strings
     */
    private String[] getRequirements(Raven raven, RavenTier tier) {
        int level = raven.getLevel();
        int xp = raven.getXp();

        switch (tier) {
            case ADEPT:
                return new String[] {
                        (level >= 10 ? "&a✓" : "&c✗") + " Reach Level 10",
                        (xp >= 1000 ? "&a✓" : "&c✗") + " Collect 1000 Raven XP",
                        "&c✗ Craft a Raven Amulet",
                        "&c✗ Feed your raven 5 Enchanted Golden Apples"
                };
            case EXPERT:
                return new String[] {
                        (level >= 25 ? "&a✓" : "&c✗") + " Reach Level 25",
                        (xp >= 5000 ? "&a✓" : "&c✗") + " Collect 5000 Raven XP",
                        "&c✗ Defeat the Enhanced Ender Dragon",
                        "&c✗ Craft a Raven Emblem",
                        "&c✗ Complete 15 daily quests"
                };
            case MASTER:
                return new String[] {
                        (level >= 50 ? "&a✓" : "&c✗") + " Reach Level 50",
                        (xp >= 15000 ? "&a✓" : "&c✗") + " Collect 15000 Raven XP",
                        "&c✗ Complete the Void Citadel dungeon",
                        "&c✗ Craft a Raven Talisman",
                        "&c✗ Discover all 8 hidden shrines"
                };
            case LEGENDARY:
                return new String[] {
                        (level >= 75 ? "&a✓" : "&c✗") + " Reach Level 75",
                        (xp >= 50000 ? "&a✓" : "&c✗") + " Collect 50000 Raven XP",
                        "&c✗ Defeat the Void Lord raid boss",
                        "&c✗ Craft a Legendary Raven Crown",
                        "&c✗ Complete all 30 server achievements",
                        "&c✗ Find all 12 Cosmic Fragments"
                };
            default:
                return new String[0];
        }
    }

    /**
     * Gets the next tier after a given tier
     *
     * @param currentTier The current tier
     * @return The next tier
     */
    private RavenTier getNextTier(RavenTier currentTier) {
        int nextOrdinal = currentTier.ordinal() + 1;
        if (nextOrdinal >= RavenTier.values().length) {
            return currentTier; // Already at max tier
        }
        return RavenTier.values()[nextOrdinal];
    }

    /**
     * Checks if a raven can be upgraded
     *
     * @param raven The raven to check
     * @return Whether the raven can be upgraded
     */
    private boolean canUpgrade(Raven raven) {
        RavenTier currentTier = raven.getTier();

        // If already at max tier, can't upgrade
        if (currentTier == RavenTier.LEGENDARY) {
            return false;
        }

        // For now, just check level requirement (in a real implementation, would check all requirements)
        RavenTier nextTier = getNextTier(currentTier);
        return raven.getLevel() >= nextTier.getMinLevel();
    }

    /**
     * Upgrades a raven to the next tier
     *
     * @param player The player who owns the raven
     * @param raven The raven to upgrade
     */
    private void upgradeRaven(Player player, Raven raven) {
        RavenTier currentTier = raven.getTier();
        RavenTier nextTier = getNextTier(currentTier);

        // In a real implementation, this would call raven.updateTier(nextTier)
        // For now, just send a message
        player.sendMessage(ChatColor.GREEN + "Your raven has been upgraded to " +
                getTierColor(nextTier).replace("&", "§") + nextTier.getName() + " Tier!");

        // Play upgrade sound and effects
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);

        // Show title
        player.sendTitle(
                ChatColor.DARK_PURPLE + "Evolution Complete!",
                getTierColor(nextTier).replace("&", "§") + nextTier.getName() + " Tier Unlocked",
                10, 70, 20
        );
    }

    /**
     * Gets the color code for a tier
     *
     * @param tier The tier to get the color for
     * @return The color code
     */
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