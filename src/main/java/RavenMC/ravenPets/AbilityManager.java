package RavenMC.ravenPets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Sound;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

public class AbilityManager {
    private final RavenPets plugin;
    private final Map<RavenElementType, RavenAbility> abilities;
    private final Map<UUID, Map<RavenElementType, Long>> abilityCooldowns;
    private final Map<UUID, Map<RavenElementType, Long>> secondaryCooldowns;

    public AbilityManager(RavenPets plugin) {
        this.plugin = plugin;
        this.abilities = new HashMap<>();
        this.abilityCooldowns = new ConcurrentHashMap<>();
        this.secondaryCooldowns = new ConcurrentHashMap<>();

        // Register all abilities
        registerAbilities();

        // Log startup message
        plugin.getLogger().info("AbilityManager initialized with " + abilities.size() + " elemental abilities");

        // Start cooldown checker task
        startCooldownChecker();
    }

    private void registerAbilities() {
        // Fire abilities
        abilities.put(RavenElementType.FIRE, new FireAbility());

        // Water abilities
        abilities.put(RavenElementType.WATER, new WaterAbility());

        // Earth abilities
        abilities.put(RavenElementType.EARTH, new EarthAbility());

        // Air abilities
        abilities.put(RavenElementType.AIR, new AirAbility());

        // Lightning abilities
        abilities.put(RavenElementType.LIGHTNING, new LightningAbility());

        // Ice abilities
        abilities.put(RavenElementType.ICE, new IceAbility());

        // Nature abilities
        abilities.put(RavenElementType.NATURE, new NatureAbility());

        // Darkness abilities
        abilities.put(RavenElementType.DARKNESS, new DarknessAbility());

        // Light abilities
        abilities.put(RavenElementType.LIGHT, new LightAbility());
    }

    /**
     * Start a periodic task to check and clean up expired cooldowns
     */
    private void startCooldownChecker() {
        // Run task every second to check cooldowns
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Current time
            long now = System.currentTimeMillis();

            // Check primary cooldowns
            for (UUID playerId : abilityCooldowns.keySet()) {
                Map<RavenElementType, Long> playerCooldowns = abilityCooldowns.get(playerId);
                if (playerCooldowns != null) {
                    // Remove expired cooldowns
                    playerCooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);

                    // Remove player from map if they have no cooldowns
                    if (playerCooldowns.isEmpty()) {
                        abilityCooldowns.remove(playerId);
                    }
                }
            }

            // Check secondary cooldowns
            for (UUID playerId : secondaryCooldowns.keySet()) {
                Map<RavenElementType, Long> playerCooldowns = secondaryCooldowns.get(playerId);
                if (playerCooldowns != null) {
                    // Remove expired cooldowns
                    playerCooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);

                    // Remove player from map if they have no cooldowns
                    if (playerCooldowns.isEmpty()) {
                        secondaryCooldowns.remove(playerId);
                    }
                }
            }
        }, 20L, 20L); // 20 ticks = 1 second
    }

    public void executeAbility(Player player) {
        // Get the PlayerRaven object for this player
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Get element type
        RavenElementType elementType = raven.getElementType();

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Player " + player.getName() + " attempting to use " + elementType + " primary ability");
        }

        // Check cooldown
        if (isOnCooldown(player, elementType, false)) {
            int remaining = getRemainingCooldown(player, elementType, false);
            player.sendMessage("§c§lAbility on cooldown! §7(§f" + remaining + "s §7remaining)");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Execute ability
        RavenAbility ability = abilities.get(elementType);
        if (ability != null) {
            // Get tier
            RavenTier tier = raven.getTier();
            ability.execute(player, tier);

            // Set cooldown AFTER ability execution
            setCooldown(player, elementType, false);

            // Debug log
            if (plugin.getConfig().getBoolean("debug-mode", false)) {
                plugin.getLogger().info("Set " + elementType + " primary ability cooldown for " + player.getName());
            }
        } else {
            // Log error if ability not found
            plugin.getLogger().warning("No ability found for element type: " + elementType);
        }
    }

    public void executeSecondaryAbility(Player player) {
        // Get the PlayerRaven object for this player
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Get element type
        RavenElementType elementType = raven.getElementType();

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Player " + player.getName() + " attempting to use " + elementType + " secondary ability");
        }

        // Check cooldown
        if (isOnCooldown(player, elementType, true)) {
            int remaining = getRemainingCooldown(player, elementType, true);
            player.sendMessage("§c§lSecondary ability on cooldown! §7(§f" + remaining + "s §7remaining)");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Execute ability
        RavenAbility ability = abilities.get(elementType);
        if (ability != null) {
            // Get tier
            RavenTier tier = raven.getTier();
            ability.executeSecondary(player, tier);

            // Set cooldown AFTER ability execution
            setCooldown(player, elementType, true);

            // Debug log
            if (plugin.getConfig().getBoolean("debug-mode", false)) {
                plugin.getLogger().info("Set " + elementType + " secondary ability cooldown for " + player.getName());
            }
        } else {
            // Log error if ability not found
            plugin.getLogger().warning("No ability found for element type: " + elementType);
        }
    }

    public ItemStack createRavenCore(Player player) {
        // Get the PlayerRaven object for this player
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Get element type
        RavenElementType elementType = raven.getElementType();

        ItemStack core = new ItemStack(getCoreItemMaterial(elementType));
        ItemMeta meta = core.getItemMeta();

        meta.setDisplayName("§5§lRaven Core: " + formatElementType(elementType));

        List<String> lore = new ArrayList<>();
        lore.add("§7Owner: " + player.getName());
        lore.add("§7Element: " + getElementColor(elementType) + formatElementType(elementType));
        lore.add("§7Tier: " + getTierColor(raven.getTier()) + formatTierName(raven.getTier()));
        lore.add("§7Level: §f" + raven.getLevel());
        lore.add("");

        // Primary ability
        RavenAbility ability = abilities.get(elementType);
        if (ability != null) {
            lore.add(getElementColor(elementType) + "§l" + ability.getName() + " §7- " + ability.getDescription());
            lore.add("§7Right-click to activate");

            // Add cooldown if applicable
            if (isOnCooldown(player, elementType, false)) {
                int remaining = getRemainingCooldown(player, elementType, false);
                lore.add("§c§lOn cooldown: §f" + remaining + "s");
            } else {
                lore.add("§a§lReady to use");
            }

            lore.add("");

            // Secondary ability
            lore.add(getElementColor(elementType) + "§l" + ability.getSecondaryName() + " §7- " + ability.getSecondaryDescription());
            lore.add("§7Shift + Right-click to activate");

            // Add cooldown if applicable
            if (isOnCooldown(player, elementType, true)) {
                int remaining = getRemainingCooldown(player, elementType, true);
                lore.add("§c§lOn cooldown: §f" + remaining + "s");
            } else {
                lore.add("§a§lReady to use");
            }
        }

        meta.setLore(lore);
        core.setItemMeta(meta);
        return core;
    }

    private Material getCoreItemMaterial(RavenElementType elementType) {
        switch (elementType) {
            case FIRE:
                return Material.BLAZE_POWDER;
            case WATER:
                return Material.PRISMARINE_CRYSTALS;
            case EARTH:
                return Material.EMERALD;
            case AIR:
                return Material.FEATHER;
            case LIGHTNING:
                return Material.NETHER_STAR;
            case ICE:
                return Material.BLUE_ICE;
            case NATURE:
                return Material.OAK_SAPLING;
            case DARKNESS:
                return Material.OBSIDIAN;
            case LIGHT:
                return Material.GLOWSTONE_DUST;
            default:
                return Material.NETHER_STAR;
        }
    }

    private String formatElementType(RavenElementType elementType) {
        String name = elementType.name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private String formatTierName(RavenTier tier) {
        String name = tier.name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private String getElementColor(RavenElementType elementType) {
        switch (elementType) {
            case FIRE:
                return "§c"; // Red
            case WATER:
                return "§9"; // Blue
            case EARTH:
                return "§2"; // Dark Green
            case AIR:
                return "§f"; // White
            case LIGHTNING:
                return "§e"; // Yellow
            case ICE:
                return "§b"; // Aqua
            case NATURE:
                return "§a"; // Light Green
            case DARKNESS:
                return "§8"; // Dark Gray
            case LIGHT:
                return "§e"; // Yellow
            default:
                return "§7"; // Gray
        }
    }

    private String getTierColor(RavenTier tier) {
        switch (tier) {
            case NOVICE:
                return "§d"; // Light Purple
            case ADEPT:
                return "§5"; // Dark Purple
            case EXPERT:
                return "§5§l"; // Bold Dark Purple
            case MASTER:
                return "§d§l"; // Bold Light Purple
            case LEGENDARY:
                return "§5§l§n"; // Bold Underlined Dark Purple
            default:
                return "§d"; // Light Purple
        }
    }

    // Cooldown methods

    public boolean isOnCooldown(Player player, RavenElementType elementType, boolean isSecondary) {
        // Check if player has bypass permission
        if (player.hasPermission("ravenpets.bypasscooldown")) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        Map<UUID, Map<RavenElementType, Long>> cooldownMap = isSecondary ? secondaryCooldowns : abilityCooldowns;

        if (!cooldownMap.containsKey(playerId)) {
            return false;
        }

        Map<RavenElementType, Long> playerCooldowns = cooldownMap.get(playerId);

        if (playerCooldowns == null || !playerCooldowns.containsKey(elementType)) {
            return false;
        }

        Long cooldownTime = playerCooldowns.get(elementType);
        if (cooldownTime == null) {
            return false;
        }

        boolean onCooldown = System.currentTimeMillis() < cooldownTime;

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Player " + player.getName() + " " + elementType +
                    (isSecondary ? " secondary" : " primary") + " ability cooldown status: " +
                    (onCooldown ? "On cooldown" : "Not on cooldown"));
        }

        return onCooldown;
    }

    public int getRemainingCooldown(Player player, RavenElementType elementType, boolean isSecondary) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Map<RavenElementType, Long>> cooldownMap = isSecondary ? secondaryCooldowns : abilityCooldowns;

        if (!cooldownMap.containsKey(playerId)) {
            return 0;
        }

        Map<RavenElementType, Long> playerCooldowns = cooldownMap.get(playerId);

        if (playerCooldowns == null || !playerCooldowns.containsKey(elementType)) {
            return 0;
        }

        Long cooldownTime = playerCooldowns.get(elementType);
        if (cooldownTime == null) {
            return 0;
        }

        long remainingMillis = cooldownTime - System.currentTimeMillis();
        int remainingSeconds = remainingMillis > 0 ? (int) (remainingMillis / 1000) : 0;

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false) && remainingSeconds > 0) {
            plugin.getLogger().info("Player " + player.getName() + " has " + remainingSeconds +
                    " seconds remaining on " + elementType + (isSecondary ? " secondary" : " primary") + " ability cooldown");
        }

        return remainingSeconds;
    }

    public void setCooldown(Player player, RavenElementType elementType, boolean isSecondary) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Map<RavenElementType, Long>> cooldownMap = isSecondary ? secondaryCooldowns : abilityCooldowns;

        // Get or create player's cooldown map
        Map<RavenElementType, Long> playerCooldowns = cooldownMap.computeIfAbsent(playerId, k -> new HashMap<>());

        // Get cooldown time from config
        String configPath = "abilities." + elementType.name().toLowerCase() +
                (isSecondary ? ".secondary-cooldown" : ".cooldown");

        int defaultCooldown = isSecondary ? 120 : 60; // Default cooldowns in seconds
        int cooldownSeconds = plugin.getConfig().getInt(configPath, defaultCooldown);

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Config cooldown for " + elementType +
                    (isSecondary ? " secondary" : " primary") + " ability: " + cooldownSeconds + " seconds");
        }

        // Apply tier-based cooldown reduction
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        RavenTier tier = raven.getTier();
        double tierMultiplier = 1.0 - (tier.ordinal() * 0.1); // 10% reduction per tier
        cooldownSeconds = (int) Math.max(5, cooldownSeconds * tierMultiplier); // Minimum 5 second cooldown

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Final cooldown after tier reduction for " + player.getName() + ": " +
                    cooldownSeconds + " seconds");
        }

        // Set cooldown time
        long cooldownTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        playerCooldowns.put(elementType, cooldownTime);

        // Guarantee the cooldown map is in the main map
        cooldownMap.put(playerId, playerCooldowns);

        // Debug log
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Set " + elementType + (isSecondary ? " secondary" : " primary") +
                    " ability cooldown for " + player.getName() + " until " + new java.util.Date(cooldownTime));
        }
    }

    public RavenAbility getAbility(RavenElementType elementType) {
        return abilities.get(elementType);
    }

    /**
     * Clear a player's cooldowns when they log out
     */
    public void clearPlayerCooldowns(UUID playerId) {
        abilityCooldowns.remove(playerId);
        secondaryCooldowns.remove(playerId);
    }

    /**
     * Debug method to print all active cooldowns
     */
    public void debugPrintCooldowns() {
        if (!plugin.getConfig().getBoolean("debug-mode", false)) {
            return;
        }

        plugin.getLogger().info("=== ACTIVE COOLDOWNS ===");
        plugin.getLogger().info("Primary Ability Cooldowns: " + abilityCooldowns.size() + " players");
        for (UUID playerId : abilityCooldowns.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            String playerName = player != null ? player.getName() : playerId.toString();

            Map<RavenElementType, Long> cooldowns = abilityCooldowns.get(playerId);
            for (Map.Entry<RavenElementType, Long> entry : cooldowns.entrySet()) {
                RavenElementType element = entry.getKey();
                long expireTime = entry.getValue();
                long remainingTime = (expireTime - System.currentTimeMillis()) / 1000;

                plugin.getLogger().info("  " + playerName + " - " + element + " primary: " +
                        remainingTime + "s remaining (expires at " + new java.util.Date(expireTime) + ")");
            }
        }

        plugin.getLogger().info("Secondary Ability Cooldowns: " + secondaryCooldowns.size() + " players");
        for (UUID playerId : secondaryCooldowns.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            String playerName = player != null ? player.getName() : playerId.toString();

            Map<RavenElementType, Long> cooldowns = secondaryCooldowns.get(playerId);
            for (Map.Entry<RavenElementType, Long> entry : cooldowns.entrySet()) {
                RavenElementType element = entry.getKey();
                long expireTime = entry.getValue();
                long remainingTime = (expireTime - System.currentTimeMillis()) / 1000;

                plugin.getLogger().info("  " + playerName + " - " + element + " secondary: " +
                        remainingTime + "s remaining (expires at " + new java.util.Date(expireTime) + ")");
            }
        }
        plugin.getLogger().info("=======================");
    }
}