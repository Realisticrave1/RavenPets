package RavenMC.ravenRpg.models;

import java.util.Map;
import java.util.HashMap;

public enum Race {
    HUMAN("Human", "Balanced and adaptable", createHumanBonuses()),
    ORK("Ork", "Strength and honor", createOrkBonuses()),
    ELF("Elf", "Grace and wisdom", createElfBonuses()),
    VAMPIRE("Vampire", "Dark eternal power", createVampireBonuses());

    private final String displayName;
    private final String description;
    private final Map<String, Integer> statBonuses;

    Race(String displayName, String description, Map<String, Integer> statBonuses) {
        this.displayName = displayName;
        this.description = description;
        this.statBonuses = statBonuses;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Map<String, Integer> getStatBonuses() { return statBonuses; }

    private static Map<String, Integer> createHumanBonuses() {
        Map<String, Integer> bonuses = new HashMap<>();
        bonuses.put("strength", 2);
        bonuses.put("agility", 2);
        bonuses.put("intelligence", 2);
        bonuses.put("vitality", 2);
        bonuses.put("luck", 2);
        return bonuses;
    }

    private static Map<String, Integer> createOrkBonuses() {
        Map<String, Integer> bonuses = new HashMap<>();
        bonuses.put("strength", 5);
        bonuses.put("vitality", 3);
        bonuses.put("agility", -1);
        bonuses.put("intelligence", -1);
        bonuses.put("luck", 0);
        return bonuses;
    }

    private static Map<String, Integer> createElfBonuses() {
        Map<String, Integer> bonuses = new HashMap<>();
        bonuses.put("agility", 4);
        bonuses.put("intelligence", 3);
        bonuses.put("strength", -1);
        bonuses.put("vitality", 0);
        bonuses.put("luck", 2);
        return bonuses;
    }

    private static Map<String, Integer> createVampireBonuses() {
        Map<String, Integer> bonuses = new HashMap<>();
        bonuses.put("strength", 3);
        bonuses.put("agility", 3);
        bonuses.put("intelligence", 2);
        bonuses.put("vitality", -2);
        bonuses.put("luck", 1);
        return bonuses;
    }
}