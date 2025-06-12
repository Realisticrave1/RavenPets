package RavenMC.ravenRpg.integration;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RavenPetsIntegration {
    private RavenRpg rpgPlugin;
    private Plugin ravenPetsPlugin;
    private boolean isEnabled;

    public RavenPetsIntegration(RavenRpg rpgPlugin) {
        this.rpgPlugin = rpgPlugin;
        this.ravenPetsPlugin = rpgPlugin.getServer().getPluginManager().getPlugin("RavenPets");
        this.isEnabled = ravenPetsPlugin != null && ravenPetsPlugin.isEnabled();

        if (isEnabled) {
            rpgPlugin.getLogger().info("RavenPets integration enabled!");
            setupIntegration();
        }
    }

    private void setupIntegration() {
        // Register listeners or hooks here if needed
        // This would depend on the specific API provided by RavenPets
    }

    public void applyRpgBonusesToRaven(Player player) {
        if (!isEnabled) return;

        PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
        if (!data.isFullyInitialized()) return;

        // Calculate bonuses based on RPG stats
        int intelligence = data.getStat("intelligence");
        int luck = data.getStat("luck");

        // Apply experience multipliers (this would need RavenPets API)
        double expMultiplier = 1.0;
        expMultiplier += (intelligence - 10) * rpgPlugin.getConfig().getDouble("integration.ravenpets.experience-multipliers.intelligence", 0.02);
        expMultiplier += (luck - 10) * rpgPlugin.getConfig().getDouble("integration.ravenpets.experience-multipliers.luck", 0.01);

        // Apply racial bonuses to raven
        if (rpgPlugin.getConfig().getBoolean("integration.ravenpets.racial-effects", true)) {
            applyRacialEffectsToRaven(player, data);
        }
    }

    private void applyRacialEffectsToRaven(Player player, PlayerData data) {
        switch (data.getSelectedRace()) {
            case HUMAN:
                // Humans get balanced raven bonuses
                break;
            case ORK:
                // Orks get stronger, more aggressive ravens
                break;
            case ELF:
                // Elves get more graceful, nature-attuned ravens
                break;
            case VAMPIRE:
                // Vampires get darker, more mysterious ravens
                break;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}