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

public class WaterAbility implements RavenAbility {
    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Water breathing
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0));

        // Higher tiers give dolphins grace for faster swimming
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, duration, amplifier));
        }

        // Expert and above can regenerate health underwater
        if (tier.ordinal() >= RavenTier.EXPERT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier - 1));
        }

        // Add visual and sound effects for 1.21 enhancement
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1.0f, 1.0f);

        player.sendMessage("§b§lAquatic Affinity §r§7activated!");
    }

    @Override
    public void executeSecondary(Player player, RavenTier tier) {
        // Water Wave ability - pushes nearby entities and gives them slowness
        int radius = 3 + tier.ordinal();
        int slownessDuration = 20 * (2 + tier.ordinal()); // Duration in ticks
        int slownessAmplifier = tier.ordinal() > 1 ? 1 : 0;

        // Visual effects - water wave
        Location center = player.getLocation();
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
            for (double r = 0.5; r < radius; r += 0.5) {
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                Location particleLoc = center.clone().add(x, 0.1, z);

                player.getWorld().spawnParticle(
                        Particle.SPLASH,
                        particleLoc,
                        5, 0.1, 0.1, 0.1, 0
                );

                if (r % 1.5 < 0.1) {
                    player.getWorld().spawnParticle(
                            Particle.BUBBLE,
                            particleLoc,
                            2, 0.1, 0.1, 0.1, 0.2
                    );
                }
            }
        }

        // Push and slow entities
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Skip entities with the same team tag (for protection)
                if (player.getScoreboardTags().stream().anyMatch(tag ->
                        tag.startsWith("raventeam:") && entity.getScoreboardTags().contains(tag))) {
                    continue;
                }

                // Apply knockback
                Vector knockback = entity.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize()
                        .multiply(1.0 + tier.ordinal() * 0.5);
                entity.setVelocity(entity.getVelocity().add(knockback));

                // Apply slowness to entities with remaining air (not drowning)
                // Using the correct method to check for air
                if (livingEntity.getRemainingAir() > 0) {
                    livingEntity.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS,
                            slownessDuration,
                            slownessAmplifier
                    ));
                }
            }
        }

        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SWIM, 1.0f, 0.6f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 0.8f);

        player.sendMessage("§b§lTidal Wave §r§7unleashed!");
    }

    @Override
    public int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        return switch (tier) {
            case NOVICE -> 20 * 60; // 1 minute
            case ADEPT -> 20 * 120; // 2 minutes
            case EXPERT -> 20 * 300; // 5 minutes
            case MASTER -> 20 * 600; // 10 minutes
            case LEGENDARY -> 20 * 1200; // 20 minutes
            default -> 20 * 60;
        };
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Aquatic Affinity";
    }

    @Override
    public String getDescription() {
        return "Grants water breathing and improved swimming abilities";
    }

    @Override
    public String getSecondaryName() {
        return "Tidal Wave";
    }

    @Override
    public String getSecondaryDescription() {
        return "Creates a wave of water that pushes and slows nearby entities";
    }
}