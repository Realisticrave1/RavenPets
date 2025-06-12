package RavenMC.ravenRpg.commands;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.gui.GuildManagementGUI;
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

public class GuildCommand implements CommandExecutor, TabCompleter {
    private RavenRpg plugin;

    public GuildCommand(RavenRpg plugin) {
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
            new GuildManagementGUI(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listGuilds(player);
                break;
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild create <name> [type]");
                    return true;
                }
                createGuild(player, args);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild join <name>");
                    return true;
                }
                joinGuild(player, args[1]);
                break;
            case "leave":
                leaveGuild(player);
                break;
            case "info":
                if (args.length < 2) {
                    showCurrentGuildInfo(player);
                } else {
                    showGuildInfo(player, args[1]);
                }
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild invite <player>");
                    return true;
                }
                invitePlayer(player, args[1]);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild kick <player>");
                    return true;
                }
                kickPlayer(player, args[1]);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown guild command! Use /guild for the GUI.");
        }

        return true;
    }

    private void listGuilds(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Available Guilds" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");

        for (GuildType type : GuildType.values()) {
            player.sendMessage(type.getSymbol() + " " + ChatColor.YELLOW + type.getDisplayName());
            player.sendMessage(ChatColor.WHITE + "  " + type.getDescription());

            // Show guilds of this type
            boolean foundGuilds = false;
            for (Guild guild : plugin.getGuildManager().getAllGuilds()) {
                if (guild.getType() == type) {
                    player.sendMessage(ChatColor.GRAY + "  • " + guild.getDisplayName() +
                            " (" + guild.getMembers().size() + " members)");
                    foundGuilds = true;
                }
            }

            if (!foundGuilds) {
                player.sendMessage(ChatColor.GRAY + "  • No guilds of this type yet");
            }
            player.sendMessage("");
        }

        player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void createGuild(Player player, String[] args) {
        String guildName = args[1];
        GuildType type = GuildType.MERCHANT_BAZAAR; // Default

        if (args.length > 2) {
            try {
                type = GuildType.valueOf(args[2].toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid guild type! Available types:");
                for (GuildType t : GuildType.values()) {
                    player.sendMessage(ChatColor.YELLOW + "  - " + t.name().toLowerCase());
                }
                return;
            }
        }

        String displayName = type.getSymbol() + " " + guildName;
        plugin.getGuildManager().createGuild(player, guildName, displayName, type);
    }

    private void joinGuild(Player player, String guildName) {
        plugin.getGuildManager().joinGuild(player, guildName);
    }

    private void leaveGuild(Player player) {
        plugin.getGuildManager().leaveGuild(player);
    }

    private void showCurrentGuildInfo(Player player) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            return;
        }
        displayGuildInfo(player, guild);
    }

    private void showGuildInfo(Player player, String guildName) {
        Guild guild = plugin.getGuildManager().getGuild(guildName);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ Guild not found! ✦");
            return;
        }
        displayGuildInfo(player, guild);
    }

    private void displayGuildInfo(Player player, Guild guild) {
        player.sendMessage("");
        player.sendMessage(guild.getType().getSymbol() + " " + ChatColor.GOLD + ChatColor.BOLD + guild.getDisplayName() + " " + guild.getType().getSymbol());
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + guild.getType().getDisplayName());
        player.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + guild.getDescription());
        player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + guild.getMembers().size());
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + guild.getLevel());
        player.sendMessage(ChatColor.YELLOW + "Treasury: " + ChatColor.GREEN + "$" + String.format("%.2f", guild.getTreasury()));

        Player leader = Bukkit.getPlayer(guild.getLeader());
        player.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE +
                (leader != null ? leader.getName() : "Offline"));

        if (!guild.getOfficers().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Officers: " + ChatColor.WHITE + guild.getOfficers().size());
        }

        player.sendMessage("");
    }

    private void invitePlayer(Player player, String targetName) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            return;
        }

        if (!guild.isLeader(player.getUniqueId()) && !guild.isOfficer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "✦ Only guild leaders and officers can invite players! ✦");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "✦ Player not found! ✦");
            return;
        }

        PlayerData targetData = plugin.getDataManager().getPlayerData(target);
        if (targetData.getCurrentGuild() != null) {
            player.sendMessage(ChatColor.RED + "✦ " + target.getName() + " is already in a guild! ✦");
            return;
        }

        // Send invitation
        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Guild Invitation" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        target.sendMessage("");
        target.sendMessage(ChatColor.YELLOW + "You have been invited to join:");
        target.sendMessage(guild.getType().getSymbol() + " " + ChatColor.AQUA + guild.getDisplayName());
        target.sendMessage(ChatColor.WHITE + "by " + ChatColor.YELLOW + player.getName());
        target.sendMessage("");
        target.sendMessage(ChatColor.GREEN + "Use /guild join " + guild.getName() + " to accept!");
        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════ ✦");
        target.sendMessage("");

        player.sendMessage(ChatColor.GREEN + "✦ Invitation sent to " + target.getName() + "! ✦");
    }

    private void kickPlayer(Player player, String targetName) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            return;
        }

        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "✦ Only the guild leader can kick players! ✦");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target != null && guild.isMember(target.getUniqueId())) {
            if (guild.isLeader(target.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "✦ You cannot kick yourself! ✦");
                return;
            }

            guild.removeMember(target.getUniqueId());
            PlayerData targetData = plugin.getDataManager().getPlayerData(target);
            targetData.setCurrentGuild(null);
            plugin.getDataManager().savePlayerData(targetData);

            target.sendMessage(ChatColor.RED + "✦ You have been removed from " + guild.getDisplayName() + "! ✦");
            player.sendMessage(ChatColor.YELLOW + "✦ " + target.getName() + " has been removed from the guild. ✦");

            plugin.getGuildManager().saveGuilds();
        } else {
            player.sendMessage(ChatColor.RED + "✦ Player not found or not in your guild! ✦");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("list", "create", "join", "leave", "info", "invite", "kick");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info")) {
                for (Guild guild : plugin.getGuildManager().getAllGuilds()) {
                    if (guild.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(guild.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            for (GuildType type : GuildType.values()) {
                String typeName = type.name().toLowerCase();
                if (typeName.startsWith(args[2].toLowerCase())) {
                    completions.add(typeName);
                }
            }
        }

        return completions;
    }
}