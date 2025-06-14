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

        // Check if plugin is fully loaded
        if (!plugin.isPluginFullyLoaded()) {
            sender.sendMessage(ChatColor.RED + "✦ Plugin is still loading, please wait... ✦");
            return true;
        }

        try {
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
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing admin command: " + e.getMessage());
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "✦ An error occurred! Please try again or check the console. ✦");
        }

        return true;
    }

    private void resetPlayer(CommandSender sender, String playerName) {
        try {
            if (plugin.getDataManager() == null) {
                sender.sendMessage(ChatColor.RED + "✦ Data system is not available! ✦");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Error loading player data!");
                return;
            }

            // Reset character data
            data.setSelectedRace(null);
            data.setSelectedBloodline(null);
            data.setCurrentGuild(null);
            data.getOwnedShops().clear();

            // Reset stats to defaults
            data.getStats().clear();
            data.getSkills().clear();

            // Re-initialize defaults
            data.validateAndFix();

            plugin.getDataManager().savePlayerData(data);

            sender.sendMessage(ChatColor.GREEN + "✦ Reset " + target.getName() + "'s RPG data! ✦");
            target.sendMessage(ChatColor.YELLOW + "✦ Your RPG data has been reset by an admin! ✦");
        } catch (Exception e) {
            plugin.getLogger().severe("Error resetting player " + playerName + ": " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "✦ Error resetting player data! ✦");
        }
    }

    private void setPlayerRace(CommandSender sender, String playerName, String raceName) {
        try {
            if (plugin.getDataManager() == null) {
                sender.sendMessage(ChatColor.RED + "✦ Data system is not available! ✦");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Error loading player data!");
                return;
            }

            try {
                Race race = Race.valueOf(raceName.toUpperCase());
                data.setSelectedRace(race);

                // Apply racial bonuses
                for (String stat : race.getStatBonuses().keySet()) {
                    int bonus = race.getStatBonuses().get(stat);
                    data.setStat(stat, 10 + bonus); // Base 10 + racial bonus
                }

                plugin.getDataManager().savePlayerData(data);

                sender.sendMessage(ChatColor.GREEN + "✦ Set " + target.getName() + "'s race to " + race.getDisplayName() + "! ✦");
                target.sendMessage(ChatColor.YELLOW + "✦ Your race has been set to " + race.getDisplayName() + " by an admin! ✦");

                // Apply RavenPets bonuses if available
                if (plugin.isRavenPetsEnabled() && plugin.getRavenPetsIntegration() != null) {
                    plugin.getRavenPetsIntegration().applyRpgBonusesToRaven(target);
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid race! Available: " + Arrays.toString(Race.values()));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error setting race for " + playerName + ": " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "✦ Error setting player race! ✦");
        }
    }

    private void setPlayerBloodline(CommandSender sender, String playerName, String bloodlineName) {
        try {
            if (plugin.getDataManager() == null) {
                sender.sendMessage(ChatColor.RED + "✦ Data system is not available! ✦");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Error loading player data!");
                return;
            }

            try {
                Bloodline bloodline = Bloodline.valueOf(bloodlineName.toUpperCase().replace(" ", "_"));
                data.setSelectedBloodline(bloodline);
                plugin.getDataManager().savePlayerData(data);

                sender.sendMessage(ChatColor.GREEN + "✦ Set " + target.getName() + "'s bloodline to " + bloodline.getDisplayName() + "! ✦");
                target.sendMessage(ChatColor.YELLOW + "✦ Your bloodline has been set to " + bloodline.getDisplayName() + " by an admin! ✦");

                // Apply RavenPets bonuses if available
                if (plugin.isRavenPetsEnabled() && plugin.getRavenPetsIntegration() != null) {
                    plugin.getRavenPetsIntegration().applyRpgBonusesToRaven(target);
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid bloodline! Available: " + Arrays.toString(Bloodline.values()));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error setting bloodline for " + playerName + ": " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "✦ Error setting player bloodline! ✦");
        }
    }

    private void addPlayerStat(CommandSender sender, String playerName, String statName, String amountStr) {
        try {
            if (plugin.getDataManager() == null) {
                sender.sendMessage(ChatColor.RED + "✦ Data system is not available! ✦");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(target);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "Error loading player data!");
                return;
            }

            try {
                int amount = Integer.parseInt(amountStr);
                int oldValue = data.getStat(statName);
                data.addStat(statName, amount);
                int newValue = data.getStat(statName);

                plugin.getDataManager().savePlayerData(data);

                sender.sendMessage(ChatColor.GREEN + "✦ Added " + amount + " " + statName + " to " + target.getName() + " (was " + oldValue + ", now " + newValue + ")! ✦");
                target.sendMessage(ChatColor.YELLOW + "✦ Your " + statName + " was modified by an admin! ✦");

                // Apply RavenPets bonuses if available
                if (plugin.isRavenPetsEnabled() && plugin.getRavenPetsIntegration() != null) {
                    plugin.getRavenPetsIntegration().onStatIncrease(target, statName, oldValue, newValue);
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount! Use a number.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding stat for " + playerName + ": " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "✦ Error modifying player stat! ✦");
        }
    }

    private void reloadPlugin(CommandSender sender) {
        try {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "✦ RavenRpg configuration reloaded! ✦");
        } catch (Exception e) {
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "✦ Error reloading configuration! ✦");
        }
    }

    private void saveAllData(CommandSender sender) {
        try {
            int savedCount = 0;

            if (plugin.getDataManager() != null) {
                plugin.getDataManager().saveAllData();
                savedCount++;
            }

            if (plugin.getGuildManager() != null) {
                plugin.getGuildManager().saveGuilds();
                savedCount++;
            }

            if (plugin.getShopManager() != null) {
                plugin.getShopManager().saveAllShops();
                savedCount++;
            }

            sender.sendMessage(ChatColor.GREEN + "✦ All RavenRpg data saved! (" + savedCount + " systems) ✦");
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving data: " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "✦ Error saving data! ✦");
        }
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
        sender.sendMessage(ChatColor.YELLOW + "Available Races: " + ChatColor.WHITE + Arrays.toString(Race.values()));
        sender.sendMessage(ChatColor.YELLOW + "Available Bloodlines: " + ChatColor.WHITE + Arrays.toString(Bloodline.values()));
        sender.sendMessage(ChatColor.YELLOW + "Available Stats: " + ChatColor.WHITE + "strength, agility, intelligence, vitality, luck");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "✦ ═══════════════════════════════════════ ✦");
        sender.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        try {
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
            } else if (args.length == 4 && args[0].equalsIgnoreCase("addstat")) {
                // Suggest common amounts
                List<String> amounts = Arrays.asList("1", "5", "10", "-1", "-5", "-10");
                for (String amount : amounts) {
                    if (amount.startsWith(args[3])) {
                        completions.add(amount);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in admin command tab completion: " + e.getMessage());
        }

        return completions;
    }
}