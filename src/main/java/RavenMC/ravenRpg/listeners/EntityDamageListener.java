package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    private RavenRpg plugin;

    public EntityDamageListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (!data.isFullyInitialized()) return;

        // Apply racial damage resistance
        double damageMultiplier = 1.0;

        switch (data.getSelectedRace()) {
            case ORK:
                // Orks have natural damage resistance
                damageMultiplier = 0.9; // 10% less damage
                break;
            case VAMPIRE:
                // Vampires are weak to certain damage types
                if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                        event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                        event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    damageMultiplier = 1.5; // 50% more fire damage
                } else {
                    damageMultiplier = 0.8; // 20% less other damage
                }
                break;
            case ELF:
                // Elves are agile and take less fall damage
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    damageMultiplier = 0.5; // 50% less fall damage
                }
                break;
            case HUMAN:
                // Humans are balanced, no special resistances
                break;
        }

        // Apply vitality-based damage reduction
        int vitality = data.getStat("vitality");
        double vitalityReduction = 1.0 - ((vitality - 10) * 0.01); // Each point above 10 reduces damage by 1%
        damageMultiplier *= Math.max(0.5, vitalityReduction); // Cap at 50% reduction

        if (damageMultiplier != 1.0) {
            event.setDamage(event.getDamage() * damageMultiplier);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle player attacking other entities
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            PlayerData data = plugin.getDataManager().getPlayerData(attacker);

            if (!data.isFullyInitialized()) return;

            // Apply racial attack bonuses
            double damageMultiplier = 1.0;

            switch (data.getSelectedRace()) {
                case ORK:
                    // Orks deal more damage
                    damageMultiplier = 1.2; // 20% more damage
                    if (Math.random() < 0.05) { // 5% chance
                        attacker.sendMessage(ChatColor.RED + "✦ Battle fury! ✦");
                        damageMultiplier = 1.5; // 50% more damage on crit
                    }
                    break;
                case VAMPIRE:
                    // Vampires have lifesteal
                    if (Math.random() < 0.1) { // 10% chance
                        double heal = event.getDamage() * 0.3;
                        attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + heal));
                        attacker.sendMessage(ChatColor.DARK_RED + "✦ Life stolen! ✦");
                    }
                    break;
                case ELF:
                    // Elves have critical hit chance
                    if (Math.random() < 0.15) { // 15% chance
                        damageMultiplier = 1.3; // 30% more damage
                        attacker.sendMessage(ChatColor.GREEN + "✦ Precise strike! ✦");
                    }
                    break;
                case HUMAN:
                    // Humans have balanced combat
                    damageMultiplier = 1.1; // 10% more damage
                    break;
            }

            // Apply strength-based damage bonus
            int strength = data.getStat("strength");
            double strengthBonus = 1.0 + ((strength - 10) * 0.02); // Each point above 10 adds 2% damage
            damageMultiplier *= strengthBonus;

            if (damageMultiplier != 1.0) {
                event.setDamage(event.getDamage() * damageMultiplier);
            }

            // Give combat experience
            data.addSkillExp("combat", 1);
        }
    }
}