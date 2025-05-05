package RavenMC.ravenPets;

import org.bukkit.entity.Player;
// Import PlaceholderAPI classes correctly
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class RavenPlaceholders extends PlaceholderExpansion {
    private final RavenPets plugin;

    public RavenPlaceholders(RavenPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "ravenpets";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // Get player's raven
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        switch (identifier.toLowerCase()) {
            case "name":
                return raven.getName();

            case "level":
                return String.valueOf(raven.getLevel());

            case "tier":
                return raven.getTier().name();

            case "tier_formatted":
                return formatTier(raven.getTier());

            case "element":
                return raven.getElementType().name();

            case "element_formatted":
                return formatElement(raven.getElementType());

            case "experience":
                return String.valueOf(raven.getExperience());

            case "experience_required":
                return String.valueOf(getRequiredExperienceForNextLevel(raven));

            case "experience_percentage":
                double percent = (double) raven.getExperience() / getRequiredExperienceForNextLevel(raven) * 100;
                return String.format("%.1f", percent);

            case "experience_bar":
                return createTextProgressBar(raven.getExperience(), getRequiredExperienceForNextLevel(raven), 10);

            case "is_spawned":
                return raven.isSpawned() ? "true" : "false";

            case "is_spawned_formatted":
                return raven.isSpawned() ? "§aYes" : "§cNo";

            case "coins":
                return String.valueOf(plugin.getCoinManager().getCoins(player));

            case "ability_name":
                RavenAbility ability = plugin.getAbilityManager().getAbility(raven.getElementType());
                return ability != null ? ability.getName() : "None";

            case "secondary_ability_name":
                RavenAbility secAbility = plugin.getAbilityManager().getAbility(raven.getElementType());
                return secAbility != null ? secAbility.getSecondaryName() : "None";

            case "ability_cooldown":
                if (!plugin.getAbilityManager().isOnCooldown(player, raven.getElementType(), false)) {
                    return "0";
                }
                return String.valueOf(plugin.getAbilityManager().getRemainingCooldown(player, raven.getElementType(), false));

            case "secondary_cooldown":
                if (!plugin.getAbilityManager().isOnCooldown(player, raven.getElementType(), true)) {
                    return "0";
                }
                return String.valueOf(plugin.getAbilityManager().getRemainingCooldown(player, raven.getElementType(), true));

            case "has_custom_colors":
                return raven.hasCustomColors() ? "true" : "false";

            case "has_custom_particles":
                return raven.hasCustomParticles() ? "true" : "false";

            default:
                return null;
        }
    }

    private String formatTier(RavenTier tier) {
        String name = tier.name();
        String formatted = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        switch (tier) {
            case NOVICE: return "§d" + formatted;
            case ADEPT: return "§5" + formatted;
            case EXPERT: return "§5§l" + formatted;
            case MASTER: return "§d§l" + formatted;
            case LEGENDARY: return "§5§l§n" + formatted;
            default: return "§d" + formatted;
        }
    }

    private String formatElement(RavenElementType elementType) {
        String color = getElementColor(elementType);
        String name = elementType.name();
        return color + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private String getElementColor(RavenElementType elementType) {
        switch (elementType) {
            case FIRE: return "§c"; // Red
            case WATER: return "§9"; // Blue
            case EARTH: return "§2"; // Dark Green
            case AIR: return "§f"; // White
            case LIGHTNING: return "§e"; // Yellow
            case ICE: return "§b"; // Aqua
            case NATURE: return "§a"; // Light Green
            case DARKNESS: return "§8"; // Dark Gray
            case LIGHT: return "§e"; // Yellow
            default: return "§7"; // Gray
        }
    }

    private int getRequiredExperienceForNextLevel(PlayerRaven raven) {
        return 100 + (raven.getLevel() * 50);
    }

    private String createTextProgressBar(int current, int max, int totalBars) {
        int progressBars = (int) Math.round(((double) current / max) * totalBars);

        StringBuilder bar = new StringBuilder("§5[");
        for (int i = 0; i < totalBars; i++) {
            if (i < progressBars) {
                bar.append("§d■");
            } else {
                bar.append("§7■");
            }
        }
        bar.append("§5]");

        return bar.toString();
    }
}