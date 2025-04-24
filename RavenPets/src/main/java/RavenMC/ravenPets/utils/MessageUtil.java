package RavenMC.ravenPets.utils;

import RavenMC.ravenPets.RavenPets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static RavenPets plugin;

    /**
     * Initialize the message utility with the plugin instance
     *
     * @param instance The plugin instance
     */
    public static void init(RavenPets instance) {
        plugin = instance;
    }

    /**
     * Send a message to a player or command sender
     *
     * @param sender The recipient of the message
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Send a message from the messages.yml file
     *
     * @param sender The recipient of the message
     * @param key The message key in the config
     */
    public static void sendConfigMessage(CommandSender sender, String key) {
        String message = getMessage(key);
        if (message != null && !message.isEmpty()) {
            sendMessage(sender, message);
        }
    }

    /**
     * Send a message from the messages.yml file with placeholders
     *
     * @param sender The recipient of the message
     * @param key The message key in the config
     * @param placeholders A map of placeholders and their values
     */
    public static void sendConfigMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        if (message == null || message.isEmpty()) {
            return;
        }

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        sendMessage(sender, message);
    }

    /**
     * Get a message from the messages.yml file
     *
     * @param key The message key in the config
     * @return The message from the config or null if not found
     */
    public static String getMessage(String key) {
        if (plugin == null) {
            return "§cMessage system not initialized!";
        }

        FileConfiguration messages = plugin.getConfigManager().getMessages();
        return messages.getString(key);
    }

    /**
     * Get a message from the messages.yml file with placeholders
     *
     * @param key The message key in the config
     * @param placeholders A map of placeholders and their values
     * @return The message with placeholders replaced
     */
    public static String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        if (message == null || message.isEmpty()) {
            return null;
        }

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return message;
    }

    /**
     * Broadcast a message to all online players
     *
     * @param message The message to broadcast
     */
    public static void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> sendMessage(player, message));
    }

    /**
     * Broadcast a message from the messages.yml file
     *
     * @param key The message key in the config
     */
    public static void broadcastConfigMessage(String key) {
        String message = getMessage(key);
        if (message != null && !message.isEmpty()) {
            broadcast(message);
        }
    }

    /**
     * Broadcast a message from the messages.yml file with placeholders
     *
     * @param key The message key in the config
     * @param placeholders A map of placeholders and their values
     */
    public static void broadcastConfigMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        if (message == null || message.isEmpty()) {
            return;
        }

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        broadcast(message);
    }

    /**
     * Send a title to a player
     *
     * @param player The player to send the title to
     * @param title The title text
     * @param subtitle The subtitle text
     * @param fadeIn The fade in time in ticks
     * @param stay The stay time in ticks
     * @param fadeOut The fade out time in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }

    /**
     * Send a title from the messages.yml file
     *
     * @param player The player to send the title to
     * @param titleKey The title key in the config
     * @param subtitleKey The subtitle key in the config
     * @param fadeIn The fade in time in ticks
     * @param stay The stay time in ticks
     * @param fadeOut The fade out time in ticks
     */
    public static void sendConfigTitle(Player player, String titleKey, String subtitleKey, int fadeIn, int stay, int fadeOut) {
        String title = getMessage(titleKey);
        String subtitle = getMessage(subtitleKey);

        if (title == null) title = "";
        if (subtitle == null) subtitle = "";

        sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Send an actionbar message to a player
     *
     * @param player The player to send the actionbar to
     * @param message The actionbar message
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message)));
    }

    /**
     * Send an actionbar message from the messages.yml file
     *
     * @param player The player to send the actionbar to
     * @param key The message key in the config
     */
    public static void sendConfigActionBar(Player player, String key) {
        String message = getMessage(key);
        if (message != null && !message.isEmpty()) {
            sendActionBar(player, message);
        }
    }

    /**
     * Create a placeholder map for message formatting
     *
     * @return A new placeholder map
     */
    public static Map<String, String> placeholders() {
        return new HashMap<>();
    }

    /**
     * Colorize a string with color codes
     *
     * @param message The message to colorize
     * @return The colorized message
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }

        // Replace hex colors (&#RRGGBB) with Bukkit color codes
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.valueOf(hex) + "");
        }

        matcher.appendTail(buffer);

        // Replace standard color codes (&a, &b, etc.)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}