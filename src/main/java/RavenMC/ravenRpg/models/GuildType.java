package RavenMC.ravenRpg.models;

public enum GuildType {
    MERCHANT_BAZAAR("Merchant Bazaar", "§6🏪", "Trade and commerce",
            new String[]{"Trading", "Economy", "Commerce"}),

    MINING_DEPTHS("Mining Depths", "§7⛏", "Deep earth excavation",
            new String[]{"Mining", "Excavation", "Ore Processing"}),

    FISHING_DOCKS("Fishing Docks", "§9🎣", "Masters of the waters",
            new String[]{"Fishing", "Navigation", "Marine Knowledge"}),

    LUMBER_YARDS("Lumber Yards", "§2🪓", "Forest harvesting specialists",
            new String[]{"Woodcutting", "Forestry", "Nature Crafting"}),

    BATTLE_ARENA("Battle Arena", "§c⚔", "Combat and warfare",
            new String[]{"Combat", "Strategy", "Weapon Mastery"});

    private final String displayName;
    private final String symbol;
    private final String description;
    private final String[] specialties;

    GuildType(String displayName, String symbol, String description, String[] specialties) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.description = description;
        this.specialties = specialties;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol() { return symbol; }
    public String getDescription() { return description; }
    public String[] getSpecialties() { return specialties; }
}