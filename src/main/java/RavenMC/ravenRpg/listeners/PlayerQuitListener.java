package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private RavenRpg plugin;

    public PlayerQuitListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save player data when they quit
        try {
            plugin.getDataManager().savePlayerData(plugin.getDataManager().getPlayerData(player));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save data for " + player.getName() + ": " + e.getMessage());
        }

        // Clean up RavenPets integration data
        if (plugin.isRavenPetsEnabled()) {
            try {
                plugin.getRavenPetsIntegration().onPlayerQuit(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to clean up RavenPets data for " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}