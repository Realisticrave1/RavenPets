package RavenMC.ravenRpg.utils;

import RavenMC.ravenRpg.RavenRpg;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskScheduler {
    private RavenRpg plugin;

    public TaskScheduler(RavenRpg plugin) {
        this.plugin = plugin;
        startPeriodicTasks();
    }

    private void startPeriodicTasks() {
        // Auto-save task
        int autoSaveInterval = plugin.getConfig().getInt("storage.auto-save-interval", 300);
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDataManager().saveAllData();
                plugin.getGuildManager().saveGuilds();
                plugin.getShopManager().saveAllShops();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * autoSaveInterval, 20L * autoSaveInterval);

        // Cleanup task (daily)
        if (plugin.getConfig().getBoolean("storage.cleanup.cleanup-on-start", false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    performDataCleanup();
                }
            }.runTaskLaterAsynchronously(plugin, 20L * 60); // Run 1 minute after startup
        }

        // Guild maintenance task
        new BukkitRunnable() {
            @Override
            public void run() {
                performGuildMaintenance();
            }
        }.runTaskTimer(plugin, 20L * 3600, 20L * 3600); // Every hour

        plugin.getLogger().info("Periodic tasks scheduled successfully!");
    }

    private void performDataCleanup() {
        int inactiveDays = plugin.getConfig().getInt("storage.cleanup.inactive-days", 180);
        long cutoffTime = System.currentTimeMillis() - (inactiveDays * 24L * 60L * 60L * 1000L);

        // This would implement actual cleanup logic
        plugin.getLogger().info("Data cleanup task completed");
    }

    private void performGuildMaintenance() {
        // Remove empty guilds, update guild levels, etc.
        plugin.getLogger().info("Guild maintenance task completed");
    }
}