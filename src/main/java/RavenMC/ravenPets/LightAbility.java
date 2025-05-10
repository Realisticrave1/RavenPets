package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class LightAbility implements RavenAbility {
    private final Random random = new Random();

    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Night vision
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 0));

        // Higher tiers give regeneration
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier - 1));
        }

        // Expert and above give damage resistance
        if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amplifier - 2));
        }

        // Master and above give strength
        if (tier.ordinal() >= RavenTier.MASTER.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amplifier - 3));
        }

        // Add visual and sound effects
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        // Create light aura effect
        new BukkitRunnable() {
            private int count = 0;
            private final int maxCount = 6;

            @Override
            public void run() {
                if (count >= maxCount || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                // Rotating light particles
                Location center = player.getLocation().add(0, 1.0, 0);
                double radius = 0.8;
                int particles = 12;

                for (int i = 0; i < particles; i++) {
                    double angle = Math.PI * 2 * i / particles + (count * Math.PI / 8);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            center.clone().add(x, 0, z),
                            1, 0, 0, 0, 0
                    );
                }

                // Rising particles
                for (int i = 0; i < 3; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * radius;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            center.clone().add(x, 0, z),
                            1, 0, 0.5, 0, 0.05
                    );
                }

                count++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 5L);

        player.sendMessage("§e§lDivine Blessing §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Radiance - healing allies and damaging undead enemies
        int radius = 5 + tier.ordinal();
        double healAmount = 2 + (tier.ordinal() * 2);
        double damageAmount = 3 + (tier.ordinal() * 2);
        int glowingDuration = 20 * (3 + tier.ordinal()); // Duration in ticks

        // Create expanding light wave
        Location center = player.getLocation();

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

                // Create a circle of particles
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;

                    Location particleLoc = center.clone().add(x, 0.1, z);

                    // Light particles
                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            particleLoc,
                            1, 0, 0, 0, 0
                    );

                    // Occasional glow effect
                    if (random.nextDouble() < 0.2) {
                        player.getWorld().spawnParticle(
                                Particle.WITCH,
                                particleLoc.clone().add(0, 0.3, 0),
                                1, 0.1, 0.1, 0.1, 0
                        );
                    }
                }

                // Add some vertical beams for effect
                if (ticks % 4 == 0) {
                    double beamAngle = random.nextDouble() * Math.PI * 2;
                    double beamRadius = random.nextDouble() * currentRadius;
                    double beamX = Math.cos(beamAngle) * beamRadius;
                    double beamZ = Math.sin(beamAngle) * beamRadius;

                    Location beamLoc = center.clone().add(beamX, 0, beamZ);

                    for (double y = 0; y < 3; y += 0.2) {
                        player.getWorld().spawnParticle(
                                Particle.END_ROD,
                                beamLoc.clone().add(0, y, 0),
                                1, 0.02, 0, 0.02, 0
                        );
                    }
                }

                // Increase radius for next tick
                currentRadius += (radius - 0.5) / maxTicks;
                ticks++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 1L);

        // Find all entities in radius
        List<LivingEntity> affectedAllies = new ArrayList<>();
        List<LivingEntity> affectedUndead = new ArrayList<>();

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Check if entity is undead
                boolean isUndead = isUndead(livingEntity);

                // If it's another player or not undead, consider as ally
                if (entity instanceof Player || (!isUndead && !(entity instanceof Player))) {
                    // Check for team tags - only heal allies
                    if (entity instanceof Player) {
                        Player targetPlayer = (Player) entity;
                        // If there's a team tag or it's the same player, add to allies
                        if (player.getScoreboardTags().stream().anyMatch(tag ->
                                tag.startsWith("raventeam:") && targetPlayer.getScoreboardTags().contains(tag)) ||
                                targetPlayer.getName().equals(player.getName())) {
                            affectedAllies.add(livingEntity);
                        }
                    } else {
                        // Non-player entities that aren't undead are considered friendly
                        affectedAllies.add(livingEntity);
                    }
                }
                // If it's undead, it takes damage from light
                else if (isUndead) {
                    affectedUndead.add(livingEntity);
                }
            }
        }

        // Heal allies
        for (LivingEntity ally : affectedAllies) {
            // Calculate current health and max health
            double currentHealth = ally.getHealth();
            double maxHealth = ally.getMaxHealth();

            // Apply healing (don't exceed max health)
            ally.setHealth(Math.min(maxHealth, currentHealth + healAmount));

            // Apply glowing effect to show they were healed
            ally.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING,
                    glowingDuration,
                    0
            ));

            // Visual healing effect
            ally.getWorld().spawnParticle(
                    Particle.HEART,
                    ally.getLocation().add(0, 1.5, 0),
                    3, 0.2, 0.2, 0.2, 0
            );

            // Additional regeneration for higher tiers
            if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
                int regenDuration = glowingDuration / 2;
                int regenAmplifier = Math.min(1, tier.ordinal() - 2);

                ally.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION,
                        regenDuration,
                        regenAmplifier
                ));
            }
        }

        // Damage undead enemies
        for (LivingEntity undead : affectedUndead) {
            // Apply damage
            undead.damage(damageAmount, player);

            // Apply glowing effect for undead too
            undead.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING,
                    glowingDuration,
                    0
            ));

            // Visual burning/damage effect
            undead.getWorld().spawnParticle(
                    Particle.SMOKE,
                    undead.getLocation().add(0, 1, 0),
                    15, 0.3, 0.5, 0.3, 0.05
            );

            // Additional fire effect for higher tiers
            if (tier.ordinal() >= RavenTier.MASTER.ordinal()) {
                int fireTicks = 20 * (tier.ordinal() - 2); // Fire duration in ticks
                undead.setFireTicks(undead.getFireTicks() + fireTicks);
            }
        }

        // Sound effects
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);

        player.sendMessage("§e§lRadiance §r§7unleashed!");
    }

    private boolean isUndead(LivingEntity entity) {
        EntityType type = entity.getType();

        // List of undead mobs in Minecraft
        return type == EntityType.ZOMBIE ||
                type == EntityType.ZOMBIE_VILLAGER ||
                type == EntityType.ZOMBIFIED_PIGLIN ||
                type == EntityType.SKELETON ||
                type == EntityType.WITHER_SKELETON ||
                type == EntityType.STRAY ||
                type == EntityType.PHANTOM ||
                type == EntityType.DROWNED ||
                type == EntityType.HUSK ||
                type == EntityType.WITHER ||
                type == EntityType.SKELETON_HORSE ||
                type == EntityType.ZOMBIE_HORSE ||
                type == EntityType.ZOGLIN;
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
        return "Divine Blessing";
    }

    @Override
    public String getDescription() {
        return "Grants protection, night vision, and healing abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Radiance";
    }

    @Override
    public String getSecondaryDescription() {
        return "Heals allies and damages undead enemies in a radius";
    }
}