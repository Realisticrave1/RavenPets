package RavenMC.ravenRpg.utils;

import org.bukkit.entity.Player;

public class PermissionManager {

    public static boolean hasRacePermission(Player player, String race) {
        return player.hasPermission("ravenrpg.racial." + race.toLowerCase()) ||
                player.hasPermission("ravenrpg.racial.*") ||
                player.hasPermission("ravenrpg.*");
    }

    public static boolean canCreateGuild(Player player) {
        return player.hasPermission("ravenrpg.guild.create") ||
                player.hasPermission("ravenrpg.guild.*") ||
                player.hasPermission("ravenrpg.*");
    }

    public static boolean canCreateShop(Player player) {
        return player.hasPermission("ravenrpg.shop.create") ||
                player.hasPermission("ravenrpg.shop.*") ||
                player.hasPermission("ravenrpg.*");
    }

    public static boolean canBypassLimits(Player player, String limitType) {
        return player.hasPermission("ravenrpg.bypass." + limitType) ||
                player.hasPermission("ravenrpg.bypass.*") ||
                player.hasPermission("ravenrpg.*");
    }

    public static boolean isAdmin(Player player) {
        return player.hasPermission("ravenrpg.admin") ||
                player.hasPermission("ravenrpg.*");
    }

    public static boolean canChangeRace(Player player) {
        return player.hasPermission("ravenrpg.bypass.race-change") ||
                player.hasPermission("ravenrpg.bypass.*") ||
                player.hasPermission("ravenrpg.*");
    }

    public static boolean canChangeBloodline(Player player) {
        return player.hasPermission("ravenrpg.bypass.bloodline-change") ||
                player.hasPermission("ravenrpg.bypass.*") ||
                player.hasPermission("ravenrpg.*");
    }
}