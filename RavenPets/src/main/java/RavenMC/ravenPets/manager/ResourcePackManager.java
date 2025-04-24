package RavenMC.ravenPets.manager;

import RavenMC.ravenPets.RavenPets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Manages the resource pack containing raven pet models
 */
public class ResourcePackManager implements Listener {

    private final RavenPets plugin;
    private final String resourcePackUrl;
    private final byte[] resourcePackHash;

    public ResourcePackManager(RavenPets plugin) {
        this.plugin = plugin;

        // Extract the resource pack from plugin jar if it doesn't exist
        extractResourcePack();

        // Load configuration values
        this.resourcePackUrl = plugin.getConfig().getString("resource-pack.url", "");
        this.resourcePackHash = loadResourcePackHash();

        // Register listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Extracts the resource pack from the plugin jar to the plugin folder
     */
    private void extractResourcePack() {
        File resourcePackFile = new File(plugin.getDataFolder(), "resources/ravenpets_resources.zip");

        // Make sure the parent directory exists
        resourcePackFile.getParentFile().mkdirs();

        // Only extract if the resource pack doesn't exist
        if (!resourcePackFile.exists()) {
            try (InputStream in = plugin.getResource("ravenpets_resources.zip")) {
                if (in == null) {
                    plugin.getLogger().warning("Could not find resource pack in plugin jar!");
                    return;
                }

                try (FileOutputStream out = new FileOutputStream(resourcePackFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                plugin.getLogger().info("Resource pack extracted successfully!");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to extract resource pack", e);
            }
        }
    }

    /**
     * Loads the SHA-1 hash of the resource pack
     *
     * @return The resource pack hash as a byte array
     */
    private byte[] loadResourcePackHash() {
        File hashFile = new File(plugin.getDataFolder(), "resources/resource_pack_hash.sha1");

        // Check if hash file exists
        if (hashFile.exists()) {
            try {
                String hashString = new String(Files.readAllBytes(hashFile.toPath())).trim();
                return hexStringToByteArray(hashString);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load resource pack hash", e);
            }
        }

        // Return empty hash if not available
        return new byte[0];
    }

    /**
     * Converts a hex string to a byte array
     *
     * @param s The hex string
     * @return The byte array
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Sends the resource pack to a player
     *
     * @param player The player to send the resource pack to
     */
    public void sendResourcePack(Player player) {
        if (resourcePackUrl.isEmpty()) {
            plugin.getLogger().warning("Resource pack URL not configured! Cannot send to " + player.getName());
            return;
        }

        if (resourcePackHash.length > 0) {
            player.setResourcePack(resourcePackUrl, resourcePackHash);
        } else {
            player.setResourcePack(resourcePackUrl);
        }

        plugin.getLogger().info("Sent resource pack to " + player.getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Send resource pack to player when they join
        Player player = event.getPlayer();

        // Delay sending the resource pack to ensure the player is fully connected
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sendResourcePack(player);
        }, 20L); // 1-second delay
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                player.sendMessage(ChatColor.GREEN + "RavenPets resource pack loaded successfully!");
                break;
            case DECLINED:
                player.sendMessage(ChatColor.YELLOW + "You declined the RavenPets resource pack. " +
                        "The raven pets may not display correctly.");
                break;
            case FAILED_DOWNLOAD:
                player.sendMessage(ChatColor.RED + "Failed to download the RavenPets resource pack. " +
                        "Please try reconnecting or contact an administrator.");
                break;
            case ACCEPTED:
                player.sendMessage(ChatColor.GREEN + "Downloading RavenPets resource pack...");
                break;
        }
    }
}