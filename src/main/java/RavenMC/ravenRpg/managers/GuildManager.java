package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GuildManager {
    private RavenRpg plugin;
    private Map<String, Guild> guilds;
    private File guildFile;

    public GuildManager(RavenRpg plugin) {
        this.plugin = plugin;
        this.guilds = new HashMap<>();
        this.guildFile = new File(plugin.getDataFolder(), "guilds.yml");
        loadGuilds();
    }

    public boolean createGuild(Player player, String guildName, String displayName, GuildType type) {
        if (player == null || guildName == null || guildName.trim().isEmpty()) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "✦ Invalid guild name! ✦");
            }
            return false;
        }

        try {
            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your data! Please try again. ✦");
                return false;
            }

            if (data.getCurrentGuild() != null) {
                player.sendMessage(ChatColor.RED + "✦ You are already in a guild! ✦");
                return false;
            }

            if (guilds.containsKey(guildName.toLowerCase())) {
                player.sendMessage(ChatColor.RED + "✦ A guild with that name already exists! ✦");
                return false;
            }

            // Check if player has enough money (if configured)
            double creationCost = plugin.getConfig().getDouble("guild.creation-cost", 1000.0);
            if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(player) < creationCost) {
                player.sendMessage(ChatColor.RED + "✦ You need $" + creationCost + " to create a guild! ✦");
                return false;
            }

            // Ensure displayName is not null
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = (type != null ? type.getSymbol() + " " : "") + guildName;
            }

            // Create guild
            Guild guild = new Guild(guildName, displayName, "A new guild", type, player.getUniqueId());
            guilds.put(guildName.toLowerCase(), guild);
            data.setCurrentGuild(guildName.toLowerCase());

            // Take money
            if (plugin.getEconomy() != null) {
                plugin.getEconomy().withdrawPlayer(player, creationCost);
            }

            player.sendMessage(ChatColor.GREEN + "✦ Guild '" + guild.getDisplayName() + "' created successfully! ✦");
            if (type != null) {
                player.sendMessage(ChatColor.GOLD + "✦ Type: " + type.getDisplayName() + " ✦");
            }

            saveGuilds();
            plugin.getDataManager().savePlayerData(data);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating guild for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "✦ Error creating guild! Please contact an administrator. ✦");
            return false;
        }
    }

    public boolean joinGuild(Player player, String guildName) {
        if (player == null || guildName == null) {
            return false;
        }

        try {
            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your data! Please try again. ✦");
                return false;
            }

            if (data.getCurrentGuild() != null) {
                player.sendMessage(ChatColor.RED + "✦ You are already in a guild! Leave first with /guild leave ✦");
                return false;
            }

            Guild guild = guilds.get(guildName.toLowerCase());
            if (guild == null) {
                player.sendMessage(ChatColor.RED + "✦ Guild not found! ✦");
                return false;
            }

            if (guild.addMember(player.getUniqueId())) {
                data.setCurrentGuild(guildName.toLowerCase());
                player.sendMessage(ChatColor.GREEN + "✦ You have joined " + guild.getDisplayName() + "! ✦");

                // Notify other members
                for (UUID memberUUID : guild.getMembers()) {
                    Player member = plugin.getServer().getPlayer(memberUUID);
                    if (member != null && !member.equals(player)) {
                        member.sendMessage(ChatColor.YELLOW + "✦ " + player.getName() + " has joined the guild! ✦");
                    }
                }

                saveGuilds();
                plugin.getDataManager().savePlayerData(data);
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error joining guild for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error joining guild! Please try again. ✦");
        }

        return false;
    }

    public boolean leaveGuild(Player player) {
        if (player == null) return false;

        try {
            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                player.sendMessage(ChatColor.RED + "✦ Error loading your data! Please try again. ✦");
                return false;
            }

            String guildName = data.getCurrentGuild();
            if (guildName == null) {
                player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return false;
            }

            Guild guild = guilds.get(guildName);
            if (guild == null) {
                data.setCurrentGuild(null);
                plugin.getDataManager().savePlayerData(data);
                return true;
            }

            if (guild.isLeader(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "✦ You cannot leave as the guild leader! Transfer leadership first. ✦");
                return false;
            }

            guild.removeMember(player.getUniqueId());
            data.setCurrentGuild(null);

            player.sendMessage(ChatColor.YELLOW + "✦ You have left " + guild.getDisplayName() + "! ✦");

            // Notify other members
            for (UUID memberUUID : guild.getMembers()) {
                Player member = plugin.getServer().getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(ChatColor.YELLOW + "✦ " + player.getName() + " has left the guild! ✦");
                }
            }

            saveGuilds();
            plugin.getDataManager().savePlayerData(data);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error leaving guild for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✦ Error leaving guild! Please try again. ✦");
            return false;
        }
    }

    public boolean promoteMember(Player leader, UUID memberUUID) {
        if (leader == null || memberUUID == null) return false;

        try {
            Guild guild = getPlayerGuild(leader);
            if (guild == null) {
                leader.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return false;
            }

            if (!guild.isLeader(leader.getUniqueId())) {
                leader.sendMessage(ChatColor.RED + "✦ Only guild leaders can promote members! ✦");
                return false;
            }

            if (!guild.isMember(memberUUID)) {
                leader.sendMessage(ChatColor.RED + "✦ Player is not in your guild! ✦");
                return false;
            }

            if (guild.isOfficer(memberUUID) || guild.isLeader(memberUUID)) {
                leader.sendMessage(ChatColor.RED + "✦ Player is already an officer or leader! ✦");
                return false;
            }

            if (guild.promoteToOfficer(memberUUID)) {
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(memberUUID);
                String targetName = target.getName() != null ? target.getName() : "Unknown";

                leader.sendMessage(ChatColor.GREEN + "✦ " + targetName + " has been promoted to officer! ✦");

                // Notify the promoted player if online
                Player onlineTarget = plugin.getServer().getPlayer(memberUUID);
                if (onlineTarget != null) {
                    onlineTarget.sendMessage(ChatColor.GOLD + "✦ You have been promoted to officer in " + guild.getDisplayName() + "! ✦");
                }

                // Notify other guild members
                for (UUID guildMemberUUID : guild.getMembers()) {
                    Player member = plugin.getServer().getPlayer(guildMemberUUID);
                    if (member != null && !member.equals(leader) && !member.getUniqueId().equals(memberUUID)) {
                        member.sendMessage(ChatColor.YELLOW + "✦ " + targetName + " has been promoted to officer! ✦");
                    }
                }

                saveGuilds();
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error promoting member: " + e.getMessage());
            leader.sendMessage(ChatColor.RED + "✦ Error promoting member! Please try again. ✦");
        }

        return false;
    }

    public boolean kickMember(Player kicker, UUID memberUUID) {
        if (kicker == null || memberUUID == null) return false;

        try {
            Guild guild = getPlayerGuild(kicker);
            if (guild == null) {
                kicker.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return false;
            }

            // Check permissions
            boolean canKick = false;
            if (guild.isLeader(kicker.getUniqueId())) {
                canKick = !guild.isLeader(memberUUID); // Leaders can kick anyone except other leaders
            } else if (guild.isOfficer(kicker.getUniqueId())) {
                canKick = !guild.isOfficer(memberUUID) && !guild.isLeader(memberUUID); // Officers can kick regular members only
            }

            if (!canKick) {
                kicker.sendMessage(ChatColor.RED + "✦ You don't have permission to kick this player! ✦");
                return false;
            }

            if (!guild.isMember(memberUUID)) {
                kicker.sendMessage(ChatColor.RED + "✦ Player is not in your guild! ✦");
                return false;
            }

            OfflinePlayer target = plugin.getServer().getOfflinePlayer(memberUUID);
            String targetName = target.getName() != null ? target.getName() : "Unknown";

            // Remove from guild
            guild.removeMember(memberUUID);

            // Update player data
            PlayerData targetData = plugin.getDataManager().getPlayerData(memberUUID, targetName);
            targetData.setCurrentGuild(null);
            plugin.getDataManager().savePlayerData(targetData);

            kicker.sendMessage(ChatColor.YELLOW + "✦ " + targetName + " has been removed from the guild! ✦");

            // Notify the kicked player if online
            Player onlineTarget = plugin.getServer().getPlayer(memberUUID);
            if (onlineTarget != null) {
                onlineTarget.sendMessage(ChatColor.RED + "✦ You have been removed from " + guild.getDisplayName() + "! ✦");
            }

            // Notify other guild members
            for (UUID guildMemberUUID : guild.getMembers()) {
                Player member = plugin.getServer().getPlayer(guildMemberUUID);
                if (member != null && !member.equals(kicker)) {
                    member.sendMessage(ChatColor.YELLOW + "✦ " + targetName + " has been removed from the guild! ✦");
                }
            }

            saveGuilds();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error kicking member: " + e.getMessage());
            kicker.sendMessage(ChatColor.RED + "✦ Error kicking member! Please try again. ✦");
            return false;
        }
    }

    public boolean invitePlayer(Player inviter, OfflinePlayer target) {
        if (inviter == null || target == null) return false;

        try {
            Guild guild = getPlayerGuild(inviter);
            if (guild == null) {
                inviter.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return false;
            }

            if (!guild.isLeader(inviter.getUniqueId()) && !guild.isOfficer(inviter.getUniqueId())) {
                inviter.sendMessage(ChatColor.RED + "✦ Only guild leaders and officers can invite players! ✦");
                return false;
            }

            if (target.getName() == null) {
                inviter.sendMessage(ChatColor.RED + "✦ Player not found! ✦");
                return false;
            }

            PlayerData targetData = plugin.getDataManager().getPlayerData(target.getUniqueId(), target.getName());
            if (targetData.getCurrentGuild() != null) {
                inviter.sendMessage(ChatColor.RED + "✦ " + target.getName() + " is already in a guild! ✦");
                return false;
            }

            if (guild.isMember(target.getUniqueId())) {
                inviter.sendMessage(ChatColor.RED + "✦ " + target.getName() + " is already in your guild! ✦");
                return false;
            }

            // Send invitation if player is online
            Player onlineTarget = plugin.getServer().getPlayer(target.getUniqueId());
            if (onlineTarget != null) {
                onlineTarget.sendMessage("");
                onlineTarget.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Guild Invitation" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
                onlineTarget.sendMessage("");
                onlineTarget.sendMessage(ChatColor.YELLOW + "You have been invited to join:");
                onlineTarget.sendMessage(guild.getType().getSymbol() + " " + ChatColor.AQUA + guild.getDisplayName());
                onlineTarget.sendMessage(ChatColor.WHITE + "by " + ChatColor.YELLOW + inviter.getName());
                onlineTarget.sendMessage("");
                onlineTarget.sendMessage(ChatColor.GREEN + "Use /guild join " + guild.getName() + " to accept!");
                onlineTarget.sendMessage("");
                onlineTarget.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════ ✦");
                onlineTarget.sendMessage("");
            }

            inviter.sendMessage(ChatColor.GREEN + "✦ Invitation sent to " + target.getName() + "! ✦");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error inviting player: " + e.getMessage());
            inviter.sendMessage(ChatColor.RED + "✦ Error sending invitation! Please try again. ✦");
            return false;
        }
    }

    public boolean disbandGuild(Player leader) {
        if (leader == null) return false;

        try {
            Guild guild = getPlayerGuild(leader);
            if (guild == null) {
                leader.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
                return false;
            }

            if (!guild.isLeader(leader.getUniqueId())) {
                leader.sendMessage(ChatColor.RED + "✦ Only the guild leader can disband the guild! ✦");
                return false;
            }

            String guildName = guild.getDisplayName();

            // Notify all members
            for (UUID memberUUID : guild.getMembers()) {
                Player member = plugin.getServer().getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(ChatColor.RED + "✦ " + guildName + " has been disbanded by " + leader.getName() + "! ✦");
                }

                // Update player data
                PlayerData memberData = plugin.getDataManager().getPlayerData(memberUUID,
                        member != null ? member.getName() : plugin.getServer().getOfflinePlayer(memberUUID).getName());
                memberData.setCurrentGuild(null);
                plugin.getDataManager().savePlayerData(memberData);
            }

            // Remove guild from the guilds map
            guilds.remove(guild.getName().toLowerCase());

            leader.sendMessage(ChatColor.YELLOW + "✦ " + guildName + " has been disbanded! ✦");

            saveGuilds();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error disbanding guild: " + e.getMessage());
            leader.sendMessage(ChatColor.RED + "✦ Error disbanding guild! Please try again. ✦");
            return false;
        }
    }

    public Guild getGuild(String guildName) {
        if (guildName == null) return null;
        return guilds.get(guildName.toLowerCase());
    }

    public Guild getPlayerGuild(Player player) {
        if (player == null) return null;

        try {
            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) return null;

            String guildName = data.getCurrentGuild();
            return guildName != null ? guilds.get(guildName) : null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting player guild for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    public Collection<Guild> getAllGuilds() {
        return new ArrayList<>(guilds.values());
    }

    private void loadGuilds() {
        if (!guildFile.exists()) {
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(guildFile);

            for (String guildKey : config.getKeys(false)) {
                try {
                    String name = config.getString(guildKey + ".name", guildKey);
                    String displayName = config.getString(guildKey + ".displayName", name);
                    String description = config.getString(guildKey + ".description", "A guild");

                    GuildType type;
                    try {
                        type = GuildType.valueOf(config.getString(guildKey + ".type", "MERCHANT_BAZAAR"));
                    } catch (IllegalArgumentException e) {
                        type = GuildType.MERCHANT_BAZAAR;
                        plugin.getLogger().warning("Invalid guild type for " + guildKey + ", using default");
                    }

                    UUID leader;
                    try {
                        leader = UUID.fromString(config.getString(guildKey + ".leader"));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid leader UUID for guild " + guildKey + ", skipping");
                        continue;
                    }

                    Guild guild = new Guild(name, displayName, description, type, leader);
                    guild.setLevel(config.getInt(guildKey + ".level", 1));
                    guild.setTreasury(config.getDouble(guildKey + ".treasury", 0.0));

                    // Load members
                    List<String> memberStrings = config.getStringList(guildKey + ".members");
                    for (String memberString : memberStrings) {
                        try {
                            guild.getMembers().add(UUID.fromString(memberString));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid member UUID in guild " + guildKey + ": " + memberString);
                        }
                    }

                    // Load officers
                    List<String> officerStrings = config.getStringList(guildKey + ".officers");
                    for (String officerString : officerStrings) {
                        try {
                            guild.getOfficers().add(UUID.fromString(officerString));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid officer UUID in guild " + guildKey + ": " + officerString);
                        }
                    }

                    guilds.put(name.toLowerCase(), guild);

                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load guild: " + guildKey + " - " + e.getMessage());
                }
            }

            plugin.getLogger().info("Loaded " + guilds.size() + " guilds successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading guilds: " + e.getMessage());
        }
    }

    public void saveGuilds() {
        try {
            FileConfiguration config = new YamlConfiguration();

            for (Guild guild : guilds.values()) {
                String key = guild.getName();
                config.set(key + ".name", guild.getName());
                config.set(key + ".displayName", guild.getDisplayName());
                config.set(key + ".description", guild.getDescription());
                config.set(key + ".type", guild.getType().name());
                config.set(key + ".leader", guild.getLeader() != null ? guild.getLeader().toString() : null);
                config.set(key + ".level", guild.getLevel());
                config.set(key + ".treasury", guild.getTreasury());
                config.set(key + ".createdDate", guild.getCreatedDate());

                List<String> memberStrings = new ArrayList<>();
                for (UUID member : guild.getMembers()) {
                    if (member != null) {
                        memberStrings.add(member.toString());
                    }
                }
                config.set(key + ".members", memberStrings);

                List<String> officerStrings = new ArrayList<>();
                for (UUID officer : guild.getOfficers()) {
                    if (officer != null) {
                        officerStrings.add(officer.toString());
                    }
                }
                config.set(key + ".officers", officerStrings);
            }

            config.save(guildFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save guilds: " + e.getMessage());
        }
    }
}