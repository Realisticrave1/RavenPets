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
        this.ravenPetsPlugin = rpgPlugin.getServer().getPluginManager().getPlugin("RavenPets");
        this.isEnabled = ravenPetsPlugin != null && ravenPetsPlugin.isEnabled();
        this.lastKnownExpMultipliers = new HashMap<>();
        this.lastUpdateTimes = new HashMap<>();

        if (isEnabled) {
            rpgPlugin.getLogger().info("RavenPets integration enabled!");
            setupIntegration();
            startPeriodicUpdates();
        } else {
            rpgPlugin.getLogger().info("RavenPets not found - integration disabled");
        }
    }

    private void setupIntegration() {
        // Register integration listeners and hooks
        rpgPlugin.getLogger().info("Setting up RavenPets integration hooks...");
    }

    private void startPeriodicUpdates() {
        // Update raven bonuses every 30 seconds for online players
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : rpgPlugin.getServer().getOnlinePlayers()) {
                    updatePlayerRavenBonuses(player);
                }
            }
        }.runTaskTimer(rpgPlugin, 600L, 600L); // 30 seconds delay, 30 seconds period
    }

    public void applyRpgBonusesToRaven(Player player) {
        if (!isEnabled) return;

        PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
        if (!data.isFullyInitialized()) return;

        try {
            updatePlayerRavenBonuses(player);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Failed to apply RPG bonuses to raven for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void updatePlayerRavenBonuses(Player player) {
        PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
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
    }

    private void applyRavenBonuses(Player player, PlayerData data, double expMultiplier) {
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
    }

    private void applyStatBonuses(Player player, PlayerData data) {
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
            rpgPlugin.getServer().dispatchCommand(rpgPlugin.getServer().getConsoleSender(), command);
        }
    }

    private void applyRacialBonuses(Player player, PlayerData data) {
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
    }

    private void applyBloodlineBonuses(Player player, PlayerData data) {
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
    }

    private void applySkillBonuses(Player player, PlayerData data) {
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
    }

    private void applyRavenCommand(Player player, String command) {
        // Apply RavenPets commands if the plugin supports them
        try {
            String fullCommand = "raven " + command + " " + player.getName();
            rpgPlugin.getServer().dispatchCommand(rpgPlugin.getServer().getConsoleSender(), fullCommand);
        } catch (Exception e) {
            rpgPlugin.getLogger().warning("Failed to apply raven command: " + command + " for " + player.getName());
        }
    }

    public double calculateTotalExpMultiplier(PlayerData data) {
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
    }

    public void onSkillLevelUp(Player player, String skill, int oldLevel, int newLevel) {
        if (!isEnabled) return;

        PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);

        // Apply immediate bonus for skill level up
        applyRpgBonusesToRaven(player);

        // Notify player of raven benefits
        if (rpgPlugin.getConfig().getBoolean("integration.ravenpets.notify-on-levelup", true)) {
            double newMultiplier = calculateTotalExpMultiplier(data);
            player.sendMessage("§5✦ Your raven gains strength from your " + skill + " mastery! §5✦");
            player.sendMessage("§6Raven EXP Multiplier: §f" + String.format("%.2fx", newMultiplier));
        }
    }

    public void onStatIncrease(Player player, String stat, int oldValue, int newValue) {
        if (!isEnabled) return;

        // Update raven bonuses when stats increase
        applyRpgBonusesToRaven(player);

        if (rpgPlugin.getConfig().getBoolean("integration.ravenpets.notify-on-stat-gain", true)) {
            player.sendMessage("§5✦ Your " + stat + " increase enhances your raven! §5✦");
        }
    }

    public void onPlayerJoin(Player player) {
        if (!isEnabled) return;

        // Apply bonuses when player joins (with small delay)
        new BukkitRunnable() {
            @Override
            public void run() {
                applyRpgBonusesToRaven(player);
            }
        }.runTaskLater(rpgPlugin, 60L); // 3 second delay
    }

    public void onPlayerQuit(Player player) {
        if (!isEnabled) return;

        // Clean up stored data
        UUID playerId = player.getUniqueId();
        lastKnownExpMultipliers.remove(playerId);
        lastUpdateTimes.remove(playerId);
    }

    private int getAverageSkillLevel(PlayerData data) {
        int total = data.getSkill("combat") + data.getSkill("mining") + data.getSkill("woodcutting") +
                data.getSkill("fishing") + data.getSkill("crafting") + data.getSkill("trading");
        return total / 6;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Plugin getRavenPetsPlugin() {
        return ravenPetsPlugin;
    }

    public Map<String, Object> getPlayerRavenBonuses(Player player) {
        PlayerData data = rpgPlugin.getDataManager().getPlayerData(player);
        Map<String, Object> bonuses = new HashMap<>();

        bonuses.put("expMultiplier", calculateTotalExpMultiplier(data));
        bonuses.put("intelligenceBonus", (data.getStat("intelligence") - 10) * 2);
        bonuses.put("luckBonus", (data.getStat("luck") - 10) * 1);
        bonuses.put("skillBonus", getAverageSkillLevel(data));
        bonuses.put("racialBonus", data.getSelectedRace() != null ? data.getSelectedRace().getDisplayName() : "None");
        bonuses.put("bloodlineBonus", data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDisplayName() : "None");

        return bonuses;
    }
}