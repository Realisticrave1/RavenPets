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
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NatureAbility implements RavenAbility {
    private final Random random = new Random();
    private final Set<Location> temporaryVegetation = new HashSet<>();

    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Regeneration
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier));

        // Higher tiers give additional benefits
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            // Saturation to prevent hunger
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,
                    20 * 10, // 10 seconds of saturation is enough
                    amplifier));
        }

        if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
            // Poison resistance for expert+ tiers
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,
                    duration, 0));
        }

        // Legendary tier lets plants grow beneath your feet temporarily
        if (tier == RavenTier.LEGENDARY) {
            startNatureBloom(player, duration / 20, tier); // Convert ticks to seconds and pass the tier
        }

        // Add visual and sound effects
        player.getWorld().spawnParticle(Particle.COMPOSTER, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);

        // Create growing vines effect
        new BukkitRunnable() {
            private int count = 0;
            private final int maxCount = 5;

            @Override
            public void run() {
                if (count >= maxCount || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                Location center = player.getLocation();

                // Rising leaf particles in a spiral
                for (double y = 0; y < 2.5; y += 0.25) {
                    double radius = 0.8 - (y * 0.2);
                    double angle = y * Math.PI * 2 + (count * Math.PI / 2);

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, y, z);

                    // Alternate between different nature particles
                    if (count % 2 == 0) {
                        player.getWorld().spawnParticle(
                                Particle.COMPOSTER,
                                particleLoc,
                                1, 0.05, 0.05, 0.05, 0
                        );
                    } else {
                        player.getWorld().spawnParticle(
                                Particle.HAPPY_VILLAGER,
                                particleLoc,
                                1, 0.05, 0.05, 0.05, 0
                        );
                    }
                }

                count++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 5L);

        player.sendMessage("§a§lNature's Blessing §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Entangling Roots - root and damage nearby enemies with vines
        int radius = 3 + tier.ordinal();
        int rootDuration = 20 * (1 + tier.ordinal()); // Duration in ticks
        double damage = 1.0 + (tier.ordinal() * 0.5);

        // Visual effects - growing vines
        Location center = player.getLocation();

        // Create expanding vine wave
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

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;

                    Location particleLoc = center.clone().add(x, 0.1, z);

                    // Different particles for a vine/roots effect
                    if (random.nextBoolean()) {
                        player.getWorld().spawnParticle(
                                Particle.COMPOSTER,
                                particleLoc,
                                1, 0.1, 0.1, 0.1, 0
                        );
                    } else {
                        player.getWorld().spawnParticle(
                                Particle.HAPPY_VILLAGER,
                                particleLoc,
                                1, 0.1, 0.1, 0.1, 0
                        );
                    }

                    // Add some vertical particles to look like growing vines
                    if (random.nextDouble() < 0.2) {
                        for (double y = 0; y < 1.5; y += 0.3) {
                            player.getWorld().spawnParticle(
                                    Particle.COMPOSTER,
                                    particleLoc.clone().add(0, y, 0),
                                    1, 0.05, 0.05, 0.05, 0
                            );
                        }
                    }
                }

                // Increase radius for next tick
                currentRadius += (radius - 0.5) / maxTicks;
                ticks++;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 1L);

        // Apply effects to nearby entities
        List<Entity> affected = new ArrayList<>();

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
                    // Apply root effect (essentially no movement)
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS,
                            rootDuration,
                            4 // Very high slowness to simulate roots
                    ));

                    // Apply jump prevention
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.JUMP_BOOST,
                            rootDuration,
                            128 // Negative jump boost to prevent jumping
                    ));

                    // Apply poison if tier is high enough
                    if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
                        livingEntity.addPotionEffect(new PotionEffect(
                                PotionEffectType.POISON,
                                rootDuration / 2, // Half the duration of the root
                                tier.ordinal() - 1
                        ));
                    }

                    // Initial damage
                    livingEntity.damage(damage, player);

                    // Periodic damage for higher tiers
                    if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
                        affected.add(entity);
                    }

                    // Visual effect of vines on the entity
                    new BukkitRunnable() {
                        private int count = 0;
                        private final int maxCount = 5;

                        @Override
                        public void run() {
                            if (count >= maxCount || entity.isDead() || !player.isOnline()) {
                                this.cancel();
                                return;
                            }

                            // Create vine particles around the entity
                            Location entityLoc = entity.getLocation().add(0, 0.5, 0);
                            for (int i = 0; i < 3; i++) {
                                double offsetX = (random.nextDouble() - 0.5) * 0.8;
                                double offsetY = random.nextDouble() * 1.5;
                                double offsetZ = (random.nextDouble() - 0.5) * 0.8;

                                entityLoc.getWorld().spawnParticle(
                                        Particle.COMPOSTER,
                                        entityLoc.clone().add(offsetX, offsetY, offsetZ),
                                        1, 0.05, 0.05, 0.05, 0
                                );
                            }

                            count++;
                        }
                    }.runTaskTimer(RavenPets.getInstance(), 0L, 5L);
                }
            }
        }

        // Apply periodic damage to rooted entities for higher tiers
        if (!affected.isEmpty() && tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
            new BukkitRunnable() {
                private int ticks = 0;
                private final int maxTicks = rootDuration / 20; // Convert to seconds

                @Override
                public void run() {
                    if (ticks >= maxTicks || !player.isOnline()) {
                        this.cancel();
                        return;
                    }

                    for (Entity entity : affected) {
                        if (entity.isValid() && !entity.isDead() && entity instanceof LivingEntity) {
                            // Apply small damage over time
                            ((LivingEntity) entity).damage(damage * 0.2, player);

                            // Vine constriction particles
                            entity.getWorld().spawnParticle(
                                    Particle.COMPOSTER,
                                    entity.getLocation().add(0, 1, 0),
                                    8, 0.3, 0.5, 0.3, 0.05
                            );
                        }
                    }

                    ticks++;
                }
            }.runTaskTimer(RavenPets.getInstance(), 20L, 20L); // Every second
        }

        // Sound effects
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BAMBOO_BREAK, 1.0f, 0.8f);

        player.sendMessage("§a§lEntangling Roots §r§7unleashed!");
    }

    private void startNatureBloom(Player player, int durationSeconds, RavenTier playerTier) {
        // Store the tier value as a final variable that can be accessed in the BukkitRunnable
        final RavenTier tier = playerTier;

        new BukkitRunnable() {
            private int timeRemaining = durationSeconds;

            @Override
            public void run() {
                if (timeRemaining <= 0 || !player.isOnline()) {
                    cleanupTemporaryVegetation();
                    this.cancel();
                    return;
                }

                // Check for dirt or grass below player
                Block block = player.getLocation().clone().subtract(0, 1, 0).getBlock();
                Block aboveBlock = block.getRelative(BlockFace.UP);

                if ((block.getType() == Material.DIRT ||
                        block.getType() == Material.GRASS_BLOCK ||
                        block.getType() == Material.PODZOL) &&
                        aboveBlock.getType() == Material.AIR) {

                    // Choose a random plant type
                    Material plantType;
                    double rand = random.nextDouble();

                    if (rand < 0.3) {
                        plantType = Material.GRASS_BLOCK;
                    } else if (rand < 0.5) {
                        plantType = Material.FERN;
                    } else if (rand < 0.7) {
                        plantType = Material.DANDELION;
                    } else if (rand < 0.9) {
                        plantType = Material.POPPY;
                    } else {
                        // Very rarely, a taller plant
                        if (tier.ordinal() >= RavenTier.MASTER.ordinal()) {
                            plantType = Material.SUNFLOWER;
                        } else {
                            plantType = Material.OXEYE_DAISY;
                        }
                    }

                    // Place the temporary plant
                    aboveBlock.setType(plantType);
                    temporaryVegetation.add(aboveBlock.getLocation());

                    // Schedule removal after player moves away
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (aboveBlock.getType() == plantType &&
                                    player.getLocation().distance(aboveBlock.getLocation()) > 3) {
                                aboveBlock.setType(Material.AIR);
                                temporaryVegetation.remove(aboveBlock.getLocation());
                            }
                        }
                    }.runTaskLater(RavenPets.getInstance(), 200); // Remove after 10 seconds

                    // Add growth particles
                    player.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            aboveBlock.getLocation().add(0.5, 0.2, 0.5),
                            5, 0.2, 0.1, 0.2, 0
                    );
                }

                timeRemaining--;
            }
        }.runTaskTimer(RavenPets.getInstance(), 0L, 10L); // Check every half-second
    }

    private void cleanupTemporaryVegetation() {
        for (Location location : temporaryVegetation) {
            Block block = location.getBlock();
            // Check if it's still one of our plants
            if (block.getType() == Material.GRASS_BLOCK ||
                    block.getType() == Material.FERN ||
                    block.getType() == Material.DANDELION ||
                    block.getType() == Material.POPPY ||
                    block.getType() == Material.OXEYE_DAISY ||
                    block.getType() == Material.SUNFLOWER) {

                block.setType(Material.AIR);
            }
        }
        temporaryVegetation.clear();
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 30; // 30 seconds
            case ADEPT -> 20 * 60; // 1 minute
            case EXPERT -> 20 * 120; // 2 minutes
            case MASTER -> 20 * 240; // 4 minutes
            case LEGENDARY -> 20 * 400; // 6.5 minutes
            default -> 20 * 30;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return Math.max(0, tier.ordinal() - 1); // Start at 0 for balance
    }

    @Override
    public String getName() {
        return "Nature's Blessing";
    }

    @Override
    public String getDescription() {
        return "Grants regeneration and nature-based protective abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Entangling Roots";
    }

    @Override
    public String getSecondaryDescription() {
        return "Summons vines that root enemies in place and damage them over time";
    }
}