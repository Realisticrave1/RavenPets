package RavenMC.ravenPets;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;

public class EarthAbility implements RavenAbility {
    @Override
    public void execute(Player player, RavenTier tier) {
        int duration = getDurationByTier(tier);
        int amplifier = getAmplifierByTier(tier);

        // Resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amplifier));

        // Higher tiers give slow falling
        if (tier.ordinal() >= RavenTier.ADEPT.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0));
        }

        // Master and above get haste for faster mining
        if (tier.ordinal() >= RavenTier.MASTER.ordinal()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, amplifier));
        }

        // Add visual and sound effects for 1.21 enhancement
        player.getWorld().spawnParticle(Particle.FALLING_DUST, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);

        player.sendMessage("§aYou have activated your earth raven's ability!");
    }

    private int getDurationByTier(RavenTier tier) {
        // Duration in ticks (20 ticks = 1 second)
        switch (tier) {
            case NOVICE:
                return 20 * 45; // 45 seconds
            case ADEPT:
                return 20 * 90; // 1.5 minutes
            case EXPERT:
                return 20 * 180; // 3 minutes
            case MASTER:
                return 20 * 360; // 6 minutes
            case LEGENDARY:
                return 20 * 720; // 12 minutes
            default:
                return 20 * 45;
        }
    }

    private int getAmplifierByTier(RavenTier tier) {
        return tier.ordinal();
    }

    @Override
    public String getName() {
        return "Stone Skin";
    }

    @Override
    public String getDescription() {
        return "Grants damage resistance and earth-based abilities.";
    }
}