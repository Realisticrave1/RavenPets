package RavenMC.ravenRpg.models;

public enum Bloodline {
    HUMAN_STRONGHOLD("Human Stronghold", "Balance and adaptability", "§6⚔",
            new String[]{"Leadership", "Versatility", "Diplomacy"}),

    ORK_WARCAMP("Ork Warcamp", "Strength and honor", "§c⚡",
            new String[]{"Battle Fury", "Intimidation", "Berserker Rage"}),

    ELVEN_GROVE("Elven Grove", "Grace and wisdom", "§a🌿",
            new String[]{"Nature Magic", "Archery", "Forest Knowledge"}),

    VAMPIRE_CRYPT("Vampire Crypt", "Dark eternal power", "§5🦇",
            new String[]{"Blood Magic", "Night Vision", "Immortal Regeneration"});

    private final String displayName;
    private final String description;
    private final String symbol;
    private final String[] abilities;

    Bloodline(String displayName, String description, String symbol, String[] abilities) {
        this.displayName = displayName;
        this.description = description;
        this.symbol = symbol;
        this.abilities = abilities;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getSymbol() { return symbol; }
    public String[] getAbilities() { return abilities; }
}