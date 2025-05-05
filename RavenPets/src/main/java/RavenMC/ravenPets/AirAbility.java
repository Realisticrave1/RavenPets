package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;

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

        player.sendMessage("§fYou have activated your air raven's ability!");
    }

    private int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        switch (tier) {
            case NOVICE:
                return 20 * 30; // 30 seconds
            case ADEPT:
                return 20 * 60; // 1 minute
            case EXPERT:
                return 20 * 120; // 2 minutes
            case MASTER:
                return 20 * 240; // 4 minutes
            case LEGENDARY:
                return 20 * 60; // Only 1 minute for flight at legendary
            default:
                return 20 * 30;
        }
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
        return "Grants increased speed, jumping, and air-based abilities.";
    }
}