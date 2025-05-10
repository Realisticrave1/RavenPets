package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class IceAbility implements RavenAbility {
    private final Set<Location> temporaryIce = new HashSet<>();
    private final Random random = new Random();

    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Resistance to cold
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, amplifier));

        // Higher tiers give slow falling (ice platforms under feet)
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0));

            // Legendary tier allows walking on water by freezing it temporarily
            if (tier == RavenTier.LEGENDARY) {
                startIceWalker(player, duration / 20); // Convert ticks to seconds
            }
        }

        // Add frost armor effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration,
                Math.min(2, amplifier))); // Cap at resistance 2 for balance

        // Add visual and sound effects
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);

        // Swirling frost particles
        new BukkitRunnable() {
            private int count = 0;
            private final int maxCount = 5;

            @Override
            public void run() {
                if (count >= maxCount) {
                    this.cancel();
                    return;
                }

                double radius = 1.0;
                Location center = player.getLocation().add(0, 1, 0);

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, 0, z);
                    player.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            particleLoc,
                            3, 0.1, 0.1, 0.1, 0.05
                    );
                }

                count++;
                radius += 0.2;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 5L);

        player.sendMessage("§b§lFrost Armor §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Ice storm - freezes and slows nearby entities
        int radius = 3 + tier.ordinal();
        int slownessDuration = 20 * (3 + tier.ordinal()); // Duration in ticks
        int slownessAmplifier = Math.min(3, tier.ordinal());
        double damage = 1 + (tier.ordinal() * 0.5);

        // Visual effects - ice storm
        Location center = player.getLocation();

        // Create expanding frost wave
        new BukkitRunnable() {
            private double currentRadius = 0.5;
            private int ticks = 0;
            private final int maxTicks = 20;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    this.cancel();
                    return;
                }

                double angle = 0;
                double angleIncrement = Math.PI / 16;

                while (angle < Math.PI * 2) {
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;

                    Location particleLoc = center.clone().add(x, 0.1, z);
                    player.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            particleLoc,
                            3, 0.1, 0.1, 0.1, 0.02
                    );

                    // Add some random ice blocks flying out
                    if (random.nextDouble() < 0.1) {
                        spawnFallingIceBlock(particleLoc, tier);
                    }

                    angle += angleIncrement;
                }

                // Increase radius for next tick
                currentRadius += (radius - 0.5) / maxTicks;
                ticks++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 1L);

        // Apply effects to nearby entities
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Skip entities with the same team tag (for protection)
                if (player.getScoreboardTags().stream().anyMatch(tag ->
                        tag.startsWith("raventeam:") && entity.getScoreboardTags().contains(tag))) {
                    continue;
                }

                // Calculate distance for effect falloff
                double distance = entity.getLocation().distance(center);
                if (distance <= radius) {
                    // Closer enemies are affected more strongly
                    double distanceRatio = 1.0 - (distance / radius);

                    // Apply damage
                    livingEntity.damage(damage * distanceRatio, player);

                    // Apply slowness - stronger effect closer to center
                    int adjustedAmplifier = (int) Math.round(slownessAmplifier * distanceRatio);
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS,
                            slownessDuration,
                            Math.max(0, adjustedAmplifier)
                    ));

                    // Apply mining fatigue to make enemies weaker
                    if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
                        livingEntity.addPotionEffect(new PotionEffect(
                                PotionEffectType.WEAKNESS,
                                slownessDuration / 2,
                                Math.max(0, tier.ordinal() - 2)
                        ));
                    }

                    // Visual effect on the entity
                    entity.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            entity.getLocation().add(0, 1, 0),
                            20, 0.2, 0.5, 0.2, 0.05
                    );
                }
            }
        }

        // Sound effects
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SNOW_FALL, 1.0f, 1.2f);

        player.sendMessage("§b§lIce Storm §r§7unleashed!");
    }

    private void spawnFallingIceBlock(Location location, RavenTier tier) {
        Material material;

        // Randomize ice block type based on tier
        if (tier.ordinal() >= RavenTier.MASTER.ordinal() && random.nextDouble() < 0.3) {
            material = Material.BLUE_ICE;
        } else if (tier.ordinal() >= RavenTier.EXPERT.ordinal() && random.nextDouble() < 0.5) {
            material = Material.PACKED_ICE;
        } else {
            material = Material.ICE;
        }

        try {
            FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(
                    location.clone().add(0, 1, 0),
                    material.createBlockData()
            );

            // Small upward velocity with random horizontal direction
            double vx = (random.nextDouble() - 0.5) * 0.2;
            double vy = 0.1 + (random.nextDouble() * 0.2);
            double vz = (random.nextDouble() - 0.5) * 0.2;

            fallingBlock.setVelocity(new Vector(vx, vy, vz));
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(false);

            // Remove the block after a short time
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fallingBlock.isDead()) {
                        fallingBlock.remove();
                        location.getWorld().spawnParticle(
                                Particle.SNOWFLAKE,
                                fallingBlock.getLocation(),
                                10, 0.2, 0.2, 0.2, 0.05
                        );
                    }
                }
            }.runTaskLater(RavenPets.getInstance(), 20);
        } catch (Exception e) {
            // Safely handle any exceptions during spawn
        }
    }

    private void startIceWalker(Player player, int durationSeconds) {
        new BukkitRunnable() {
            private int timeRemaining = durationSeconds;

            @Override
            public void run() {
                if (timeRemaining <= 0 || !player.isOnline()) {
                    cleanupTemporaryIce();
                    this.cancel();
                    return;
                }

                Block block = player.getLocation().clone().subtract(0, 1, 0).getBlock();

                // Check if block below is water and freeze it temporarily
                if (block.getType() == Material.WATER) {
                    block.setType(Material.FROSTED_ICE);
                    temporaryIce.add(block.getLocation());

                    // Schedule the ice to melt back to water
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (block.getType() == Material.FROSTED_ICE) {
                                block.setType(Material.WATER);
                                temporaryIce.remove(block.getLocation());
                            }
                        }
                    }.runTaskLater(RavenPets.getInstance(), 60); // Melt after 3 seconds
                }

                // Add frost step particles
                player.getWorld().spawnParticle(
                        Particle.SNOWFLAKE,
                        player.getLocation(),
                        5, 0.1, 0, 0.1, 0
                );

                timeRemaining--;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 20L); // Check every second
    }

    private void cleanupTemporaryIce() {
        for (Location location : temporaryIce) {
            Block block = location.getBlock();
            if (block.getType() == Material.FROSTED_ICE) {
                block.setType(Material.WATER);
            }
        }
        temporaryIce.clear();
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 45; // 45 seconds
            case ADEPT -> 20 * 90; // 1.5 minutes
            case EXPERT -> 20 * 180; // 3 minutes
            case MASTER -> 20 * 300; // 5 minutes
            case LEGENDARY -> 20 * 600; // 10 minutes
            default -> 20 * 45;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Frost Armor";
    }

    @Override
    public String getDescription() {
        return "Grants protection from damage and fire with ice-based abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Ice Storm";
    }

    @Override
    public String getSecondaryDescription() {
        return "Creates a freezing storm that damages and slows nearby entities";
    }
}