package RavenMC.ravenRpg.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void sendRpgMessage(Player player, String message) {
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ [RavenRpg] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendSuccessMessage(Player player, String message) {
        player.sendMessage(ChatColor.GREEN + "✦ " + ChatColor.translateAlternateColorCodes('&', message) + " ✦");
    }

    public static void sendErrorMessage(Player player, String message) {
        player.sendMessage(ChatColor.RED + "✦ " + ChatColor.translateAlternateColorCodes('&', message) + " ✦");
    }

    public static void sendInfoMessage(Player player, String message) {
        player.sendMessage(ChatColor.AQUA + "✦ " + ChatColor.translateAlternateColorCodes('&', message) + " ✦");
    }

    public static void sendGuildMessage(Player player, String message) {
        player.sendMessage(ChatColor.GOLD + "✦ [Guild] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendShopMessage(Player player, String message) {
        player.sendMessage(ChatColor.YELLOW + "✦ [Shop] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static String formatRaceDisplay(String raceName) {
        switch (raceName.toUpperCase()) {
            case "HUMAN": return ChatColor.YELLOW + "[Human]" + ChatColor.RESET;
            case "ORK": return ChatColor.RED + "[Ork]" + ChatColor.RESET;
            case "ELF": return ChatColor.GREEN + "[Elf]" + ChatColor.RESET;
            case "VAMPIRE": return ChatColor.DARK_PURPLE + "[Vampire]" + ChatColor.RESET;
            default: return ChatColor.GRAY + "[Unknown]" + ChatColor.RESET;
        }
    }

    public static String formatStatValue(int value, int baseValue) {
        if (value > baseValue) {
            return ChatColor.GREEN + "+" + (value - baseValue) + ChatColor.RESET;
        } else if (value < baseValue) {
            return ChatColor.RED + "-" + (baseValue - value) + ChatColor.RESET;
        } else {
            return ChatColor.WHITE + "0" + ChatColor.RESET;
        }
    }

    public static void sendWelcomeMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Welcome to RavenRpg!" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "Choose your race and bloodline to begin your adventure!");
        player.sendMessage(ChatColor.AQUA + "• Join guilds to meet other players");
        player.sendMessage(ChatColor.AQUA + "• Create shops to earn money");
        player.sendMessage(ChatColor.AQUA + "• Level up your skills through activities");
        player.sendMessage(ChatColor.AQUA + "• Unlock unique racial abilities");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/rpg help" + ChatColor.YELLOW + " for a list of commands!");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════════ ✦");
        player.sendMessage("");
    }
}