package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BloodlineManager {
    private RavenRpg plugin;

    public BloodlineManager(RavenRpg plugin) {
        this.plugin = plugin;
    }

    public void selectBloodline(Player player, Bloodline bloodline) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (data.hasChosenBloodline()) {
            player.sendMessage(ChatColor.RED + "✦ You have already chosen your bloodline! ✦");
            return;
        }

        if (!data.hasChosenRace()) {
            player.sendMessage(ChatColor.RED + "✦ You must choose a race first! Use /rpg race ✦");
            return;
        }

        data.setSelectedBloodline(bloodline);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ You have joined the " + bloodline.getDisplayName() + "! ✦");
        player.sendMessage(ChatColor.YELLOW + "✦ " + bloodline.getDescription() + " ✦");

        // Show abilities
        player.sendMessage(ChatColor.GOLD + "✦ Bloodline Abilities: ✦");
        for (String ability : bloodline.getAbilities()) {
            player.sendMessage(ChatColor.WHITE + "  • " + ability);
        }

        // Save data
        plugin.getDataManager().savePlayerData(data);

        // Welcome message for fully initialized player
        if (data.isFullyInitialized()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "✦ Character creation complete! ✦");
            player.sendMessage(ChatColor.AQUA + "✦ Use /rpg help to see available commands ✦");
            player.sendMessage(ChatColor.GOLD + "✦ Join a guild with /guild list ✦");
        }
    }

    public String getBloodlineInfo(Bloodline bloodline) {
        StringBuilder info = new StringBuilder();
        info.append(bloodline.getSymbol()).append(" ")
                .append(ChatColor.LIGHT_PURPLE + bloodline.getDisplayName()).append(" ")
                .append(bloodline.getSymbol()).append("\n");
        info.append(ChatColor.WHITE + bloodline.getDescription()).append("\n\n");
        info.append(ChatColor.YELLOW + "Special Abilities:\n");

        for (String ability : bloodline.getAbilities()) {
            info.append(ChatColor.WHITE + "  • ").append(ability).append("\n");
        }

        return info.toString();
    }
}