package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;

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

        player.sendMessage("§bYou have activated your water raven's ability!");
    }

    private int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        switch (tier) {
            case NOVICE:
                return 20 * 60; // 1 minute
            case ADEPT:
                return 20 * 120; // 2 minutes
            case EXPERT:
                return 20 * 300; // 5 minutes
            case MASTER:
                return 20 * 600; // 10 minutes
            case LEGENDARY:
                return 20 * 1200; // 20 minutes
            default:
                return 20 * 60;
        }
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
        return "Grants water breathing and improved swimming abilities.";
    }
}