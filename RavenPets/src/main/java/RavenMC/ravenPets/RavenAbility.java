package RavenMC.ravenPets;

import org.bukkit.entity.Player;

public interface RavenAbility {
    void execute(Player player, RavenTier tier);
    String getName();
    String getDescription();
}