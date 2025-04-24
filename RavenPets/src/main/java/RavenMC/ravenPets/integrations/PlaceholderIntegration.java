package RavenMC.ravenPets.integrations;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderIntegration extends PlaceholderExpansion {

    private final RavenPets plugin;

    public PlaceholderIntegration(RavenPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ravenpets";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return "";
        }

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return "";
        }

        Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
        if (raven == null) {
            return "";
        }

        // Handle placeholders
        switch (params.toLowerCase()) {
            case "name":
                return raven.getName();

            case "level":
                return String.valueOf(raven.getLevel());

            case "xp":
                return String.valueOf(raven.getXp());

            case "xp_required":
                int required = 100 + (raven.getLevel() * 50);
                return String.valueOf(required);

            case "xp_progress":
                int xpRequired = 100 + (raven.getLevel() * 50);
                double progress = (double) raven.getXp() / xpRequired * 100;
                return String.format("%.1f", progress) + "%";

            case "tier":
                return raven.getTier().getName();

            case "tier_roman":
                return raven.getTier().getRoman();

            case "inventory_slots":
                return String.valueOf(raven.getInventorySlots());

            case "flight_duration":
                int seconds = raven.getFlightDuration();
                if (seconds == Integer.MAX_VALUE) {
                    return "Unlimited";
                } else if (seconds == 0) {
                    return "0";
                } else {
                    return formatTime(seconds);
                }

            case "detection_radius":
                return String.valueOf(raven.getDetectionRadius());

            case "is_active":
                return raven.isActive() ? "Yes" : "No";

            case "home_count":
                return String.valueOf(raven.getHomeLocations().size());

            case "max_homes":
                int maxHomes;
                switch (raven.getTier()) {
                    case ADEPT:
                        maxHomes = 3;
                        break;
                    case EXPERT:
                        maxHomes = 5;
                        break;
                    case MASTER:
                        maxHomes = 8;
                        break;
                    case LEGENDARY:
                        maxHomes = Integer.MAX_VALUE;
                        break;
                    default:
                        maxHomes = 0;
                }
                return String.valueOf(maxHomes);

            case "progress_bar":
                int currentXp = raven.getXp();
                int requiredXp = 100 + (raven.getLevel() * 50);
                return generateProgressBar(currentXp, requiredXp);

            case "tier_color":
                switch (raven.getTier()) {
                    case NOVICE:
                        return "&d"; // Light Purple
                    case ADEPT:
                        return "&5"; // Dark Purple
                    case EXPERT:
                        return "&5&l"; // Bold Dark Purple
                    case MASTER:
                        return "&5&l"; // Bold Dark Purple
                    case LEGENDARY:
                        return "&5&l&o"; // Bold Italic Dark Purple
                    default:
                        return "&d";
                }
        }

        // Check for ability placeholders
        if (params.toLowerCase().startsWith("has_ability_")) {
            String abilityName = params.substring("has_ability_".length());
            return raven.hasAbility(abilityName) ? "Yes" : "No";
        }

        return null;
    }

    /**
     * Format seconds into a human-readable time string
     *
     * @param seconds The time in seconds
     * @return A formatted time string
     */
    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (remainingSeconds == 0) {
                return minutes + "m";
            } else {
                return minutes + "m " + remainingSeconds + "s";
            }
        }
    }

    /**
     * Generate a progress bar for display
     *
     * @param current The current value
     * @param max The maximum value
     * @return A text-based progress bar
     */
    private String generateProgressBar(int current, int max) {
        final int BAR_LENGTH = 20;
        int filledBars = (int) Math.ceil((double) current / max * BAR_LENGTH);
        filledBars = Math.min(filledBars, BAR_LENGTH);

        StringBuilder bar = new StringBuilder("&5[");

        for (int i = 0; i < BAR_LENGTH; i++) {
            if (i < filledBars) {
                bar.append("&d|");
            } else {
                bar.append("&8|");
            }
        }

        bar.append("&5]");

        return bar.toString();
    }
}