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

        // Check if plugin is fully loaded
        if (!plugin.isPluginFullyLoaded()) {
            player.sendMessage(ChatColor.RED + "✦ Plugin is still loading, please wait... ✦");
            return true;
        }

        try {
            if (args.length == 0) {
                new ShopManagementGUI(plugin, player).open();
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /shop create <n> [type]");
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
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing shop command for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "✦ An error occurred! Please try again or contact an administrator. ✦");
        }

        return true;
    }

    private void createShop(Player player, String[] args) {
        try {
            if (plugin.getShopManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Shop system is not available! ✦");
                return;
            }

            String shopName = args[1];
            if (shopName == null || shopName.trim().isEmpty()) {
                player.sendMessage(ChatColor.RED + "✦ Shop name cannot be empty! ✦");
                return;
            }

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
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating shop for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error creating shop! Please try again. ✦");
        }
    }

    private void listPlayerShops(Player player) {
        try {
            if (plugin.getShopManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Shop system is not available! ✦");
                return;
            }

            List<PlayerShop> shops = plugin.getShopManager().getPlayerShops(player);

            if (shops == null || shops.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "✦ You don't own any shops yet! ✦");
                player.sendMessage(ChatColor.AQUA + "✦ Create one with /shop create <n> ✦");
                return;
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Your Shops" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
            player.sendMessage("");

            for (PlayerShop shop : shops) {
                if (shop == null) continue;

                String status = shop.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Closed";
                player.sendMessage(shop.getType().getSymbol() + " " + ChatColor.YELLOW + shop.getShopName());
                player.sendMessage(ChatColor.WHITE + "  Type: " + shop.getType().getDisplayName() + " | Status: " + status);
                player.sendMessage(ChatColor.WHITE + "  Earnings: " + ChatColor.GREEN + "$" + String.format("%.2f", shop.getEarnings()));
                player.sendMessage("");
            }

            player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════════════════════ ✦");
            player.sendMessage("");
        } catch (Exception e) {
            plugin.getLogger().severe("Error listing shops for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error loading shop list! ✦");
        }
    }

    private void showShopHelp(Player player) {
        try {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Shop Help" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Shop Commands:");
            player.sendMessage(ChatColor.AQUA + "  /shop" + ChatColor.WHITE + " - Open shop management GUI");
            player.sendMessage(ChatColor.AQUA + "  /shop create <n> [type]" + ChatColor.WHITE + " - Create a new shop");
            player.sendMessage(ChatColor.AQUA + "  /shop list" + ChatColor.WHITE + " - List your shops");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Shop Types:");
            for (ShopType type : ShopType.values()) {
                player.sendMessage(ChatColor.WHITE + "  " + type.getSymbol() + " " + type.getDisplayName() +
                        ChatColor.GRAY + " (" + type.name().toLowerCase() + ")");
            }
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Tips:");
            player.sendMessage(ChatColor.WHITE + "• Each shop type has different specializations");
            player.sendMessage(ChatColor.WHITE + "• Use the GUI (/shop) for advanced management");
            player.sendMessage(ChatColor.WHITE + "• Shop creation requires money and may have limits");
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════════════════════ ✦");
            player.sendMessage("");
        } catch (Exception e) {
            plugin.getLogger().warning("Error showing shop help to " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        try {
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
        } catch (Exception e) {
            plugin.getLogger().warning("Error in shop command tab completion: " + e.getMessage());
        }

        return completions;
    }
}