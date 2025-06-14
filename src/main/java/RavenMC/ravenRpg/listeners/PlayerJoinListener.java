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
        if (player == null) return;

        try {
            // Check if plugin is fully loaded
            if (!plugin.isPluginFullyLoaded()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(ChatColor.YELLOW + "✦ RavenRpg is still loading, please wait... ✦");
                    }
                }, 40L);
                return;
            }

            // Check if DataManager is available
            if (plugin.getDataManager() == null) {
                plugin.getLogger().warning("DataManager is null when " + player.getName() + " joined");
                return;
            }

            PlayerData data = plugin.getDataManager().getPlayerData(player);
            if (data == null) {
                plugin.getLogger().warning("Could not load player data for " + player.getName());
                player.sendMessage(ChatColor.RED + "✦ Error loading your character data! Please rejoin or contact an administrator. ✦");
                return;
            }

            // Check if player needs to complete character creation
            if (!data.isFullyInitialized()) {
                // Schedule to open character creation GUI after a short delay
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        showWelcomeMessage(player);

                        // Open character creation GUI
                        try {
                            new CharacterCreationGUI(plugin, player).open();
                        } catch (Exception e) {
                            plugin.getLogger().severe("Error opening character creation GUI for " + player.getName() + ": " + e.getMessage());
                            player.sendMessage(ChatColor.RED + "✦ Error opening character creation! Use /rpg to try again. ✦");
                        }
                    }
                }, 40L); // 2 second delay
            } else {
                // Welcome back message for existing players
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        showWelcomeBackMessage(player, data);
                    }
                }, 20L); // 1 second delay

                // Apply RavenPets bonuses
                if (plugin.isRavenPetsEnabled()) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && plugin.getRavenPetsIntegration() != null) {
                            plugin.getRavenPetsIntegration().onPlayerJoin(player);
                        }
                    }, 60L); // 3 second delay for RavenPets
                }
            }

            // Apply racial bonuses to stats (in case they were reset)
            if (data.getSelectedRace() != null) {
                applyRacialBonuses(data);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling player join for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "✦ Error loading your character! Please try /rpg or contact an administrator. ✦");
        }
    }

    private void showWelcomeMessage(Player player) {
        try {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Welcome to RavenRpg!" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
            player.sendMessage("");
            player.sendMessage(ChatColor.WHITE + "Create your character by choosing your race and bloodline!");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "✦ Opening character creation menu... ✦");
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════════ ✦");
            player.sendMessage("");
        } catch (Exception e) {
            plugin.getLogger().warning("Error showing welcome message to " + player.getName() + ": " + e.getMessage());
        }
    }

    private void showWelcomeBackMessage(Player player, PlayerData data) {
        try {
            if (data == null) return;

            String raceDisplay = data.getSelectedRace() != null ? data.getSelectedRace().getDisplayName() : "Unknown";
            String bloodlineDisplay = data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDisplayName() : "Unknown";

            player.sendMessage(ChatColor.DARK_PURPLE + "✦ Welcome back to RavenRpg, " +
                    raceDisplay + " of the " + bloodlineDisplay + "! ✦");

            if (data.getCurrentGuild() != null && plugin.getGuildManager() != null) {
                var guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
                if (guild != null) {
                    player.sendMessage(ChatColor.GOLD + "✦ Guild: " + guild.getDisplayName() + " ✦");
                }
            }

            // Show RavenPets integration status
            if (plugin.isRavenPetsEnabled() && plugin.getRavenPetsIntegration() != null) {
                try {
                    var bonuses = plugin.getRavenPetsIntegration().getPlayerRavenBonuses(player);
                    if (bonuses != null && bonuses.containsKey("expMultiplier")) {
                        double expMultiplier = (Double) bonuses.get("expMultiplier");
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Raven EXP Multiplier: " + ChatColor.GOLD + String.format("%.2fx", expMultiplier) + " ✦");
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error showing RavenPets status to " + player.getName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error showing welcome back message to " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applyRacialBonuses(PlayerData data) {
        if (data == null || data.getSelectedRace() == null) return;

        try {
            // This ensures racial bonuses are always applied correctly
            // even if the player's data was modified or corrupted
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
        } catch (Exception e) {
            plugin.getLogger().warning("Error applying racial bonuses: " + e.getMessage());
        }
    }
}