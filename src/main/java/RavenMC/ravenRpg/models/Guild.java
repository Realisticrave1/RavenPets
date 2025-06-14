package RavenMC.ravenRpg.models;

import java.util.*;

public class Guild {
    private String name;
    private String displayName;
    private String description;
    private GuildType type;
    private UUID leader;
    private List<UUID> members;
    private List<UUID> officers;
    private Map<String, Object> settings;
    private long createdDate;
    private int level;
    private double treasury;

    public Guild(String name, String displayName, String description, GuildType type, UUID leader) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.officers = new ArrayList<>();
        this.settings = new HashMap<>();
        this.createdDate = System.currentTimeMillis();
        this.level = 1;
        this.treasury = 0.0;

        // Add leader as member
        members.add(leader);
        initializeSettings();
    }

    private void initializeSettings() {
        settings.put("maxMembers", 20);
        settings.put("requireInvite", true);
        settings.put("allowPublicChat", true);
        settings.put("taxRate", 0.0);
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public GuildType getType() { return type; }
    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return members; }
    public List<UUID> getOfficers() { return officers; }
    public Map<String, Object> getSettings() { return settings; }
    public long getCreatedDate() { return createdDate; }
    public int getLevel() { return level; }
    public double getTreasury() { return treasury; }

    public void setLeader(UUID leader) { this.leader = leader; }
    public void setLevel(int level) { this.level = level; }
    public void setTreasury(double treasury) { this.treasury = treasury; }

    // Member management
    public boolean addMember(UUID playerUUID) {
        if (!members.contains(playerUUID)) {
            members.add(playerUUID);
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID playerUUID) {
        officers.remove(playerUUID);
        return members.remove(playerUUID);
    }

    public boolean isOfficer(UUID playerUUID) {
        return officers.contains(playerUUID);
    }

    public boolean isLeader(UUID playerUUID) {
        return leader.equals(playerUUID);
    }

    public boolean isMember(UUID playerUUID) {
        return members.contains(playerUUID);
    }

    public boolean promoteToOfficer(UUID playerUUID) {
        if (isMember(playerUUID) && !isOfficer(playerUUID)) {
            officers.add(playerUUID);
            return true;
        }
        return false;
    }

    public boolean demoteFromOfficer(UUID playerUUID) {
        return officers.remove(playerUUID);
    }
}