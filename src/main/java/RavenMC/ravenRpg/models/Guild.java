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
        this.name = name != null ? name : "Unknown Guild";
        this.displayName = displayName != null ? displayName : this.name;
        this.description = description != null ? description : "A new guild";
        this.type = type != null ? type : GuildType.MERCHANT_BAZAAR;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.officers = new ArrayList<>();
        this.settings = new HashMap<>();
        this.createdDate = System.currentTimeMillis();
        this.level = 1;
        this.treasury = 0.0;

        // Add leader as member if leader is not null
        if (leader != null) {
            members.add(leader);
        }
        initializeSettings();
    }

    private void initializeSettings() {
        settings.put("maxMembers", 20);
        settings.put("requireInvite", true);
        settings.put("allowPublicChat", true);
        settings.put("taxRate", 0.0);
    }

    // Getters and setters with null safety
    public String getName() {
        return name != null ? name : "Unknown Guild";
    }

    public String getDisplayName() {
        return displayName != null ? displayName : (name != null ? name : "Unknown Guild");
    }

    public String getDescription() {
        return description != null ? description : "A guild";
    }

    public GuildType getType() {
        return type != null ? type : GuildType.MERCHANT_BAZAAR;
    }

    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return members != null ? members : new ArrayList<>(); }
    public List<UUID> getOfficers() { return officers != null ? officers : new ArrayList<>(); }
    public Map<String, Object> getSettings() { return settings != null ? settings : new HashMap<>(); }
    public long getCreatedDate() { return createdDate; }
    public int getLevel() { return level; }
    public double getTreasury() { return treasury; }

    public void setLeader(UUID leader) { this.leader = leader; }
    public void setLevel(int level) { this.level = Math.max(1, level); }
    public void setTreasury(double treasury) { this.treasury = Math.max(0.0, treasury); }

    // Member management with null safety
    public boolean addMember(UUID playerUUID) {
        if (playerUUID == null) return false;
        if (members == null) members = new ArrayList<>();

        if (!members.contains(playerUUID)) {
            members.add(playerUUID);
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID playerUUID) {
        if (playerUUID == null) return false;
        if (officers != null) {
            officers.remove(playerUUID);
        }
        if (members != null) {
            return members.remove(playerUUID);
        }
        return false;
    }

    public boolean isOfficer(UUID playerUUID) {
        if (playerUUID == null || officers == null) return false;
        return officers.contains(playerUUID);
    }

    public boolean isLeader(UUID playerUUID) {
        if (playerUUID == null || leader == null) return false;
        return leader.equals(playerUUID);
    }

    public boolean isMember(UUID playerUUID) {
        if (playerUUID == null || members == null) return false;
        return members.contains(playerUUID);
    }

    public boolean promoteToOfficer(UUID playerUUID) {
        if (playerUUID == null) return false;
        if (officers == null) officers = new ArrayList<>();

        if (isMember(playerUUID) && !isOfficer(playerUUID)) {
            officers.add(playerUUID);
            return true;
        }
        return false;
    }

    public boolean demoteFromOfficer(UUID playerUUID) {
        if (playerUUID == null || officers == null) return false;
        return officers.remove(playerUUID);
    }
}