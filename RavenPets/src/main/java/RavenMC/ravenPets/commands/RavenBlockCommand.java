package RavenMC.ravenPets.commands;

import RavenMC.ravenPets.RavenPets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

/**
 * Command for managing raven pet blocks
 */
public class RavenBlockCommand implements CommandExecutor, TabCompleter {

    private final RavenPets plugin;

    public RavenBlockCommand(RavenPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGiveCommand(sender, args);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /ravenblock help for assistance.");
                break;
        }

        return true;
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("ravenpets.block.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return;
        }

        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ravenblock give <player> [tier] [amount]");
            return;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        // Determine tier
        int tier = 1; // Default to Novice
        if (args.length >= 3) {
            try {
                tier = Integer.parseInt(args[2]);
                if (tier < 1 || tier > 5) {
                    sender.sendMessage(ChatColor.RED + "Tier must be between 1 and 5!");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid tier number!");
                return;
            }
        }

        // Determine amount
        int amount = 1; // Default to 1
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64!");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount!");
                return;
            }
        }

        // Create the raven block
        ItemStack ravenBlock = plugin.getRavenBlockManager().createRavenBlock(tier);
        ravenBlock.setAmount(amount);

        // Give the item to the player
        target.getInventory().addItem(ravenBlock);

        // Get tier name for messages
        String tierName = getTierName(tier);

        // Notify both sender and target
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " " + amount + " " + tierName + " Raven block(s)!");

        if (sender != target) {
            target.sendMessage(ChatColor.GREEN + "You received " + amount + " " + tierName + " Raven block(s)!");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "==== RavenBlock Commands ====");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenblock give <player> [tier] [amount] " +
                ChatColor.GRAY + "- Give raven blocks to a player");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenblock help " +
                ChatColor.GRAY + "- Show this help message");

        sender.sendMessage(ChatColor.GRAY + "Tiers: 1=Novice, 2=Adept, 3=Expert, 4=Master, 5=Legendary");
    }

    private String getTierName(int tier) {
        switch (tier) {
            case 1: return "Novice";
            case 2: return "Adept";
            case 3: return "Expert";
            case 4: return "Master";
            case 5: return "Legendary";
            default: return "Unknown";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("give", "help");
            return filterCompletions(subcommands, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> tiers = Arrays.asList("1", "2", "3", "4", "5");
            return filterCompletions(tiers, args[2]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String partialArg) {
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(partialArg.toLowerCase()))
                .collect(Collectors.toList());
    }
}