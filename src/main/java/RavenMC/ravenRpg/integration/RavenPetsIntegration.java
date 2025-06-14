package RavenMC.ravenRpg.integration;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import RavenMC.ravenRpg.models.Race;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RavenPetsIntegration {
    private RavenRpg rpgPlugin;
    private Plugin ravenPetsPlugin;
    private boolean isEnabled;
    private Map<UUID, Double> lastKnownExpMultipliers;
    private Map<UUID, Long> lastUpdateTimes;

    public RavenPetsIntegration(RavenRpg rpgPlugin) {
        this.rpgPlugin = rpgPlugin;
        this.lastKnownExpMultipliers = new HashMap<>();
        this.lastUpdateTimes = new HashMap<>();

        try {
            this.ravenPetsPlugin = rpgPlugin.getServer().getPluginManager().getPlugin("RavenPets");
            this.isEnabled = ravenPetsPlugin != null && ravenPetsPlugin.isEnabled();

            if (isEnabled) {
                rpgPlugin.getLogger().info("RavenPets integration enabled!");
                setupIntegration();
                startPeriodicUpdates();
            } else {
                rpgPlugin.getLogger().info("RavenPets not found - integration disabled");
            }
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error initializing RavenPets integration: " + e.getMessage());
            this.isEnabled = false;
        }
    }

    private void setupIntegration() {
        try {
            // Register integration listeners and hooks
            rpgPlugin.getLogger().info("Setting up RavenPets integration hooks...");
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error setting up RavenPets integration: " + e.getMessage());
            isEnabled = false;
        }
    }

    private void startPeriodicUpdates() {
        try {
            // Update raven bonuses every 30 seconds for online players
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        for (Player player : rpgPlugin.getServer().getOnlinePlayers()) {
                            if (player != null && player.isOnline()) {
                                updatePlayerRavenBonuses(player);
                            }
                        }
                    } catch (Exception e) {
                        rpgPlugin.getLogger().warning("Error in periodic RavenPets update: " + e.getMessage());
                    }
                }
            }.runTaskTimer(rpgPlugin, 600L, 600L); // 30 seconds delay, 30 seconds period
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error starting periodic RavenPets updates: " + e.getMessage());
        }
    }

    public void applyRpgBonusesToRaven(Player player) {
        if (!isEnabled || player == null) return;

        try {
            if (rpgPlugin.getDataManager() == null) {
                rpgPlugin.getLogger().warning("DataManager is null, cannot apply RavenPets bonuses");
                return;
            }

            PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
            if (data == null) {
                rpgPlugin.getLogger().warning("PlayerData is null for " + player.getName() + ", cannot apply RavenPets bonuses");
                return;
            }

            if (!data.isFullyInitialized()) return;

            updatePlayerRavenBonuses(player);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Failed to apply RPG bonuses to raven for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void updatePlayerRavenBonuses(Player player) {
        if (player == null || !player.isOnline()) return;

        try {
            if (rpgPlugin.getDataManager() == null) return;

            PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
            if (data == null) return;

            UUID playerId = player.getUniqueId();

            // Calculate current multipliers
            double currentMultiplier = calculateTotalExpMultiplier(data);

            // Check if multiplier has changed significantly
            Double lastMultiplier = lastKnownExpMultipliers.get(playerId);
            Long lastUpdate = lastUpdateTimes.get(playerId);
            long currentTime = System.currentTimeMillis();

            // Update if it's been more than 5 minutes or multiplier changed by more than 5%
            boolean shouldUpdate = lastMultiplier == null ||
                    lastUpdate == null ||
                    (currentTime - lastUpdate) > 300000 || // 5 minutes
                    Math.abs(currentMultiplier - lastMultiplier) > 0.05;

            if (shouldUpdate) {
                applyRavenBonuses(player, data, currentMultiplier);
                lastKnownExpMultipliers.put(playerId, currentMultiplier);
                lastUpdateTimes.put(playerId, currentTime);
            }
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error updating raven bonuses for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applyRavenBonuses(Player player, PlayerData data, double expMultiplier) {
        if (player == null || data == null) return;

        try {
            // Since we don't have direct access to RavenPets API, we'll use reflection or commands
            // For now, we'll log the bonus and notify the player

            if (rpgPlugin.getConfig().getBoolean("debug.log-integration", false)) {
                rpgPlugin.getLogger().info("Applying RPG bonuses to " + player.getName() + "'s raven:");
                rpgPlugin.getLogger().info("  Experience Multiplier: " + String.format("%.2fx", expMultiplier));
            }

            // Apply stat-based bonuses
            applyStatBonuses(player, data);

            // Apply racial bonuses
            if (data.getSelectedRace() != null) {
                applyRacialBonuses(player, data);
            }

            // Apply bloodline bonuses
            if (data.getSelectedBloodline() != null) {
                applyBloodlineBonuses(player, data);
            }

            // Apply skill-based bonuses
            applySkillBonuses(player, data);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error applying raven bonuses for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applyStatBonuses(Player player, PlayerData data) {
        if (player == null || data == null) return;

        try {
            int intelligence = data.getStat("intelligence");
            int luck = data.getStat("luck");
            int agility = data.getStat("agility");

            // These would be actual RavenPets API calls if available
            // For now, we'll store the bonuses in custom data for reference
            data.getCustomData().put("ravenBonus_intelligence", (intelligence - 10) * 2);
            data.getCustomData().put("ravenBonus_luck", (luck - 10) * 1);
            data.getCustomData().put("ravenBonus_agility", (agility - 10) * 0.5);

            // Apply actual bonuses through RavenPets commands or API
            if (rpgPlugin.getConfig().getBoolean("integration.ravenpets.use-commands", true)) {
                // Example: Use commands to apply bonuses
                String command = "ravenpets bonus " + player.getName() + " exp " + String.format("%.2f", calculateTotalExpMultiplier(data));
                try {
                    rpgPlugin.getServer().dispatchCommand(rpgPlugin.getServer().getConsoleSender(), command);
                } catch (Exception e) {
                    // Command may not exist, ignore
                }
            }
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error applying stat bonuses for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applyRacialBonuses(Player player, PlayerData data) {
        if (player == null || data == null || data.getSelectedRace() == null) return;

        try {
            Race race = data.getSelectedRace();

            switch (race) {
                case HUMAN:
                    // Humans get balanced raven bonuses
                    data.getCustomData().put("ravenBonus_racial", "balanced_growth");
                    applyRavenCommand(player, "settype balanced");
                    break;
                case ORK:
                    // Orks get stronger, more aggressive ravens
                    data.getCustomData().put("ravenBonus_racial", "combat_focused");
                    applyRavenCommand(player, "settype aggressive");
                    break;
                case ELF:
                    // Elves get more graceful, nature-attuned ravens
                    data.getCustomData().put("ravenBonus_racial", "nature_attuned");
                    applyRavenCommand(player, "settype graceful");
                    break;
                case VAMPIRE:
                    // Vampires get darker, more mysterious ravens
                    data.getCustomData().put("ravenBonus_racial", "dark_mystical");
                    applyRavenCommand(player, "settype dark");
                    break;
            }
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error applying racial bonuses for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applyBloodlineBonuses(Player player, PlayerData data) {
        if (player == null || data == null || data.getSelectedBloodline() == null) return;

        try {
            String bloodlineBonus = "";

            switch (data.getSelectedBloodline()) {
                case HUMAN_STRONGHOLD:
                    bloodlineBonus = "leadership_aura";
                    break;
                case ORK_WARCAMP:
                    bloodlineBonus = "battle_fury";
                    break;
                case ELVEN_GROVE:
                    bloodlineBonus = "nature_magic";
                    break;
                case VAMPIRE_CRYPT:
                    bloodlineBonus = "blood_magic";
                    break;
            }

            data.getCustomData().put("ravenBonus_bloodline", bloodlineBonus);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error applying bloodline bonuses for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applySkillBonuses(Player player, PlayerData data) {
        if (player == null || data == null) return;

        try {
            // Apply bonuses based on highest skills
            int combatLevel = data.getSkill("combat");
            int miningLevel = data.getSkill("mining");
            int fishingLevel = data.getSkill("fishing");

            // Store skill bonuses
            data.getCustomData().put("ravenBonus_combat", combatLevel);
            data.getCustomData().put("ravenBonus_mining", miningLevel);
            data.getCustomData().put("ravenBonus_fishing", fishingLevel);

            // Apply skill-based experience multiplier
            double skillMultiplier = 1.0 + (getAverageSkillLevel(data) * 0.01); // 1% per average skill level
            data.getCustomData().put("ravenBonus_skillMultiplier", skillMultiplier);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error applying skill bonuses for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void applyRavenCommand(Player player, String command) {
        if (player == null || command == null) return;

        try {
            // Apply RavenPets commands if the plugin supports them
            String fullCommand = "raven " + command + " " + player.getName();
            rpgPlugin.getServer().dispatchCommand(rpgPlugin.getServer().getConsoleSender(), fullCommand);
        } catch (Exception e) {
            // Command may not exist, this is normal
        }
    }

    public double calculateTotalExpMultiplier(PlayerData data) {
        if (data == null) return 1.0;

        try {
            double multiplier = 1.0;

            // Base config multiplier
            multiplier *= rpgPlugin.getConfig().getDouble("integration.ravenpets.base-multiplier", 1.0);

            // Intelligence bonus (2% per point above 10)
            int intelligence = data.getStat("intelligence");
            multiplier += (intelligence - 10) * rpgPlugin.getConfig().getDouble("integration.ravenpets.experience-multipliers.intelligence", 0.02);

            // Luck bonus (1% per point above 10)
            int luck = data.getStat("luck");
            multiplier += (luck - 10) * rpgPlugin.getConfig().getDouble("integration.ravenpets.experience-multipliers.luck", 0.01);

            // Skill bonus (0.5% per average skill level)
            double skillBonus = getAverageSkillLevel(data) * 0.005;
            multiplier += skillBonus;

            // Racial multiplier
            if (data.getSelectedRace() != null) {
                switch (data.getSelectedRace()) {
                    case HUMAN:
                        multiplier *= rpgPlugin.getConfig().getDouble("integration.ravenpets.racial-multipliers.human", 1.1);
                        break;
                    case ORK:
                        multiplier *= rpgPlugin.getConfig().getDouble("integration.ravenpets.racial-multipliers.ork", 1.15);
                        break;
                    case ELF:
                        multiplier *= rpgPlugin.getConfig().getDouble("integration.ravenpets.racial-multipliers.elf", 1.2);
                        break;
                    case VAMPIRE:
                        multiplier *= rpgPlugin.getConfig().getDouble("integration.ravenpets.racial-multipliers.vampire", 1.25);
                        break;
                }
            }

            return Math.max(1.0, multiplier); // Ensure multiplier is at least 1.0
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error calculating exp multiplier: " + e.getMessage());
            return 1.0;
        }
    }

    public void onSkillLevelUp(Player player, String skill, int oldLevel, int newLevel) {
        if (!isEnabled || player == null || skill == null) return;

        try {
            if (rpgPlugin.getDataManager() == null) return;

            PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
            if (data == null) return;

            // Apply immediate bonus for skill level up
            applyRpgBonusesToRaven(player);

            // Notify player of raven benefits
            if (rpgPlugin.getConfig().getBoolean("integration.ravenpets.notify-on-levelup", true)) {
                double newMultiplier = calculateTotalExpMultiplier(data);
                player.sendMessage("§5✦ Your raven gains strength from your " + skill + " mastery! §5✦");
                player.sendMessage("§6Raven EXP Multiplier: §f" + String.format("%.2fx", newMultiplier));
            }
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error in onSkillLevelUp for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void onStatIncrease(Player player, String stat, int oldValue, int newValue) {
        if (!isEnabled || player == null || stat == null) return;

        try {
            // Update raven bonuses when stats increase
            applyRpgBonusesToRaven(player);

            if (rpgPlugin.getConfig().getBoolean("integration.ravenpets.notify-on-stat-gain", true)) {
                player.sendMessage("§5✦ Your " + stat + " increase enhances your raven! §5✦");
            }
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error in onStatIncrease for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void onPlayerJoin(Player player) {
        if (!isEnabled || player == null) return;

        try {
            // Apply bonuses when player joins (with small delay)
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        applyRpgBonusesToRaven(player);
                    }
                }
            }.runTaskLater(rpgPlugin, 60L); // 3 second delay
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error in onPlayerJoin for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void onPlayerQuit(Player player) {
        if (!isEnabled || player == null) return;

        try {
            // Clean up stored data
            UUID playerId = player.getUniqueId();
            lastKnownExpMultipliers.remove(playerId);
            lastUpdateTimes.remove(playerId);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error in onPlayerQuit for " + player.getName() + ": " + e.getMessage());
        }
    }

    private int getAverageSkillLevel(PlayerData data) {
        if (data == null) return 1;

        try {
            int total = data.getSkill("combat") + data.getSkill("mining") + data.getSkill("woodcutting") +
                    data.getSkill("fishing") + data.getSkill("crafting") + data.getSkill("trading");
            return total / 6;
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error calculating average skill level: " + e.getMessage());
            return 1;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Plugin getRavenPetsPlugin() {
        return ravenPetsPlugin;
    }

    public Map<String, Object> getPlayerRavenBonuses(Player player) {
        Map<String, Object> bonuses = new HashMap<>();

        try {
            if (player == null || rpgPlugin.getDataManager() == null) {
                // Return default values
                bonuses.put("expMultiplier", 1.0);
                bonuses.put("intelligenceBonus", 0);
                bonuses.put("luckBonus", 0);
                bonuses.put("skillBonus", 0);
                bonuses.put("racialBonus", "None");
                bonuses.put("bloodlineBonus", "None");
                return bonuses;
            }

            PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
            if (data == null) {
                // Return default values
                bonuses.put("expMultiplier", 1.0);
                bonuses.put("intelligenceBonus", 0);
                bonuses.put("luckBonus", 0);
                bonuses.put("skillBonus", 0);
                bonuses.put("racialBonus", "None");
                bonuses.put("bloodlineBonus", "None");
                return bonuses;
            }

            bonuses.put("expMultiplier", calculateTotalExpMultiplier(data));
            bonuses.put("intelligenceBonus", (data.getStat("intelligence") - 10) * 2);
            bonuses.put("luckBonus", (data.getStat("luck") - 10) * 1);
            bonuses.put("skillBonus", getAverageSkillLevel(data));
            bonuses.put("racialBonus", data.getSelectedRace() != null ? data.getSelectedRace().getDisplayName() : "None");
            bonuses.put("bloodlineBonus", data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDisplayName() : "None");
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Error getting player raven bonuses for " + player.getName() + ": " + e.getMessage());
            // Return default values on error
            bonuses.put("expMultiplier", 1.0);
            bonuses.put("intelligenceBonus", 0);
            bonuses.put("luckBonus", 0);
            bonuses.put("skillBonus", 0);
            bonuses.put("racialBonus", "None");
            bonuses.put("bloodlineBonus", "None");
        }

        return bonuses;
    }
}