package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;
import org.bukkit.entity.EntityType;

public class RavenListener implements Listener {
    private final RavenPets plugin;

    public RavenListener(RavenPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player's raven data
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Give the player a raven core if they don't have one
        if (player.hasPlayedBefore()) {
            // Auto-spawn raven if enabled in config
            if (plugin.getConfig().getBoolean("auto-spawn-on-join", false)) {
                plugin.getRavenManager().spawnRaven(player);
            }
        } else {
            // First time player
            ItemStack core = plugin.getAbilityManager().createRavenCore(player);
            player.getInventory().addItem(core);
            player.sendMessage("§dWelcome! You have received your raven core!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Clear player's cooldowns using the new direct cooldown manager
        plugin.getCooldownManager().clearCooldowns(player.getUniqueId());

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Cleared cooldowns for " + player.getName() + " on logout");
        }

        // Despawn raven when player leaves
        if (plugin.getRavenManager().hasRaven(player)) {
            plugin.getRavenManager().despawnRaven(player);
        }
    }

    // Note: The PlayerInteractEvent handler is now in the main plugin class
    // so we don't need to duplicate it here

    // New in 1.21 version - XP and coins from mob kills
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Only give XP and coins if the raven is spawned
        if (!plugin.getRavenManager().hasRaven(player)) {
            return;
        }

        // Get XP and coins from config
        int xp = plugin.getConfig().getInt("exp-per-mob-kill", 10);
        int coins = plugin.getConfig().getInt("coin-per-mob-kill", 1);

        // Bosses give more XP and coins
        if (event.getEntityType() == EntityType.ENDER_DRAGON ||
                event.getEntityType() == EntityType.WITHER) {
            xp *= 10;
            coins *= 10;
        }

        // Apply multipliers
        xp *= plugin.getConfig().getDouble("exp-multiplier", 1.0);
        coins *= plugin.getConfig().getDouble("coin-multiplier", 1.0);

        // Add XP and coins
        raven.addExperience(xp);
        plugin.getCoinManager().addCoins(player, coins);

        // Show notification (only for significant amounts)
        if (xp >= 20 || coins >= 5) {
            player.sendMessage("§dYour raven gained §b" + xp + " XP §dand §e" + coins + " coins §dfrom combat!");
        }
    }

    // New in 1.21 version - XP from block mining
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Only give XP if the raven is spawned
        if (!plugin.getRavenManager().hasRaven(player)) {
            return;
        }

        // Get XP from config and apply multiplier
        int xp = plugin.getConfig().getInt("exp-per-block-mine", 1);
        xp *= plugin.getConfig().getDouble("exp-multiplier", 1.0);

        // Add XP
        raven.addExperience(xp);
    }
}