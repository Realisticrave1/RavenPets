package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EarthAbility implements RavenAbility {
    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amplifier));

        // Higher tiers give slow falling
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0));
        }

        // Master and above get haste for faster mining
        if (tier.ordinal() >= RavenTier.MASTER.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, amplifier));
        }

        // Add visual and sound effects for 1.21 enhancement
        player.getWorld().spawnParticle(Particle.FALLING_DUST, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);

        player.sendMessage("§2§lStone Skin §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Earth Shatter - create a shockwave that damages and knocks back nearby entities
        int radius = 3 + tier.ordinal();
        int damage = 1 + tier.ordinal();
        Random random = new Random();

        // Visual effects - ground cracks and dust
        Location center = player.getLocation();

        // Create falling block particles
        List<Material> earthBlocks = new ArrayList<>();
        earthBlocks.add(Material.DIRT);
        earthBlocks.add(Material.STONE);
        earthBlocks.add(Material.COBBLESTONE);
        earthBlocks.add(Material.GRAVEL);

        for (int i = 0; i < 5 + (tier.ordinal() * 2); i++) {
            Material blockType = earthBlocks.get(random.nextInt(earthBlocks.size()));

            // Create a small throw effect around the player
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * 2;
            Location spawnLoc = center.clone().add(
                    Math.cos(angle) * distance,
                    0.1,
                    Math.sin(angle) * distance
            );

            try {
                // Create a falling block with low gravity
                FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(
                        spawnLoc,
                        blockType.createBlockData()
                );

                // Set properties
                fallingBlock.setGravity(true);
                fallingBlock.setDropItem(false);
                fallingBlock.setHurtEntities(false);

                // Apply velocity
                Vector velocity = new Vector(
                        (random.nextDouble() - 0.5) * 0.3,
                        0.2 + random.nextDouble() * 0.3,
                        (random.nextDouble() - 0.5) * 0.3
                );
                fallingBlock.setVelocity(velocity);

                // Make blocks disappear after a short time
                RavenPets.getInstance().getServer().getScheduler().runTaskLater(
                        RavenPets.getInstance(),
                        fallingBlock::remove,
                        20 + random.nextInt(20)
                );
            } catch (IllegalArgumentException e) {
                // Some materials may not work as falling blocks, just skip them
            }
        }

        // Spawn particles in a circular pattern
        for (double r = 0.5; r <= radius; r += 0.5) {
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / (8 + r)) {
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                Location particleLoc = center.clone().add(x, 0.1, z);

                player.getWorld().spawnParticle(
                        Particle.BLOCK,
                        particleLoc,
                        3, 0.2, 0, 0.2, 0,
                        Material.DIRT.createBlockData()
                );
            }
        }

        // Apply effects to nearby entities
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Skip entities with the same team tag (for protection)
                if (player.getScoreboardTags().stream().anyMatch(tag ->
                        tag.startsWith("raventeam:") && entity.getScoreboardTags().contains(tag))) {
                    continue;
                }

                // Calculate distance for damage falloff
                double distance = entity.getLocation().distance(center);
                if (distance <= radius) {
                    // Closer enemies take more damage
                    double damageMultiplier = 1.0 - (distance / radius);

                    // Apply damage
                    livingEntity.damage(damage * damageMultiplier, player);

                    // Apply slowness
                    int slowDuration = 20 * (3 + tier.ordinal());
                    int slowAmplifier = tier.ordinal() > 1 ? 1 : 0;
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS,
                            slowDuration,
                            slowAmplifier
                    ));

                    // Calculate knockback - entities are pushed away from center
                    Vector knockback = entity.getLocation().toVector()
                            .subtract(center.toVector())
                            .normalize()
                            .multiply(1.0 + (tier.ordinal() * 0.3));
                    entity.setVelocity(entity.getVelocity().add(knockback));
                }
            }
        }

        // Sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);

        player.sendMessage("§2§lEarth Shatter §r§7unleashed!");
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 45; // 45 seconds
            case ADEPT -> 20 * 90; // 1.5 minutes
            case EXPERT -> 20 * 180; // 3 minutes
            case MASTER -> 20 * 360; // 6 minutes
            case LEGENDARY -> 20 * 720; // 12 minutes
            default -> 20 * 45;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Stone Skin";
    }

    @Override
    public String getDescription() {
        return "Grants damage resistance and earth-based abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Earth Shatter";
    }

    @Override
    public String getSecondaryDescription() {
        return "Creates a shockwave that damages and slows nearby enemies";
    }
}