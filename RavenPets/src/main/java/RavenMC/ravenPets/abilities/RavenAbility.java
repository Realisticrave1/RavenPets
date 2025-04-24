package RavenMC.ravenPets.abilities;

import RavenMC.ravenPets.enums.RavenTier;

public enum RavenAbility {
    // Novice abilities
    BASIC_CRAFTING("basic_crafting", "Basic crafting recipes", RavenTier.NOVICE),
    SMALL_INVENTORY("small_inventory", "Small inventory (3 slots)", RavenTier.NOVICE),
    ITEM_RETRIEVAL("item_retrieval", "Item retrieval (10 block radius)", RavenTier.NOVICE),
    ILLUMINATION("illumination", "Illumination (night vision 1min)", RavenTier.NOVICE),

    // Adept abilities
    IMPROVED_CRAFTING("improved_crafting", "Improved crafting (4 unique recipes)", RavenTier.ADEPT),
    MEDIUM_INVENTORY("medium_inventory", "Medium inventory (6 slots)", RavenTier.ADEPT),
    BASIC_FLIGHT("basic_flight", "Basic flight (30 seconds)", RavenTier.ADEPT),
    ENEMY_DETECTION("enemy_detection", "Enemy detection (30 block radius)", RavenTier.ADEPT),
    RESOURCE_HIGHLIGHTING("resource_highlighting", "Resource highlighting (ore detection)", RavenTier.ADEPT),
    HOME_LOCATIONS("home_locations", "3 Home locations", RavenTier.ADEPT),

    // Expert abilities
    ADVANCED_CRAFTING("advanced_crafting", "Advanced crafting (8 unique recipes)", RavenTier.EXPERT),
    LARGE_INVENTORY("large_inventory", "Large inventory (9 slots)", RavenTier.EXPERT),
    ENHANCED_FLIGHT("enhanced_flight", "Enhanced flight (2 minutes)", RavenTier.EXPERT),
    COMBAT_ASSISTANCE("combat_assistance", "Combat assistance (damage boost)", RavenTier.EXPERT),
    TELEPORTATION("teleportation", "Teleportation (1000 block range)", RavenTier.EXPERT),
    RESOURCE_AUTO_COLLECTION("resource_auto_collection", "Resource auto-collection", RavenTier.EXPERT),
    EXPERT_HOME_LOCATIONS("expert_home_locations", "5 Home locations", RavenTier.EXPERT),

    // Master abilities
    MASTER_CRAFTING("master_crafting", "Master crafting (12 unique recipes)", RavenTier.MASTER),
    XL_INVENTORY("xl_inventory", "XL inventory (12 slots)", RavenTier.MASTER),
    ADVANCED_FLIGHT("advanced_flight", "Advanced flight (5 minutes)", RavenTier.MASTER),
    CUSTOM_HOME_DIMENSION("custom_home_dimension", "Custom home dimension access", RavenTier.MASTER),
    ADVANCED_MAGIC("advanced_magic", "Advanced magic abilities (4 spells)", RavenTier.MASTER),
    AUTO_REPAIR("auto_repair", "Auto-repair of tools and armor", RavenTier.MASTER),
    WEATHER_CONTROL("weather_control", "Weather control commands", RavenTier.MASTER),
    MASTER_HOME_LOCATIONS("master_home_locations", "8 Home locations", RavenTier.MASTER),

    // Legendary abilities
    LEGENDARY_CRAFTING("legendary_crafting", "Legendary crafting (all special recipes)", RavenTier.LEGENDARY),
    UNLIMITED_INVENTORY("unlimited_inventory", "Unlimited inventory storage", RavenTier.LEGENDARY),
    UNLIMITED_FLIGHT("unlimited_flight", "Unlimited flight duration", RavenTier.LEGENDARY),
    WORLD_ALTERING("world_altering", "World-altering powers", RavenTier.LEGENDARY),
    CUSTOM_COMMANDS("custom_commands", "Custom command abilities", RavenTier.LEGENDARY),
    SERVER_TITLES("server_titles", "Special server titles and aura", RavenTier.LEGENDARY),
    TIME_CONTROL("time_control", "Time control abilities", RavenTier.LEGENDARY),
    VOID_WALKING("void_walking", "Void walking (no fall damage)", RavenTier.LEGENDARY),
    RESOURCE_GENERATION("resource_generation", "Resource generation powers", RavenTier.LEGENDARY),
    UNLIMITED_HOME_LOCATIONS("unlimited_home_locations", "Unlimited Home locations", RavenTier.LEGENDARY);

    private final String name;
    private final String description;
    private final RavenTier requiredTier;

    RavenAbility(String name, String description, RavenTier requiredTier) {
        this.name = name;
        this.description = description;
        this.requiredTier = requiredTier;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RavenTier getRequiredTier() {
        return requiredTier;
    }
}