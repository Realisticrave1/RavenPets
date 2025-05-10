package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.GameMode;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class DarknessAbility implements RavenAbility {
    private final Random random = new Random();
    private final Map<UUID, Boolean> shadowFormActive = new HashMap<>();

    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Night vision
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 0));

        // Higher tiers give invisibility
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0));
        }

        // Expert and above give unique shadow form ability
        if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
            startShadowForm(player, duration);
        }

        // Add visual and sound effects
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);

        player.sendMessage("§8§lShadow Cloak §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Fear - applies weakness and blindness to nearby entities
        int radius = 4 + tier.ordinal();
        int blindnessDuration = 20 * (2 + tier.ordinal()); // Duration in ticks
        int weaknessDuration = 20 * (3 + tier.ordinal()); // Duration in ticks

        // Visual effects - expanding shadow wave
        Location center = player.getLocation();

        new BukkitRunnable() {
            private double currentRadius = 0.5;
            private int ticks = 0;
            private final int maxTicks = 15;

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

                    // Dark smoke particles
                    player.getWorld().spawnParticle(
                            Particle.SMOKE,
                            particleLoc,
                            1, 0.1, 0.1, 0.1, 0
                    );

                    // Occasional soul particles for a more ethereal effect
                    if (random.nextDouble() < 0.3) {
                        player.getWorld().spawnParticle(
                                Particle.SOUL,
                                particleLoc.clone().add(0, 0.5, 0),
                                1, 0.1, 0.2, 0.1, 0.02
                        );
                    }
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
                    // Apply blindness - affects all entities within range
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.BLINDNESS,
                            blindnessDuration,
                            0 // Blindness doesn't have levels
                    ));

                    // Apply weakness - affects all entities within range
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS,
                            weaknessDuration,
                            Math.min(2, tier.ordinal()) // Cap at weakness III for balance
                    ));

                    // Apply slowness for higher tiers
                    if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
                        livingEntity.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS,
                                blindnessDuration,
                                tier.ordinal() - 1
                        ));
                    }

                    // For legendary tier, also apply wither
                    if (tier == RavenTier.LEGENDARY) {
                        livingEntity.addPotionEffect(new PotionEffect(
                                PotionEffectType.WITHER,
                                blindnessDuration / 2, // Half duration for balance
                                0 // Wither I
                        ));
                    }

                    // Visual effect - darkness surrounding the entity
                    new BukkitRunnable() {
                        private int count = 0;
                        private final int maxCount = 3;

                        @Override
                        public void run() {
                            if (count >= maxCount || entity.isDead() || !player.isOnline()) {
                                this.cancel();
                                return;
                            }

                            // Create circling darkness particles
                            Location entityLoc = entity.getLocation().add(0, 1, 0);
                            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                                double radius = 0.6;
                                double x = Math.cos(angle) * radius;
                                double z = Math.sin(angle) * radius;

                                entityLoc.getWorld().spawnParticle(
                                        Particle.SMOKE,
                                        entityLoc.clone().add(x, 0, z),
                                        1, 0.05, 0.2, 0.05, 0
                                );
                            }

                            // Some particles around the head for blindness effect
                            entityLoc.getWorld().spawnParticle(
                                    Particle.SMOKE,
                                    entityLoc.clone().add(0, 0.5, 0),
                                    5, 0.2, 0.1, 0.2, 0
                            );

                            count++;
                        }
                    }.runTaskTimer(RavenPets.getInstance(), 0L, 10L);
                }
            }
        }

        // Sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.3f, 0.7f);

        player.sendMessage("§8§lTerror §r§7unleashed!");
    }

    private void startShadowForm(Player player, int duration) {
        // Store the player's original game mode
        GameMode originalMode = player.getGameMode();
        shadowFormActive.put(player.getUniqueId(), true);

        // Create visual effect for shadow form activation
        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                50, 0.5, 0.5, 0.5, 0.1
        );

        // For Master and Legendary tiers, provide spectator-like effects while keeping the player in survival
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            // Stronger shadow form effect
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !shadowFormActive.getOrDefault(player.getUniqueId(), false)) {
                        this.cancel();
                        return;
                    }

                    // Create shadow effect around the player
                    Location location = player.getLocation().add(0, 1, 0);
                    player.getWorld().spawnParticle(
                            Particle.SMOKE,
                            location,
                            3, 0.2, 0.4, 0.2, 0
                    );

                    // Add occasional soul particles
                    if (random.nextDouble() < 0.2) {
                        player.getWorld().spawnParticle(
                                Particle.SOUL,
                                location,
                                1, 0.2, 0.4, 0.2, 0.02
                        );
                    }
                }
            }.runTaskTimer(RavenPets.getInstance(), 0L, 5L);
        }

        // Schedule the end of shadow form
        new BukkitRunnable() {
            @Override
            public void run() {
                endShadowForm(player, originalMode);
            }
        }.runTaskLater(RavenPets.getInstance(), duration);
    }

    private void endShadowForm(Player player, GameMode originalMode) {
        // Only proceed if player is online
        if (!player.isOnline()) {
            shadowFormActive.remove(player.getUniqueId());
            return;
        }

        // Remove from active shadow forms
        shadowFormActive.remove(player.getUniqueId());

        // Visual effect for shadow form deactivation
        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1
        );

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.8f);
        player.sendMessage("§8§lShadow Form §r§7faded away...");
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 30; // 30 seconds
            case ADEPT -> 20 * 60; // 1 minute
            case EXPERT -> 20 * 120; // 2 minutes
            case MASTER -> 20 * 180; // 3 minutes
            case LEGENDARY -> 20 * 300; // 5 minutes
            default -> 20 * 30;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Shadow Cloak";
    }

    @Override
    public String getDescription() {
        return "Grants night vision, invisibility, and shadow form abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Terror";
    }

    @Override
    public String getSecondaryDescription() {
        return "Inflicts blindness and weakness on nearby entities";
    }
}