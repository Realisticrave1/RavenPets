package RavenMC.ravenRpg.models;

import java.util.*;

public class PlayerData {
    private UUID playerUUID;
    private String playerName;
    private Race selectedRace;
    private Bloodline selectedBloodline;
    private String currentGuild;
    private Map<String, Integer> stats;
    private Map<String, Integer> skills;
    private List<String> ownedShops;
    private boolean hasChosenRace;
    private boolean hasChosenBloodline;
    private long joinDate;
    private Map<String, Object> customData;

    public PlayerData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID != null ? playerUUID : UUID.randomUUID();
        this.playerName = playerName != null ? playerName : "Unknown";
        this.selectedRace = null;
        this.selectedBloodline = null;
        this.currentGuild = null;
        this.stats = new HashMap<>();
        this.skills = new HashMap<>();
        this.ownedShops = new ArrayList<>();
        this.hasChosenRace = false;
        this.hasChosenBloodline = false;
        this.joinDate = System.currentTimeMillis();
        this.customData = new HashMap<>();
        initializeDefaults();
    }

    private void initializeDefaults() {
        // Initialize base stats
        stats.put("strength", 10);
        stats.put("agility", 10);
        stats.put("intelligence", 10);
        stats.put("vitality", 10);
        stats.put("luck", 10);

        // Initialize skills
        skills.put("combat", 1);
        skills.put("mining", 1);
        skills.put("woodcutting", 1);
        skills.put("fishing", 1);
        skills.put("crafting", 1);
        skills.put("trading", 1);
    }

    // Getters and setters with null safety
    public UUID getPlayerUUID() {
        return playerUUID != null ? playerUUID : UUID.randomUUID();
    }

    public String getPlayerName() {
        return playerName != null ? playerName : "Unknown";
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName != null ? playerName : "Unknown";
    }

    public Race getSelectedRace() { return selectedRace; }
    public void setSelectedRace(Race selectedRace) {
        this.selectedRace = selectedRace;
        this.hasChosenRace = selectedRace != null;
    }

    public Bloodline getSelectedBloodline() { return selectedBloodline; }
    public void setSelectedBloodline(Bloodline selectedBloodline) {
        this.selectedBloodline = selectedBloodline;
        this.hasChosenBloodline = selectedBloodline != null;
    }

    public String getCurrentGuild() { return currentGuild; }
    public void setCurrentGuild(String currentGuild) { this.currentGuild = currentGuild; }

    public Map<String, Integer> getStats() {
        return stats != null ? stats : new HashMap<>();
    }

    public Map<String, Integer> getSkills() {
        return skills != null ? skills : new HashMap<>();
    }

    public List<String> getOwnedShops() {
        return ownedShops != null ? ownedShops : new ArrayList<>();
    }

    public boolean hasChosenRace() { return hasChosenRace && selectedRace != null; }
    public boolean hasChosenBloodline() { return hasChosenBloodline && selectedBloodline != null; }
    public boolean isFullyInitialized() { return hasChosenRace() && hasChosenBloodline(); }

    public long getJoinDate() { return joinDate; }
    public Map<String, Object> getCustomData() {
        return customData != null ? customData : new HashMap<>();
    }

    // Stat methods with safety checks
    public int getStat(String stat) {
        if (stat == null || stats == null) return 10; // Default stat value
        return stats.getOrDefault(stat, 10);
    }

    public void setStat(String stat, int value) {
        if (stat != null) {
            if (stats == null) stats = new HashMap<>();
            stats.put(stat, Math.max(0, value)); // Ensure non-negative
        }
    }

    public void addStat(String stat, int amount) {
        if (stat != null) {
            if (stats == null) stats = new HashMap<>();
            int currentValue = getStat(stat);
            stats.put(stat, Math.max(0, currentValue + amount));
        }
    }

    // Skill methods with safety checks
    public int getSkill(String skill) {
        if (skill == null || skills == null) return 1; // Default skill level
        return skills.getOrDefault(skill, 1);
    }

    public void setSkill(String skill, int level) {
        if (skill != null) {
            if (skills == null) skills = new HashMap<>();
            skills.put(skill, Math.max(1, level)); // Ensure at least level 1
        }
    }

    public void addSkillExp(String skill, int exp) {
        if (skill == null || exp <= 0) return;

        if (skills == null) skills = new HashMap<>();

        // Simple skill leveling system
        int currentLevel = getSkill(skill);
        int expRequired = currentLevel * 100; // Simple formula: level * 100 exp needed

        if (exp >= expRequired) {
            setSkill(skill, currentLevel + 1);
        }
    }

    // Utility methods
    public boolean hasCustomData(String key) {
        return key != null && customData != null && customData.containsKey(key);
    }

    public Object getCustomData(String key) {
        if (key == null || customData == null) return null;
        return customData.get(key);
    }

    public void setCustomData(String key, Object value) {
        if (key != null) {
            if (customData == null) customData = new HashMap<>();
            if (value != null) {
                customData.put(key, value);
            } else {
                customData.remove(key);
            }
        }
    }

    public void removeCustomData(String key) {
        if (key != null && customData != null) {
            customData.remove(key);
        }
    }

    // Validation methods
    public boolean isValid() {
        return playerUUID != null && playerName != null &&
                stats != null && skills != null &&
                ownedShops != null && customData != null;
    }

    public void validateAndFix() {
        if (playerUUID == null) playerUUID = UUID.randomUUID();
        if (playerName == null) playerName = "Unknown";
        if (stats == null) {
            stats = new HashMap<>();
            initializeDefaults();
        }
        if (skills == null) {
            skills = new HashMap<>();
            initializeDefaults();
        }
        if (ownedShops == null) ownedShops = new ArrayList<>();
        if (customData == null) customData = new HashMap<>();

        // Ensure hasChosen flags match actual selections
        hasChosenRace = selectedRace != null;
        hasChosenBloodline = selectedBloodline != null;

        // Ensure valid join date
        if (joinDate <= 0) joinDate = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "playerUUID=" + playerUUID +
                ", playerName='" + playerName + '\'' +
                ", selectedRace=" + selectedRace +
                ", selectedBloodline=" + selectedBloodline +
                ", currentGuild='" + currentGuild + '\'' +
                ", hasChosenRace=" + hasChosenRace +
                ", hasChosenBloodline=" + hasChosenBloodline +
                ", isFullyInitialized=" + isFullyInitialized() +
                '}';
    }
}