package RavenMC.ravenPets.listeners;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final RavenPets plugin;

    public PlayerListener(RavenPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player's raven data - do this async to avoid lag on join
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getRavenManager().loadRaven(player.getUniqueId());

                // Check if player is new
                if (!player.hasPlayedBefore()) {
                    // Run tutorial in main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            startWelcomeTutorial(player);
                        }
                    }.runTask(plugin);
                }

                // Auto-summon raven if enabled
                if (plugin.getConfigManager().getConfig().getBoolean("settings.auto-spawn", true)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Get the loaded raven
                            Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
                            if (raven != null && !raven.isActive()) {
                                plugin.getRavenManager().spawnRaven(player);
                            }
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Despawn raven if active
        plugin.getRavenManager().despawnRaven(player);

        // Save raven data async to avoid lag on quit
        new BukkitRunnable() {
            @Override
            public void run() {
                Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
                if (raven != null) {
                    plugin.getDatabaseManager().saveRaven(raven);
                }

                // Unload raven data
                plugin.getRavenManager().unloadRaven(player.getUniqueId());
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Respawn raven if it was active
        Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
        if (raven != null && raven.isActive()) {
            // Delay the spawn to ensure player is fully respawned
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getRavenManager().spawnRaven(player);
                }
            }.runTaskLater(plugin, 20L); // 1 second delay
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Check for combat assistance ability
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());

            if (raven != null && raven.isActive() && raven.hasAbility("combat_assistance")) {
                // Boost damage for players with combat assistance ability
                double originalDamage = event.getFinalDamage();
                double multiplier = 1.0;

                // Different boost based on tier
                switch (raven.getTier()) {
                    case EXPERT:
                        multiplier = 1.1; // 10% boost
                        break;
                    case MASTER:
                        multiplier = 1.2; // 20% boost
                        break;
                    case LEGENDARY:
                        multiplier = 1.3; // 30% boost
                        break;
                    default:
                        // No boost for lower tiers
                        break;
                }

                event.setDamage(originalDamage * multiplier);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Award Raven XP for kills
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());

            if (raven != null) {
                // Award XP based on entity type
                String entityType = event.getEntityType().name();
                int xpAmount = calculateXpAmount(entityType);

                raven.addXp(xpAmount);
            }
        }
    }

    private int calculateXpAmount(String entityType) {
        // Different XP amounts based on entity type
        return switch (entityType) {
            case "ENDER_DRAGON" -> 1000;
            case "WITHER" -> 500;
            case "ELDER_GUARDIAN" -> 300;
            case "WARDEN" -> 700;
            case "RAVAGER" -> 200;
            case "CREEPER" -> 50;
            case "ZOMBIE", "SKELETON", "SPIDER" -> 20;
            default -> 10;
        };
    }

    private void startWelcomeTutorial(Player player) {
        // This would show a welcome tutorial to new players
        player.sendMessage("§5Welcome to RavenMC! Complete the welcome tutorial to claim your Raven Egg.");

        // In a real implementation, this would trigger a series of guided tasks
        // or teleport the player to a tutorial area
    }
}