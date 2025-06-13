package RavenMC.ravenRpg.commands;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RpgAdminCommand implements CommandExecutor, TabCompleter {
    private RavenRpg plugin;

    public RpgAdminCommand(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ravenrpg.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin reset <player>");
                    return true;
                }
                resetPlayer(sender, args[1]);
                break;
            case "setrace":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin setrace <player> <race>");
                    return true;
                }
                setPlayerRace(sender, args[1], args[2]);
                break;
            case "setbloodline":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin setbloodline <player> <bloodline>");
                    return true;
                }
                setPlayerBloodline(sender, args[1], args[2]);
                break;
            case "addstat":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rpgadmin addstat <player> <stat> <amount>");
                    return true;
                }
                addPlayerStat(sender, args[1], args[2], args[3]);
                break;
            case "reload":
                reloadPlugin(sender);
                break;
            case "save":
                saveAllData(sender);
                break;
            default:
                showAdminHelp(sender);
        }

        return true;
    }

    private void resetPlayer(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target);
        data.setSelectedRace(null);
        data.setSelectedBloodline(null);
        data.setCurrentGuild(null);
        data.getOwnedShops().clear();

        // Reset stats to defaults
        data.getStats().clear();
        data.getSkills().clear();

        plugin.getDataManager().savePlayerData(data);

        sender.sendMessage(ChatColor.GREEN + "✦ Reset " + target.getName() + "'s RPG data! ✦");
        target.sendMessage(ChatColor.YELLOW + "✦ Your RPG data has been reset by an admin! ✦");
    }

    private void setPlayerRace(CommandSender sender, String playerName, String raceName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            Race race = Race.valueOf(raceName.toUpperCase());
            PlayerData data = plugin.getDataManager().getPlayerData(target);
            data.setSelectedRace(race);
            plugin.getDataManager().savePlayerData(data);

            sender.sendMessage(ChatColor.GREEN + "✦ Set " + target.getName() + "'s race to " + race.getDisplayName() + "! ✦");
            target.sendMessage(ChatColor.YELLOW + "✦ Your race has been set to " + race.getDisplayName() + " by an admin! ✦");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid race! Available: " + Arrays.toString(Race.values()));
        }
    }

    private void setPlayerBloodline(CommandSender sender, String playerName, String bloodlineName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            Bloodline bloodline = Bloodline.valueOf(bloodlineName.toUpperCase().replace(" ", "_"));
            PlayerData data = plugin.getDataManager().getPlayerData(target);
            data.setSelectedBloodline(bloodline);
            plugin.getDataManager().savePlayerData(data);

            sender.sendMessage(ChatColor.GREEN + "✦ Set " + target.getName() + "'s bloodline to " + bloodline.getDisplayName() + "! ✦");
            target.sendMessage(ChatColor.YELLOW + "✦ Your bloodline has been set to " + bloodline.getDisplayName() + " by an admin! ✦");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid bloodline! Available: " + Arrays.toString(Bloodline.values()));
        }
    }

    private void addPlayerStat(CommandSender sender, String playerName, String statName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            PlayerData data = plugin.getDataManager().getPlayerData(target);
            data.addStat(statName, amount);
            plugin.getDataManager().savePlayerData(data);

            sender.sendMessage(ChatColor.GREEN + "✦ Added " + amount + " " + statName + " to " + target.getName() + "! ✦");
            target.sendMessage(ChatColor.YELLOW + "✦ Your " + statName + " was modified by an admin! ✦");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount! Use a number.");
        }
    }

    private void reloadPlugin(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "✦ RavenRpg configuration reloaded! ✦");
    }

    private void saveAllData(CommandSender sender) {
        plugin.getDataManager().saveAllData();
        plugin.getGuildManager().saveGuilds();
        plugin.getShopManager().saveAllShops();
        sender.sendMessage(ChatColor.GREEN + "✦ All RavenRpg data saved! ✦");
    }

    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "✦ ═══════ " + ChatColor.BOLD + "RavenRpg Admin Help" + ChatColor.RESET + ChatColor.RED + " ═══════ ✦");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Player Management:");
        sender.sendMessage(ChatColor.AQUA + "  /rpgadmin reset <player>" + ChatColor.WHITE + " - Reset player's RPG data");
        sender.sendMessage(ChatColor.AQUA + "  /rpgadmin setrace <player> <race>" + ChatColor.WHITE + " - Set player's race");
        sender.sendMessage(ChatColor.AQUA + "  /rpgadmin setbloodline <player> <bloodline>" + ChatColor.WHITE + " - Set player's bloodline");
        sender.sendMessage(ChatColor.AQUA + "  /rpgadmin addstat <player> <stat> <amount>" + ChatColor.WHITE + " - Modify player stats");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "System:");
        sender.sendMessage(ChatColor.AQUA + "  /rpgadmin reload" + ChatColor.WHITE + " - Reload configuration");
        sender.sendMessage(ChatColor.AQUA + "  /rpgadmin save" + ChatColor.WHITE + " - Save all data");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "✦ ═══════════════════════════════════════ ✦");
        sender.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reset", "setrace", "setbloodline", "addstat", "reload", "save");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            // Player names for most commands
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setrace")) {
                for (Race race : Race.values()) {
                    if (race.name().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(race.name().toLowerCase());
                    }
                }
            } else if (args[0].equalsIgnoreCase("setbloodline")) {
                for (Bloodline bloodline : Bloodline.values()) {
                    String name = bloodline.name().toLowerCase().replace("_", " ");
                    if (name.startsWith(args[2].toLowerCase())) {
                        completions.add(name);
                    }
                }
            } else if (args[0].equalsIgnoreCase("addstat")) {
                List<String> stats = Arrays.asList("strength", "agility", "intelligence", "vitality", "luck");
                for (String stat : stats) {
                    if (stat.startsWith(args[2].toLowerCase())) {
                        completions.add(stat);
                    }
                }
            }
        }

        return completions;
    }
}