package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    private RavenRpg plugin;

    public PlayerChatListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Add race and bloodline prefixes to chat
        if (data.isFullyInitialized()) {
            String racePrefix = getRacePrefix(data.getSelectedRace());
            String bloodlinePrefix = data.getSelectedBloodline().getSymbol();

            // Modify the player's display name for this message
            String originalFormat = event.getFormat();
            String newFormat = bloodlinePrefix + " " + racePrefix + " " + originalFormat;
            event.setFormat(newFormat);
        }

        // Handle guild chat if message starts with !
        if (event.getMessage().startsWith("!")) {
            event.setCancelled(true);
            handleGuildChat(player, event.getMessage().substring(1));
        }
    }

    private String getRacePrefix(Race race) {
        switch (race) {
            case HUMAN: return ChatColor.YELLOW + "[Human]";
            case ORK: return ChatColor.RED + "[Ork]";
            case ELF: return ChatColor.GREEN + "[Elf]";
            case VAMPIRE: return ChatColor.DARK_PURPLE + "[Vampire]";
            default: return "";
        }
    }

    private void handleGuildChat(Player player, String message) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (data.getCurrentGuild() == null) {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            return;
        }

        Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ Guild not found! ✦");
            return;
        }

        if (message.trim().isEmpty()) {
            player.sendMessage(ChatColor.RED + "✦ Message cannot be empty! ✦");
            return;
        }

        // Send message to all guild members
        String guildMessage = guild.getType().getSymbol() + " " + ChatColor.GOLD + "[Guild] " +
                ChatColor.WHITE + player.getName() + ": " + ChatColor.GRAY + message;

        for (java.util.UUID memberUUID : guild.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.sendMessage(guildMessage);
            }
        }
    }
}