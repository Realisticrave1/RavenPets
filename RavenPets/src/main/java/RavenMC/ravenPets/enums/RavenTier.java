package RavenMC.ravenPets.enums;

public enum RavenTier {
    NOVICE("Novice Raven", "I", 1, 10),
    ADEPT("Adept Raven", "II", 11, 25),
    EXPERT("Expert Raven", "III", 26, 50),
    MASTER("Master Raven", "IV", 51, 75),
    LEGENDARY("Legendary Raven", "V", 76, 100);

    private final String name;
    private final String roman;
    private final int minLevel;
    private final int maxLevel;

    RavenTier(String name, String roman, int minLevel, int maxLevel) {
        this.name = name;
        this.roman = roman;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public String getRoman() {
        return roman;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public static RavenTier getByLevel(int level) {
        for (RavenTier tier : values()) {
            if (level >= tier.getMinLevel() && level <= tier.getMaxLevel()) {
                return tier;
            }
        }
        return NOVICE; // Default
    }
}