package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BackupManager {
    private RavenRpg plugin;
    private File backupFolder;
    private SimpleDateFormat dateFormat;

    public BackupManager(RavenRpg plugin) {
        this.plugin = plugin;
        this.backupFolder = new File(plugin.getDataFolder(), "backups");
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        startBackupScheduler();
    }

    private void startBackupScheduler() {
        if (!plugin.getConfig().getBoolean("storage.backup.enabled", true)) {
            return;
        }

        int interval = plugin.getConfig().getInt("storage.backup.interval", 86400); // Default: daily

        new BukkitRunnable() {
            @Override
            public void run() {
                createBackup();
                cleanOldBackups();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * interval, 20L * interval);
    }

    public void createBackup() {
        try {
            String timestamp = dateFormat.format(new Date());
            File backupDir = new File(backupFolder, "backup_" + timestamp);
            backupDir.mkdirs();

            // Backup player data
            File playerDataDir = new File(plugin.getDataFolder(), "playerdata");
            if (playerDataDir.exists()) {
                copyDirectory(playerDataDir.toPath(), new File(backupDir, "playerdata").toPath());
            }

            // Backup guilds
            File guildsFile = new File(plugin.getDataFolder(), "guilds.yml");
            if (guildsFile.exists()) {
                Files.copy(guildsFile.toPath(), new File(backupDir, "guilds.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Backup shops
            File shopsFile = new File(plugin.getDataFolder(), "shops.yml");
            if (shopsFile.exists()) {
                Files.copy(shopsFile.toPath(), new File(backupDir, "shops.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Backup config
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (configFile.exists()) {
                Files.copy(configFile.toPath(), new File(backupDir, "config.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            plugin.getLogger().info("Backup created successfully: " + backupDir.getName());

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to copy file: " + sourcePath + " - " + e.getMessage());
            }
        });
    }

    private void cleanOldBackups() {
        int keepBackups = plugin.getConfig().getInt("storage.backup.keep-backups", 7);

        File[] backups = backupFolder.listFiles(File::isDirectory);
        if (backups == null || backups.length <= keepBackups) {
            return;
        }

        // Sort by name (which includes timestamp)
        Arrays.sort(backups, (a, b) -> b.getName().compareTo(a.getName()));

        // Delete old backups
        for (int i = keepBackups; i < backups.length; i++) {
            deleteDirectory(backups[i]);
            plugin.getLogger().info("Deleted old backup: " + backups[i].getName());
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}