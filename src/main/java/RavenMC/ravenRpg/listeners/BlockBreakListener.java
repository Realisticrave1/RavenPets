package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import org.bukkit.ChatColor;
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
            int oldLevel = data.getSkill("mining");
            int expGained = getMiningExp(material);
            data.addSkillExp("mining", expGained);
            int newLevel = data.getSkill("mining");

            // Check for level up
            if (newLevel > oldLevel) {
                handleSkillLevelUp(player, "mining", oldLevel, newLevel);
            }

            // Apply racial bonuses
            applyRacialMiningBonus(data, material);

        } else if (isLoggingBlock(material)) {
            int oldLevel = data.getSkill("woodcutting");
            int expGained = getLoggingExp(material);
            data.addSkillExp("woodcutting", expGained);
            int newLevel = data.getSkill("woodcutting");

            // Check for level up
            if (newLevel > oldLevel) {
                handleSkillLevelUp(player, "woodcutting", oldLevel, newLevel);
            }

            // Apply racial bonuses
            applyRacialLoggingBonus(data, material);
        }

        // Random chance for rare drops based on race
        handleRareDrop(player, data, material);

        // Save data after changes
        plugin.getDataManager().savePlayerData(data);
    }

    private void handleSkillLevelUp(Player player, String skill, int oldLevel, int newLevel) {
        // Send level up message
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "SKILL LEVEL UP!" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Your " + capitalize(skill) + " skill reached level " + ChatColor.GOLD + newLevel + ChatColor.YELLOW + "!");
        player.sendMessage("");

        // Show milestone rewards
        if (newLevel % 10 == 0) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Milestone Reached! ✦");
            showMilestoneReward(player, skill, newLevel);
        }

        player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════════════ ✦");
        player.sendMessage("");

        // Trigger RavenPets integration
        if (plugin.isRavenPetsEnabled()) {
            plugin.getRavenPetsIntegration().onSkillLevelUp(player, skill, oldLevel, newLevel);
        }
    }

    private void showMilestoneReward(Player player, String skill, int level) {
        switch (level) {
            case 10:
                player.sendMessage(ChatColor.GREEN + "Unlocked: Basic " + capitalize(skill) + " Mastery");
                break;
            case 20:
                player.sendMessage(ChatColor.BLUE + "Unlocked: Advanced " + capitalize(skill) + " Techniques");
                break;
            case 30:
                player.sendMessage(ChatColor.BLUE + "Unlocked: " + capitalize(skill) + " Efficiency Boost");
                break;
            case 40:
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Unlocked: Expert " + capitalize(skill) + " Knowledge");
                break;
            case 50:
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Unlocked: " + capitalize(skill) + " Master Abilities");
                break;
            case 75:
                player.sendMessage(ChatColor.GOLD + "Unlocked: Grandmaster " + capitalize(skill) + " Powers");
                break;
            case 100:
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "LEGENDARY " + skill.toUpperCase() + " MASTERY ACHIEVED!");
                break;
        }
    }

    private boolean isMiningBlock(Material material) {
        return material.name().contains("_ORE") ||
                material == Material.STONE ||
                material == Material.COBBLESTONE ||
                material == Material.DEEPSLATE ||
                material == Material.COBBLED_DEEPSLATE ||
                material == Material.NETHERRACK ||
                material == Material.END_STONE;
    }

    private boolean isLoggingBlock(Material material) {
        return material.name().contains("_LOG") ||
                material.name().contains("_WOOD") ||
                material.name().contains("_STEM");
    }

    private int getMiningExp(Material material) {
        if (material.name().contains("DIAMOND_ORE")) {
            return 15;
        } else if (material.name().contains("EMERALD_ORE")) {
            return 20;
        } else if (material.name().contains("GOLD_ORE")) {
            return 8;
        } else if (material.name().contains("IRON_ORE")) {
            return 5;
        } else if (material.name().contains("COAL_ORE")) {
            return 3;
        } else if (material.name().contains("_ORE")) {
            return 4; // Other ores
        } else {
            return 1; // Stone, cobblestone, etc.
        }
    }

    private int getLoggingExp(Material material) {
        if (material.name().contains("STRIPPED")) {
            return 4;
        } else if (material.name().contains("WOOD")) {
            return 2;
        } else {
            return 3; // Regular logs
        }
    }

    private void applyRacialMiningBonus(PlayerData data, Material material) {
        switch (data.getSelectedRace()) {
            case ORK:
                // Orks are better miners
                if (Math.random() < 0.1) { // 10% chance
                    int oldStrength = data.getStat("strength");
                    data.addStat("strength", 1);

                    // Trigger RavenPets update for stat change
                    if (plugin.isRavenPetsEnabled()) {
                        plugin.getRavenPetsIntegration().onStatIncrease(
                                plugin.getServer().getPlayer(data.getPlayerUUID()),
                                "strength", oldStrength, data.getStat("strength")
                        );
                    }
                }
                break;
            case HUMAN:
                // Humans are versatile
                if (Math.random() < 0.05) { // 5% chance
                    int oldLuck = data.getStat("luck");
                    data.addStat("luck", 1);

                    // Trigger RavenPets update for stat change
                    if (plugin.isRavenPetsEnabled()) {
                        plugin.getRavenPetsIntegration().onStatIncrease(
                                plugin.getServer().getPlayer(data.getPlayerUUID()),
                                "luck", oldLuck, data.getStat("luck")
                        );
                    }
                }
                break;
        }
    }

    private void applyRacialLoggingBonus(PlayerData data, Material material) {
        switch (data.getSelectedRace()) {
            case ELF:
                // Elves are better with nature
                if (Math.random() < 0.1) { // 10% chance
                    int oldAgility = data.getStat("agility");
                    data.addStat("agility", 1);

                    // Trigger RavenPets update for stat change
                    if (plugin.isRavenPetsEnabled()) {
                        plugin.getRavenPetsIntegration().onStatIncrease(
                                plugin.getServer().getPlayer(data.getPlayerUUID()),
                                "agility", oldAgility, data.getStat("agility")
                        );
                    }
                }
                break;
            case HUMAN:
                // Humans are versatile
                if (Math.random() < 0.05) { // 5% chance
                    int oldLuck = data.getStat("luck");
                    data.addStat("luck", 1);

                    // Trigger RavenPets update for stat change
                    if (plugin.isRavenPetsEnabled()) {
                        plugin.getRavenPetsIntegration().onStatIncrease(
                                plugin.getServer().getPlayer(data.getPlayerUUID()),
                                "luck", oldLuck, data.getStat("luck")
                        );
                    }
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
            case ELF:
                if (isLoggingBlock(material)) {
                    rareChance *= 2.0; // Elves are great with nature
                }
                break;
            case ORK:
                if (isMiningBlock(material)) {
                    rareChance *= 1.8; // Orks are great miners
                }
                break;
        }

        // Modify chance based on luck stat
        int luck = data.getStat("luck");
        rareChance *= (1.0 + (luck - 10) * 0.01); // Each point of luck above 10 adds 1%

        if (Math.random() < rareChance) {
            // Give rare item based on block type
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ You found something rare while " +
                    (isMiningBlock(material) ? "mining" : "woodcutting") + "! ✦");

            // Here you could actually give rare items
            // player.getInventory().addItem(new ItemStack(Material.DIAMOND));
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}