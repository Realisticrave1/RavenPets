package RavenMC.ravenPets;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RavenAdminCommand implements CommandExecutor, TabCompleter, Listener {
    private final RavenPets plugin;
    private final Map<UUID, String> adminAction = new HashMap<>();
    private final Map<UUID, UUID> targetPlayer = new HashMap<>();

    public RavenAdminCommand(RavenPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ravenpets.admin")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            openAdminGUI(player);
            return true;
        }

        // Direct subcommands processing
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                plugin.reloadConfig();
                player.sendMessage("§5§lRavenPets §dconfiguration reloaded!");
                break;

            case "killall":
                int count = killAllRavens();
                player.sendMessage("§5§lRavenPets §dDespawned §f" + count + " §dravens!");
                break;

            case "player":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /radmin player <playername>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }

                openPlayerManagementGUI(player, target);
                break;

            case "stats":
                showPluginStats(player);
                break;

            case "setlevel":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /radmin setlevel <player> <level>");
                    return true;
                }

                Player levelTarget = Bukkit.getPlayer(args[1]);
                if (levelTarget == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }

                try {
                    int level = Integer.parseInt(args[2]);
                    if (level < 1 || level > 100) {
                        player.sendMessage("§cLevel must be between 1 and 100!");
                        return true;
                    }

                    PlayerRaven raven = plugin.getRavenManager().getRaven(levelTarget);
                    raven.setLevel(level);

                    player.sendMessage("§5§lRavenPets §dSet §f" + levelTarget.getName() + "'s §draven level to §f" + level + "§d!");
                    levelTarget.sendMessage("§5§lRavenPets §dYour raven's level has been set to §f" + level + " §dby an admin!");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid level value!");
                }
                break;

            case "setelement":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /radmin setelement <player> <element>");
                    return true;
                }

                Player elementTarget = Bukkit.getPlayer(args[1]);
                if (elementTarget == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }

                try {
                    RavenElementType element = RavenElementType.valueOf(args[2].toUpperCase());

                    PlayerRaven raven = plugin.getRavenManager().getRaven(elementTarget);
                    raven.setElementType(element);

                    player.sendMessage("§5§lRavenPets §dSet §f" + elementTarget.getName() + "'s §draven element to §f" + element.name() + "§d!");
                    elementTarget.sendMessage("§5§lRavenPets §dYour raven's element has been changed to §f" + element.name() + " §dby an admin!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid element! Valid elements: " + Arrays.toString(RavenElementType.values()));
                }
                break;

            case "addcoins":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /radmin addcoins <player> <amount>");
                    return true;
                }

                Player coinsTarget = Bukkit.getPlayer(args[1]);
                if (coinsTarget == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        player.sendMessage("§cAmount must be positive!");
                        return true;
                    }

                    plugin.getCoinManager().addCoins(coinsTarget, amount);

                    player.sendMessage("§5§lRavenPets §dAdded §e" + amount + " Raven Coins §dto §f" + coinsTarget.getName() + "§d!");
                    coinsTarget.sendMessage("§5§lRavenPets §dYou received §e" + amount + " Raven Coins §dfrom an admin!");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid amount value!");
                }
                break;

            case "reset":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /radmin reset <player>");
                    return true;
                }

                Player resetTarget = Bukkit.getPlayer(args[1]);
                if (resetTarget == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }

                // Reset by creating a new raven and replacing the old one
                plugin.getRavenManager().resetRaven(resetTarget);

                player.sendMessage("§5§lRavenPets §dReset §f" + resetTarget.getName() + "'s §draven!");
                resetTarget.sendMessage("§5§lRavenPets §dYour raven has been reset by an admin!");
                break;

            default:
                player.sendMessage("§cUnknown subcommand! Use /radmin for admin panel.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            completions.addAll(Arrays.asList(
                    "reload", "killall", "player", "stats",
                    "setlevel", "setelement", "addcoins", "reset"
            ));
        } else if (args.length == 2) {
            // Player name for commands that need it
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("player") || subCommand.equals("setlevel") ||
                    subCommand.equals("setelement") || subCommand.equals("addcoins") ||
                    subCommand.equals("reset")) {

                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 3) {
            // Element names for setelement
            if (args[0].equalsIgnoreCase("setelement")) {
                completions.addAll(Arrays.stream(RavenElementType.values())
                        .map(elem -> elem.name().toLowerCase())
                        .collect(Collectors.toList()));
            }
        }

        // Filter based on current input
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }

    private void openAdminGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, "§5§l⚙ Raven Admin Panel ⚙");

        // Fill with glass pane background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Border with purple glass
        ItemStack border = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(gui.getSize() - 9 + i, border); // Bottom row
        }
        for (int i = 1; i < 3; i++) {
            gui.setItem(i * 9, border); // Left column
            gui.setItem(i * 9 + 8, border); // Right column
        }

        // Player Management
        ItemStack playerManagement = createGuiItem(
                Material.PLAYER_HEAD,
                "§d§lPlayer Management",
                "§7View and modify players' ravens",
                "",
                "§8» Click to select a player"
        );
        gui.setItem(11, playerManagement);

        // Economy Management
        ItemStack economyManagement = createGuiItem(
                Material.GOLD_INGOT,
                "§e§lEconomy Management",
                "§7Manage raven coins",
                "",
                "§8» Click to open economy options"
        );
        gui.setItem(13, economyManagement);

        // Server Settings
        ItemStack serverSettings = createGuiItem(
                Material.REDSTONE_BLOCK,
                "§c§lServer Actions",
                "§7Perform server-wide actions",
                "",
                "§8» Click to view options"
        );
        gui.setItem(15, serverSettings);

        // Statistics
        ItemStack statistics = createGuiItem(
                Material.BOOK,
                "§b§lStatistics",
                "§7View plugin statistics",
                "",
                "§8» Click to view stats"
        );
        gui.setItem(22, statistics);

        player.openInventory(gui);
    }

    private void openPlayerManagementGUI(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 36, "§5§l⚙ Manage: " + target.getName() + " ⚙");
        targetPlayer.put(admin.getUniqueId(), target.getUniqueId());

        // Fill with glass pane background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Player head
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName("§d§l" + target.getName());

        PlayerRaven raven = plugin.getRavenManager().getRaven(target);
        List<String> skullLore = new ArrayList<>();
        skullLore.add("§5§m                    ");
        skullLore.add("§5▸ §dRaven: §f" + raven.getName());
        skullLore.add("§5▸ §dLevel: §f" + raven.getLevel());
        skullLore.add("§5▸ §dTier: §f" + raven.getTier().name());
        skullLore.add("§5▸ §dElement: §f" + raven.getElementType().name());
        skullLore.add("§5▸ §dCoins: §e" + plugin.getCoinManager().getCoins(target));
        skullLore.add("§5§m                    ");
        skullMeta.setLore(skullLore);
        playerHead.setItemMeta(skullMeta);
        gui.setItem(4, playerHead);

        // Set Level
        ItemStack setLevel = createGuiItem(
                Material.EXPERIENCE_BOTTLE,
                "§a§lSet Level",
                "§7Change player's raven level",
                "",
                "§8» Click to set level"
        );
        gui.setItem(10, setLevel);

        // Set Element
        ItemStack setElement = createGuiItem(
                Material.BLAZE_POWDER,
                "§c§lSet Element",
                "§7Change player's raven element",
                "",
                "§8» Click to choose element"
        );
        gui.setItem(12, setElement);

        // Add Coins
        ItemStack addCoins = createGuiItem(
                Material.GOLD_INGOT,
                "§e§lAdd Coins",
                "§7Give player raven coins",
                "",
                "§8» Click to add coins"
        );
        gui.setItem(14, addCoins);

        // Reset Raven
        ItemStack resetRaven = createGuiItem(
                Material.BARRIER,
                "§4§lReset Raven",
                "§7Reset player's raven to defaults",
                "",
                "§c§lWARNING: §7This can't be undone!",
                "",
                "§8» Click to reset"
        );
        gui.setItem(16, resetRaven);

        // Back Button
        ItemStack backButton = createGuiItem(
                Material.ARROW,
                "§f§lBack",
                "§7Return to admin panel",
                "",
                "§8» Click to go back"
        );
        gui.setItem(31, backButton);

        admin.openInventory(gui);
    }

    private void openElementSelectionGUI(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 45, "§5§l⚙ Select Element ⚙");
        targetPlayer.put(admin.getUniqueId(), target.getUniqueId());
        adminAction.put(admin.getUniqueId(), "setelement");

        // Fill with glass pane background
        ItemStack background = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        // Element options
        gui.setItem(10, createGuiItem(Material.BLAZE_POWDER, "§c§lFire", "§7Set raven element to Fire"));
        gui.setItem(12, createGuiItem(Material.PRISMARINE_CRYSTALS, "§9§lWater", "§7Set raven element to Water"));
        gui.setItem(14, createGuiItem(Material.EMERALD, "§2§lEarth", "§7Set raven element to Earth"));
        gui.setItem(16, createGuiItem(Material.FEATHER, "§f§lAir", "§7Set raven element to Air"));
        gui.setItem(20, createGuiItem(Material.NETHER_STAR, "§e§lLightning", "§7Set raven element to Lightning"));
        gui.setItem(22, createGuiItem(Material.BLUE_ICE, "§b§lIce", "§7Set raven element to Ice"));
        gui.setItem(24, createGuiItem(Material.OAK_SAPLING, "§a§lNature", "§7Set raven element to Nature"));
        gui.setItem(28, createGuiItem(Material.OBSIDIAN, "§8§lDarkness", "§7Set raven element to Darkness"));
        gui.setItem(30, createGuiItem(Material.GLOWSTONE_DUST, "§e§lLight", "§7Set raven element to Light"));

        // Back Button
        ItemStack backButton = createGuiItem(
                Material.ARROW,
                "§f§lBack",
                "§7Return to player management",
                "",
                "§8» Click to go back"
        );
        gui.setItem(40, backButton);

        admin.openInventory(gui);
    }

    private void promptForInput(Player admin, String action, Player target) {
        admin.closeInventory();

        targetPlayer.put(admin.getUniqueId(), target.getUniqueId());
        adminAction.put(admin.getUniqueId(), action);

        switch (action) {
            case "setlevel":
                admin.sendMessage("§5§lRavenPets §dEnter the level (1-100) for §f" + target.getName() + "'s §draven in chat:");
                break;
            case "addcoins":
                admin.sendMessage("§5§lRavenPets §dEnter the amount of coins to add to §f" + target.getName() + " §din chat:");
                break;
        }
    }

    private int killAllRavens() {
        return plugin.getRavenManager().despawnAllRavens();
    }

    private void showPluginStats(Player player) {
        int totalRavens = plugin.getRavenManager().getTotalRavenCount();
        int activeRavens = plugin.getRavenManager().getActiveRavenCount();

        Map<RavenElementType, Integer> elementCounts = plugin.getRavenManager().getElementStats();
        Map<RavenTier, Integer> tierCounts = plugin.getRavenManager().getTierStats();

        // Calculate total coins
        int totalCoins = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            totalCoins += plugin.getCoinManager().getCoins(onlinePlayer);
        }

        // Display statistics
        player.sendMessage("§5§l========== RavenPets Statistics ==========");
        player.sendMessage("§d• Total Ravens: §f" + totalRavens);
        player.sendMessage("§d• Active Ravens: §f" + activeRavens);
        player.sendMessage("§d• Total Raven Coins: §e" + totalCoins);

        player.sendMessage("§d• Elements Distribution:");
        for (RavenElementType type : RavenElementType.values()) {
            player.sendMessage("  §5- " + formatElement(type) + ": §f" + elementCounts.get(type));
        }

        player.sendMessage("§d• Tiers Distribution:");
        for (RavenTier tier : RavenTier.values()) {
            player.sendMessage("  §5- " + formatTier(tier) + ": §f" + tierCounts.get(tier));
        }

        player.sendMessage("§5§l==========================================");
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

        if (!title.startsWith("§5§l⚙")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // Main admin panel
        if (title.equals("§5§l⚙ Raven Admin Panel ⚙")) {
            handleMainAdminPanelClick(player, event.getSlot());
        }
        // Player management
        else if (title.contains("Manage:")) {
            handlePlayerManagementClick(player, event.getSlot());
        }
        // Element selection
        else if (title.equals("§5§l⚙ Select Element ⚙")) {
            handleElementSelectionClick(player, event.getSlot());
        }
    }

    private void handleMainAdminPanelClick(Player player, int slot) {
        switch (slot) {
            case 11: // Player Management
                player.closeInventory();
                player.sendMessage("§5§lRavenPets §dType the player's name in chat to manage their raven:");
                adminAction.put(player.getUniqueId(), "selectplayer");
                break;

            case 13: // Economy Management
                player.closeInventory();
                player.sendMessage("§5§lRavenPets §dUse §f/radmin addcoins <player> <amount> §dto give coins.");
                break;

            case 15: // Server Actions
                player.closeInventory();
                player.sendMessage("§5§lRavenPets §dAvailable server actions:");
                player.sendMessage("§5▸ §d/radmin killall §7- Despawn all ravens");
                player.sendMessage("§5▸ §d/radmin reload §7- Reload configuration");
                break;

            case 22: // Statistics
                player.closeInventory();
                showPluginStats(player);
                break;
        }
    }

    private void handlePlayerManagementClick(Player admin, int slot) {
        if (!targetPlayer.containsKey(admin.getUniqueId())) {
            admin.closeInventory();
            return;
        }

        Player target = Bukkit.getPlayer(targetPlayer.get(admin.getUniqueId()));
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§cTarget player is offline!");
            admin.closeInventory();
            return;
        }

        switch (slot) {
            case 10: // Set Level
                promptForInput(admin, "setlevel", target);
                break;

            case 12: // Set Element
                openElementSelectionGUI(admin, target);
                break;

            case 14: // Add Coins
                promptForInput(admin, "addcoins", target);
                break;

            case 16: // Reset Raven
                admin.closeInventory();
                admin.sendMessage("§5§lRavenPets §dType §fCONFIRM §dto reset §f" + target.getName() + "'s §draven or §fCANCEL §dto abort:");
                adminAction.put(admin.getUniqueId(), "resetconfirm");
                break;

            case 31: // Back
                admin.closeInventory();
                openAdminGUI(admin);
                break;
        }
    }

    private void handleElementSelectionClick(Player admin, int slot) {
        if (!targetPlayer.containsKey(admin.getUniqueId())) {
            admin.closeInventory();
            return;
        }

        Player target = Bukkit.getPlayer(targetPlayer.get(admin.getUniqueId()));
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§cTarget player is offline!");
            admin.closeInventory();
            return;
        }

        RavenElementType element = null;

        switch (slot) {
            case 10: element = RavenElementType.FIRE; break;
            case 12: element = RavenElementType.WATER; break;
            case 14: element = RavenElementType.EARTH; break;
            case 16: element = RavenElementType.AIR; break;
            case 20: element = RavenElementType.LIGHTNING; break;
            case 22: element = RavenElementType.ICE; break;
            case 24: element = RavenElementType.NATURE; break;
            case 28: element = RavenElementType.DARKNESS; break;
            case 30: element = RavenElementType.LIGHT; break;
            case 40: // Back
                admin.closeInventory();
                openPlayerManagementGUI(admin, target);
                return;
        }

        if (element != null) {
            PlayerRaven raven = plugin.getRavenManager().getRaven(target);
            raven.setElementType(element);

            admin.sendMessage("§5§lRavenPets §dSet §f" + target.getName() + "'s §draven element to §f" + element.name() + "§d!");
            target.sendMessage("§5§lRavenPets §dYour raven's element has been changed to §f" + element.name() + " §dby an admin!");

            admin.closeInventory();
            openPlayerManagementGUI(admin, target);
        }
    }

    /**
     * Check if a player has a pending admin action
     * @param player The player to check
     * @return True if the player has a pending action
     */
    public boolean hasAction(Player player) {
        return adminAction.containsKey(player.getUniqueId());
    }

    /**
     * Handle chat input for admin actions
     * @param admin The admin player
     * @param message The chat message
     */
    public void handleChatInput(Player admin, String message) {
        UUID adminId = admin.getUniqueId();

        if (!adminAction.containsKey(adminId)) {
            return;
        }

        String action = adminAction.get(adminId);
        Player target = null;

        if (targetPlayer.containsKey(adminId)) {
            UUID targetId = targetPlayer.get(adminId);
            target = Bukkit.getPlayer(targetId);
        }

        switch (action) {
            case "selectplayer":
                adminAction.remove(adminId);

                Player selectedPlayer = Bukkit.getPlayer(message);
                if (selectedPlayer == null) {
                    admin.sendMessage("§cPlayer not found! Try again or use the command.");
                    return;
                }

                openPlayerManagementGUI(admin, selectedPlayer);
                break;

            case "setlevel":
                adminAction.remove(adminId);

                if (target == null) {
                    admin.sendMessage("§cTarget player is no longer online!");
                    return;
                }

                try {
                    int level = Integer.parseInt(message);
                    if (level < 1 || level > 100) {
                        admin.sendMessage("§cLevel must be between 1 and 100!");
                        return;
                    }

                    PlayerRaven raven = plugin.getRavenManager().getRaven(target);
                    raven.setLevel(level);

                    admin.sendMessage("§5§lRavenPets §dSet §f" + target.getName() + "'s §draven level to §f" + level + "§d!");
                    target.sendMessage("§5§lRavenPets §dYour raven's level has been set to §f" + level + " §dby an admin!");
                } catch (NumberFormatException e) {
                    admin.sendMessage("§cInvalid level value! Must be a number.");
                }
                break;

            case "addcoins":
                adminAction.remove(adminId);

                if (target == null) {
                    admin.sendMessage("§cTarget player is no longer online!");
                    return;
                }

                try {
                    int amount = Integer.parseInt(message);
                    if (amount <= 0) {
                        admin.sendMessage("§cAmount must be positive!");
                        return;
                    }

                    plugin.getCoinManager().addCoins(target, amount);

                    admin.sendMessage("§5§lRavenPets §dAdded §e" + amount + " Raven Coins §dto §f" + target.getName() + "§d!");
                    target.sendMessage("§5§lRavenPets §dYou received §e" + amount + " Raven Coins §dfrom an admin!");
                } catch (NumberFormatException e) {
                    admin.sendMessage("§cInvalid amount value! Must be a number.");
                }
                break;

            case "resetconfirm":
                adminAction.remove(adminId);

                if (target == null) {
                    admin.sendMessage("§cTarget player is no longer online!");
                    return;
                }

                if (message.equalsIgnoreCase("CONFIRM")) {
                    // Reset the raven
                    plugin.getRavenManager().resetRaven(target);

                    admin.sendMessage("§5§lRavenPets §dReset §f" + target.getName() + "'s §draven!");
                    target.sendMessage("§5§lRavenPets §dYour raven has been reset by an admin!");
                } else {
                    admin.sendMessage("§5§lRavenPets §dRaven reset cancelled.");
                }
                break;
        }
    }
}