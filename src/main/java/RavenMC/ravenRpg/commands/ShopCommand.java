package RavenMC.ravenRpg.commands;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.gui.ShopManagementGUI;
import RavenMC.ravenRpg.models.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {
    private RavenRpg plugin;

    public ShopCommand(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new ShopManagementGUI(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /shop create <name> [type]");
                    return true;
                }
                createShop(player, args);
                break;
            case "list":
                listPlayerShops(player);
                break;
            case "help":
                showShopHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown shop command! Use /shop for the GUI.");
        }

        return true;
    }

    private void createShop(Player player, String[] args) {
        String shopName = args[1];
        ShopType type = ShopType.GENERAL; // Default

        if (args.length > 2) {
            try {
                type = ShopType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid shop type! Available types:");
                for (ShopType t : ShopType.values()) {
                    player.sendMessage(ChatColor.YELLOW + "  - " + t.name().toLowerCase() + " (" + t.getDisplayName() + ")");
                }
                return;
            }
        }

        plugin.getShopManager().createShop(player, shopName, type);
    }

    private void listPlayerShops(Player player) {
        List<PlayerShop> shops = plugin.getShopManager().getPlayerShops(player);

        if (shops.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "✦ You don't own any shops yet! ✦");
            player.sendMessage(ChatColor.AQUA + "✦ Create one with /shop create <name> ✦");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Your Shops" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");

        for (PlayerShop shop : shops) {
            String status = shop.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Closed";
            player.sendMessage(shop.getType().getSymbol() + " " + ChatColor.YELLOW + shop.getShopName());
            player.sendMessage(ChatColor.WHITE + "  Type: " + shop.getType().getDisplayName() + " | Status: " + status);
            player.sendMessage(ChatColor.WHITE + "  Earnings: " + ChatColor.GREEN + "$" + String.format("%.2f", shop.getEarnings()));
            player.sendMessage("");
        }

        player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showShopHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Shop Help" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Shop Commands:");
        player.sendMessage(ChatColor.AQUA + "  /shop" + ChatColor.WHITE + " - Open shop management GUI");
        player.sendMessage(ChatColor.AQUA + "  /shop create <name> [type]" + ChatColor.WHITE + " - Create a new shop");
        player.sendMessage(ChatColor.AQUA + "  /shop list" + ChatColor.WHITE + " - List your shops");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Shop Types:");
        for (ShopType type : ShopType.values()) {
            player.sendMessage(ChatColor.WHITE + "  " + type.getSymbol() + " " + type.getDisplayName() +
                    ChatColor.GRAY + " (" + type.name().toLowerCase() + ")");
        }
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════════════════════ ✦");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("create", "list", "help");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            for (ShopType type : ShopType.values()) {
                String typeName = type.name().toLowerCase();
                if (typeName.startsWith(args[2].toLowerCase())) {
                    completions.add(typeName);
                }
            }
        }

        return completions;
    }
}