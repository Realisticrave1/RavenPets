package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import RavenMC.ravenRpg.gui.CharacterCreationGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private RavenRpg plugin;

    public PlayerJoinListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Check if player needs to complete character creation
        if (!data.isFullyInitialized()) {
            // Schedule to open character creation GUI after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Welcome to RavenRpg!" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.WHITE + "Create your character by choosing your race and bloodline!");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + "✦ Opening character creation menu... ✦");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════════ ✦");
                    player.sendMessage("");

                    // Open character creation GUI
                    new CharacterCreationGUI(plugin, player).open();
                }
            }, 40L); // 2 second delay
        } else {
            // Welcome back message for existing players
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.DARK_PURPLE + "✦ Welcome back to RavenRpg, " +
                            data.getSelectedRace().getDisplayName() + " of the " +
                            data.getSelectedBloodline().getDisplayName() + "! ✦");

                    if (data.getCurrentGuild() != null) {
                        player.sendMessage(ChatColor.GOLD + "✦ Guild: " +
                                plugin.getGuildManager().getGuild(data.getCurrentGuild()).getDisplayName() + " ✦");
                    }

                    // Show RavenPets integration status
                    if (plugin.isRavenPetsEnabled()) {
                        var bonuses = plugin.getRavenPetsIntegration().getPlayerRavenBonuses(player);
                        double expMultiplier = (Double) bonuses.get("expMultiplier");
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Raven EXP Multiplier: " + ChatColor.GOLD + String.format("%.2fx", expMultiplier) + " ✦");
                    }
                }
            }, 20L); // 1 second delay

            // Apply RavenPets bonuses
            if (plugin.isRavenPetsEnabled()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getRavenPetsIntegration().onPlayerJoin(player);
                    }
                }, 60L); // 3 second delay for RavenPets
            }
        }

        // Apply racial bonuses to stats (in case they were reset)
        if (data.getSelectedRace() != null) {
            applyRacialBonuses(data);
        }
    }

    private void applyRacialBonuses(PlayerData data) {
        // This ensures racial bonuses are always applied correctly
        // even if the player's data was modified or corrupted
        if (data.getSelectedRace() != null) {
            for (String stat : data.getSelectedRace().getStatBonuses().keySet()) {
                int currentStat = data.getStat(stat);
                int baseStat = 10; // Base stat value
                int expectedBonus = data.getSelectedRace().getStatBonuses().get(stat);
                int expectedTotal = baseStat + expectedBonus;

                // Only adjust if the stat is not at the expected value
                if (currentStat < expectedTotal) {
                    data.setStat(stat, expectedTotal);
                }
            }
        }
    }
}