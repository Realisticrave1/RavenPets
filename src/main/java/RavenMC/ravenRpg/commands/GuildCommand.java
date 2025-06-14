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

        // Check if plugin is fully loaded
        if (!plugin.isPluginFullyLoaded()) {
            player.sendMessage(ChatColor.RED + "✦ Plugin is still loading, please wait... ✦");
            return true;
        }

        try {
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
                    kickPlayerCommand(player, args[1]);
                    break;
                case "disband":
                    disbandGuildCommand(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown guild command! Use /guild for the GUI.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing guild command for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "✦ An error occurred! Please try again or contact an administrator. ✦");
        }

        return true;
    }

    private void listGuilds(Player player) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Available Guilds" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
            player.sendMessage("");

            for (GuildType type : GuildType.values()) {
                player.sendMessage(type.getSymbol() + " " + ChatColor.YELLOW + type.getDisplayName());
                player.sendMessage(ChatColor.WHITE + "  " + type.getDescription());

                // Show guilds of this type
                boolean foundGuilds = false;
                for (Guild guild : plugin.getGuildManager().getAllGuilds()) {
                    if (guild != null && guild.getType() == type) {
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
        } catch (Exception e) {
            plugin.getLogger().severe("Error listing guilds for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error loading guild list! ✦");
        }
    }

    private void createGuild(Player player, String[] args) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

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
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating guild for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error creating guild! Please try again. ✦");
        }
    }

    private void joinGuild(Player player, String guildName) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

            plugin.getGuildManager().joinGuild(player, guildName);
        } catch (Exception e) {
            plugin.getLogger().severe("Error joining guild for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error joining guild! Please try again. ✦");
        }
    }

    private void leaveGuild(Player player) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

            plugin.getGuildManager().leaveGuild(player);
        } catch (Exception e) {
            plugin.getLogger().severe("Error leaving guild for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error leaving guild! Please try again. ✦");
        }
    }

    private void showCurrentGuildInfo(Player player) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

            Guild guild = plugin.getGuildManager().getPlayerGuild(player);
            if (guild == null) {
                player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return;
            }
            displayGuildInfo(player, guild);
        } catch (Exception e) {
            plugin.getLogger().severe("Error showing guild info for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error loading guild information! ✦");
        }
    }

    private void showGuildInfo(Player player, String guildName) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

            Guild guild = plugin.getGuildManager().getGuild(guildName);
            if (guild == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild not found! ✦");
                return;
            }
            displayGuildInfo(player, guild);
        } catch (Exception e) {
            plugin.getLogger().severe("Error showing guild info for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error loading guild information! ✦");
        }
    }

    private void displayGuildInfo(Player player, Guild guild) {
        try {
            if (guild == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild information unavailable! ✦");
                return;
            }

            player.sendMessage("");
            player.sendMessage(guild.getType().getSymbol() + " " + ChatColor.GOLD + ChatColor.BOLD + guild.getDisplayName() + " " + guild.getType().getSymbol());
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + guild.getType().getDisplayName());
            player.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + guild.getDescription());
            player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + guild.getMembers().size());
            player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + guild.getLevel());
            player.sendMessage(ChatColor.YELLOW + "Treasury: " + ChatColor.GREEN + "$" + String.format("%.2f", guild.getTreasury()));

            Player leader = guild.getLeader() != null ? Bukkit.getPlayer(guild.getLeader()) : null;
            player.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE +
                    (leader != null ? leader.getName() : "Offline"));

            if (!guild.getOfficers().isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Officers: " + ChatColor.WHITE + guild.getOfficers().size());
            }

            player.sendMessage("");
        } catch (Exception e) {
            plugin.getLogger().severe("Error displaying guild info: " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error displaying guild information! ✦");
        }
    }

    private void invitePlayer(Player player, String targetName) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

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

            if (plugin.getDataManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Player data system is not available! ✦");
                return;
            }

            PlayerData targetData = plugin.getDataManager().getPlayerData(target);
            if (targetData == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading player data! ✦");
                return;
            }

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
        } catch (Exception e) {
            plugin.getLogger().severe("Error inviting player for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error sending invitation! Please try again. ✦");
        }
    }

    private void kickPlayerCommand(Player player, String targetName) {
        player.sendMessage(ChatColor.YELLOW + "✦ Use the guild management GUI for better member management! ✦");
        player.sendMessage(ChatColor.AQUA + "✦ Type /guild to open the GUI and click 'Kick Member' ✦");
    }

    private void disbandGuildCommand(Player player) {
        try {
            if (plugin.getGuildManager() == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild system is not available! ✦");
                return;
            }

            Guild guild = plugin.getGuildManager().getPlayerGuild(player);
            if (guild == null) {
                player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return;
            }

            if (!guild.isLeader(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "✦ Only the guild leader can disband the guild! ✦");
                return;
            }

            // Require confirmation
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "✦ ═══════ " + ChatColor.BOLD + "GUILD DISBANDING" + ChatColor.RESET + ChatColor.RED + " ═══════ ✦");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Are you sure you want to disband " + guild.getDisplayName() + "?");
            player.sendMessage(ChatColor.RED + "This action CANNOT be undone!");
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "Use the GUI to confirm (type /guild and click Disband)");
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "✦ ═══════════════════════════════════════ ✦");
            player.sendMessage("");
        } catch (Exception e) {
            plugin.getLogger().severe("Error in disband command for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error accessing guild information! ✦");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (args.length == 1) {
                List<String> subcommands = Arrays.asList("list", "create", "join", "leave", "info", "invite", "kick", "disband");
                for (String subcommand : subcommands) {
                    if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(subcommand);
                    }
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info")) {
                    if (plugin.getGuildManager() != null) {
                        for (Guild guild : plugin.getGuildManager().getAllGuilds()) {
                            if (guild != null && guild.getName() != null &&
                                    guild.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(guild.getName());
                            }
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
        } catch (Exception e) {
            plugin.getLogger().warning("Error in guild command tab completion: " + e.getMessage());
        }

        return completions;
    }
}