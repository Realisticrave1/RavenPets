package RavenMC.ravenPets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class AbilityManager {
    private final RavenPets plugin;
    private final Map<RavenElementType, RavenAbility> abilities;

    public AbilityManager(RavenPets plugin) {
        this.plugin = plugin;
        this.abilities = new HashMap<>();

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
    }

    public void executeAbility(Player player) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        RavenElementType elementType = raven.getElementType();

        RavenAbility ability = abilities.get(elementType);
        if (ability != null) {
            ability.execute(player, raven.getTier());
        }
    }

    public ItemStack createRavenCore(Player player) {
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        RavenElementType elementType = raven.getElementType();

        ItemStack core = new ItemStack(getCoreItemMaterial(elementType));
        ItemMeta meta = core.getItemMeta();

        meta.setDisplayName("§5§lRaven Core: " + formatElementType(elementType));
        meta.setLore(Arrays.asList(
                "§7Owner: " + player.getName(),
                "§7Element: " + formatElementType(elementType),
                "§7Tier: " + formatTierName(raven.getTier()),
                "§7Level: " + raven.getLevel(),
                "",
                "§dRight-click to activate your raven ability!"
        ));

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
}