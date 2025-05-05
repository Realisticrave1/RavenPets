package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;

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
            player.launchProjectile(org.bukkit.entity.Fireball.class);
        }

        // Add visual and sound effects for 1.21 enhancement
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

        player.sendMessage("§cYou have activated your fire raven's ability!");
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
                return 20 * 300; // 5 minutes
            case LEGENDARY:
                return 20 * 600; // 10 minutes
            default:
                return 20 * 30;
        }
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
        return "Grants fire resistance and increased damage.";
    }
}