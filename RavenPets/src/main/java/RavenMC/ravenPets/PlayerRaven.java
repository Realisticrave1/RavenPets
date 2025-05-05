package RavenMC.ravenPets;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    private boolean hasCustomColors;
    private boolean hasCustomParticles;
    private BukkitTask particleTask;

    public PlayerRaven(UUID ownerId) {
        this.ownerId = ownerId;
        this.name = "Raven";
        this.tier = RavenTier.NOVICE;
        this.level = 1;
        this.experience = 0;
        // Default to a random element type - this will be overridden if saved data exists
        this.elementType = RavenElementType.getRandomElement();
        this.isSpawned = false;
        this.hasCustomColors = false;
        this.hasCustomParticles = false;
    }

    public void spawn(Location location) {
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            ravenEntity.teleport(location);
            return;
        }

        // Create BlockDisplay entity (for Minecraft 1.21)
        ravenEntity = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        ravenEntityId = ravenEntity.getUniqueId();

        // Configure block display
        ravenEntity.setBlock(getDisplayBlockData());
        ravenEntity.setGravity(false);

        // Make the raven glow like an enchanted item
        ravenEntity.setGlowing(true);

        // Set glowing color based on element - fixed to use the correct method
        try {
            // Use the correct Color object method
            org.bukkit.Color glowColor = getElementJavaColor();
            // Try to apply the color if the method exists
            try {
                // This is only available in newer versions, so we'll handle it gracefully
                ravenEntity.setGlowColorOverride(glowColor);
            } catch (NoSuchMethodError e) {
                // If not supported, it will use the default color
            }
        } catch (Exception e) {
            // Fallback for any other errors
        }

        // Set scale to make it smaller
        Transformation currentTransform = ravenEntity.getTransformation();
        Vector3f scale = new Vector3f(0.5f, 0.5f, 0.5f);
        Transformation newTransform = new Transformation(
                currentTransform.getTranslation(),
                currentTransform.getLeftRotation(),
                scale,
                currentTransform.getRightRotation()
        );
        ravenEntity.setTransformation(newTransform);

        // Set custom name with level indicator and stylized display
        updateRavenName();
        ravenEntity.setCustomNameVisible(true);

        // Spawn effect
        spawnEffect(location);

        // Start particle effects
        startParticleEffects();

        isSpawned = true;
    }

    private void spawnEffect(Location location) {
        location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 0.5, 0.5, 0.5, 0.5);
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);

        // Element-specific particles
        Particle elementParticle = getElementParticle();
        location.getWorld().spawnParticle(elementParticle, location, 30, 0.3, 0.3, 0.3, 0.05);
    }

    public void despawn() {
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            // Despawn effect
            Location location = ravenEntity.getLocation();
            location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 50, 0.5, 0.5, 0.5, 0.5);
            location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.8f);

            // Remove entity
            ravenEntity.remove();

            // Stop particle task
            if (particleTask != null) {
                particleTask.cancel();
                particleTask = null;
            }
        }

        isSpawned = false;
        ravenEntity = null;
    }

    private BlockData getDisplayBlockData() {
        // If player has custom colors unlocked, use element-specific blocks
        if (hasCustomColors) {
            switch (elementType) {
                case FIRE:
                    return Bukkit.createBlockData(Material.MAGMA_BLOCK);
                case WATER:
                    return Bukkit.createBlockData(Material.BLUE_CONCRETE);
                case EARTH:
                    return Bukkit.createBlockData(Material.EMERALD_BLOCK);
                case AIR:
                    return Bukkit.createBlockData(Material.WHITE_CONCRETE);
                case LIGHTNING:
                    return Bukkit.createBlockData(Material.YELLOW_CONCRETE);
                case ICE:
                    return Bukkit.createBlockData(Material.LIGHT_BLUE_CONCRETE);
                case NATURE:
                    return Bukkit.createBlockData(Material.LIME_CONCRETE);
                case DARKNESS:
                    return Bukkit.createBlockData(Material.BLACK_CONCRETE);
                case LIGHT:
                    return Bukkit.createBlockData(Material.GLOWSTONE);
                default:
                    return Bukkit.createBlockData(Material.PURPLE_CONCRETE);
            }
        }

        // Default tier-based blocks (purple theme)
        switch (tier) {
            case NOVICE:
                return Bukkit.createBlockData(Material.PURPLE_CONCRETE);
            case ADEPT:
                return Bukkit.createBlockData(Material.PURPLE_TERRACOTTA);
            case EXPERT:
                return Bukkit.createBlockData(Material.PURPLE_GLAZED_TERRACOTTA);
            case MASTER:
                return Bukkit.createBlockData(Material.PURPUR_BLOCK);
            case LEGENDARY:
                return Bukkit.createBlockData(Material.AMETHYST_BLOCK);
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

    private String getElementColor() {
        switch (elementType) {
            case FIRE:
                return "§c"; // Red
            case WATER:
                return "§9"; // Blue
            case EARTH:
                return "§2"; // Dark Green
            case AIR:
                return "§f"; // White
            case LIGHTNING:
                return "§e"; // Yellow
            case ICE:
                return "§b"; // Aqua
            case NATURE:
                return "§a"; // Light Green
            case DARKNESS:
                return "§8"; // Dark Gray
            case LIGHT:
                return "§e"; // Yellow
            default:
                return "§7"; // Gray
        }
    }

    private org.bukkit.Color getElementJavaColor() {
        switch (elementType) {
            case FIRE:
                return org.bukkit.Color.RED;
            case WATER:
                return org.bukkit.Color.BLUE;
            case EARTH:
                return org.bukkit.Color.GREEN;
            case AIR:
                return org.bukkit.Color.WHITE;
            case LIGHTNING:
                return org.bukkit.Color.YELLOW;
            case ICE:
                return org.bukkit.Color.AQUA;
            case NATURE:
                return org.bukkit.Color.LIME;
            case DARKNESS:
                return org.bukkit.Color.BLACK;
            case LIGHT:
                return org.bukkit.Color.YELLOW;
            default:
                return org.bukkit.Color.PURPLE;
        }
    }

    private Particle getElementParticle() {
        switch (elementType) {
            case FIRE:
                return Particle.FLAME;
            case WATER:
                return Particle.DRIPPING_WATER;
            case EARTH:
                return Particle.FALLING_DUST;
            case AIR:
                return Particle.CLOUD;
            case LIGHTNING:
                return Particle.ELECTRIC_SPARK;
            case ICE:
                return Particle.SNOWFLAKE;
            case NATURE:
                return Particle.COMPOSTER;
            case DARKNESS:
                return Particle.SMOKE;
            case LIGHT:
                return Particle.END_ROD;
            default:
                return Particle.WITCH;
        }
    }

    private void startParticleEffects() {
        // Cancel existing task if any
        if (particleTask != null) {
            particleTask.cancel();
        }

        // Create a new task that runs every 20 ticks (1 second)
        particleTask = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!isSpawned || ravenEntity == null || ravenEntity.isDead()) {
                    this.cancel();
                    return;
                }

                Location location = ravenEntity.getLocation().add(0, 0.5, 0);
                Particle particle = getElementParticle();

                // Create orbiting particles
                double radius = 0.5;
                int particles = 2; // Keep particles subtle

                for (int i = 0; i < particles; i++) {
                    double currentAngle = angle + ((Math.PI * 2) / particles) * i;
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;

                    location.getWorld().spawnParticle(
                            particle,
                            location.clone().add(x, 0, z),
                            1, 0.05, 0.05, 0.05, 0.01
                    );
                }

                // Update angle for next iteration
                angle += Math.PI / 8; // 22.5 degrees per second
                if (angle >= Math.PI * 2) {
                    angle = 0;
                }
            }
        }.runTaskTimer(RavenPets.getInstance(), 20, 20);
    }

    public void addExperience(int exp) {
        this.experience += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int nextLevelExp = getRequiredExperienceForNextLevel();
        if (experience >= nextLevelExp) {
            int oldLevel = level;
            RavenTier oldTier = tier;

            level++;
            experience -= nextLevelExp;
            checkTierUp();

            // Update the display if spawned
            if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
                updateRavenAppearance();

                // Level up effect
                Location location = ravenEntity.getLocation();
                location.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, location, 50, 0.5, 0.5, 0.5, 0.3);
                location.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

                // Try to notify the owner
                try {
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null && owner.isOnline()) {
                        owner.sendMessage("§d§lYour raven leveled up to level §f" + level + "§d§l!");

                        // If tier changed, send special message
                        if (oldTier != tier) {
                            owner.sendMessage("§5§l⚡ Your raven advanced to tier §f" + tier.name() + "§5§l! ⚡");
                            owner.playSound(owner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        }
                    }
                } catch (Exception e) {
                    // Ignore errors with owner notification
                }
            }

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
    }

    private void updateRavenAppearance() {
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            ravenEntity.setBlock(getDisplayBlockData());
            updateRavenName();

            // Update glowing effect if tier changed
            ravenEntity.setGlowing(true);
        }
    }

    private void updateRavenName() {
        if (ravenEntity != null) {
            ravenEntity.setCustomName(getFormattedName());
        }
    }

    private String getFormattedName() {
        String tierColor = getTierColor();
        String elementColor = getElementColor();

        // Create a stylized name tag with element and tier indicators
        StringBuilder nameTag = new StringBuilder();

        // Add element indicator prefix
        switch (elementType) {
            case FIRE: nameTag.append("§c🔥 "); break;
            case WATER: nameTag.append("§9💧 "); break;
            case EARTH: nameTag.append("§2🌍 "); break;
            case AIR: nameTag.append("§f💨 "); break;
            case LIGHTNING: nameTag.append("§e⚡ "); break;
            case ICE: nameTag.append("§b❄ "); break;
            case NATURE: nameTag.append("§a🌿 "); break;
            case DARKNESS: nameTag.append("§8🌑 "); break;
            case LIGHT: nameTag.append("§e☀ "); break;
            default: nameTag.append("§d✨ "); break;
        }

        // Add raven name with tier color
        nameTag.append(tierColor).append(name);

        // Add level indicator in a stylized format
        nameTag.append(" §8[").append(elementColor).append("Lvl ").append(level).append(tierColor).append("]");

        return nameTag.toString();
    }

    public int getRequiredExperienceForNextLevel() {
        return 100 + (level * 50);
    }

    // Getters and setters - these need to be PUBLIC for AbilityManager to use them

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateRavenName();
    }

    public RavenTier getTier() {
        return tier;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 1) level = 1;
        if (level > 100) level = 100;

        this.level = level;
        this.tier = RavenTier.getTierByLevel(level);
        updateRavenAppearance();
    }

    public int getExperience() {
        return experience;
    }

    /**
     * Sets the experience value directly
     * Use with caution - does not trigger level-ups
     * @param experience The new experience value
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    public RavenElementType getElementType() {
        return elementType;
    }

    public void setElementType(RavenElementType elementType) {
        this.elementType = elementType;
        updateRavenAppearance();

        // Update particle effects if spawned
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            startParticleEffects();
        }
    }

    public boolean isSpawned() {
        return isSpawned;
    }

    public BlockDisplay getRavenEntity() {
        return ravenEntity;
    }

    public boolean hasCustomColors() {
        return hasCustomColors;
    }

    public void setCustomColors(boolean hasCustomColors) {
        this.hasCustomColors = hasCustomColors;
        updateRavenAppearance();
    }

    public boolean hasCustomParticles() {
        return hasCustomParticles;
    }

    public void setCustomParticles(boolean hasCustomParticles) {
        this.hasCustomParticles = hasCustomParticles;

        // Update particle effects if needed
        if (isSpawned && ravenEntity != null && !ravenEntity.isDead()) {
            startParticleEffects();
        }
    }
}