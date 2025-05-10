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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RavenPlayerGUI implements Listener {
    private final RavenPets plugin;
    private final Map<UUID, String> currentMenu = new HashMap<>();

    private static final String MAIN_MENU = "main";
    private static final String ABILITIES_MENU = "abilities";
    private static final String STATS_MENU = "stats";
    private static final String CUSTOMIZATION_MENU = "customization";

    public RavenPlayerGUI(RavenPets plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, "§5§l⚜ Raven Menu ⚜");
        currentMenu.put(player.getUniqueId(), MAIN_MENU);

        // Fill background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Create purple border
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(36 + i, border); // Bottom row
        }
        for (int i = 1; i < 4; i++) {
            gui.setItem(i * 9, border); // Left column
            gui.setItem(i * 9 + 8, border); // Right column
        }

        // Get the player's raven
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Raven information (center)
        ItemStack ravenItem = getRavenDisplayItem(player);
        gui.setItem(13, ravenItem);

        // Raven abilities button
        ItemStack abilitiesButton = createGuiItem(
                getAbilityMaterial(raven.getElementType()),
                "§d§lRaven Abilities",
                "§7View and manage your raven's abilities",
                "",
                "§8» Click to view abilities"
        );
        gui.setItem(20, abilitiesButton);

        // Raven stats button
        ItemStack statsButton = createGuiItem(
                Material.EXPERIENCE_BOTTLE,
                "§d§lRaven Statistics",
                "§7View detailed statistics about your raven",
                "",
                "§8» Click to view stats"
        );
        gui.setItem(22, statsButton);

        // Customization button
        ItemStack customizationButton = createGuiItem(
                Material.NAME_TAG,
                "§d§lCustomization",
                "§7Customize your raven's appearance",
                "",
                "§8» Click to customize"
        );
        gui.setItem(24, customizationButton);

        // Spawn/Despawn button
        if (raven.isSpawned()) {
            ItemStack despawnButton = createGuiItem(
                    Material.BARRIER,
                    "§c§lDespawn Raven",
                    "§7Send your raven back to its realm",
                    "",
                    "§8» Click to despawn"
            );
            gui.setItem(30, despawnButton);
        } else {
            ItemStack spawnButton = createGuiItem(
                    Material.DRAGON_EGG,
                    "§a§lSpawn Raven",
                    "§7Summon your loyal companion",
                    "",
                    "§8» Click to spawn"
            );
            gui.setItem(30, spawnButton);
        }

        // Get raven core button
        ItemStack coreButton = createGuiItem(
                Material.NETHER_STAR,
                "§d§lRaven Core",
                "§7Obtain your raven ability item",
                "",
                "§8» Click to receive"
        );
        gui.setItem(32, coreButton);

        // Shop button
        ItemStack shopButton = createGuiItem(
                Material.EMERALD,
                "§a§lRaven Shop",
                "§7Spend your coins on upgrades",
                "",
                "§5▸ §dRaven Coins: §e" + plugin.getCoinManager().getCoins(player),
                "",
                "§8» Click to open shop"
        );
        gui.setItem(40, shopButton);

        player.openInventory(gui);
    }

    public void openAbilitiesMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, "§5§l⚜ Raven Abilities ⚜");
        currentMenu.put(player.getUniqueId(), ABILITIES_MENU);

        // Fill background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Create purple border
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(36 + i, border); // Bottom row
        }
        for (int i = 1; i < 4; i++) {
            gui.setItem(i * 9, border); // Left column
            gui.setItem(i * 9 + 8, border); // Right column
        }

        // Get the player's raven
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        RavenElementType elementType = raven.getElementType();
        RavenAbility ability = plugin.getAbilityManager().getAbility(elementType);

        // Element type display (top center)
        ItemStack elementItem = createGuiItem(
                getElementMaterial(elementType),
                getElementColor(elementType) + "§l" + formatElementName(elementType) + " Element",
                "§7Your raven's elemental affinity",
                "",
                "§5▸ §dElement: " + getElementColor(elementType) + formatElementName(elementType),
                "§5▸ §dTier: " + formatTier(raven.getTier()),
                ""
        );
        gui.setItem(4, elementItem);

        // Primary ability (left)
        ItemStack primaryAbilityItem = createGuiItem(
                getAbilityMaterial(elementType),
                getElementColor(elementType) + "§l" + ability.getName(),
                "§7" + ability.getDescription(),
                "",
                "§5▸ §dDuration: §f" + formatDuration(ability.getDurationByTier(raven.getTier())),
                "§5▸ §dTier Bonus: §f" + getTierBonus(raven.getTier(), elementType, false),
                "",
                "§5▸ §dCooldown: §f" + getCooldownText(player, elementType, false),
                "",
                "§8» Right-click your Raven Core to activate"
        );
        gui.setItem(20, primaryAbilityItem);

        // Secondary ability (right)
        ItemStack secondaryAbilityItem = createGuiItem(
                getSecondaryAbilityMaterial(elementType),
                getElementColor(elementType) + "§l" + ability.getSecondaryName(),
                "§7" + ability.getSecondaryDescription(),
                "",
                "§5▸ §dEffect: §f" + getSecondaryEffectText(elementType),
                "§5▸ §dTier Bonus: §f" + getTierBonus(raven.getTier(), elementType, true),
                "",
                "§5▸ §dCooldown: §f" + getCooldownText(player, elementType, true),
                "",
                "§8» Shift + Right-click your Raven Core to activate"
        );
        gui.setItem(24, secondaryAbilityItem);

        // Element description
        ItemStack elementDescriptionItem = createGuiItem(
                Material.BOOK,
                "§d§l" + formatElementName(elementType) + " Element Properties",
                "§7Learn about your element's traits",
                "",
                "§5▸ " + getElementDescription(elementType),
                "",
                "§5▸ §dStrengths: §f" + getElementStrengths(elementType),
                "§5▸ §dWeaknesses: §f" + getElementWeaknesses(elementType)
        );
        gui.setItem(22, elementDescriptionItem);

        // Back button
        ItemStack backButton = createGuiItem(
                Material.ARROW,
                "§f§lBack to Main Menu",
                "§7Return to the main raven menu",
                "",
                "§8» Click to go back"
        );
        gui.setItem(40, backButton);

        player.openInventory(gui);
    }

    public void openStatsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, "§5§l⚜ Raven Statistics ⚜");
        currentMenu.put(player.getUniqueId(), STATS_MENU);

        // Fill background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Create purple border
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(36 + i, border); // Bottom row
        }
        for (int i = 1; i < 4; i++) {
            gui.setItem(i * 9, border); // Left column
            gui.setItem(i * 9 + 8, border); // Right column
        }

        // Get the player's raven
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Player head with raven info
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§d§l" + player.getName() + "'s Raven");

        List<String> headLore = new ArrayList<>();
        headLore.add("§5§m                    ");
        headLore.add("§5▸ §dName: §f" + raven.getName());
        headLore.add("§5▸ §dElement: " + getElementColor(raven.getElementType()) + formatElementName(raven.getElementType()));
        headLore.add("§5▸ §dStatus: " + (raven.isSpawned() ? "§aActive" : "§cInactive"));
        headLore.add("§5§m                    ");

        skullMeta.setLore(headLore);
        playerHead.setItemMeta(skullMeta);
        gui.setItem(4, playerHead);

        // Level and experience stats
        ItemStack levelItem = createGuiItem(
                Material.EXPERIENCE_BOTTLE,
                "§d§lLevel and Experience",
                "§5§m                    ",
                "§5▸ §dCurrent Level: §f" + raven.getLevel(),
                "§5▸ §dTier: " + formatTier(raven.getTier()),
                "§5▸ §dExperience: §f" + raven.getExperience() + "§7/§f" + getRequiredExperienceForNextLevel(raven),
                "§5▸ §dProgress: §f" + calculateProgress(raven) + "%",
                "",
                nextTierInfo(raven),
                "§5§m                    "
        );
        gui.setItem(19, levelItem);

        // Ability stats
        RavenAbility ability = plugin.getAbilityManager().getAbility(raven.getElementType());
        ItemStack abilityStatsItem = createGuiItem(
                getAbilityMaterial(raven.getElementType()),
                "§d§lAbility Information",
                "§5§m                    ",
                "§5▸ §dPrimary Ability: " + getElementColor(raven.getElementType()) + ability.getName(),
                "§5▸ §dSecondary Ability: " + getElementColor(raven.getElementType()) + ability.getSecondaryName(),
                "§5▸ §dPrimary Cooldown: §f" + formatCooldown(plugin.getConfig().getInt("abilities." +
                        raven.getElementType().name().toLowerCase() + ".cooldown")) + "s",
                "§5▸ §dSecondary Cooldown: §f" + formatCooldown(plugin.getConfig().getInt("abilities." +
                        raven.getElementType().name().toLowerCase() + ".secondary-cooldown")) + "s",
                "§5§m                    "
        );
        gui.setItem(21, abilityStatsItem);

        // Economy stats
        ItemStack economyItem = createGuiItem(
                Material.GOLD_INGOT,
                "§d§lEconomy Information",
                "§5§m                    ",
                "§5▸ §dRaven Coins: §e" + plugin.getCoinManager().getCoins(player),
                "§5▸ §dXP Boost: " + (hasXpBoost(player) ? "§aActive" : "§cInactive"),
                "§5▸ §dCoin Boost: " + (hasCoinBoost(player) ? "§aActive" : "§cInactive"),
                "§5§m                    "
        );
        gui.setItem(23, economyItem);

        // Customization stats
        ItemStack customizationItem = createGuiItem(
                Material.PURPLE_DYE,
                "§d§lCustomization Status",
                "§5§m                    ",
                "§5▸ §dCustom Colors: " + (raven.hasCustomColors() ? "§aUnlocked" : "§cLocked"),
                "§5▸ §dCustom Particles: " + (raven.hasCustomParticles() ? "§aUnlocked" : "§cLocked"),
                "§5§m                    "
        );
        gui.setItem(25, customizationItem);

        // Create progress bar in the middle
        createExperienceBar(gui, raven, 28, 34);

        // Back button
        ItemStack backButton = createGuiItem(
                Material.ARROW,
                "§f§lBack to Main Menu",
                "§7Return to the main raven menu",
                "",
                "§8» Click to go back"
        );
        gui.setItem(40, backButton);

        player.openInventory(gui);
    }

    public void openCustomizationMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, "§5§l⚜ Raven Customization ⚜");
        currentMenu.put(player.getUniqueId(), CUSTOMIZATION_MENU);

        // Fill background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Create purple border
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(36 + i, border); // Bottom row
        }
        for (int i = 1; i < 4; i++) {
            gui.setItem(i * 9, border); // Left column
            gui.setItem(i * 9 + 8, border); // Right column
        }

        // Get the player's raven
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Raven name display
        ItemStack nameItem = createGuiItem(
                Material.NAME_TAG,
                "§d§lRaven Name: §f" + raven.getName(),
                "§7Click to rename your raven"
        );
        gui.setItem(4, nameItem);

        // Raven colors (if unlocked or purchasable)
        ItemStack colorsItem;
        if (raven.hasCustomColors()) {
            colorsItem = createGuiItem(
                    Material.PURPLE_DYE,
                    "§d§lElement Colors",
                    "§7Your raven displays element-specific colors",
                    "",
                    "§a§lUNLOCKED",
                    "§7Your raven now shows colors based on its element"
            );
        } else {
            int cost = plugin.getConfig().getInt("shop.custom-colors.cost");
            colorsItem = createGuiItem(
                    Material.GRAY_DYE,
                    "§7§lElement Colors §c§lLOCKED",
                    "§7Make your raven display element-specific colors",
                    "",
                    "§5▸ §dCost: §e" + cost + " Raven Coins",
                    "§5▸ §dYour Balance: §e" + plugin.getCoinManager().getCoins(player),
                    "",
                    plugin.getCoinManager().getCoins(player) >= cost ?
                            "§8» Click to purchase" :
                            "§c§lNot enough coins to purchase"
            );
        }
        gui.setItem(20, colorsItem);

        // Raven particles (if unlocked or purchasable)
        ItemStack particlesItem;
        if (raven.hasCustomParticles()) {
            particlesItem = createGuiItem(
                    Material.BLAZE_POWDER,
                    "§d§lElement Particles",
                    "§7Your raven displays enhanced element particles",
                    "",
                    "§a§lUNLOCKED",
                    "§7Your raven now shows enhanced particle effects"
            );
        } else {
            int cost = plugin.getConfig().getInt("shop.custom-particles.cost");
            particlesItem = createGuiItem(
                    Material.GUNPOWDER,
                    "§7§lElement Particles §c§lLOCKED",
                    "§7Give your raven enhanced particle effects",
                    "",
                    "§5▸ §dCost: §e" + cost + " Raven Coins",
                    "§5▸ §dYour Balance: §e" + plugin.getCoinManager().getCoins(player),
                    "",
                    plugin.getCoinManager().getCoins(player) >= cost ?
                            "§8» Click to purchase" :
                            "§c§lNot enough coins to purchase"
            );
        }
        gui.setItem(24, particlesItem);

        // Back button
        ItemStack backButton = createGuiItem(
                Material.ARROW,
                "§f§lBack to Main Menu",
                "§7Return to the main raven menu",
                "",
                "§8» Click to go back"
        );
        gui.setItem(40, backButton);

        player.openInventory(gui);
    }

    private ItemStack getRavenDisplayItem(Player player) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        Material material = getRavenMaterial(raven);
        String elementColor = getElementColor(raven.getElementType());

        List<String> lore = new ArrayList<>();
        lore.add("§5§m                    ");
        lore.add("§5▸ §dName: §f" + raven.getName());
        lore.add("§5▸ §dElement: " + elementColor + formatElementName(raven.getElementType()));
        lore.add("§5▸ §dTier: " + formatTier(raven.getTier()));
        lore.add("§5▸ §dLevel: §f" + raven.getLevel());
        lore.add("§5▸ §dXP: §f" + raven.getExperience() + "§7/§f" + getRequiredExperienceForNextLevel(raven));
        lore.add("§5▸ §dStatus: " + (raven.isSpawned() ? "§aActive" : "§cInactive"));
        lore.add("§5§m                    ");

        return createGuiItem(material, "§d§l" + raven.getName(), lore.toArray(new String[0]));
    }

    private void createExperienceBar(Inventory gui, PlayerRaven raven, int startSlot, int endSlot) {
        int barLength = (endSlot - startSlot) + 1;
        double progress = (double) raven.getExperience() / getRequiredExperienceForNextLevel(raven);
        int filledSlots = (int) Math.ceil(progress * barLength);

        for (int i = 0; i < barLength; i++) {
            Material material;
            String name;

            if (i < filledSlots) {
                material = Material.MAGENTA_STAINED_GLASS_PANE;
                name = "§d◆ " + (int)(progress * 100) + "% Progress §d◆";
            } else {
                material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
                name = "§8◇ XP Needed §8◇";
            }

            ItemStack item = createGuiItem(material, name);
            gui.setItem(startSlot + i, item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if it's one of our GUIs
        if (!title.startsWith("§5§l⚜")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // Get the current menu the player is in
        String menu = currentMenu.getOrDefault(player.getUniqueId(), MAIN_MENU);
        int slot = event.getSlot();

        // Handle clicks based on current menu
        switch (menu) {
            case MAIN_MENU:
                handleMainMenuClick(player, slot);
                break;
            case ABILITIES_MENU:
                handleAbilitiesMenuClick(player, slot);
                break;
            case STATS_MENU:
                handleStatsMenuClick(player, slot);
                break;
            case CUSTOMIZATION_MENU:
                handleCustomizationMenuClick(player, slot);
                break;
        }
    }

    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 20: // Abilities button
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openAbilitiesMenu(player);
                break;

            case 22: // Stats button
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openStatsMenu(player);
                break;

            case 24: // Customization button
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openCustomizationMenu(player);
                break;

            case 30: // Spawn/Despawn button
                PlayerRaven raven = plugin.getRavenManager().getRaven(player);

                if (raven.isSpawned()) {
                    plugin.getRavenManager().despawnRaven(player);
                    player.sendMessage("§d§lYour raven has been despawned!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.7f);
                } else {
                    plugin.getRavenManager().spawnRaven(player);
                    player.sendMessage("§d§lYour raven has been spawned!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
                }
                player.closeInventory();
                break;

            case 32: // Get raven core
                ItemStack core = plugin.getAbilityManager().createRavenCore(player);
                player.getInventory().addItem(core);
                player.sendMessage("§d§lYou have received your raven core!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                player.closeInventory();
                break;

            case 40: // Shop button
                player.closeInventory();
                plugin.getShopGUI().openShop(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
                break;
        }
    }

    private void handleAbilitiesMenuClick(Player player, int slot) {
        switch (slot) {
            case 40: // Back button
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMainMenu(player);
                break;
        }
    }

    private void handleStatsMenuClick(Player player, int slot) {
        switch (slot) {
            case 40: // Back button
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMainMenu(player);
                break;
        }
    }

    private void handleCustomizationMenuClick(Player player, int slot) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        switch (slot) {
            case 4: // Rename raven
                player.closeInventory();
                player.sendMessage("§d§lType your desired raven name in chat:");
                plugin.getChatListener().registerPendingInput(player, "rename_raven");
                break;

            case 20: // Element colors
                if (!raven.hasCustomColors()) {
                    // Try to purchase
                    int cost = plugin.getConfig().getInt("shop.custom-colors.cost");
                    if (plugin.getCoinManager().hasEnoughCoins(player, cost)) {
                        plugin.getCoinManager().removeCoins(player, cost);
                        raven.setCustomColors(true);
                        player.sendMessage("§a§lYou have purchased custom raven colors!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                        // Refresh GUI
                        openCustomizationMenu(player);
                    } else {
                        player.sendMessage("§c§lYou don't have enough Raven Coins!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
                break;

            case 24: // Element particles
                if (!raven.hasCustomParticles()) {
                    // Try to purchase
                    int cost = plugin.getConfig().getInt("shop.custom-particles.cost");
                    if (plugin.getCoinManager().hasEnoughCoins(player, cost)) {
                        plugin.getCoinManager().removeCoins(player, cost);
                        raven.setCustomParticles(true);
                        player.sendMessage("§a§lYou have purchased custom raven particles!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                        // Refresh GUI
                        openCustomizationMenu(player);
                    } else {
                        player.sendMessage("§c§lYou don't have enough Raven Coins!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
                break;

            case 40: // Back button
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMainMenu(player);
                break;
        }
    }

    // Utility methods

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

    private Material getElementMaterial(RavenElementType elementType) {
        switch (elementType) {
            case FIRE: return Material.BLAZE_POWDER;
            case WATER: return Material.HEART_OF_THE_SEA;
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

    private Material getAbilityMaterial(RavenElementType elementType) {
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

    private Material getSecondaryAbilityMaterial(RavenElementType elementType) {
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

    private String formatElementName(RavenElementType elementType) {
        String name = elementType.name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
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

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        if (seconds < 60) {
            return seconds + " seconds";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") +
                    (remainingSeconds > 0 ? " " + remainingSeconds + " second" + (remainingSeconds > 1 ? "s" : "") : "");
        }
    }

    private int getRequiredExperienceForNextLevel(PlayerRaven raven) {
        return 100 + (raven.getLevel() * 50);
    }

    private String getTierBonus(RavenTier tier, RavenElementType elementType, boolean isSecondary) {
        // Return tier-specific bonuses for each element ability
        switch (elementType) {
            case FIRE:
                if (isSecondary) {
                    return "Increased damage and range";
                } else {
                    return "Fire resistance and strength";
                }
            case WATER:
                if (isSecondary) {
                    return "Stronger push effect and slowness";
                } else {
                    return "Water breathing and dolphin's grace";
                }
            case EARTH:
                if (isSecondary) {
                    return "Stronger shockwave and damage";
                } else {
                    return "Resistance and slow falling";
                }
            case AIR:
                if (isSecondary) {
                    return "Larger cyclone with higher lift";
                } else {
                    return "Speed, jump boost, and slow falling";
                }
            case LIGHTNING:
                if (isSecondary) {
                    return "More chain targets and damage";
                } else {
                    return "Speed, strength, and resistance";
                }
            case ICE:
                if (isSecondary) {
                    return "Increased freeze duration and damage";
                } else {
                    return "Frost armor and slow falling";
                }
            case NATURE:
                if (isSecondary) {
                    return "Longer root duration and poison effect";
                } else {
                    return "Regeneration and hunger resistance";
                }
            case DARKNESS:
                if (isSecondary) {
                    return "Added slowness and wither effects";
                } else {
                    return "Night vision and invisibility";
                }
            case LIGHT:
                if (isSecondary) {
                    return "More healing and undead damage";
                } else {
                    return "Night vision, regeneration, and resistance";
                }
            default:
                return "Increased potency with tier";
        }
    }

    private String getCooldownText(Player player, RavenElementType elementType, boolean isSecondary) {
        if (plugin.getAbilityManager().isOnCooldown(player, elementType, isSecondary)) {
            int remaining = plugin.getAbilityManager().getRemainingCooldown(player, elementType, isSecondary);
            return "§c" + remaining + "s remaining";
        } else {
            return "§aReady to use";
        }
    }

    private boolean hasXpBoost(Player player) {
        // Check if player has active XP boost
        return plugin.getShopGUI().hasXpBoost(player);
    }

    private boolean hasCoinBoost(Player player) {
        // Check if player has active coin boost
        return plugin.getShopGUI().hasCoinBoost(player);
    }

    private int calculateProgress(PlayerRaven raven) {
        double progress = (double) raven.getExperience() / getRequiredExperienceForNextLevel(raven) * 100;
        return (int) progress;
    }

    private String nextTierInfo(PlayerRaven raven) {
        if (raven.getTier() == RavenTier.LEGENDARY) {
            return "§5▸ §d§lMAX TIER REACHED";
        } else {
            RavenTier nextTier = RavenTier.values()[raven.getTier().ordinal() + 1];
            int levelsToNextTier = nextTier.getMinLevel() - raven.getLevel();

            return "§5▸ §dNext Tier: " + formatTier(nextTier) + " §7(in " + levelsToNextTier + " levels)";
        }
    }

    private String formatCooldown(int seconds) {
        if (seconds < 60) {
            return String.valueOf(seconds);
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        }
    }

    private String getSecondaryEffectText(RavenElementType elementType) {
        switch (elementType) {
            case FIRE: return "Area damage and knockback with fire";
            case WATER: return "Push and slow enemies with water";
            case EARTH: return "Damage and slow with earth shockwave";
            case AIR: return "Create a cyclone that lifts enemies";
            case LIGHTNING: return "Chain lightning between multiple enemies";
            case ICE: return "Freeze and damage enemies in radius";
            case NATURE: return "Root enemies with entangling vines";
            case DARKNESS: return "Apply blindness and weakness to enemies";
            case LIGHT: return "Heal allies and damage undead enemies";
            default: return "Element-specific effect";
        }
    }

    private String getElementDescription(RavenElementType elementType) {
        switch (elementType) {
            case FIRE:
                return "§cFire§7 embodies destruction and power, burning through obstacles with intense heat.";
            case WATER:
                return "§9Water§7 flows with adaptability and healing, providing sustenance and protection.";
            case EARTH:
                return "§2Earth§7 represents stability and strength, standing firm against all challenges.";
            case AIR:
                return "§fAir§7 brings freedom and swiftness, allowing movement unhindered by boundaries.";
            case LIGHTNING:
                return "§eLightning§7 strikes with speed and precision, channeling raw elemental energy.";
            case ICE:
                return "§bIce§7 freezes and preserves, slowing enemies and creating protective barriers.";
            case NATURE:
                return "§aNature§7 grows and nurtures, controlling the living forces of plants and life.";
            case DARKNESS:
                return "§8Darkness§7 conceals and obscures, wielding shadow to confuse and terrify.";
            case LIGHT:
                return "§eLight§7 illuminates and purifies, healing allies and smiting undead foes.";
            default:
                return "A powerful elemental force with unique properties.";
        }
    }

    private String getElementStrengths(RavenElementType elementType) {
        switch (elementType) {
            case FIRE: return "Offensive damage, area effects";
            case WATER: return "Healing, mobility in water";
            case EARTH: return "Defense, knockback resistance";
            case AIR: return "Movement speed, jumping";
            case LIGHTNING: return "Chain damage, speed";
            case ICE: return "Crowd control, resistance";
            case NATURE: return "Regeneration, rooting enemies";
            case DARKNESS: return "Stealth, debuffing enemies";
            case LIGHT: return "Healing allies, damaging undead";
            default: return "Balanced elemental powers";
        }
    }

    private String getElementWeaknesses(RavenElementType elementType) {
        switch (elementType) {
            case FIRE: return "Can be extinguished by water";
            case WATER: return "Weaker offensive capabilities";
            case EARTH: return "Slower movement speed";
            case AIR: return "Lower defensive capabilities";
            case LIGHTNING: return "Requires line of sight";
            case ICE: return "Less effective in hot biomes";
            case NATURE: return "Vulnerable to fire damage";
            case DARKNESS: return "Weaker in bright areas";
            case LIGHT: return "Less effective against living enemies";
            default: return "Standard elemental limitations";
        }
    }
}