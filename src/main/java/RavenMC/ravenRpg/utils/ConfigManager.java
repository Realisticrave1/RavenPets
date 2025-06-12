package RavenMC.ravenRpg.utils;

import RavenMC.ravenRpg.RavenRpg;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private RavenRpg plugin;
    private Map<String, Object> defaultValues;

    public ConfigManager(RavenRpg plugin) {
        this.plugin = plugin;
        this.defaultValues = new HashMap<>();
        setupDefaults();
    }

    private void setupDefaults() {
        // Character creation settings
        defaultValues.put("character.force-creation-on-join", true);
        defaultValues.put("character.allow-race-change", false);
        defaultValues.put("character.allow-bloodline-change", false);
        defaultValues.put("character.starting-stats.strength", 10);
        defaultValues.put("character.starting-stats.agility", 10);
        defaultValues.put("character.starting-stats.intelligence", 10);
        defaultValues.put("character.starting-stats.vitality", 10);
        defaultValues.put("character.starting-stats.luck", 10);

        // Guild settings
        defaultValues.put("guild.creation-cost", 1000.0);
        defaultValues.put("guild.max-members-default", 20);
        defaultValues.put("guild.allow-multiple-guilds", false);
        defaultValues.put("guild.guild-chat-prefix", "!");
        defaultValues.put("guild.auto-save-interval", 300); // 5 minutes

        // Shop settings
        defaultValues.put("shop.creation-cost", 500.0);
        defaultValues.put("shop.max-shops-per-player", 3);
        defaultValues.put("shop.tax-rate", 0.05);
        defaultValues.put("shop.auto-save-interval", 300); // 5 minutes

        // Combat settings
        defaultValues.put("combat.enable-racial-bonuses", true);
        defaultValues.put("combat.enable-stat-bonuses", true);
        defaultValues.put("combat.damage-multiplier", 1.0);
        defaultValues.put("combat.enable-critical-hits", true);

        // Skill settings
        defaultValues.put("skills.max-level", 100);
        defaultValues.put("skills.exp-multiplier", 1.0);
        defaultValues.put("skills.enable-skill-rewards", true);

        // Integration settings
        defaultValues.put("integration.ravenpets.enabled", true);
        defaultValues.put("integration.ravenpets.stat-bonuses", true);
        defaultValues.put("integration.ravenpets.skill-bonuses", true);

        // Chat settings
        defaultValues.put("chat.show-race-prefix", true);
        defaultValues.put("chat.show-bloodline-symbol", true);
        defaultValues.put("chat.enable-guild-chat", true);

        // Debug settings
        defaultValues.put("debug.enabled", false);
        defaultValues.put("debug.log-player-actions", false);
        defaultValues.put("debug.log-skill-gains", false);
    }

    public <T> T getConfigValue(String path, Class<T> type) {
        FileConfiguration config = plugin.getConfig();
        Object defaultValue = defaultValues.get(path);

        if (defaultValue == null) {
            plugin.getLogger().warning("No default value found for config path: " + path);
            return null;
        }

        Object value = config.get(path, defaultValue);

        try {
            return type.cast(value);
        } catch (ClassCastException e) {
            plugin.getLogger().warning("Invalid type for config path " + path + ". Expected " + type.getSimpleName() + ", got " + value.getClass().getSimpleName());
            return type.cast(defaultValue);
        }
    }

    public void setConfigValue(String path, Object value) {
        plugin.getConfig().set(path, value);
    }

    public void validateAndFixConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean changed = false;

        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            String path = entry.getKey();
            Object defaultValue = entry.getValue();

            if (!config.contains(path)) {
                plugin.getLogger().info("Adding missing config value: " + path);
                config.set(path, defaultValue);
                changed = true;
            }
        }

        if (changed) {
            plugin.saveConfig();
            plugin.getLogger().info("Configuration validation completed with changes.");
        }
    }

    public Map<String, Object> getDefaultValues() {
        return new HashMap<>(defaultValues);
    }
}