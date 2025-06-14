package RavenMC.ravenRpg.gui;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerSkillsGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public PlayerSkillsGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.AQUA + "✦ " + ChatColor.BOLD + "Character Skills" + ChatColor.RESET + ChatColor.AQUA + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Skill overview
        addSkillOverview(data);

        // Individual skills
        addSkill(19, Material.DIAMOND_SWORD, "Combat", data.getSkill("combat"),
                "Fight monsters and players", "Unlocks combat abilities", "Increases damage and defense");

        addSkill(20, Material.IRON_PICKAXE, "Mining", data.getSkill("mining"),
                "Extract ores and minerals", "Chance for double drops", "Faster mining speed");

        addSkill(21, Material.IRON_AXE, "Woodcutting", data.getSkill("woodcutting"),
                "Harvest wood and logs", "Access to rare wood types", "Improved tool efficiency");

        addSkill(22, Material.FISHING_ROD, "Fishing", data.getSkill("fishing"),
                "Catch fish and treasures", "Better catch rates", "Rare item fishing");

        addSkill(23, Material.CRAFTING_TABLE, "Crafting", data.getSkill("crafting"),
                "Create items and tools", "Unlock advanced recipes", "Higher quality crafts");

        addSkill(25, Material.EMERALD, "Trading", data.getSkill("trading"),
                "Buy and sell with shops", "Better shop prices", "Increased profits");

        // RavenPets integration status
        if (plugin.isRavenPetsEnabled()) {
            addRavenPetsIntegration(data);
        }

        // Skill milestones
        addSkillMilestones(data);

        // Back button
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to RPG Menu");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add(ChatColor.GRAY + "Return to main RPG menu");
        backLore.add("");
        backLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "BACK");
        backMeta.setLore(backLore);
        backBtn.setItemMeta(backMeta);
        inventory.setItem(49, backBtn);

        // Fill background
        fillBackground();
    }

    private void addSkillOverview(PlayerData data) {
        ItemStack overviewItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta overviewMeta = overviewItem.getItemMeta();
        overviewMeta.setDisplayName(ChatColor.GREEN + "✦ " + ChatColor.BOLD + "Skill Overview" + ChatColor.RESET + ChatColor.GREEN + " ✦");

        List<String> overviewLore = new ArrayList<>();
        overviewLore.add("");

        int totalLevels = getTotalSkillLevels(data);
        int averageLevel = getAverageSkillLevel(data);
        int highestLevel = getHighestSkillLevel(data);

        overviewLore.add(ChatColor.YELLOW + "Total Skill Levels: " + ChatColor.WHITE + totalLevels);
        overviewLore.add(ChatColor.YELLOW + "Average Level: " + ChatColor.WHITE + averageLevel);
        overviewLore.add(ChatColor.YELLOW + "Highest Level: " + ChatColor.WHITE + highestLevel);
        overviewLore.add("");

        // Show skill progression
        overviewLore.add(ChatColor.GOLD + "Skill Progression:");
        String[] skills = {"combat", "mining", "woodcutting", "fishing", "crafting", "trading"};
        for (String skill : skills) {
            int level = data.getSkill(skill);
            String color = getSkillColor(level);
            overviewLore.add(ChatColor.GRAY + "  " + capitalize(skill) + ": " + color + "Level " + level);
        }

        overviewMeta.setLore(overviewLore);
        overviewItem.setItemMeta(overviewMeta);
        inventory.setItem(13, overviewItem);
    }

    private void addSkill(int slot, Material material, String skillName, int skillLevel, String... benefits) {
        ItemStack skillItem = new ItemStack(material);
        ItemMeta skillMeta = skillItem.getItemMeta();
        skillMeta.setDisplayName(ChatColor.AQUA + "✦ " + skillName + " ✦");

        List<String> skillLore = new ArrayList<>();
        skillLore.add("");
        skillLore.add(ChatColor.WHITE + "Current Level: " + getSkillColor(skillLevel) + skillLevel);

        // Progress bar
        String progressBar = createProgressBar(skillLevel, 100);
        skillLore.add(ChatColor.WHITE + "Progress: " + progressBar);
        skillLore.add("");

        // Experience info
        int expRequired = skillLevel * 100; // Simple formula
        skillLore.add(ChatColor.YELLOW + "Next Level: " + ChatColor.WHITE + expRequired + " EXP required");
        skillLore.add("");

        skillLore.add(ChatColor.GOLD + "Benefits:");
        for (String benefit : benefits) {
            skillLore.add(ChatColor.GRAY + "  • " + benefit);
        }
        skillLore.add("");

        // Show milestones
        addSkillMilestones(skillLore, skillLevel);

        // RavenPets bonus
        if (plugin.isRavenPetsEnabled()) {
            skillLore.add(ChatColor.LIGHT_PURPLE + "✦ RavenPets Bonus: " + ChatColor.WHITE + "+" + (skillLevel * 2) + "% Raven EXP");
        }

        skillMeta.setLore(skillLore);
        skillItem.setItemMeta(skillMeta);
        inventory.setItem(slot, skillItem);
    }

    private void addSkillMilestones(List<String> lore, int level) {
        lore.add(ChatColor.GOLD + "Milestones:");

        if (level >= 10) {
            lore.add(ChatColor.GREEN + "  ✓ Level 10: Basic mastery");
        } else {
            lore.add(ChatColor.DARK_GRAY + "  ✗ Level 10: Basic mastery");
        }

        if (level >= 25) {
            lore.add(ChatColor.GREEN + "  ✓ Level 25: Advanced techniques");
        } else {
            lore.add(ChatColor.DARK_GRAY + "  ✗ Level 25: Advanced techniques");
        }

        if (level >= 50) {
            lore.add(ChatColor.GREEN + "  ✓ Level 50: Expert level");
        } else {
            lore.add(ChatColor.DARK_GRAY + "  ✗ Level 50: Expert level");
        }

        if (level >= 75) {
            lore.add(ChatColor.GREEN + "  ✓ Level 75: Master tier");
        } else {
            lore.add(ChatColor.DARK_GRAY + "  ✗ Level 75: Master tier");
        }

        if (level >= 100) {
            lore.add(ChatColor.GOLD + "  ✓ Level 100: GRANDMASTER!");
        } else {
            lore.add(ChatColor.DARK_GRAY + "  ✗ Level 100: Grandmaster");
        }

        lore.add("");
    }

    private void addRavenPetsIntegration(PlayerData data) {
        ItemStack ravenItem = new ItemStack(Material.COAL_BLOCK);
        ItemMeta ravenMeta = ravenItem.getItemMeta();
        ravenMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ " + ChatColor.BOLD + "RavenPets Integration" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " ✦");

        List<String> ravenLore = new ArrayList<>();
        ravenLore.add("");
        ravenLore.add(ChatColor.WHITE + "Your skills enhance your raven companion!");
        ravenLore.add("");
        ravenLore.add(ChatColor.YELLOW + "Active Bonuses:");

        // Calculate bonuses based on stats and skills
        int intelligence = data.getStat("intelligence");
        int luck = data.getStat("luck");
        double expMultiplier = 1.0 + (intelligence - 10) * 0.02 + (luck - 10) * 0.01;

        ravenLore.add(ChatColor.GREEN + "  • Experience Multiplier: " + ChatColor.WHITE + String.format("%.1f%%", (expMultiplier - 1) * 100));
        ravenLore.add(ChatColor.GREEN + "  • Skill-based EXP Bonus: " + ChatColor.WHITE + getAverageSkillLevel(data) + "%");

        if (data.getSelectedRace() != null) {
            ravenLore.add(ChatColor.GREEN + "  • " + data.getSelectedRace().getDisplayName() + " Racial Bonus");
        }

        ravenLore.add("");
        ravenLore.add(ChatColor.AQUA + "Use /raven to manage your companion!");

        ravenMeta.setLore(ravenLore);
        ravenItem.setItemMeta(ravenMeta);
        inventory.setItem(37, ravenItem);
    }

    private void addSkillMilestones(PlayerData data) {
        ItemStack milestoneItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta milestoneMeta = milestoneItem.getItemMeta();
        milestoneMeta.setDisplayName(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "Skill Milestones" + ChatColor.RESET + ChatColor.GOLD + " ✦");

        List<String> milestoneLore = new ArrayList<>();
        milestoneLore.add("");
        milestoneLore.add(ChatColor.WHITE + "Track your progress across all skills!");
        milestoneLore.add("");

        int totalLevels = getTotalSkillLevels(data);

        // Global milestones
        milestoneLore.add(ChatColor.YELLOW + "Global Milestones:");

        if (totalLevels >= 60) {
            milestoneLore.add(ChatColor.GREEN + "  ✓ Apprentice (60 levels): +5% EXP");
        } else {
            milestoneLore.add(ChatColor.DARK_GRAY + "  ✗ Apprentice (60 levels): +5% EXP");
        }

        if (totalLevels >= 150) {
            milestoneLore.add(ChatColor.GREEN + "  ✓ Journeyman (150 levels): +10% EXP");
        } else {
            milestoneLore.add(ChatColor.DARK_GRAY + "  ✗ Journeyman (150 levels): +10% EXP");
        }

        if (totalLevels >= 300) {
            milestoneLore.add(ChatColor.GREEN + "  ✓ Expert (300 levels): +15% EXP");
        } else {
            milestoneLore.add(ChatColor.DARK_GRAY + "  ✗ Expert (300 levels): +15% EXP");
        }

        if (totalLevels >= 600) {
            milestoneLore.add(ChatColor.GOLD + "  ✓ MASTER (600 levels): +25% EXP");
        } else {
            milestoneLore.add(ChatColor.DARK_GRAY + "  ✗ Master (600 levels): +25% EXP");
        }

        milestoneMeta.setLore(milestoneLore);
        milestoneItem.setItemMeta(milestoneMeta);
        inventory.setItem(39, milestoneItem);
    }

    private String createProgressBar(int current, int max) {
        int barLength = 20;
        int filledBars = (int) ((double) current / max * barLength);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);

        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else if (i == filledBars) {
                bar.append(ChatColor.YELLOW + "█");
            } else {
                bar.append(ChatColor.DARK_GRAY + "█");
            }
        }

        bar.append(ChatColor.WHITE + " " + current + "/" + max);
        return bar.toString();
    }

    private String getSkillColor(int level) {
        if (level >= 100) return ChatColor.GOLD + "";
        if (level >= 75) return ChatColor.LIGHT_PURPLE + "";
        if (level >= 50) return ChatColor.BLUE + "";
        if (level >= 25) return ChatColor.GREEN + "";
        if (level >= 10) return ChatColor.YELLOW + "";
        return ChatColor.WHITE + "";
    }

    private int getTotalSkillLevels(PlayerData data) {
        return data.getSkill("combat") + data.getSkill("mining") + data.getSkill("woodcutting") +
                data.getSkill("fishing") + data.getSkill("crafting") + data.getSkill("trading");
    }

    private int getAverageSkillLevel(PlayerData data) {
        return getTotalSkillLevels(data) / 6;
    }

    private int getHighestSkillLevel(PlayerData data) {
        int highest = 0;
        String[] skills = {"combat", "mining", "woodcutting", "fishing", "crafting", "trading"};
        for (String skill : skills) {
            highest = Math.max(highest, data.getSkill(skill));
        }
        return highest;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (!meta.hasLore()) return;

        List<String> lore = meta.getLore();
        if (lore.isEmpty()) return;

        String lastLine = lore.get(lore.size() - 1);

        if (lastLine.contains("BACK")) {
            clicker.closeInventory();
            new RpgMainGUI(plugin, clicker).open();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            cleanup();
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void cleanup() {
        if (isRegistered) {
            HandlerList.unregisterAll(this);
            isRegistered = false;
        }
    }
}