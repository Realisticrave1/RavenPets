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

public class AbilityManager {
    private final RavenPets plugin;
    private final Map<RavenElementType, RavenAbility> abilities;
    private final Map<UUID, Map<RavenElementType, Long>> abilityCooldowns;
    private final Map<UUID, Map<RavenElementType, Long>> secondaryCooldowns;

    public AbilityManager(RavenPets plugin) {
        this.plugin = plugin;
        this.abilities = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        this.secondaryCooldowns = new HashMap<>();

        // Register all abilities
        registerAbilities();
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

        // Additional elements can be added here
        // TODO: Implement remaining element abilities
    }

    public void executeAbility(Player player) {
        // Get the PlayerRaven object for this player
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Get element type
        RavenElementType elementType = raven.getElementType();

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
            setCooldown(player, elementType, false);
        }
    }

    public void executeSecondaryAbility(Player player) {
        // Get the PlayerRaven object for this player
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);

        // Get element type
        RavenElementType elementType = raven.getElementType();

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
            setCooldown(player, elementType, true);
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
            }

            lore.add("");

            // Secondary ability
            lore.add(getElementColor(elementType) + "§l" + ability.getSecondaryName() + " §7- " + ability.getSecondaryDescription());
            lore.add("§7Shift + Right-click to activate");

            // Add cooldown if applicable
            if (isOnCooldown(player, elementType, true)) {
                int remaining = getRemainingCooldown(player, elementType, true);
                lore.add("§c§lOn cooldown: §f" + remaining + "s");
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

        if (!playerCooldowns.containsKey(elementType)) {
            return false;
        }

        long cooldownTime = playerCooldowns.get(elementType);
        return System.currentTimeMillis() < cooldownTime;
    }

    public int getRemainingCooldown(Player player, RavenElementType elementType, boolean isSecondary) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Map<RavenElementType, Long>> cooldownMap = isSecondary ? secondaryCooldowns : abilityCooldowns;

        if (!cooldownMap.containsKey(playerId)) {
            return 0;
        }

        Map<RavenElementType, Long> playerCooldowns = cooldownMap.get(playerId);

        if (!playerCooldowns.containsKey(elementType)) {
            return 0;
        }

        long cooldownTime = playerCooldowns.get(elementType);
        long remainingMillis = cooldownTime - System.currentTimeMillis();

        return remainingMillis > 0 ? (int) (remainingMillis / 1000) : 0;
    }

    public void setCooldown(Player player, RavenElementType elementType, boolean isSecondary) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Map<RavenElementType, Long>> cooldownMap = isSecondary ? secondaryCooldowns : abilityCooldowns;

        if (!cooldownMap.containsKey(playerId)) {
            cooldownMap.put(playerId, new HashMap<>());
        }

        // Get cooldown based on ability type
        String configPath = "abilities." + elementType.name().toLowerCase() +
                (isSecondary ? ".secondary-cooldown" : ".cooldown");

        int defaultCooldown = isSecondary ? 120 : 60; // Secondary abilities default to longer cooldown
        int cooldownSeconds = plugin.getConfig().getInt(configPath, defaultCooldown);

        // Reduced cooldown for higher tiers
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        RavenTier tier = raven.getTier();
        double tierMultiplier = 1.0 - (tier.ordinal() * 0.1); // 10% reduction per tier
        cooldownSeconds = (int) Math.max(5, cooldownSeconds * tierMultiplier); // Minimum 5 second cooldown

        long cooldownTime = System.currentTimeMillis() + (cooldownSeconds * 1000);
        cooldownMap.get(playerId).put(elementType, cooldownTime);
    }

    public RavenAbility getAbility(RavenElementType elementType) {
        return abilities.get(elementType);
    }
}