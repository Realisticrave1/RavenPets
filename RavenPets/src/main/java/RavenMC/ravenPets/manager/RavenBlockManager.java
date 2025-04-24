package RavenMC.ravenPets.manager;

import RavenMC.ravenPets.RavenPets;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * Manager for handling Raven Pet blocks
 */
public class RavenBlockManager {

    private final RavenPets plugin;
    private final NamespacedKey ravenBlockKey;

    public RavenBlockManager(RavenPets plugin) {
        this.plugin = plugin;
        this.ravenBlockKey = new NamespacedKey(plugin, "raven_block");
    }

    /**
     * Creates a raven pet block item for a specific tier
     *
     * @param tier The tier of the raven (1-5)
     * @return The raven block item
     */
    public ItemStack createRavenBlock(int tier) {
        // Select the appropriate material based on the tier
        Material material;
        String tierName;
        String colorCode;

        switch (tier) {
            case 1: // Novice
                material = Material.PURPLE_CONCRETE;
                tierName = "Novice";
                colorCode = "§d"; // Light purple
                break;
            case 2: // Adept
                material = Material.PURPLE_TERRACOTTA;
                tierName = "Adept";
                colorCode = "§5"; // Dark purple
                break;
            case 3: // Expert
                material = Material.PURPLE_WOOL;
                tierName = "Expert";
                colorCode = "§5§l"; // Bold dark purple
                break;
            case 4: // Master
                material = Material.PURPLE_GLAZED_TERRACOTTA;
                tierName = "Master";
                colorCode = "§5§l"; // Bold dark purple
                break;
            case 5: // Legendary
                material = Material.PURPUR_BLOCK;
                tierName = "Legendary";
                colorCode = "§5§l§o"; // Bold italic dark purple
                break;
            default:
                material = Material.PURPLE_CONCRETE;
                tierName = "Unknown";
                colorCode = "§7"; // Gray
                break;
        }

        // Create the item stack
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set display name
            meta.setDisplayName(colorCode + tierName + " Raven");

            // Set lore
            meta.setLore(Arrays.asList(
                    "§7" + getRavenDescription(tier),
                    "§7",
                    "§7Place this block to summon",
                    "§7your " + tierName + " Raven companion!",
                    "§7",
                    colorCode + "Tier " + tier + " Raven Pet"
            ));

            // Store the raven tier in persistent data
            meta.getPersistentDataContainer().set(
                    ravenBlockKey,
                    PersistentDataType.INTEGER,
                    tier
            );

            // Add enchantment glow
            meta.addEnchant(Enchantment.PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // Apply the metadata to the item
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Gets a description for a specific raven tier
     *
     * @param tier The tier of the raven
     * @return The description text
     */
    private String getRavenDescription(int tier) {
        switch (tier) {
            case 1: // Novice
                return "A small, purple-tinged raven that follows you around.";
            case 2: // Adept
                return "A larger raven with basic magical abilities.";
            case 3: // Expert
                return "A raven with glowing eyes and combat assistance.";
            case 4: // Master
                return "A majestic raven with powerful magical abilities.";
            case 5: // Legendary
                return "A mythical raven with reality-altering powers.";
            default:
                return "A mysterious raven companion.";
        }
    }

    /**
     * Checks if an item is a raven block
     *
     * @param item The item to check
     * @return Whether the item is a raven block
     */
    public boolean isRavenBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(ravenBlockKey, PersistentDataType.INTEGER);
    }

    /**
     * Gets the tier of a raven block
     *
     * @param item The raven block item
     * @return The tier of the raven block, or 0 if not a raven block
     */
    public int getRavenBlockTier(ItemStack item) {
        if (!isRavenBlock(item)) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }

        return meta.getPersistentDataContainer().getOrDefault(
                ravenBlockKey,
                PersistentDataType.INTEGER,
                0
        );
    }
}