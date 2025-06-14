package RavenMC.ravenRpg.commands;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.gui.*;
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

public class RpgCommand implements CommandExecutor, TabCompleter {
    private RavenRpg plugin;

    public RpgCommand(RavenRpg plugin) {
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
            // Get player data safely
            PlayerData data = null;
            if (plugin.getDataManager() != null) {
                data = plugin.getDataManager().getPlayerData(player);
            }

            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your character data! Please try again. ✦");
                return true;
            }

            if (args.length == 0) {
                if (!data.isFullyInitialized()) {
                    new CharacterCreationGUI(plugin, player).open();
                } else {
                    new RpgMainGUI(plugin, player).open();
                }
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "help":
                    showHelp(player);
                    break;
                case "race":
                    handleRaceCommand(player, args);
                    break;
                case "bloodline":
                    handleBloodlineCommand(player, args);
                    break;
                case "stats":
                    new PlayerStatsGUI(plugin, player).open();
                    break;
                case "profile":
                    new PlayerProfileGUI(plugin, player).open();
                    break;
                case "skills":
                    new PlayerSkillsGUI(plugin, player).open();
                    break;
                case "menu":
                case "gui":
                    new RpgMainGUI(plugin, player).open();
                    break;
                case "ravenpets":
                    handleRavenPetsCommand(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "✦ Unknown command! Use /rpg help or /rpg gui ✦");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing RPG command for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "✦ An error occurred! Please try again or contact an administrator. ✦");
        }

        return true;
    }

    private void handleRaceCommand(Player player, String[] args) {
        try {
            if (plugin.getDataManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Character system is not available! ✦");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your character data! ✦");
                return;
            }

            if (args.length == 1) {
                new RaceSelectionGUI(plugin, player).open();
                return;
            }

            if (data.hasChosenRace()) {
                player.sendMessage(ChatColor.RED + "✦ You have already chosen your race! ✦");
                player.sendMessage(ChatColor.AQUA + "✦ Use the GUI to view your race info: /rpg race ✦");
                return;
            }

            try {
                Race race = Race.valueOf(args[1].toUpperCase());
                if (plugin.getRaceManager() != null) {
                    plugin.getRaceManager().selectRace(player, race);
                } else {
                    player.sendMessage(ChatColor.RED + "✦ Race system is not available! ✦");
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "✦ Invalid race! Available: " + Arrays.toString(Race.values()) + " ✦");
                player.sendMessage(ChatColor.AQUA + "✦ Use /rpg race to open the selection GUI ✦");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error in race command for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error processing race command! ✦");
        }
    }

    private void handleBloodlineCommand(Player player, String[] args) {
        try {
            if (plugin.getDataManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Character system is not available! ✦");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your character data! ✦");
                return;
            }

            if (args.length == 1) {
                new BloodlineSelectionGUI(plugin, player).open();
                return;
            }

            if (!data.hasChosenRace()) {
                player.sendMessage(ChatColor.RED + "✦ You must choose a race first! Use /rpg race ✦");
                return;
            }

            if (data.hasChosenBloodline()) {
                player.sendMessage(ChatColor.RED + "✦ You have already chosen your bloodline! ✦");
                player.sendMessage(ChatColor.AQUA + "✦ Use the GUI to view your bloodline info: /rpg bloodline ✦");
                return;
            }

            try {
                Bloodline bloodline = Bloodline.valueOf(args[1].toUpperCase().replace(" ", "_"));
                if (plugin.getBloodlineManager() != null) {
                    plugin.getBloodlineManager().selectBloodline(player, bloodline);
                } else {
                    player.sendMessage(ChatColor.RED + "✦ Bloodline system is not available! ✦");
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "✦ Invalid bloodline! Use the GUI to select. ✦");
                player.sendMessage(ChatColor.AQUA + "✦ Use /rpg bloodline to open the selection GUI ✦");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error in bloodline command for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error processing bloodline command! ✦");
        }
    }

    private void handleRavenPetsCommand(Player player) {
        try {
            if (!plugin.isRavenPetsEnabled()) {
                player.sendMessage(ChatColor.RED + "✦ RavenPets integration is not available! ✦");
                return;
            }

            if (plugin.getDataManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Character system is not available! ✦");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your character data! ✦");
                return;
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenPets Integration" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
            player.sendMessage("");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Your RPG character enhances your raven companion! ✦");
            player.sendMessage("");

            if (data.isFullyInitialized()) {
                // Show current bonuses
                if (plugin.getRavenPetsIntegration() != null) {
                    var bonuses = plugin.getRavenPetsIntegration().getPlayerRavenBonuses(player);

                    player.sendMessage(ChatColor.YELLOW + "Current Bonuses:");
                    player.sendMessage(ChatColor.GREEN + "  • Total EXP Multiplier: " + ChatColor.GOLD + String.format("%.2fx", (Double) bonuses.get("expMultiplier")));
                    player.sendMessage(ChatColor.GREEN + "  • Intelligence Bonus: " + ChatColor.WHITE + "+" + bonuses.get("intelligenceBonus") + "% Raven EXP");
                    player.sendMessage(ChatColor.GREEN + "  • Luck Bonus: " + ChatColor.WHITE + "+" + bonuses.get("luckBonus") + "% Raven EXP");
                    player.sendMessage(ChatColor.GREEN + "  • Skill Bonus: " + ChatColor.WHITE + "+" + bonuses.get("skillBonus") + "% Raven EXP");
                    player.sendMessage(ChatColor.GREEN + "  • Racial Bonus: " + ChatColor.AQUA + bonuses.get("racialBonus"));
                    player.sendMessage(ChatColor.GREEN + "  • Bloodline Bonus: " + ChatColor.LIGHT_PURPLE + bonuses.get("bloodlineBonus"));
                } else {
                    player.sendMessage(ChatColor.RED + "RavenPets integration system is not properly loaded!");
                }

                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "Tips to improve your raven:");
                player.sendMessage(ChatColor.WHITE + "  • Increase Intelligence and Luck stats");
                player.sendMessage(ChatColor.WHITE + "  • Level up your skills through activities");
                player.sendMessage(ChatColor.WHITE + "  • Complete your character if not done");
            } else {
                player.sendMessage(ChatColor.RED + "Complete your character creation to unlock raven bonuses!");
                player.sendMessage(ChatColor.AQUA + "Use /rpg to finish character creation.");
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.AQUA + "Use /raven to manage your companion!");
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════════════════════════════════════ ✦");
            player.sendMessage("");
        } catch (Exception e) {
            plugin.getLogger().severe("Error in RavenPets command for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error accessing RavenPets integration! ✦");
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenRpg Help" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Main Commands:");
        player.sendMessage(ChatColor.AQUA + "  /rpg" + ChatColor.WHITE + " - Open main RPG menu (GUI)");
        player.sendMessage(ChatColor.AQUA + "  /rpg gui" + ChatColor.WHITE + " - Open main RPG menu");
        player.sendMessage(ChatColor.AQUA + "  /rpg help" + ChatColor.WHITE + " - Show this help message");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Character Commands:");
        player.sendMessage(ChatColor.AQUA + "  /rpg race" + ChatColor.WHITE + " - Choose/view your race (GUI)");
        player.sendMessage(ChatColor.AQUA + "  /rpg bloodline" + ChatColor.WHITE + " - Choose/view your bloodline (GUI)");
        player.sendMessage(ChatColor.AQUA + "  /rpg stats" + ChatColor.WHITE + " - View your stats (GUI)");
        player.sendMessage(ChatColor.AQUA + "  /rpg skills" + ChatColor.WHITE + " - View your skills (GUI)");
        player.sendMessage(ChatColor.AQUA + "  /rpg profile" + ChatColor.WHITE + " - View your profile (GUI)");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Other Systems:");
        player.sendMessage(ChatColor.AQUA + "  /guild" + ChatColor.WHITE + " - Guild management (GUI)");
        player.sendMessage(ChatColor.AQUA + "  /shop" + ChatColor.WHITE + " - Shop management (GUI)");

        if (plugin.isRavenPetsEnabled()) {
            player.sendMessage(ChatColor.AQUA + "  /rpg ravenpets" + ChatColor.WHITE + " - RavenPets integration info");
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✦ Most features are now available through interactive GUIs! ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═════════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (args.length == 1) {
                List<String> subcommands = Arrays.asList("help", "race", "bloodline", "stats", "profile", "skills", "gui", "menu");

                if (plugin.isRavenPetsEnabled()) {
                    subcommands = new ArrayList<>(subcommands);
                    subcommands.add("ravenpets");
                }

                for (String subcommand : subcommands) {
                    if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(subcommand);
                    }
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("race")) {
                    for (Race race : Race.values()) {
                        if (race.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(race.name().toLowerCase());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("bloodline")) {
                    for (Bloodline bloodline : Bloodline.values()) {
                        String name = bloodline.name().toLowerCase().replace("_", " ");
                        if (name.startsWith(args[1].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in RPG command tab completion: " + e.getMessage());
        }

        return completions;
    }
}