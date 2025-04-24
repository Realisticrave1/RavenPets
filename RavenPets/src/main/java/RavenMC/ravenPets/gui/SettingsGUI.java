package RavenMC.ravenPets.gui;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * GUI for configuring raven settings
 */
public class SettingsGUI extends GUI {

    private static final NamespacedKey SETTING_KEY = new NamespacedKey("ravenpets", "setting_id");

    public SettingsGUI(RavenPets plugin) {
        super(plugin, "&5&lRaven Settings", 45);
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
            inventory.setItem(40, createItem(
                    Material.ARROW,
                    "&c&l← Back",
                    "&7Return to the main menu"
            ));

            return;
        }

        // Add title
        inventory.setItem(4, createItem(
                Material.REDSTONE_TORCH,
                "&d&lRaven Settings",
                "&7Configure your raven's behavior",
                "&7and appearance."
        ));

        // Add settings

        // Name setting
        inventory.setItem(10, createSettingItem(
                "name",
                Material.NAME_TAG,
                "&d&lRename Raven",
                "&7Current name: &f" + raven.getName(),
                "&7",
                "&eClick to rename your raven"
        ));

        // Auto-summon setting
        boolean autoSummon = getAutoSummonSetting(player.getUniqueId());
        inventory.setItem(12, createSettingItem(
                "auto_summon",
                autoSummon ? Material.LIME_DYE : Material.GRAY_DYE,
                "&d&lAuto-Summon",
                "&7Automatically summon your raven",
                "&7when you join the server",
                "&7",
                autoSummon ? "&aEnabled" : "&cDisabled",
                "&7",
                "&eClick to toggle"
        ));

        // Visibility setting
        boolean visible = getVisibilitySetting(player.getUniqueId());
        inventory.setItem(14, createSettingItem(
                "visibility",
                visible ? Material.ENDER_EYE : Material.ENDER_PEARL,
                "&d&lRaven Visibility",
                "&7Toggle whether your raven is",
                "&7visible to other players",
                "&7",
                visible ? "&aVisible to others" : "&cInvisible to others",
                "&7",
                "&eClick to toggle"
        ));

        // Particle effects setting
        boolean particles = getParticlesSetting(player.getUniqueId());
        inventory.setItem(16, createSettingItem(
                "particles",
                particles ? Material.BLAZE_POWDER : Material.GUNPOWDER,
                "&d&lParticle Effects",
                "&7Toggle special particle effects",
                "&7for your raven",
                "&7",
                particles ? "&aEnabled" : "&cDisabled",
                "&7",
                "&eClick to toggle"
        ));

        // Sound effects setting
        boolean sounds = getSoundsSetting(player.getUniqueId());
        inventory.setItem(28, createSettingItem(
                "sounds",
                sounds ? Material.NOTE_BLOCK : Material.BARRIER,
                "&d&lSound Effects",
                "&7Toggle sound effects from",
                "&7your raven's abilities",
                "&7",
                sounds ? "&aEnabled" : "&cDisabled",
                "&7",
                "&eClick to toggle"
        ));

        // Notification setting
        boolean notifications = getNotificationsSetting(player.getUniqueId());
        inventory.setItem(30, createSettingItem(
                "notifications",
                notifications ? Material.WRITABLE_BOOK : Material.BOOK,
                "&d&lNotifications",
                "&7Toggle ability notifications",
                "&7in chat",
                "&7",
                notifications ? "&aEnabled" : "&cDisabled",
                "&7",
                "&eClick to toggle"
        ));

        // Appearance setting (only for higher tiers)
        if (raven.getTier().ordinal() >= RavenTier.EXPERT.ordinal()) {
            inventory.setItem(32, createSettingItem(
                    "appearance",
                    Material.DRAGON_HEAD,
                    "&d&lAppearance",
                    "&7Customize your raven's appearance",
                    "&7with special effects",
                    "&7",
                    "&eClick to open appearance menu"
            ));
        }

        // Reset settings
        inventory.setItem(34, createSettingItem(
                "reset",
                Material.REDSTONE,
                "&c&lReset Settings",
                "&7Reset all settings to default",
                "&7",
                "&cWarning: This cannot be undone!",
                "&7",
                "&eClick to reset"
        ));

        // Add navigation buttons
        inventory.setItem(40, createItem(
                Material.ARROW,
                "&c&l← Back",
                "&7Return to the main menu"
        ));
    }

    @Override
    public boolean handleClick(Player player, int slot) {
        // Handle back button
        if (slot == 40) {
            plugin.getGUIManager().openMainGUI(player);
            return true;
        }

        Raven raven = getRaven(player);
        if (raven == null) {
            return false;
        }

        // Get the clicked item
        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null) {
            return false;
        }

        // Check if it's a setting item
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String settingId = container.get(SETTING_KEY, PersistentDataType.STRING);

        if (settingId != null) {
            // Handle the setting
            handleSettingClick(player, raven, settingId);
            return true;
        }

        return false;
    }

    /**
     * Creates a setting item with the setting ID stored in it
     *
     * @param settingId The ID of the setting
     * @param material The material of the item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The setting item
     */
    private ItemStack createSettingItem(String settingId, Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();

        // Store the setting ID in the item
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(SETTING_KEY, PersistentDataType.STRING, settingId);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Handles a click on a setting
     *
     * @param player The player who clicked
     * @param raven The player's raven
     * @param settingId The ID of the setting that was clicked
     */
    private void handleSettingClick(Player player, Raven raven, String settingId) {
        UUID playerId = player.getUniqueId();

        switch (settingId) {
            case "name":
                // Close inventory and prompt for new name
                player.closeInventory();
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Enter a new name for your raven in chat:");
                // In a real implementation, you would register a chat listener here
                break;

            case "auto_summon":
                // Toggle auto-summon setting
                boolean autoSummon = getAutoSummonSetting(playerId);
                setAutoSummonSetting(playerId, !autoSummon);
                player.sendMessage(ChatColor.GREEN + "Auto-summon " + (!autoSummon ? "enabled" : "disabled") + ".");
                update(player);
                break;

            case "visibility":
                // Toggle visibility setting
                boolean visible = getVisibilitySetting(playerId);
                setVisibilitySetting(playerId, !visible);
                player.sendMessage(ChatColor.GREEN + "Raven is now " + (!visible ? "visible" : "invisible") + " to other players.");
                update(player);
                break;

            case "particles":
                // Toggle particles setting
                boolean particles = getParticlesSetting(playerId);
                setParticlesSetting(playerId, !particles);
                player.sendMessage(ChatColor.GREEN + "Particle effects " + (!particles ? "enabled" : "disabled") + ".");
                update(player);
                break;

            case "sounds":
                // Toggle sounds setting
                boolean sounds = getSoundsSetting(playerId);
                setSoundsSetting(playerId, !sounds);
                player.sendMessage(ChatColor.GREEN + "Sound effects " + (!sounds ? "enabled" : "disabled") + ".");
                update(player);
                break;

            case "notifications":
                // Toggle notifications setting
                boolean notifications = getNotificationsSetting(playerId);
                setNotificationsSetting(playerId, !notifications);
                player.sendMessage(ChatColor.GREEN + "Notifications " + (!notifications ? "enabled" : "disabled") + ".");
                update(player);
                break;

            case "appearance":
                // Open appearance menu (would be implemented in a real plugin)
                player.sendMessage(ChatColor.YELLOW + "Appearance menu not implemented yet.");
                break;

            case "reset":
                // Reset all settings to default
                resetSettings(playerId);
                player.sendMessage(ChatColor.GREEN + "All settings reset to default.");
                update(player);
                break;
        }
    }

    // Settings storage methods
    // In a real implementation, these would save to a database or config file

    private static final List<UUID> autoSummonEnabled = new ArrayList<>();
    private static final List<UUID> visibilityDisabled = new ArrayList<>();
    private static final List<UUID> particlesDisabled = new ArrayList<>();
    private static final List<UUID> soundsDisabled = new ArrayList<>();
    private static final List<UUID> notificationsDisabled = new ArrayList<>();

    private boolean getAutoSummonSetting(UUID playerId) {
        return autoSummonEnabled.contains(playerId);
    }

    private void setAutoSummonSetting(UUID playerId, boolean enabled) {
        if (enabled) {
            if (!autoSummonEnabled.contains(playerId)) {
                autoSummonEnabled.add(playerId);
            }
        } else {
            autoSummonEnabled.remove(playerId);
        }
    }

    private boolean getVisibilitySetting(UUID playerId) {
        return !visibilityDisabled.contains(playerId);
    }

    private void setVisibilitySetting(UUID playerId, boolean visible) {
        if (visible) {
            visibilityDisabled.remove(playerId);
        } else {
            if (!visibilityDisabled.contains(playerId)) {
                visibilityDisabled.add(playerId);
            }
        }
    }

    private boolean getParticlesSetting(UUID playerId) {
        return !particlesDisabled.contains(playerId);
    }

    private void setParticlesSetting(UUID playerId, boolean enabled) {
        if (enabled) {
            particlesDisabled.remove(playerId);
        } else {
            if (!particlesDisabled.contains(playerId)) {
                particlesDisabled.add(playerId);
            }
        }
    }

    private boolean getSoundsSetting(UUID playerId) {
        return !soundsDisabled.contains(playerId);
    }

    private void setSoundsSetting(UUID playerId, boolean enabled) {
        if (enabled) {
            soundsDisabled.remove(playerId);
        } else {
            if (!soundsDisabled.contains(playerId)) {
                soundsDisabled.add(playerId);
            }
        }
    }

    private boolean getNotificationsSetting(UUID playerId) {
        return !notificationsDisabled.contains(playerId);
    }

    private void setNotificationsSetting(UUID playerId, boolean enabled) {
        if (enabled) {
            notificationsDisabled.remove(playerId);
        } else {
            if (!notificationsDisabled.contains(playerId)) {
                notificationsDisabled.add(playerId);
            }
        }
    }

    private void resetSettings(UUID playerId) {
        autoSummonEnabled.remove(playerId);
        visibilityDisabled.remove(playerId);
        particlesDisabled.remove(playerId);
        soundsDisabled.remove(playerId);
        notificationsDisabled.remove(playerId);
    }
}