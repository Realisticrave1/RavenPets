package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.ChatColor;
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
        PlayerData data = plugin.getDataManager().getPlayerData(player);

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
        if (plugin.getEconomy().getBalance(player) < creationCost) {
            player.sendMessage(ChatColor.RED + "✦ You need $" + creationCost + " to create a guild! ✦");
            return false;
        }

        // Create guild
        Guild guild = new Guild(guildName, displayName, "A new guild", type, player.getUniqueId());
        guilds.put(guildName.toLowerCase(), guild);
        data.setCurrentGuild(guildName.toLowerCase());

        // Take money
        plugin.getEconomy().withdrawPlayer(player, creationCost);

        player.sendMessage(ChatColor.GREEN + "✦ Guild '" + displayName + "' created successfully! ✦");
        player.sendMessage(ChatColor.GOLD + "✦ Type: " + type.getDisplayName() + " ✦");

        saveGuilds();
        plugin.getDataManager().savePlayerData(data);
        return true;
    }

    public boolean joinGuild(Player player, String guildName) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

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

        return false;
    }

    public boolean leaveGuild(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
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
    }

    public Guild getGuild(String guildName) {
        return guilds.get(guildName.toLowerCase());
    }

    public Guild getPlayerGuild(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        String guildName = data.getCurrentGuild();
        return guildName != null ? guilds.get(guildName) : null;
    }

    public Collection<Guild> getAllGuilds() {
        return guilds.values();
    }

    private void loadGuilds() {
        if (!guildFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(guildFile);

        for (String guildKey : config.getKeys(false)) {
            try {
                String name = config.getString(guildKey + ".name");
                String displayName = config.getString(guildKey + ".displayName");
                String description = config.getString(guildKey + ".description");
                GuildType type = GuildType.valueOf(config.getString(guildKey + ".type"));
                UUID leader = UUID.fromString(config.getString(guildKey + ".leader"));

                Guild guild = new Guild(name, displayName, description, type, leader);
                guild.setLevel(config.getInt(guildKey + ".level", 1));
                guild.setTreasury(config.getDouble(guildKey + ".treasury", 0.0));

                // Load members
                List<String> memberStrings = config.getStringList(guildKey + ".members");
                for (String memberString : memberStrings) {
                    guild.getMembers().add(UUID.fromString(memberString));
                }

                // Load officers
                List<String> officerStrings = config.getStringList(guildKey + ".officers");
                for (String officerString : officerStrings) {
                    guild.getOfficers().add(UUID.fromString(officerString));
                }

                guilds.put(name.toLowerCase(), guild);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load guild: " + guildKey + " - " + e.getMessage());
            }
        }
    }

    public void saveGuilds() {
        FileConfiguration config = new YamlConfiguration();

        for (Guild guild : guilds.values()) {
            String key = guild.getName();
            config.set(key + ".name", guild.getName());
            config.set(key + ".displayName", guild.getDisplayName());
            config.set(key + ".description", guild.getDescription());
            config.set(key + ".type", guild.getType().name());
            config.set(key + ".leader", guild.getLeader().toString());
            config.set(key + ".level", guild.getLevel());
            config.set(key + ".treasury", guild.getTreasury());
            config.set(key + ".createdDate", guild.getCreatedDate());

            List<String> memberStrings = new ArrayList<>();
            for (UUID member : guild.getMembers()) {
                memberStrings.add(member.toString());
            }
            config.set(key + ".members", memberStrings);

            List<String> officerStrings = new ArrayList<>();
            for (UUID officer : guild.getOfficers()) {
                officerStrings.add(officer.toString());
            }
            config.set(key + ".officers", officerStrings);
        }

        try {
            config.save(guildFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save guilds: " + e.getMessage());
        }
    }
}