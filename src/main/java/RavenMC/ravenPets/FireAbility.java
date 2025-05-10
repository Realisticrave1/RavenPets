package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Fireball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

public class FireAbility implements RavenAbility {
    @Override
    public void execute(Player player, RavenTier tier) {
        // Give fire resistance based on tier
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, amplifier));

        // Higher tiers also give strength
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, tier.ordinal() - 1));
        }

        // Legendary tier gives ability to shoot fireballs
        if (tier == RavenTier.LEGENDARY) {
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setYield(2.0f); // Explosion power
            fireball.setIsIncendiary(true);

            // Add custom velocity for better control
            fireball.setVelocity(player.getLocation().getDirection().multiply(1.5));
        }

        // Add visual and sound effects
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

        player.sendMessage("§c§lFire Aura §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Fire wave that damages nearby enemies and sets them on fire
        int radius = 2 + tier.ordinal();
        int damage = 1 + tier.ordinal();
        int fireTicks = 20 * (tier.ordinal() + 2); // Fire duration in ticks - increases with tier

        // Create expanding ring of fire particles
        new BukkitRunnable() {
            private double radius = 0.5;
            private int count = 0;
            private final int maxCount = 15;
            private final double maxRadius = 2 + tier.ordinal();

            @Override
            public void run() {
                if (count >= maxCount) {
                    this.cancel();
                    return;
                }

                // Create a ring of fire particles
                Location center = player.getLocation();
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = center.clone().add(x, 0.1, z);

                    player.getWorld().spawnParticle(
                            Particle.FLAME,
                            particleLoc,
                            3, 0.1, 0.1, 0.1, 0.01
                    );

                    if (count % 3 == 0) {
                        player.getWorld().spawnParticle(
                                Particle.LAVA,
                                particleLoc,
                                1, 0.1, 0.1, 0.1, 0
                        );
                    }
                }

                radius += (maxRadius - 0.5) / maxCount;
                count++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 1L);

        // Apply damage to nearby entities
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Skip entities with the same team tag (for protection)
                if (player.getScoreboardTags().stream().anyMatch(tag ->
                        tag.startsWith("raventeam:") && entity.getScoreboardTags().contains(tag))) {
                    continue;
                }

                // Calculate damage reduction based on distance
                double distance = entity.getLocation().distance(player.getLocation());
                double damageMultiplier = 1.0 - (distance / radius);

                // Apply damage
                livingEntity.damage(damage * damageMultiplier, player);
                livingEntity.setFireTicks(fireTicks);

                // Knockback effect
                Vector knockback = entity.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize()
                        .multiply(0.5 + tier.ordinal() * 0.3);
                entity.setVelocity(entity.getVelocity().add(knockback));
            }
        }

        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);

        player.sendMessage("§c§lFire Wave §r§7unleashed!");
    }

    // Changed to public to properly override the interface method
    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 30; // 30 seconds
            case ADEPT -> 20 * 60; // 1 minute
            case EXPERT -> 20 * 120; // 2 minutes
            case MASTER -> 20 * 300; // 5 minutes
            case LEGENDARY -> 20 * 600; // 10 minutes
            default -> 20 * 30;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Fire Aura";
    }

    @Override
    public String getDescription() {
        return "Grants fire resistance and increased damage";
    }

    @Override
    public String getSecondaryName() {
        return "Fire Wave";
    }

    @Override
    public String getSecondaryDescription() {
        return "Unleash a wave of fire that damages nearby enemies";
    }
}