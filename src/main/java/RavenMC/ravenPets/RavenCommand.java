package RavenMC.ravenPets;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RavenCommand implements CommandExecutor, TabCompleter {
    private final RavenPets plugin;

    public RavenCommand(RavenPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open the new player GUI instead of showing help
            plugin.getPlayerGUI().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                spawnRaven(player);
                break;
            case "despawn":
                despawnRaven(player);
                break;
            case "info":
                showInfo(player);
                break;
            case "rename":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /raven rename <n>");
                    return true;
                }
                renameRaven(player, args[1]);
                break;
            case "core":
                giveRavenCore(player);
                break;
            case "shop":
                openShop(player);
                break;
            case "help":
                showHelp(player);
                break;
            case "coins":
                showCoins(player);
                break;
            case "abilities":
                // Open the abilities menu
                plugin.getPlayerGUI().openAbilitiesMenu(player);
                break;
            case "stats":
                // Open the stats menu
                plugin.getPlayerGUI().openStatsMenu(player);
                break;
            case "customize":
                // Open the customization menu
                plugin.getPlayerGUI().openCustomizationMenu(player);
                break;
            case "gui":
                // Open the main menu
                plugin.getPlayerGUI().openMainMenu(player);
                break;
            case "debug":
                // Debug command for cooldowns
                if (player.hasPermission("ravenpets.admin")) {
                    handleDebugCommand(player, args);
                } else {
                    player.sendMessage("§cYou don't have permission to use debug commands.");
                }
                break;
            case "cooldown":
                // Check cooldown status directly
                showCooldownStatus(player);
                break;
            case "admin":
                if (!player.hasPermission("ravenpets.admin")) {
                    player.sendMessage("§cYou don't have permission to use admin commands.");
                    return true;
                }
                handleAdminCommand(player, args);
                break;
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList(
                    "spawn", "despawn", "info", "rename", "core", "shop", "help", "coins",
                    "abilities", "stats", "customize", "gui", "cooldown"
            ));

            if (sender.hasPermission("ravenpets.admin")) {
                completions.add("admin");
                completions.add("debug");
            }

            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("ravenpets.admin")) {
                return Arrays.asList("addcoins", "setlevel", "setraven", "resetraven", "reload")
                        .stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("debug") && sender.hasPermission("ravenpets.admin")) {
                return Arrays.asList("cooldowns", "toggledebug", "clearcooldowns", "resetcooldown")
                        .stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    private void handleDebugCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§5§l=== RavenPets Debug Commands ===");
            player.sendMessage("§d/raven debug cooldowns §7- Show all active cooldowns");
            player.sendMessage("§d/raven debug toggledebug §7- Toggle debug mode");
            player.sendMessage("§d/raven debug clearcooldowns §7- Clear your own cooldowns");
            player.sendMessage("§d/raven debug resetcooldown <primary|secondary> §7- Reset a specific cooldown");
            return;
        }

        String debugCommand = args[1].toLowerCase();

        switch (debugCommand) {
            case "cooldowns":
                // Print all active cooldowns to console
                plugin.getCooldownManager().printActiveCooldowns();
                player.sendMessage("§dCooldown information has been printed to console.");
                break;

            case "toggledebug":
                // Toggle debug mode
                boolean currentMode = plugin.getConfig().getBoolean("debug-mode", false);
                plugin.getConfig().set("debug-mode", !currentMode);
                plugin.saveConfig();
                player.sendMessage("§dDebug mode is now " + (!currentMode ? "§aenabled" : "§cdisabled") + "§d.");
                break;

            case "clearcooldowns":
                // Clear player's cooldowns
                plugin.getCooldownManager().clearCooldowns(player.getUniqueId());
                player.sendMessage("§dYour cooldowns have been cleared.");
                break;

            case "resetcooldown":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /raven debug resetcooldown <primary|secondary>");
                    return;
                }

                boolean isSecondary = args[2].equalsIgnoreCase("secondary");
                PlayerRaven raven = plugin.getRavenManager().getRaven(player);
                String elementType = raven.getElementType().name();

                plugin.getCooldownManager().clearCooldown(player, elementType, isSecondary);
                player.sendMessage("§dYour " + (isSecondary ? "secondary" : "primary") +
                        " ability cooldown has been reset.");
                break;

            default:
                player.sendMessage("§cUnknown debug command. Use /raven debug for help.");
                break;
        }
    }

    private void showCooldownStatus(Player player) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        String elementType = raven.getElementType().name();
        RavenAbility ability = plugin.getAbilityManager().getAbility(raven.getElementType());

        player.sendMessage("§5§l=== Raven Cooldown Status ===");

        // Primary ability
        boolean primaryOnCooldown = plugin.getCooldownManager().isOnCooldown(player, elementType, false);
        if (primaryOnCooldown) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player, elementType, false);
            player.sendMessage("§d" + ability.getName() + ": §c" + remaining + "s remaining");
        } else {
            player.sendMessage("§d" + ability.getName() + ": §aReady to use");
        }

        // Secondary ability
        boolean secondaryOnCooldown = plugin.getCooldownManager().isOnCooldown(player, elementType, true);
        if (secondaryOnCooldown) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player, elementType, true);
            player.sendMessage("§d" + ability.getSecondaryName() + ": §c" + remaining + "s remaining");
        } else {
            player.sendMessage("§d" + ability.getSecondaryName() + ": §aReady to use");
        }

        // Show base cooldown times from config
        int primaryCooldown = plugin.getCooldownManager().getCooldownTime(elementType, false);
        int secondaryCooldown = plugin.getCooldownManager().getCooldownTime(elementType, true);

        // Apply tier reduction
        int reducedPrimary = plugin.getCooldownManager().getReducedCooldown(player, primaryCooldown);
        int reducedSecondary = plugin.getCooldownManager().getReducedCooldown(player, secondaryCooldown);

        player.sendMessage("§5§m                    ");
        player.sendMessage("§dPrimary base cooldown: §f" + primaryCooldown + "s");
        player.sendMessage("§dPrimary with tier reduction: §f" + reducedPrimary + "s");
        player.sendMessage("§dSecondary base cooldown: §f" + secondaryCooldown + "s");
        player.sendMessage("§dSecondary with tier reduction: §f" + reducedSecondary + "s");

        if (player.hasPermission("ravenpets.bypasscooldown")) {
            player.sendMessage("§a§lYou have cooldown bypass permission!");
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("§5§l=== RavenPets Help ===");
        player.sendMessage("§d/raven §7- Open the main Raven GUI");
        player.sendMessage("§d/raven spawn §7- Spawn your raven");
        player.sendMessage("§d/raven despawn §7- Despawn your raven");
        player.sendMessage("§d/raven info §7- Show information about your raven");
        player.sendMessage("§d/raven rename <n> §7- Rename your raven");
        player.sendMessage("§d/raven core §7- Get your raven core item");
        player.sendMessage("§d/raven shop §7- Open the raven shop");
        player.sendMessage("§d/raven coins §7- Show your raven coin balance");
        player.sendMessage("§d/raven abilities §7- View your raven's abilities");
        player.sendMessage("§d/raven stats §7- View detailed raven statistics");
        player.sendMessage("§d/raven customize §7- Customize your raven's appearance");
        player.sendMessage("§d/raven cooldown §7- Check your ability cooldowns");
        player.sendMessage("§d/raven gui §7- Open the main raven GUI menu");

        if (player.hasPermission("ravenpets.admin")) {
            player.sendMessage("§d/raven admin §7- Admin commands");
            player.sendMessage("§d/raven debug §7- Debug commands");
        }
    }

    private void spawnRaven(Player player) {
        if (plugin.getRavenManager().hasRaven(player)) {
            player.sendMessage("§cYour raven is already spawned!");
            return;
        }

        plugin.getRavenManager().spawnRaven(player);
        player.sendMessage("§dYour raven has been spawned!");
    }

    private void despawnRaven(Player player) {
        if (!plugin.getRavenManager().hasRaven(player)) {
            player.sendMessage("§cYour raven is not spawned!");
            return;
        }

        plugin.getRavenManager().despawnRaven(player);
        player.sendMessage("§dYour raven has been despawned!");
    }

    private void showInfo(Player player) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        String elementType = raven.getElementType().name();

        player.sendMessage("§5§l=== Raven Information ===");
        player.sendMessage("§dName: §f" + raven.getName());
        player.sendMessage("§dTier: §f" + raven.getTier());
        player.sendMessage("§dLevel: §f" + raven.getLevel());
        player.sendMessage("§dExperience: §f" + raven.getExperience() + "/" + getRequiredExperienceForNextLevel(raven));
        player.sendMessage("§dElement: §f" + raven.getElementType());

        // Show tier progression
        if (raven.getTier() != RavenTier.LEGENDARY) {
            RavenTier nextTier = RavenTier.values()[raven.getTier().ordinal() + 1];
            player.sendMessage("§dNext tier: §f" + nextTier + " (Level " + nextTier.getMinLevel() + ")");
        }

        // Show cooldown info using the new direct cooldown manager
        RavenAbility ability = plugin.getAbilityManager().getAbility(raven.getElementType());
        if (ability != null) {
            player.sendMessage("§dPrimary Ability: §f" + ability.getName());
            if (plugin.getCooldownManager().isOnCooldown(player, elementType, false)) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, elementType, false);
                player.sendMessage("§dPrimary Cooldown: §c" + remaining + "s remaining");
            } else {
                player.sendMessage("§dPrimary Cooldown: §aReady");
            }

            player.sendMessage("§dSecondary Ability: §f" + ability.getSecondaryName());
            if (plugin.getCooldownManager().isOnCooldown(player, elementType, true)) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, elementType, true);
                player.sendMessage("§dSecondary Cooldown: §c" + remaining + "s remaining");
            } else {
                player.sendMessage("§dSecondary Cooldown: §aReady");
            }
        }
    }

    private int getRequiredExperienceForNextLevel(PlayerRaven raven) {
        return 100 + (raven.getLevel() * 50);
    }

    private void renameRaven(Player player, String newName) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Check name length
        if (newName.length() > 16) {
            player.sendMessage("§cRaven name cannot be longer than 16 characters!");
            return;
        }

        // Check for color codes
        if (!player.hasPermission("ravenpets.colornames") && newName.contains("§")) {
            player.sendMessage("§cYou don't have permission to use color codes in your raven's name!");
            return;
        }

        raven.setName(newName);
        player.sendMessage("§dYour raven has been renamed to §f" + newName + "§d!");
    }

    private void giveRavenCore(Player player) {
        ItemStack core = plugin.getAbilityManager().createRavenCore(player);
        player.getInventory().addItem(core);
        player.sendMessage("§dYou have received your raven core!");
    }

    private void openShop(Player player) {
        plugin.getShopGUI().openShop(player);
    }

    private void showCoins(Player player) {
        int coins = plugin.getCoinManager().getCoins(player);
        player.sendMessage("§dYou have §e" + coins + " Raven Coins§d!");
    }

    private void handleAdminCommand(Player player, String[] args) {
        if (args.length < 2) {
            showAdminHelp(player);
            return;
        }

        String adminCommand = args[1].toLowerCase();

        switch (adminCommand) {
            case "addcoins":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /raven admin addcoins <player> <amount>");
                    return;
                }
                handleAddCoins(player, args[2], args[3]);
                break;
            case "setlevel":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /raven admin setlevel <player> <level>");
                    return;
                }
                handleSetLevel(player, args[2], args[3]);
                break;
            case "setraven":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /raven admin setraven <player> <element>");
                    return;
                }
                handleSetRaven(player, args[2], args[3]);
                break;
            case "resetraven":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /raven admin resetraven <player>");
                    return;
                }
                handleResetRaven(player, args[2]);
                break;
            case "reload":
                handleReload(player);
                break;
            default:
                showAdminHelp(player);
                break;
        }
    }

    private void showAdminHelp(Player player) {
        player.sendMessage("§5§l=== RavenPets Admin Help ===");
        player.sendMessage("§d/raven admin addcoins <player> <amount> §7- Add raven coins to a player");
        player.sendMessage("§d/raven admin setlevel <player> <level> §7- Set a player's raven level");
        player.sendMessage("§d/raven admin setraven <player> <element> §7- Set a player's raven element");
        player.sendMessage("§d/raven admin resetraven <player> §7- Reset a player's raven");
        player.sendMessage("§d/raven admin reload §7- Reload configuration");
    }

    private void handleAddCoins(Player player, String targetName, String amountStr) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount!");
            return;
        }

        plugin.getCoinManager().addCoins(target, amount);
        player.sendMessage("§dAdded §e" + amount + " Raven Coins §dto §f" + target.getName() + "§d!");
        target.sendMessage("§dYou received §e" + amount + " Raven Coins §dfrom an admin!");
    }

    private void handleSetLevel(Player player, String targetName, String levelStr) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        int level;
        try {
            level = Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid level!");
            return;
        }

        if (level < 1 || level > 100) {
            player.sendMessage("§cLevel must be between 1 and 100!");
            return;
        }

        PlayerRaven raven = plugin.getRavenManager().getRaven(target);
        raven.setLevel(level);

        player.sendMessage("§dSet §f" + target.getName() + "§d's raven level to §f" + level + "§d!");
        target.sendMessage("§dYour raven's level has been set to §f" + level + " §dby an admin!");
    }

    private void handleSetRaven(Player player, String targetName, String elementStr) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        RavenElementType element;
        try {
            element = RavenElementType.valueOf(elementStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid element! Valid elements: " +
                    Arrays.toString(RavenElementType.values()));
            return;
        }

        PlayerRaven raven = plugin.getRavenManager().getRaven(target);
        raven.setElementType(element);

        player.sendMessage("§dSet §f" + target.getName() + "§d's raven element to §f" + element + "§d!");
        target.sendMessage("§dYour raven's element has been changed to §f" + element + " §dby an admin!");
    }

    private void handleResetRaven(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        plugin.getRavenManager().resetRaven(target);

        player.sendMessage("§dReset §f" + target.getName() + "§d's raven!");
        target.sendMessage("§dYour raven has been reset by an admin!");
    }

    private void handleReload(Player player) {
        plugin.reloadConfig();
        player.sendMessage("§dRavenPets configuration reloaded!");
    }
}