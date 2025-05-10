package RavenMC.ravenPets;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RavenMainGUI implements Listener {
    private final RavenPets plugin;

    public RavenMainGUI(RavenPets plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§5§l⚔ Raven Dashboard ⚔");

        // Fill with glass pane background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Add borders with purple glass
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(gui.getSize() - 9 + i, border); // Bottom row
        }
        for (int i = 1; i < 5; i++) {
            gui.setItem(i * 9, border); // Left column
            gui.setItem(i * 9 + 8, border); // Right column
        }

        // Get player's raven
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Raven Display (Center)
        ItemStack ravenDisplay = createGuiItem(
                getRavenMaterial(raven),
                "§d§l" + raven.getName(),
                "§5§m                    ",
                "§5▸ §dTier: " + formatTier(raven.getTier()),
                "§5▸ §dLevel: §f" + raven.getLevel(),
                "§5▸ §dElement: §f" + formatElement(raven.getElementType()),
                "§5▸ §dXP: §f" + raven.getExperience() + "§7/§f" + getRequiredExperienceForNextLevel(raven),
                "§5§m                    ",
                "§8» Your loyal companion"
        );
        gui.setItem(22, ravenDisplay);

        // Progress bar
        createProgressBar(gui, raven, 28, 34);

        // Raven Controls Section
        if (raven.isSpawned()) {
            ItemStack despawnButton = createGuiItem(
                    Material.BARRIER,
                    "§c§lDespawn Raven",
                    "§7Send your raven back to its realm",
                    "",
                    "§8» Click to despawn"
            );
            gui.setItem(38, despawnButton);
        } else {
            ItemStack spawnButton = createGuiItem(
                    Material.DRAGON_EGG,
                    "§a§lSpawn Raven",
                    "§7Summon your loyal companion",
                    "",
                    "§8» Click to spawn"
            );
            gui.setItem(38, spawnButton);
        }

        // Raven Core
        ItemStack coreButton = createGuiItem(
                Material.NETHER_STAR,
                "§d§lRaven Core",
                "§7Receive your raven ability item",
                "",
                "§8» Click to obtain"
        );
        gui.setItem(40, coreButton);

        // Rename
        ItemStack renameButton = createGuiItem(
                Material.NAME_TAG,
                "§e§lRename Raven",
                "§7Change your raven's name",
                "",
                "§8» Click to rename"
        );
        gui.setItem(42, renameButton);

        // Ability Information
        RavenAbility ability = plugin.getAbilityManager().getAbility(raven.getElementType());
        if (ability != null) {
            // Primary ability
            ItemStack primaryAbility = createGuiItem(
                    getAbilityMaterial(raven.getElementType(), false),
                    getElementColor(raven.getElementType()) + "§l" + ability.getName(),
                    "§7" + ability.getDescription(),
                    "",
                    "§5▸ §dDuration: §f" + formatDuration(ability.getDurationByTier(raven.getTier())),
                    "§5▸ §dStrength: §f" + getTierStars(raven.getTier()),
                    "",
                    "§8» Right-click your Raven Core to use"
            );
            gui.setItem(11, primaryAbility);

            // Secondary ability
            ItemStack secondaryAbility = createGuiItem(
                    getAbilityMaterial(raven.getElementType(), true),
                    getElementColor(raven.getElementType()) + "§l" + ability.getSecondaryName(),
                    "§7" + ability.getSecondaryDescription(),
                    "",
                    "§5▸ §dPower: §f" + getTierStars(raven.getTier()),
                    "",
                    "§8» Shift + Right-click your Raven Core to use"
            );
            gui.setItem(15, secondaryAbility);
        }

        // Shop Button
        ItemStack shopButton = createGuiItem(
                Material.EMERALD,
                "§a§lRaven Shop",
                "§7Purchase upgrades and boosts",
                "",
                "§5▸ §dRaven Coins: §e" + plugin.getCoinManager().getCoins(player),
                "",
                "§8» Click to browse"
        );
        gui.setItem(49, shopButton);

        player.openInventory(gui);
    }

    private Material getRavenMaterial(PlayerRaven raven) {
        if (raven.hasCustomColors()) {
            switch (raven.getElementType()) {
                case FIRE: return Material.MAGMA_BLOCK;
                case WATER: return Material.PRISMARINE;
                case EARTH: return Material.EMERALD_BLOCK;
                case AIR: return Material.WHITE_CONCRETE;
                case LIGHTNING: return Material.GOLD_BLOCK;
                case ICE: return Material.BLUE_ICE;
                case NATURE: return Material.MOSS_BLOCK;
                case DARKNESS: return Material.COAL_BLOCK;
                case LIGHT: return Material.GLOWSTONE;
                default: return Material.AMETHYST_BLOCK;
            }
        } else {
            switch (raven.getTier()) {
                case NOVICE: return Material.PURPLE_CONCRETE;
                case ADEPT: return Material.PURPLE_TERRACOTTA;
                case EXPERT: return Material.PURPLE_GLAZED_TERRACOTTA;
                case MASTER: return Material.PURPUR_BLOCK;
                case LEGENDARY: return Material.AMETHYST_BLOCK;
                default: return Material.PURPLE_CONCRETE;
            }
        }
    }

    private Material getAbilityMaterial(RavenElementType elementType, boolean isSecondary) {
        if (isSecondary) {
            // Secondary abilities use different materials
            switch (elementType) {
                case FIRE: return Material.FIRE_CHARGE;
                case WATER: return Material.HEART_OF_THE_SEA;
                case EARTH: return Material.TERRACOTTA;
                case AIR: return Material.PHANTOM_MEMBRANE;
                case LIGHTNING: return Material.LIGHTNING_ROD;
                case ICE: return Material.PACKED_ICE;
                case NATURE: return Material.GLOW_BERRIES;
                case DARKNESS: return Material.WITHER_ROSE;
                case LIGHT: return Material.SUNFLOWER;
                default: return Material.NETHER_STAR;
            }
        } else {
            // Primary abilities
            switch (elementType) {
                case FIRE: return Material.BLAZE_POWDER;
                case WATER: return Material.PRISMARINE_CRYSTALS;
                case EARTH: return Material.EMERALD;
                case AIR: return Material.FEATHER;
                case LIGHTNING: return Material.NETHER_STAR;
                case ICE: return Material.BLUE_ICE;
                case NATURE: return Material.OAK_SAPLING;
                case DARKNESS: return Material.OBSIDIAN;
                case LIGHT: return Material.GLOWSTONE_DUST;
                default: return Material.NETHER_STAR;
            }
        }
    }

    private String getElementColor(RavenElementType elementType) {
        switch (elementType) {
            case FIRE: return "§c"; // Red
            case WATER: return "§9"; // Blue
            case EARTH: return "§2"; // Dark Green
            case AIR: return "§f"; // White
            case LIGHTNING: return "§e"; // Yellow
            case ICE: return "§b"; // Aqua
            case NATURE: return "§a"; // Light Green
            case DARKNESS: return "§8"; // Dark Gray
            case LIGHT: return "§e"; // Yellow
            default: return "§7"; // Gray
        }
    }

    private String formatElement(RavenElementType elementType) {
        String color = getElementColor(elementType);
        String name = elementType.name();
        return color + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private String formatTier(RavenTier tier) {
        String name = tier.name();
        String formatted = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        switch (tier) {
            case NOVICE: return "§d" + formatted;
            case ADEPT: return "§5" + formatted;
            case EXPERT: return "§5§l" + formatted;
            case MASTER: return "§d§l" + formatted;
            case LEGENDARY: return "§5§l§n" + formatted;
            default: return "§d" + formatted;
        }
    }

    private String formatDuration(int durationTicks) {
        int seconds = durationTicks / 20;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + (remainingSeconds > 0 ? remainingSeconds + "s" : "");
        }
    }

    private String getTierStars(RavenTier tier) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i <= tier.ordinal(); i++) {
            stars.append("★");
        }
        int remaining = 4 - tier.ordinal();
        for (int i = 0; i < remaining; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    private int getRequiredExperienceForNextLevel(PlayerRaven raven) {
        return 100 + (raven.getLevel() * 50);
    }

    private void createProgressBar(Inventory gui, PlayerRaven raven, int startSlot, int endSlot) {
        int barLength = (endSlot - startSlot) + 1;
        double percentage = (double) raven.getExperience() / getRequiredExperienceForNextLevel(raven);
        int filledSlots = (int) Math.ceil(percentage * barLength);

        for (int i = 0; i < barLength; i++) {
            Material material;
            String name;

            if (i < filledSlots) {
                material = Material.MAGENTA_STAINED_GLASS_PANE;
                name = "§d◆ XP Progress §d◆";
            } else {
                material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
                name = "§8◇ XP Needed §8◇";
            }

            ItemStack item = createGuiItem(material, name);
            gui.setItem(startSlot + i, item);
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals("§5§l⚔ Raven Dashboard ⚔")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // Handle clicks
        switch (event.getSlot()) {
            case 38: // Spawn/Despawn
                if (plugin.getRavenManager().hasRaven(player)) {
                    plugin.getRavenManager().despawnRaven(player);
                    player.sendMessage("§d§lYour raven has been despawned!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.7f);
                } else {
                    plugin.getRavenManager().spawnRaven(player);
                    player.sendMessage("§d§lYour raven has been spawned!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
                }
                player.closeInventory();
                break;

            case 40: // Raven Core
                ItemStack core = plugin.getAbilityManager().createRavenCore(player);
                player.getInventory().addItem(core);
                player.sendMessage("§d§lYou have received your raven core!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                player.closeInventory();
                break;

            case 42: // Rename
                player.closeInventory();
                player.sendMessage("§d§lType your desired raven name in chat:");
                // Implement chat listener for name input
                // This would need a separate class to handle the chat input
                break;

            case 49: // Shop
                player.closeInventory();
                plugin.getShopGUI().openShop(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                break;
        }
    }
}