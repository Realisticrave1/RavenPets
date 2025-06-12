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
        PlayerData data = plugin.getDataManager().getPlayerData(player);

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
                showStats(player);
                break;
            case "profile":
                showProfile(player);
                break;
            case "skills":
                showSkills(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "✦ Unknown command! Use /rpg help ✦");
        }

        return true;
    }

    private void handleRaceCommand(Player player, String[] args) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (args.length == 1) {
            new RaceSelectionGUI(plugin, player).open();
            return;
        }

        if (data.hasChosenRace()) {
            player.sendMessage(ChatColor.RED + "✦ You have already chosen your race! ✦");
            return;
        }

        try {
            Race race = Race.valueOf(args[1].toUpperCase());
            plugin.getRaceManager().selectRace(player, race);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "✦ Invalid race! Available: " + Arrays.toString(Race.values()) + " ✦");
        }
    }

    private void handleBloodlineCommand(Player player, String[] args) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

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
            return;
        }

        try {
            Bloodline bloodline = Bloodline.valueOf(args[1].toUpperCase().replace(" ", "_"));
            plugin.getBloodlineManager().selectBloodline(player, bloodline);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "✦ Invalid bloodline! Use the GUI to select. ✦");
        }
    }

    private void showStats(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Your Stats" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");

        for (String stat : Arrays.asList("strength", "agility", "intelligence", "vitality", "luck")) {
            int value = data.getStat(stat);
            player.sendMessage(ChatColor.YELLOW + "  " + capitalize(stat) + ": " + ChatColor.WHITE + value);
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showProfile(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "Character Profile" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Name: " + ChatColor.WHITE + player.getName());

        if (data.getSelectedRace() != null) {
            player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Race: " + ChatColor.WHITE + data.getSelectedRace().getDisplayName());
        }

        if (data.getSelectedBloodline() != null) {
            player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Bloodline: " + ChatColor.WHITE + data.getSelectedBloodline().getDisplayName());
        }

        if (data.getCurrentGuild() != null) {
            Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
            if (guild != null) {
                player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Guild: " + ChatColor.WHITE + guild.getDisplayName());
            }
        }

        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Shops Owned: " + ChatColor.WHITE + data.getOwnedShops().size());
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showSkills(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "✦ ═══════ " + ChatColor.BOLD + "Your Skills" + ChatColor.RESET + ChatColor.AQUA + " ═══════ ✦");
        player.sendMessage("");

        for (String skill : Arrays.asList("combat", "mining", "woodcutting", "fishing", "crafting", "trading")) {
            int level = data.getSkill(skill);
            player.sendMessage(ChatColor.YELLOW + "  " + capitalize(skill) + ": " + ChatColor.WHITE + "Level " + level);
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "✦ ══════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenRpg Help" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Character Commands:");
        player.sendMessage(ChatColor.AQUA + "  /rpg" + ChatColor.WHITE + " - Open main RPG menu");
        player.sendMessage(ChatColor.AQUA + "  /rpg race" + ChatColor.WHITE + " - Choose your race");
        player.sendMessage(ChatColor.AQUA + "  /rpg bloodline" + ChatColor.WHITE + " - Choose your bloodline");
        player.sendMessage(ChatColor.AQUA + "  /rpg stats" + ChatColor.WHITE + " - View your stats");
        player.sendMessage(ChatColor.AQUA + "  /rpg profile" + ChatColor.WHITE + " - View your profile");
        player.sendMessage(ChatColor.AQUA + "  /rpg skills" + ChatColor.WHITE + " - View your skills");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Guild Commands:");
        player.sendMessage(ChatColor.AQUA + "  /guild list" + ChatColor.WHITE + " - List all guilds");
        player.sendMessage(ChatColor.AQUA + "  /guild create <name>" + ChatColor.WHITE + " - Create a guild");
        player.sendMessage(ChatColor.AQUA + "  /guild join <name>" + ChatColor.WHITE + " - Join a guild");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Shop Commands:");
        player.sendMessage(ChatColor.AQUA + "  /shop create <name>" + ChatColor.WHITE + " - Create a shop");
        player.sendMessage(ChatColor.AQUA + "  /shop list" + ChatColor.WHITE + " - List your shops");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "race", "bloodline", "stats", "profile", "skills");
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

        return completions;
    }
}