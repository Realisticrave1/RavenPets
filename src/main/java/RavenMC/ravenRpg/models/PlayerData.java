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
        this.playerUUID = playerUUID;
        this.playerName = playerName;
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

    // Getters and setters
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Race getSelectedRace() { return selectedRace; }
    public void setSelectedRace(Race selectedRace) {
        this.selectedRace = selectedRace;
        this.hasChosenRace = true;
    }

    public Bloodline getSelectedBloodline() { return selectedBloodline; }
    public void setSelectedBloodline(Bloodline selectedBloodline) {
        this.selectedBloodline = selectedBloodline;
        this.hasChosenBloodline = true;
    }

    public String getCurrentGuild() { return currentGuild; }
    public void setCurrentGuild(String currentGuild) { this.currentGuild = currentGuild; }

    public Map<String, Integer> getStats() { return stats; }
    public Map<String, Integer> getSkills() { return skills; }
    public List<String> getOwnedShops() { return ownedShops; }

    public boolean hasChosenRace() { return hasChosenRace; }
    public boolean hasChosenBloodline() { return hasChosenBloodline; }
    public boolean isFullyInitialized() { return hasChosenRace && hasChosenBloodline; }

    public long getJoinDate() { return joinDate; }
    public Map<String, Object> getCustomData() { return customData; }

    // Stat methods
    public int getStat(String stat) { return stats.getOrDefault(stat, 0); }
    public void setStat(String stat, int value) { stats.put(stat, value); }
    public void addStat(String stat, int amount) {
        stats.put(stat, getStat(stat) + amount);
    }

    // Skill methods
    public int getSkill(String skill) { return skills.getOrDefault(skill, 1); }
    public void setSkill(String skill, int level) { skills.put(skill, level); }
    public void addSkillExp(String skill, int exp) {
        // Simple skill leveling system
        int currentLevel = getSkill(skill);
        int expRequired = currentLevel * 100;
        if (exp >= expRequired) {
            setSkill(skill, currentLevel + 1);
        }
    }
}