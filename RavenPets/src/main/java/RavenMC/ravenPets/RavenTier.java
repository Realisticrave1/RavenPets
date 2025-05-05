package RavenMC.ravenPets;

public enum RavenTier {
    NOVICE(1, 10, 3),
    ADEPT(11, 25, 6),
    EXPERT(26, 50, 9),
    MASTER(51, 75, 12),
    LEGENDARY(76, 100, -1); // -1 for unlimited

    private final int minLevel;
    private final int maxLevel;
    private final int inventorySlots;

    RavenTier(int minLevel, int maxLevel, int inventorySlots) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.inventorySlots = inventorySlots;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getInventorySlots() {
        return inventorySlots;
    }

    public static RavenTier getTierByLevel(int level) {
        for (RavenTier tier : values()) {
            if (level >= tier.getMinLevel() && level <= tier.getMaxLevel()) {
                return tier;
            }
        }
        return LEGENDARY;
    }
}