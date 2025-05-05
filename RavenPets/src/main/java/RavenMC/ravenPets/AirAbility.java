package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class AirAbility implements RavenAbility {
    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Speed boost
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));

        // Higher tiers give jump boost
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, amplifier));
        }

        // Expert and above get slow falling
        if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0));
        }

        // Legendary tier can give temporary flight
        if (tier == RavenTier.LEGENDARY) {
            // In 1.21, we should check if player already has flight ability
            boolean hadFlight = player.getAllowFlight();

            player.setAllowFlight(true);
            player.setFlying(true);

            // Schedule task to disable flight after duration
            RavenPets.getInstance().getServer().getScheduler().runTaskLater(
                    RavenPets.getInstance(),
                    () -> {
                        if (player.isOnline()) {
                            player.setFlying(false);
                            // Only disable flight if player didn't have it before
                            if (!hadFlight) {
                                player.setAllowFlight(false);
                            }
                            player.sendMessage("§7Your temporary flight has expired.");
                        }
                    },
                    duration
            );
        }

        // Add visual and sound effects for 1.21 enhancement
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);

        player.sendMessage("§f§lWind Rush §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Cyclone ability - creates a whirlwind that lifts entities
        int radius = 4;
        int height = 1 + tier.ordinal();

        // Visual effects - swirling wind particles
        Location center = player.getLocation();
        for (double y = 0; y < height * 2; y += 0.3) {
            double radiusAtHeight = radius * (1 - (y / (height * 2.5)));
            double angle = y * Math.PI;

            for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                double x = Math.cos(i + angle) * radiusAtHeight;
                double z = Math.sin(i + angle) * radiusAtHeight;

                Location particleLoc = center.clone().add(x, y, z);
                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        particleLoc,
                        1, 0.1, 0.1, 0.1, 0
                );

                if (Math.random() < 0.3) {
                    player.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            particleLoc,
                            1, 0.1, 0.1, 0.1, 0.05
                    );
                }
            }
        }

        // Lift nearby entities
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                // Skip entities with the same team tag (for protection)
                if (player.getScoreboardTags().stream().anyMatch(tag ->
                        tag.startsWith("raventeam:") && entity.getScoreboardTags().contains(tag))) {
                    continue;
                }

                // Calculate distance from center
                double distance = entity.getLocation().distance(center);
                if (distance < radius) {
                    // Closer entities get lifted higher
                    double liftForce = 0.5 + ((radius - distance) / radius) * (0.5 * tier.ordinal());
                    Vector velocity = entity.getVelocity();

                    // Add upward force
                    velocity.setY(Math.max(0.4, velocity.getY()) + liftForce);

                    // Add some spinning force
                    Vector toEntityVec = entity.getLocation().toVector().subtract(center.toVector());
                    Vector spinForce = new Vector(-toEntityVec.getZ(), 0, toEntityVec.getX()).normalize().multiply(0.3);
                    velocity.add(spinForce);

                    entity.setVelocity(velocity);

                    // Apply slow falling to prevent fall damage
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(
                                new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * (5 + tier.ordinal()), 0)
                        );
                    }
                }
            }
        }

        // Sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);

        player.sendMessage("§f§lCyclone §r§7unleashed!");
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 30; // 30 seconds
            case ADEPT -> 20 * 60; // 1 minute
            case EXPERT -> 20 * 120; // 2 minutes
            case MASTER -> 20 * 240; // 4 minutes
            case LEGENDARY -> 20 * 60; // Only 1 minute for flight at legendary
            default -> 20 * 30;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Wind Rush";
    }

    @Override
    public String getDescription() {
        return "Grants increased speed, jumping, and air-based abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Cyclone";
    }

    @Override
    public String getSecondaryDescription() {
        return "Creates a whirlwind that lifts enemies into the air";
    }
}