package RavenMC.ravenPets;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class PlayerRaven {
    private final UUID ownerId;
    private UUID ravenEntityId;
    private BlockDisplay ravenEntity;
    private String name;
    private RavenTier tier;
    private int level;
    private int experience;
    private RavenElementType elementType;
    private boolean isSpawned;

    public PlayerRaven(UUID ownerId) {
        this.ownerId = ownerId;
        this.name = "Raven";
        this.tier = RavenTier.NOVICE;
        this.level = 1;
        this.experience = 0;
        this.elementType = RavenElementType.getRandomElement();
        this.isSpawned = false;
    }

    public void spawn(Location location) {
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            ravenEntity.teleport(location);
            return;
        }

        // Create BlockDisplay entity (new in 1.19.4+, better for 1.21)
        ravenEntity = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        ravenEntityId = ravenEntity.getUniqueId();

        // Configure block display
        ravenEntity.setBlock(getDisplayBlockData());
        ravenEntity.setGravity(false);

        // Set scale to make it smaller - Fixed using proper Transformation creation
        // Instead of using the private scale() method
        Transformation currentTransform = ravenEntity.getTransformation();
        Vector3f scale = new Vector3f(0.5f, 0.5f, 0.5f);
        Transformation newTransform = new Transformation(
                currentTransform.getTranslation(),
                currentTransform.getLeftRotation(),
                scale,  // Use the scale vector directly
                currentTransform.getRightRotation()
        );
        ravenEntity.setTransformation(newTransform);

        // Set custom name
        ravenEntity.setCustomName(getTierColor() + name);
        ravenEntity.setCustomNameVisible(true);

        isSpawned = true;
    }

    public void despawn() {
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            ravenEntity.remove();
        }

        isSpawned = false;
        ravenEntity = null;
    }

    private BlockData getDisplayBlockData() {
        switch (tier) {
            case NOVICE:
                return Bukkit.createBlockData(Material.PURPLE_CONCRETE);
            case ADEPT:
                return Bukkit.createBlockData(Material.PURPLE_TERRACOTTA);
            case EXPERT:
                return Bukkit.createBlockData(Material.PURPLE_GLAZED_TERRACOTTA);
            case MASTER:
                return Bukkit.createBlockData(Material.PURPLE_WOOL);
            case LEGENDARY:
                return Bukkit.createBlockData(Material.PURPUR_BLOCK);
            default:
                return Bukkit.createBlockData(Material.PURPLE_CONCRETE);
        }
    }

    private String getTierColor() {
        switch (tier) {
            case NOVICE:
                return "§d"; // Light Purple
            case ADEPT:
                return "§5"; // Dark Purple
            case EXPERT:
                return "§5§l"; // Bold Dark Purple
            case MASTER:
                return "§d§l"; // Bold Light Purple
            case LEGENDARY:
                return "§5§l§n"; // Bold Underlined Dark Purple
            default:
                return "§d"; // Light Purple
        }
    }

    public void addExperience(int exp) {
        this.experience += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int nextLevelExp = getRequiredExperienceForNextLevel();
        if (experience >= nextLevelExp) {
            level++;
            experience -= nextLevelExp;
            checkTierUp();
            checkLevelUp(); // Check for multiple level ups
        }
    }

    private void checkTierUp() {
        if (level >= 11 && tier == RavenTier.NOVICE) {
            tier = RavenTier.ADEPT;
        } else if (level >= 26 && tier == RavenTier.ADEPT) {
            tier = RavenTier.EXPERT;
        } else if (level >= 51 && tier == RavenTier.EXPERT) {
            tier = RavenTier.MASTER;
        } else if (level >= 76 && tier == RavenTier.MASTER) {
            tier = RavenTier.LEGENDARY;
        }

        // Update the display if spawned
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            ravenEntity.setBlock(getDisplayBlockData());
            ravenEntity.setCustomName(getTierColor() + name);
        }
    }

    private int getRequiredExperienceForNextLevel() {
        return 100 + (level * 50);
    }

    // Getters and setters below

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            ravenEntity.setCustomName(getTierColor() + name);
        }
    }

    public RavenTier getTier() {
        return tier;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public RavenElementType getElementType() {
        return elementType;
    }

    public boolean isSpawned() {
        return isSpawned;
    }
}