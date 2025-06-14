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
            player.sendMessage(ChatColor.AQUA + "✦ Use /rpg to access your character menu ✦");
            player.sendMessage(ChatColor.GOLD + "✦ Join a guild with /guild list ✦");

            // Apply RavenPets bonuses for completed character
            if (plugin.isRavenPetsEnabled()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getRavenPetsIntegration().applyRpgBonusesToRaven(player);

                    var bonuses = plugin.getRavenPetsIntegration().getPlayerRavenBonuses(player);
                    double expMultiplier = (Double) bonuses.get("expMultiplier");

                    player.sendMessage("");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenPets Enhanced!" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " ═══════ ✦");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.WHITE + "Your completed character enhances your raven companion!");
                    player.sendMessage(ChatColor.YELLOW + "Raven EXP Multiplier: " + ChatColor.GOLD + String.format("%.2fx", expMultiplier));
                    player.sendMessage(ChatColor.YELLOW + "Racial Bonus: " + ChatColor.AQUA + data.getSelectedRace().getDisplayName());
                    player.sendMessage(ChatColor.YELLOW + "Bloodline Bonus: " + bloodline.getSymbol() + " " + ChatColor.LIGHT_PURPLE + bloodline.getDisplayName());
                    player.sendMessage("");
                    player.sendMessage(ChatColor.AQUA + "Use /raven to manage your companion!");
                    player.sendMessage(ChatColor.YELLOW + "Level up your RPG character to boost your raven further!");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ ═══════════════════════════════════════════ ✦");
                    player.sendMessage("");
                }, 40L); // 2 second delay
            }
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