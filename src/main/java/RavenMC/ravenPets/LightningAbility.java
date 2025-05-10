package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

public class LightningAbility implements RavenAbility {
    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Speed boost
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));

        // Higher tiers also give strength
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amplifier - 1));
        }

        // Master tier and above gives resistance to prevent self-damage
        if (tier.ordinal() >= RavenTier.MASTER.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amplifier - 2));
        }

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.7f, 1.2f);

        // Create electric aura effect
        new BukkitRunnable() {
            private int count = 0;
            private final int maxCount = 5;

            @Override
            public void run() {
                if (count >= maxCount) {
                    this.cancel();
                    return;
                }

                // Electric particles in spiral
                Location center = player.getLocation().add(0, 1, 0);
                for (double y = 0; y < 2; y += 0.2) {
                    double radius = 0.8 - (y * 0.3);
                    double angle = y * Math.PI * 4 + (count * Math.PI / 2);

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, y, z);
                    player.getWorld().spawnParticle(
                            Particle.ELECTRIC_SPARK,
                            particleLoc,
                            2, 0.05, 0.05, 0.05, 0
                    );
                }

                count++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 4L);

        player.sendMessage("§e§lLightning Speed §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Chain lightning that jumps between nearby entities
        int maxTargets = 2 + tier.ordinal();
        int damage = 2 + tier.ordinal();
        double range = 5.0 + (tier.ordinal() * 1.5);
        double jumpRange = 3.0 + (tier.ordinal() * 0.5);

        // Find initial targets
        Entity[] nearbyEntities = player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> e != player)
                .toArray(Entity[]::new);

        if (nearbyEntities.length == 0) {
            player.sendMessage("§e§lChain Lightning §r§7- No targets in range!");
            return;
        }

        // Sound effect at player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.0f);

        // First lightning strike - from player to closest entity
        Entity firstTarget = getNearestEntity(player.getLocation(), nearbyEntities);

        if (firstTarget != null) {
            // Create lightning effect
            drawLightningEffect(player.getLocation().add(0, 1, 0), firstTarget.getLocation().add(0, 1, 0));

            // Deal damage
            if (firstTarget instanceof LivingEntity) {
                // Skip entities with the same team tag (for protection)
                if (!player.getScoreboardTags().stream().anyMatch(tag ->
                        tag.startsWith("raventeam:") && firstTarget.getScoreboardTags().contains(tag))) {
                    ((LivingEntity) firstTarget).damage(damage, player);
                }
            }

            // Chain to subsequent targets
            Entity currentTarget = firstTarget;
            Entity[] remainingTargets = removeEntityFromArray(nearbyEntities, currentTarget);
            int targetCount = 1;

            while (targetCount < maxTargets && remainingTargets.length > 0) {
                // Find next closest target to the current target
                Entity nextTarget = getNearestEntityWithinRange(currentTarget.getLocation(), remainingTargets, jumpRange);

                if (nextTarget == null) {
                    break;
                }

                // Create lightning effect between targets
                drawLightningEffect(currentTarget.getLocation().add(0, 1, 0), nextTarget.getLocation().add(0, 1, 0));

                // Deal damage (reduced with each jump)
                if (nextTarget instanceof LivingEntity) {
                    // Skip entities with the same team tag
                    if (!player.getScoreboardTags().stream().anyMatch(tag ->
                            tag.startsWith("raventeam:") && nextTarget.getScoreboardTags().contains(tag))) {
                        double reducedDamage = damage * (0.8 - (0.1 * targetCount)); // Damage reduces with each jump
                        ((LivingEntity) nextTarget).damage(Math.max(1, reducedDamage), player);
                    }
                }

                // Update for next iteration
                currentTarget = nextTarget;
                remainingTargets = removeEntityFromArray(remainingTargets, currentTarget);
                targetCount++;
            }
        }

        player.sendMessage("§e§lChain Lightning §r§7unleashed!");
    }

    private void drawLightningEffect(Location start, Location end) {
        // Calculate distance
        double distance = start.distance(end);
        int segments = Math.max(3, (int)(distance * 2));  // More segments for longer distances
        Vector direction = end.clone().subtract(start).toVector().normalize();

        // Calculate segment length
        double segmentLength = distance / segments;

        // Create lightning bolt with random offsets
        Location current = start.clone();
        for (int i = 0; i < segments; i++) {
            // Get next point with some randomness
            Vector randomOffset;
            // First and last segments stay closer to the line
            if (i == 0 || i == segments - 1) {
                randomOffset = new Vector(
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2
                );
            } else {
                randomOffset = new Vector(
                        (Math.random() - 0.5) * 0.5,
                        (Math.random() - 0.5) * 0.5,
                        (Math.random() - 0.5) * 0.5
                );
            }

            // Adjust offset based on distance from start/end
            double distanceRatio = (double) i / segments;
            // Larger offsets in the middle of the bolt
            double offsetMultiplier = 4 * distanceRatio * (1 - distanceRatio);
            randomOffset.multiply(offsetMultiplier);

            // Calculate next point
            Vector nextVector = direction.clone().multiply(segmentLength);
            Location next = current.clone().add(nextVector).add(randomOffset);

            // Draw particles between current and next
            double particleDistance = current.distance(next);
            Vector particleDirection = next.clone().subtract(current).toVector().normalize();
            double particleStep = 0.2; // Distance between particles

            for (double d = 0; d < particleDistance; d += particleStep) {
                Location particleLoc = current.clone().add(particleDirection.clone().multiply(d));
                current.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
                // Occasional larger sparks
                if (Math.random() < 0.2) {
                    current.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.02, 0.02, 0.02, 0.01);
                }
            }

            // Move to next segment
            current = next;
        }

        // Final effect at the target
        end.getWorld().spawnParticle(Particle.FLASH, end, 1, 0, 0, 0, 0);
        end.getWorld().playSound(end, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.4f, 1.2f);
    }

    private Entity getNearestEntity(Location location, Entity[] entities) {
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            double distance = entity.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private Entity getNearestEntityWithinRange(Location location, Entity[] entities, double range) {
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            double distance = entity.getLocation().distance(location);
            if (distance < range && distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private Entity[] removeEntityFromArray(Entity[] entities, Entity entityToRemove) {
        return java.util.Arrays.stream(entities)
                .filter(e -> e != entityToRemove)
                .toArray(Entity[]::new);
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 20; // 20 seconds
            case ADEPT -> 20 * 40; // 40 seconds
            case EXPERT -> 20 * 60; // 1 minute
            case MASTER -> 20 * 120; // 2 minutes
            case LEGENDARY -> 20 * 240; // 4 minutes
            default -> 20 * 20;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Lightning Speed";
    }

    @Override
    public String getDescription() {
        return "Grants increased speed and strength with electric aura";
    }

    @Override
    public String getSecondaryName() {
        return "Chain Lightning";
    }

    @Override
    public String getSecondaryDescription() {
        return "Strike nearby entities with lightning that jumps between targets";
    }
}