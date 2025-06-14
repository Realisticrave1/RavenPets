package RavenMC.ravenRpg.utils;

import RavenMC.ravenRpg.models.PlayerData;
import RavenMC.ravenRpg.models.Race;

public class StatCalculator {

    public static int getEffectiveStat(PlayerData data, String statName) {
        int baseStat = data.getStat(statName);

        // Apply racial bonuses
        if (data.getSelectedRace() != null) {
            Race race = data.getSelectedRace();
            Integer racialBonus = race.getStatBonuses().get(statName);
            if (racialBonus != null) {
                baseStat += racialBonus;
            }
        }

        // Apply guild bonuses (if implemented)
        // Apply equipment bonuses (if implemented)
        // Apply temporary effects (if implemented)

        return Math.max(0, baseStat); // Ensure stats don't go negative
    }

    public static double calculateDamageMultiplier(PlayerData data) {
        int strength = getEffectiveStat(data, "strength");
        double multiplier = 1.0 + ((strength - 10) * 0.02); // 2% per point above 10

        // Apply racial combat bonuses
        if (data.getSelectedRace() != null) {
            switch (data.getSelectedRace()) {
                case ORK:
                    multiplier *= 1.2; // 20% bonus
                    break;
                case VAMPIRE:
                    multiplier *= 1.15; // 15% bonus
                    break;
                case HUMAN:
                    multiplier *= 1.1; // 10% bonus
                    break;
                case ELF:
                    multiplier *= 1.05; // 5% bonus
                    break;
            }
        }

        return multiplier;
    }

    public static double calculateCriticalChance(PlayerData data) {
        int agility = getEffectiveStat(data, "agility");
        int luck = getEffectiveStat(data, "luck");

        double critChance = 0.05; // 5% base
        critChance += (agility - 10) * 0.001; // 0.1% per agility point above 10
        critChance += (luck - 10) * 0.002; // 0.2% per luck point above 10

        // Apply racial bonuses
        if (data.getSelectedRace() != null) {
            switch (data.getSelectedRace()) {
                case ELF:
                    critChance += 0.1; // +10% crit chance
                    break;
                case ORK:
                    critChance += 0.05; // +5% crit chance
                    break;
            }
        }

        return Math.min(0.5, critChance); // Cap at 50%
    }

    public static double calculateExpMultiplier(PlayerData data) {
        int intelligence = getEffectiveStat(data, "intelligence");
        double multiplier = 1.0 + ((intelligence - 10) * 0.01); // 1% per point above 10

        // Apply racial bonuses
        if (data.getSelectedRace() != null) {
            switch (data.getSelectedRace()) {
                case HUMAN:
                    multiplier *= 1.1; // 10% bonus
                    break;
                case ELF:
                    multiplier *= 1.05; // 5% bonus
                    break;
            }
        }

        return multiplier;
    }
}