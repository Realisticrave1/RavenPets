package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    private RavenRpg plugin;

    public BlockBreakListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Only process if player has chosen a race
        if (!data.isFullyInitialized()) return;

        // Gain skill experience based on block type
        if (isMiningBlock(material)) {
            data.addSkillExp("mining", getMiningExp(material));

            // Apply racial bonuses
            applyRacialMiningBonus(data, material);

        } else if (isLoggingBlock(material)) {
            data.addSkillExp("woodcutting", getLoggingExp(material));

            // Apply racial bonuses
            applyRacialLoggingBonus(data, material);
        }

        // Random chance for rare drops based on race
        handleRareDrop(player, data, material);
    }

    private boolean isMiningBlock(Material material) {
        return material.name().contains("_ORE") ||
                material == Material.STONE ||
                material == Material.COBBLESTONE ||
                material == Material.DEEPSLATE ||
                material == Material.COBBLED_DEEPSLATE;
    }

    private boolean isLoggingBlock(Material material) {
        return material.name().contains("_LOG") ||
                material.name().contains("_WOOD");
    }

    private int getMiningExp(Material material) {
        if (material.name().contains("_ORE")) {
            return 5;
        } else {
            return 1;
        }
    }

    private int getLoggingExp(Material material) {
        return 3;
    }

    private void applyRacialMiningBonus(PlayerData data, Material material) {
        switch (data.getSelectedRace()) {
            case ORK:
                // Orks are better miners
                if (Math.random() < 0.1) { // 10% chance
                    data.addStat("strength", 1);
                }
                break;
            case HUMAN:
                // Humans are versatile
                if (Math.random() < 0.05) { // 5% chance
                    data.addStat("luck", 1);
                }
                break;
        }
    }

    private void applyRacialLoggingBonus(PlayerData data, Material material) {
        switch (data.getSelectedRace()) {
            case ELF:
                // Elves are better with nature
                if (Math.random() < 0.1) { // 10% chance
                    data.addStat("agility", 1);
                }
                break;
            case HUMAN:
                // Humans are versatile
                if (Math.random() < 0.05) { // 5% chance
                    data.addStat("luck", 1);
                }
                break;
        }
    }

    private void handleRareDrop(Player player, PlayerData data, Material material) {
        double rareChance = 0.01; // 1% base chance

        // Modify chance based on race
        switch (data.getSelectedRace()) {
            case VAMPIRE:
                rareChance *= 1.5; // Vampires have better luck with rare items
                break;
            case HUMAN:
                rareChance *= 1.2; // Humans have slight luck bonus
                break;
        }

        // Modify chance based on luck stat
        int luck = data.getStat("luck");
        rareChance *= (1.0 + (luck - 10) * 0.01); // Each point of luck above 10 adds 1%

        if (Math.random() < rareChance) {
            // Give rare item based on block type
            // This is a placeholder - implement actual rare drops
            player.sendMessage("§5✦ You found something rare! ✦");
        }
    }
}