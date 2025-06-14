package RavenMC.ravenRpg.models;

public enum ShopType {
    GENERAL("General Store", "§f🏪"),
    WEAPONS("Weapon Shop", "§c⚔"),
    ARMOR("Armor Shop", "§7🛡"),
    POTIONS("Potion Shop", "§5🧪"),
    FOOD("Food Market", "§6🍞"),
    MATERIALS("Materials Shop", "§8⚒"),
    RARE("Rare Items", "§d✨"),
    CUSTOM("Custom Shop", "§b📦");

    private final String displayName;
    private final String symbol;

    ShopType(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol() { return symbol; }
}