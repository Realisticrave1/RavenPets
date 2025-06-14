package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class RaceManager {
    private RavenRpg plugin;

    public RaceManager(RavenRpg plugin) {
        this.plugin = plugin;
    }

    public void selectRace(Player player, Race race) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (data.hasChosenRace()) {
            player.sendMessage(ChatColor.RED + "✦ You have already chosen your race! ✦");
            return;
        }

        data.setSelectedRace(race);
        applyRacialBonuses(data, race);

        player.sendMessage(ChatColor.GREEN + "✦ You have chosen the " + race.getDisplayName() + " race! ✦");
        player.sendMessage(ChatColor.YELLOW + "✦ " + race.getDescription() + " ✦");

        // Show stat bonuses
        player.sendMessage(ChatColor.GOLD + "✦ Racial Bonuses Applied: ✦");
        for (Map.Entry<String, Integer> bonus : race.getStatBonuses().entrySet()) {
            if (bonus.getValue() != 0) {
                String color = bonus.getValue() > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "";
                player.sendMessage(ChatColor.WHITE + "  " + bonus.getKey() + ": " + color + bonus.getValue());
            }
        }

        // Save data
        plugin.getDataManager().savePlayerData(data);

        // Apply RavenPets bonuses
        if (plugin.isRavenPetsEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getRavenPetsIntegration().applyRpgBonusesToRaven(player);

                if (data.isFullyInitialized()) {
                    var bonuses = plugin.getRavenPetsIntegration().getPlayerRavenBonuses(player);
                    double expMultiplier = (Double) bonuses.get("expMultiplier");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Your raven is enhanced! EXP Multiplier: " +
                            ChatColor.GOLD + String.format("%.2fx", expMultiplier) + " ✦");
                }
            }, 20L); // 1 second delay
        }

        // Check if player needs to choose bloodline
        if (!data.hasChosenBloodline()) {
            player.sendMessage(ChatColor.AQUA + "✦ Now choose your bloodline with /rpg bloodline ✦");
        }
    }

    private void applyRacialBonuses(PlayerData data, Race race) {
        for (Map.Entry<String, Integer> bonus : race.getStatBonuses().entrySet()) {
            data.addStat(bonus.getKey(), bonus.getValue());
        }
    }

    public String getRaceInfo(Race race) {
        StringBuilder info = new StringBuilder();
        info.append(ChatColor.GOLD + "✦ ").append(race.getDisplayName()).append(" ✦\n");
        info.append(ChatColor.WHITE + race.getDescription()).append("\n\n");
        info.append(ChatColor.YELLOW + "Stat Bonuses:\n");

        for (Map.Entry<String, Integer> bonus : race.getStatBonuses().entrySet()) {
            if (bonus.getValue() != 0) {
                String color = bonus.getValue() > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "";
                info.append(ChatColor.WHITE + "  ").append(bonus.getKey()).append(": ")
                        .append(color).append(bonus.getValue()).append("\n");
            }
        }

        return info.toString();
    }
}