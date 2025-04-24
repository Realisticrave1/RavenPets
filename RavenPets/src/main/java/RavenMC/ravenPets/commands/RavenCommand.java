package RavenMC.ravenPets.commands;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.abilities.RavenAbility;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import RavenMC.ravenPets.utils.MessageUtil;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class RavenCommand implements CommandExecutor, TabCompleter {

    private final RavenPets plugin;

    public RavenCommand(RavenPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open the main GUI instead of showing text help
            plugin.getGUIManager().openMainGUI(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(player);
                break;
            case "gui":
                // Open the main GUI
                plugin.getGUIManager().openMainGUI(player);
                break;
            case "spawn":
                spawnRaven(player);
                break;
            case "despawn":
            case "dismiss":
                despawnRaven(player);
                break;
            case "info":
                // Open the main GUI instead of showing text info
                plugin.getGUIManager().openMainGUI(player);
                break;
            case "abilities":
                // Open the abilities GUI
                plugin.getGUIManager().openAbilitiesGUI(player);
                break;
            case "inventory":
                // Open the inventory GUI
                plugin.getGUIManager().openInventoryGUI(player);
                break;
            case "upgrade":
                // Open the upgrade GUI
                plugin.getGUIManager().openUpgradeGUI(player);
                break;
            case "settings":
                // Open the settings GUI
                plugin.getGUIManager().openSettingsGUI(player);
                break;
            case "name":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /ravenpet name <n>");
                    return true;
                }
                nameRaven(player, args[1]);
                break;
            case "home":
                if (args.length < 2) {
                    // Open the home locations GUI
                    plugin.getGUIManager().openHomeLocationsGUI(player);
                    return true;
                }
                handleHomeCommand(player, args);
                break;
            case "addxp":
                if (player.hasPermission("ravenpets.admin")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Usage: /ravenpet addxp <player> <amount>");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found!");
                        return true;
                    }

                    try {
                        int amount = Integer.parseInt(args[2]);
                        addRavenXP(target, amount);
                        player.sendMessage(ChatColor.GREEN + "Added " + amount + " XP to " + target.getName() + "'s raven!");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid XP amount!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;
            case "setlevel":
                if (player.hasPermission("ravenpets.admin")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Usage: /ravenpet setlevel <player> <level>");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found!");
                        return true;
                    }

                    try {
                        int level = Integer.parseInt(args[2]);
                        setRavenLevel(target, level);
                        player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s raven level to " + level + "!");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid level amount!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;
            case "reload":
                if (player.hasPermission("ravenpets.admin")) {
                    plugin.getConfigManager().reloadConfigs();
                    player.sendMessage(ChatColor.GREEN + "RavenPets configuration reloaded!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use /ravenpet help for a list of commands.");
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "==== RavenPets Commands ====");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet " + ChatColor.GRAY + "- Open the main raven interface");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet gui " + ChatColor.GRAY + "- Open the main raven interface");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet spawn " + ChatColor.GRAY + "- Summon your raven");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet despawn " + ChatColor.GRAY + "- Dismiss your raven");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet abilities " + ChatColor.GRAY + "- Open abilities interface");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet inventory " + ChatColor.GRAY + "- Open inventory interface");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet upgrade " + ChatColor.GRAY + "- Open upgrade interface");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet name <n> " + ChatColor.GRAY + "- Rename your raven");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet home " + ChatColor.GRAY + "- Open home locations interface");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet home add " + ChatColor.GRAY + "- Add a home location");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet home tp <id> " + ChatColor.GRAY + "- Teleport to a home location");

        if (player.hasPermission("ravenpets.admin")) {
            player.sendMessage(ChatColor.DARK_PURPLE + "==== Admin Commands ====");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet addxp <player> <amount> " + ChatColor.GRAY + "- Add XP to a player's raven");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet setlevel <player> <level> " + ChatColor.GRAY + "- Set a player's raven level");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "/ravenpet reload " + ChatColor.GRAY + "- Reload the plugin configuration");
        }
    }

    private void spawnRaven(Player player) {
        Raven raven = getRaven(player);
        if (raven == null) return;

        if (raven.isActive()) {
            player.sendMessage(ChatColor.RED + "Your raven is already active!");
            return;
        }

        plugin.getRavenManager().spawnRaven(player);
    }

    private void despawnRaven(Player player) {
        Raven raven = getRaven(player);
        if (raven == null) return;

        if (!raven.isActive()) {
            player.sendMessage(ChatColor.RED + "Your raven is not active!");
            return;
        }

        plugin.getRavenManager().despawnRaven(player);
    }

    private void nameRaven(Player player, String name) {
        Raven raven = getRaven(player);
        if (raven == null) return;

        // Check name length
        if (name.length() < 3 || name.length() > 16) {
            player.sendMessage(ChatColor.RED + "Raven name must be between 3 and 16 characters!");
            return;
        }

        // Check for inappropriate names (this would be more comprehensive)
        if (containsInappropriateWords(name)) {
            player.sendMessage(ChatColor.RED + "That name is not allowed!");
            return;
        }

        // Set name
        raven.setName(name);
        player.sendMessage(ChatColor.GREEN + "Your raven has been renamed to " + name + "!");
    }

    private boolean containsInappropriateWords(String name) {
        // This would check against a list of inappropriate words
        // For demonstration, just a simple check
        String nameLower = name.toLowerCase();
        String[] inappropriate = {"badword1", "badword2", "badword3"};

        for (String word : inappropriate) {
            if (nameLower.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private void handleHomeCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ravenpet home [add|list|tp]");
            return;
        }

        Raven raven = getRaven(player);
        if (raven == null) return;

        switch (args[1].toLowerCase()) {
            case "add":
                // Check if player can add more homes
                int maxHomes = getMaxHomeLocations(raven.getTier());
                if (raven.getHomeLocations().size() >= maxHomes) {
                    player.sendMessage(ChatColor.RED + "You can't add more home locations! Maximum: " + maxHomes);
                    return;
                }

                // Add home
                raven.addHomeLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Home location added! You now have " +
                        raven.getHomeLocations().size() + "/" + maxHomes + " home locations.");
                break;
            case "list":
                // Open the home locations GUI
                plugin.getGUIManager().openHomeLocationsGUI(player);
                break;
            case "tp":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /ravenpet home tp <id>");
                    return;
                }

                try {
                    int id = Integer.parseInt(args[2]) - 1; // Convert to 0-based index

                    if (id < 0 || id >= raven.getHomeLocations().size()) {
                        player.sendMessage(ChatColor.RED + "Invalid home location id!");
                        return;
                    }

                    // Check if teleportation ability is unlocked
                    if (!raven.hasAbility("teleportation")) {
                        player.sendMessage(ChatColor.RED + "Your raven needs the teleportation ability!");
                        return;
                    }

                    // Teleport player
                    player.teleport(raven.getHomeLocations().get(id));
                    player.sendMessage(ChatColor.GREEN + "Teleported to home location " + (id + 1) + "!");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid home location id!");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Usage: /ravenpet home [add|list|tp]");
                break;
        }
    }

    private int getMaxHomeLocations(RavenTier tier) {
        switch (tier) {
            case NOVICE: return 0;
            case ADEPT: return 3;
            case EXPERT: return 5;
            case MASTER: return 8;
            case LEGENDARY: return Integer.MAX_VALUE;
            default: return 0;
        }
    }

    private void addRavenXP(Player player, int amount) {
        Raven raven = getRaven(player);
        if (raven == null) return;

        raven.addXp(amount);
    }

    private void setRavenLevel(Player player, int level) {
        Raven raven = getRaven(player);
        if (raven == null) return;

        // Validate level
        if (level < 1 || level > 100) {
            return;
        }

        raven.setLevel(level);

        // Update tier based on level
        RavenTier newTier = RavenTier.getByLevel(level);
        // This would call the updateTier method in the Raven class
    }

    private Raven getRaven(Player player) {
        UUID playerId = player.getUniqueId();

        // Load raven if not loaded
        if (plugin.getRavenManager().getRavenByPlayer(playerId) == null) {
            plugin.getRavenManager().loadRaven(playerId);
        }

        Raven raven = plugin.getRavenManager().getRavenByPlayer(playerId);
        if (raven == null) {
            player.sendMessage(ChatColor.RED + "Error loading your raven! Please contact an administrator.");
        }

        return raven;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = Arrays.asList("help", "gui", "spawn", "despawn", "abilities", "inventory", "upgrade", "settings", "name", "home");

            if (sender.hasPermission("ravenpets.admin")) {
                commands = new ArrayList<>(commands);
                commands.add("addxp");
                commands.add("setlevel");
                commands.add("reload");
            }

            return filterCompletions(commands, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("home")) {
                return filterCompletions(Arrays.asList("add", "list", "tp"), args[1]);
            } else if (args[0].equalsIgnoreCase("addxp") || args[0].equalsIgnoreCase("setlevel")) {
                if (sender.hasPermission("ravenpets.admin")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("home") && args[1].equalsIgnoreCase("tp")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());

                    if (raven != null) {
                        int homes = raven.getHomeLocations().size();
                        List<String> homeIds = new ArrayList<>();

                        for (int i = 1; i <= homes; i++) {
                            homeIds.add(String.valueOf(i));
                        }

                        return filterCompletions(homeIds, args[2]);
                    }
                }
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String partialArg) {
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(partialArg.toLowerCase()))
                .collect(Collectors.toList());
    }
}