package RavenMC.ravenPets.model;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.abilities.RavenAbility;
import RavenMC.ravenPets.enums.RavenTier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class Raven {

    private final UUID ownerId;
    private UUID entityId;
    private String name;
    private int level;
    private int xp;
    private RavenTier tier;
    private Map<String, Boolean> unlockedAbilities;
    private int inventorySlots;
    private int flightDuration;
    private int detectionRadius;
    private boolean isActive;
    private List<Location> homeLocations;
    private Entity entity;

    public Raven(UUID ownerId) {
        this.ownerId = ownerId;
        this.name = "Raven";
        this.level = 1;
        this.xp = 0;
        this.tier = RavenTier.NOVICE;
        this.unlockedAbilities = new HashMap<>();
        this.inventorySlots = 3; // Default for Novice
        this.flightDuration = 0; // No flight for Novice
        this.detectionRadius = 10; // Default for Novice
        this.isActive = false;
        this.homeLocations = new ArrayList<>();

        // Initialize default abilities for Novice
        for (RavenAbility ability : RavenAbility.values()) {
            if (ability.getRequiredTier() == RavenTier.NOVICE) {
                unlockedAbilities.put(ability.getName(), true);
            } else {
                unlockedAbilities.put(ability.getName(), false);
            }
        }
    }

    public void spawn(Player owner) {
        if (entity != null && !entity.isDead()) {
            teleportToOwner(owner);
            return;
        }

        Location spawnLoc = owner.getLocation().add(0, 1.5, 0);
        Entity newEntity = owner.getWorld().spawnEntity(spawnLoc, EntityType.PARROT);

        if (newEntity instanceof Parrot parrot) {
            parrot.setVariant(Parrot.Variant.BLUE); // Closest to purple
            parrot.setCustomName(getTierColorCode() + name + " (Lvl " + level + ")");
            parrot.setCustomNameVisible(true);
            parrot.setOwner(owner);
            parrot.setSitting(false);

            // Store raven data in entity
            PersistentDataContainer container = parrot.getPersistentDataContainer();
            container.set(RavenPets.getInstance().getConfigManager().getRavenKey(),
                    PersistentDataType.STRING, ownerId.toString());

            this.entity = parrot;
            this.entityId = parrot.getUniqueId();
            this.isActive = true;

            // Apply visual effects based on tier
            applyTierEffects(parrot);
        }
    }

    public void despawn() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
        this.entity = null;
        this.isActive = false;
    }

    public void teleportToOwner(Player owner) {
        if (entity == null || entity.isDead()) {
            spawn(owner);
            return;
        }

        Location ownerLoc = owner.getLocation();
        Vector direction = ownerLoc.getDirection().normalize();
        Location spawnLoc = ownerLoc.clone().add(direction.multiply(-1).normalize().multiply(1.5));
        spawnLoc.setY(ownerLoc.getY() + 1.5);

        entity.teleport(spawnLoc);
    }

    public void addXp(int amount) {
        this.xp += amount;
        checkLevelUp();
        updateNameTag();
    }

    private void checkLevelUp() {
        int xpRequired = calculateRequiredXp();
        if (xp >= xpRequired && canLevelUp()) {
            level++;
            xp -= xpRequired;

            // Check for tier upgrade
            if (level == 11) updateTier(RavenTier.ADEPT);
            else if (level == 26) updateTier(RavenTier.EXPERT);
            else if (level == 51) updateTier(RavenTier.MASTER);
            else if (level == 76) updateTier(RavenTier.LEGENDARY);

            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage("§5Your raven has reached level " + level + "!");

                // Check if tier changed
                if (level == 11 || level == 26 || level == 51 || level == 76) {
                    owner.sendMessage("§5Your raven has evolved to " + tier.getName() + " tier!");
                }
            }

            checkLevelUp(); // Check for multiple level ups
        }
    }

    private int calculateRequiredXp() {
        return 100 + (level * 50); // Simple formula: 100 + (level * 50) XP per level
    }

    private boolean canLevelUp() {
        // Check if all requirements are met for the next tier
        if (level == 10 && tier == RavenTier.NOVICE) {
            return true; // Implement actual checks for tier requirements
        } else if (level == 25 && tier == RavenTier.ADEPT) {
            return true; // Implement actual checks for tier requirements
        } else if (level == 50 && tier == RavenTier.EXPERT) {
            return true; // Implement actual checks for tier requirements
        } else if (level == 75 && tier == RavenTier.MASTER) {
            return true; // Implement actual checks for tier requirements
        } else {
            return level < 100; // Cap at level 100
        }
    }

    private void updateTier(RavenTier newTier) {
        this.tier = newTier;

        // Update stats based on new tier
        switch (newTier) {
            case ADEPT:
                this.inventorySlots = 6;
                this.flightDuration = 30;
                this.detectionRadius = 30;
                break;
            case EXPERT:
                this.inventorySlots = 9;
                this.flightDuration = 120;
                this.detectionRadius = 50;
                break;
            case MASTER:
                this.inventorySlots = 12;
                this.flightDuration = 300;
                this.detectionRadius = 100;
                break;
            case LEGENDARY:
                this.inventorySlots = Integer.MAX_VALUE;
                this.flightDuration = Integer.MAX_VALUE;
                this.detectionRadius = 1000;
                break;
        }

        // Update unlocked abilities
        for (RavenAbility ability : RavenAbility.values()) {
            if (ability.getRequiredTier().ordinal() <= newTier.ordinal()) {
                unlockedAbilities.put(ability.getName(), true);
            }
        }

        // Apply visual effects if entity exists
        if (entity != null && !entity.isDead() && entity instanceof Parrot) {
            applyTierEffects((Parrot) entity);
        }
    }

    private void applyTierEffects(Parrot parrot) {
        // Apply visual effects based on tier
        switch (tier) {
            case NOVICE:
                // Basic effects
                break;
            case ADEPT:
                // Add particle effects or other visual changes
                break;
            case EXPERT:
                // Add glowing eyes effect
                parrot.setGlowing(true);
                break;
            case MASTER:
                // Add more intense effects
                parrot.setGlowing(true);
                break;
            case LEGENDARY:
                // Add legendary effects
                parrot.setGlowing(true);
                // More particles and effects would be added here
                break;
        }
    }

    private void updateNameTag() {
        if (entity != null && !entity.isDead()) {
            entity.setCustomName(getTierColorCode() + name + " (Lvl " + level + ")");
        }
    }

    private String getTierColorCode() {
        switch (tier) {
            case NOVICE: return "§d"; // Light Purple
            case ADEPT: return "§5"; // Dark Purple
            case EXPERT: return "§5§l"; // Bold Dark Purple
            case MASTER: return "§5§l"; // Bold Dark Purple
            case LEGENDARY: return "§5§l§o"; // Bold Italic Dark Purple
            default: return "§d";
        }
    }

    public void addHomeLocation(Location location) {
        int maxHomes = getMaxHomeLocations();
        if (homeLocations.size() < maxHomes) {
            homeLocations.add(location.clone());
        }
    }

    private int getMaxHomeLocations() {
        switch (tier) {
            case NOVICE: return 0;
            case ADEPT: return 3;
            case EXPERT: return 5;
            case MASTER: return 8;
            case LEGENDARY: return Integer.MAX_VALUE;
            default: return 0;
        }
    }

    public boolean hasAbility(String abilityName) {
        return unlockedAbilities.getOrDefault(abilityName, false);
    }

    // Getters and Setters
    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateNameTag();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        updateNameTag();
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public RavenTier getTier() {
        return tier;
    }

    public int getInventorySlots() {
        return inventorySlots;
    }

    public int getFlightDuration() {
        return flightDuration;
    }

    public int getDetectionRadius() {
        return detectionRadius;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<Location> getHomeLocations() {
        return homeLocations;
    }

    public Entity getEntity() {
        return entity;
    }
}