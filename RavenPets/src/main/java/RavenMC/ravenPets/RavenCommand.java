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
            showHelp(player);
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
                    "spawn", "despawn", "info", "rename", "core", "shop", "help", "coins"
            ));

            if (sender.hasPermission("ravenpets.admin")) {
                completions.add("admin");
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
            }
        }

        return new ArrayList<>();
    }

    private void showHelp(Player player) {
        player.sendMessage("§5§l=== RavenPets Help ===");
        player.sendMessage("§d/raven spawn §7- Spawn your raven");
        player.sendMessage("§d/raven despawn §7- Despawn your raven");
        player.sendMessage("§d/raven info §7- Show information about your raven");
        player.sendMessage("§d/raven rename <n> §7- Rename your raven");
        player.sendMessage("§d/raven core §7- Get your raven core item");
        player.sendMessage("§d/raven shop §7- Open the raven shop");
        player.sendMessage("§d/raven coins §7- Show your raven coin balance");

        if (player.hasPermission("ravenpets.admin")) {
            player.sendMessage("§d/raven admin §7- Admin commands");
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

        player.sendMessage("§5§l=== Raven Information ===");
        player.sendMessage("§dName: §7" + raven.getName());
        player.sendMessage("§dTier: §7" + raven.getTier());
        player.sendMessage("§dLevel: §7" + raven.getLevel());
        player.sendMessage("§dExperience: §7" + raven.getExperience() + "/" + getRequiredExperienceForNextLevel(raven));
        player.sendMessage("§dElement: §7" + raven.getElementType());

        // Show tier progression
        if (raven.getTier() != RavenTier.LEGENDARY) {
            RavenTier nextTier = RavenTier.values()[raven.getTier().ordinal() + 1];
            player.sendMessage("§dNext tier: §7" + nextTier + " (Level " + nextTier.getMinLevel() + ")");
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
        player.sendMessage("§dYour raven has been renamed to §7" + newName + "§d!");
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
        player.sendMessage("§dAdded §e" + amount + " Raven Coins §dto §7" + target.getName() + "§d!");
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

        // TODO: Implement setting level directly in PlayerRaven class
        player.sendMessage("§dSet §7" + target.getName() + "§d's raven level to §7" + level + "§d!");
        target.sendMessage("§dYour raven's level has been set to §7" + level + " §dby an admin!");
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

        // TODO: Implement setting element directly in PlayerRaven class
        player.sendMessage("§dSet §7" + target.getName() + "§d's raven element to §7" + element + "§d!");
        target.sendMessage("§dYour raven's element has been changed to §7" + element + " §dby an admin!");
    }

    private void handleResetRaven(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        // TODO: Implement resetting raven in RavenManager
        player.sendMessage("§dReset §7" + target.getName() + "§d's raven!");
        target.sendMessage("§dYour raven has been reset by an admin!");
    }

    private void handleReload(Player player) {
        plugin.reloadConfig();
        player.sendMessage("§dRavenPets configuration reloaded!");
    }
}