package RavenMC.ravenPets;

import org.bukkit.entity.Player;

public interface RavenAbility {
    // Primary ability
    void execute(Player player, RavenTier tier);
    String getName();
    String getDescription();

    // Secondary ability (shift+right-click)
    void executeSecondary(Player player, RavenTier tier);
    String getSecondaryName();
    String getSecondaryDescription();

    // Making this public to fix access modifier clash
    public int getDurationByTier(RavenTier tier);
}