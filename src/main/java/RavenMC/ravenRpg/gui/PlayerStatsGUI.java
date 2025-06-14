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
import java.util.Map;

public class PlayerStatsGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public PlayerStatsGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.GOLD + "✦ " + ChatColor.BOLD + "Character Stats" + ChatColor.RESET + ChatColor.GOLD + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Character overview
        addCharacterOverview(data);

        // Base Stats
        addBaseStat(19, Material.IRON_SWORD, "Strength", data.getStat("strength"),
                "Increases melee damage", "Improves mining efficiency", "Enhances carrying capacity");

        addBaseStat(20, Material.FEATHER, "Agility", data.getStat("agility"),
                "Increases movement speed", "Improves critical hit chance", "Reduces fall damage");

        addBaseStat(21, Material.BOOK, "Intelligence", data.getStat("intelligence"),
                "Increases experience gain", "Improves mana capacity", "Enhances spell damage");

        addBaseStat(22, Material.APPLE, "Vitality", data.getStat("vitality"),
                "Increases maximum health", "Improves health regeneration", "Reduces damage taken");

        addBaseStat(23, Material.GOLD_INGOT, "Luck", data.getStat("luck"),
                "Increases rare drop chance", "Improves critical hit chance", "Enhances shop profits");

        // Racial bonuses
        if (data.getSelectedRace() != null) {
            addRacialBonuses(data);
        }

        // Combat stats
        addCombatStats(data);

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

    private void addCharacterOverview(PlayerData data) {
        Material material = getCharacterMaterial(data);
        ItemStack overviewItem = new ItemStack(material);
        ItemMeta overviewMeta = overviewItem.getItemMeta();
        overviewMeta.setDisplayName(ChatColor.DARK_PURPLE + "✦ " + ChatColor.BOLD + player.getName() + ChatColor.RESET + ChatColor.DARK_PURPLE + " ✦");

        List<String> overviewLore = new ArrayList<>();
        overviewLore.add("");
        if (data.getSelectedRace() != null) {
            overviewLore.add(ChatColor.GOLD + "Race: " + ChatColor.WHITE + data.getSelectedRace().getDisplayName());
        }
        if (data.getSelectedBloodline() != null) {
            overviewLore.add(ChatColor.LIGHT_PURPLE + "Bloodline: " + data.getSelectedBloodline().getSymbol() + " " + ChatColor.WHITE + data.getSelectedBloodline().getDisplayName());
        }
        if (data.getCurrentGuild() != null) {
            Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
            if (guild != null) {
                overviewLore.add(ChatColor.YELLOW + "Guild: " + guild.getType().getSymbol() + " " + ChatColor.WHITE + guild.getDisplayName());
            }
        }
        overviewLore.add("");
        overviewLore.add(ChatColor.GREEN + "Total Stat Points: " + ChatColor.WHITE + getTotalStats(data));
        overviewLore.add(ChatColor.AQUA + "Average Skill Level: " + ChatColor.WHITE + getAverageSkillLevel(data));

        overviewMeta.setLore(overviewLore);
        overviewItem.setItemMeta(overviewMeta);
        inventory.setItem(13, overviewItem);
    }

    private void addBaseStat(int slot, Material material, String statName, int statValue, String... benefits) {
        ItemStack statItem = new ItemStack(material);
        ItemMeta statMeta = statItem.getItemMeta();
        statMeta.setDisplayName(ChatColor.YELLOW + "✦ " + statName + " ✦");

        List<String> statLore = new ArrayList<>();
        statLore.add("");
        statLore.add(ChatColor.WHITE + "Current Value: " + getStatColor(statValue) + statValue);
        statLore.add("");
        statLore.add(ChatColor.GOLD + "Benefits:");
        for (String benefit : benefits) {
            statLore.add(ChatColor.GRAY + "  • " + benefit);
        }
        statLore.add("");

        // Show stat breakdown
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        int baseStat = 10; // Base stat value
        int racialBonus = 0;
        if (data.getSelectedRace() != null) {
            Map<String, Integer> bonuses = data.getSelectedRace().getStatBonuses();
            racialBonus = bonuses.getOrDefault(statName.toLowerCase(), 0);
        }
        int allocatedPoints = statValue - baseStat - racialBonus;

        statLore.add(ChatColor.AQUA + "Stat Breakdown:");
        statLore.add(ChatColor.WHITE + "  Base: " + ChatColor.GRAY + baseStat);
        if (racialBonus != 0) {
            String color = racialBonus > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "";
            statLore.add(ChatColor.WHITE + "  Racial: " + color + racialBonus);
        }
        if (allocatedPoints > 0) {
            statLore.add(ChatColor.WHITE + "  Allocated: " + ChatColor.GREEN + "+" + allocatedPoints);
        }

        statMeta.setLore(statLore);
        statItem.setItemMeta(statMeta);
        inventory.setItem(slot, statItem);
    }

    private void addRacialBonuses(PlayerData data) {
        Race race = data.getSelectedRace();
        ItemStack raceItem = new ItemStack(getRaceMaterial(race));
        ItemMeta raceMeta = raceItem.getItemMeta();
        raceMeta.setDisplayName(race.getDisplayName() + " " + ChatColor.GOLD + "Racial Bonuses");

        List<String> raceLore = new ArrayList<>();
        raceLore.add("");
        raceLore.add(ChatColor.WHITE + race.getDescription());
        raceLore.add("");
        raceLore.add(ChatColor.YELLOW + "Stat Bonuses:");

        for (Map.Entry<String, Integer> bonus : race.getStatBonuses().entrySet()) {
            if (bonus.getValue() != 0) {
                String color = bonus.getValue() > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "";
                raceLore.add(ChatColor.WHITE + "  " + capitalize(bonus.getKey()) + ": " + color + bonus.getValue());
            }
        }

        raceMeta.setLore(raceLore);
        raceItem.setItemMeta(raceMeta);
        inventory.setItem(37, raceItem);
    }

    private void addCombatStats(PlayerData data) {
        ItemStack combatItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta combatMeta = combatItem.getItemMeta();
        combatMeta.setDisplayName(ChatColor.RED + "✦ Combat Statistics ✦");

        List<String> combatLore = new ArrayList<>();
        combatLore.add("");

        // Calculate derived stats
        double damageMultiplier = calculateDamageMultiplier(data);
        double criticalChance = calculateCriticalChance(data);
        double damageReduction = calculateDamageReduction(data);

        combatLore.add(ChatColor.YELLOW + "Combat Stats:");
        combatLore.add(ChatColor.WHITE + "  Damage Multiplier: " + ChatColor.GREEN + String.format("%.1f%%", (damageMultiplier - 1) * 100));
        combatLore.add(ChatColor.WHITE + "  Critical Chance: " + ChatColor.AQUA + String.format("%.1f%%", criticalChance * 100));
        combatLore.add(ChatColor.WHITE + "  Damage Reduction: " + ChatColor.BLUE + String.format("%.1f%%", damageReduction * 100));
        combatLore.add("");

        combatLore.add(ChatColor.GOLD + "Combat Level: " + ChatColor.WHITE + data.getSkill("combat"));

        combatMeta.setLore(combatLore);
        combatItem.setItemMeta(combatMeta);
        inventory.setItem(39, combatItem);
    }

    private Material getCharacterMaterial(PlayerData data) {
        if (data.getSelectedRace() == null) return Material.PLAYER_HEAD;

        switch (data.getSelectedRace()) {
            case HUMAN: return Material.IRON_HELMET;
            case ORK: return Material.NETHERITE_HELMET;
            case ELF: return Material.LEATHER_HELMET;
            case VAMPIRE: return Material.CHAINMAIL_HELMET;
            default: return Material.PLAYER_HEAD;
        }
    }

    private Material getRaceMaterial(Race race) {
        switch (race) {
            case HUMAN: return Material.IRON_INGOT;
            case ORK: return Material.NETHERITE_AXE;
            case ELF: return Material.BOW;
            case VAMPIRE: return Material.REDSTONE;
            default: return Material.STICK;
        }
    }

    private String getStatColor(int statValue) {
        if (statValue >= 20) return ChatColor.GOLD + "";
        if (statValue >= 15) return ChatColor.GREEN + "";
        if (statValue >= 10) return ChatColor.YELLOW + "";
        if (statValue >= 5) return ChatColor.WHITE + "";
        return ChatColor.GRAY + "";
    }

    private int getTotalStats(PlayerData data) {
        return data.getStat("strength") + data.getStat("agility") + data.getStat("intelligence") +
                data.getStat("vitality") + data.getStat("luck");
    }

    private int getAverageSkillLevel(PlayerData data) {
        int total = data.getSkill("combat") + data.getSkill("mining") + data.getSkill("woodcutting") +
                data.getSkill("fishing") + data.getSkill("crafting") + data.getSkill("trading");
        return total / 6;
    }

    private double calculateDamageMultiplier(PlayerData data) {
        int strength = data.getStat("strength");
        double multiplier = 1.0 + ((strength - 10) * 0.02);

        if (data.getSelectedRace() != null) {
            switch (data.getSelectedRace()) {
                case ORK: multiplier *= 1.2; break;
                case VAMPIRE: multiplier *= 1.15; break;
                case HUMAN: multiplier *= 1.1; break;
                case ELF: multiplier *= 1.05; break;
            }
        }
        return multiplier;
    }

    private double calculateCriticalChance(PlayerData data) {
        int agility = data.getStat("agility");
        int luck = data.getStat("luck");
        double critChance = 0.05 + (agility - 10) * 0.001 + (luck - 10) * 0.002;

        if (data.getSelectedRace() != null) {
            switch (data.getSelectedRace()) {
                case ELF: critChance += 0.1; break;
                case ORK: critChance += 0.05; break;
            }
        }
        return Math.min(0.5, critChance);
    }

    private double calculateDamageReduction(PlayerData data) {
        int vitality = data.getStat("vitality");
        double reduction = (vitality - 10) * 0.01;

        if (data.getSelectedRace() != null) {
            switch (data.getSelectedRace()) {
                case ORK: reduction += 0.1; break;
                case VAMPIRE: reduction += 0.05; break;
            }
        }
        return Math.min(0.5, Math.max(0, reduction));
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
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