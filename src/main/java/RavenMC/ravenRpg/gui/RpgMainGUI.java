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

public class RpgMainGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public RpgMainGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + "✦ " + ChatColor.BOLD + "RavenRpg Menu" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Character Profile (center)
        addMenuItem(22, getCharacterMaterial(data), "Character Profile",
                new String[]{
                        "",
                        "View your complete character information",
                        "",
                        "§6Race: §f" + (data.getSelectedRace() != null ? data.getSelectedRace().getDisplayName() : "None"),
                        "§6Bloodline: §f" + (data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDisplayName() : "None"),
                        data.getCurrentGuild() != null ? "§6Guild: §f" + plugin.getGuildManager().getGuild(data.getCurrentGuild()).getDisplayName() : "§7No guild",
                        "",
                        "§a» Click to view detailed profile «"
                }, "PROFILE");

        // Stats & Skills (left side)
        addMenuItem(20, Material.DIAMOND_SWORD, "Character Stats",
                new String[]{
                        "",
                        "View your character statistics",
                        "",
                        "§6Strength: §f" + data.getStat("strength"),
                        "§6Agility: §f" + data.getStat("agility"),
                        "§6Intelligence: §f" + data.getStat("intelligence"),
                        "§6Vitality: §f" + data.getStat("vitality"),
                        "§6Luck: §f" + data.getStat("luck"),
                        "",
                        "§a» Click to view detailed stats «"
                }, "STATS");

        addMenuItem(21, Material.EXPERIENCE_BOTTLE, "Character Skills",
                new String[]{
                        "",
                        "View your skill progression",
                        "",
                        "§6Combat: §fLevel " + data.getSkill("combat"),
                        "§6Mining: §fLevel " + data.getSkill("mining"),
                        "§6Woodcutting: §fLevel " + data.getSkill("woodcutting"),
                        "§6Fishing: §fLevel " + data.getSkill("fishing"),
                        "§6Crafting: §fLevel " + data.getSkill("crafting"),
                        "§6Trading: §fLevel " + data.getSkill("trading"),
                        "",
                        "§a» Click to view detailed skills «"
                }, "SKILLS");

        // Guild Management (right side)
        addMenuItem(23, Material.MAGENTA_BANNER, "Guild Management",
                new String[]{
                        "",
                        "Manage your guild membership",
                        "",
                        data.getCurrentGuild() != null ?
                                "§6Current Guild: §f" + plugin.getGuildManager().getGuild(data.getCurrentGuild()).getDisplayName() :
                                "§7You are not in a guild",
                        "",
                        "§a» Click to manage guilds «"
                }, "GUILD");

        // Shop Management
        addMenuItem(24, Material.EMERALD, "Shop Management",
                new String[]{
                        "",
                        "Create and manage your shops",
                        "",
                        "§6Shops Owned: §f" + data.getOwnedShops().size(),
                        "",
                        "§a» Click to manage shops «"
                }, "SHOP");

        // RavenPets Integration (if available)
        if (plugin.isRavenPetsEnabled()) {
            addMenuItem(31, Material.COAL_BLOCK, "RavenPets Integration",
                    new String[]{
                            "",
                            "§5✦ RavenPets Integration Active ✦",
                            "",
                            "§fYour RPG progression enhances",
                            "§fyour raven companion's abilities!",
                            "",
                            "§6Intelligence Bonus: §f+" + ((data.getStat("intelligence") - 10) * 2) + "% Raven EXP",
                            "§6Luck Bonus: §f+" + ((data.getStat("luck") - 10) * 1) + "% Raven EXP",
                            "§6Skill Bonus: §f+" + getAverageSkillLevel(data) + "% Raven EXP",
                            "",
                            "§a» Click for detailed integration info «"
                    }, "RAVENPETS");
        }

        // Character Creation (if not complete)
        if (!data.isFullyInitialized()) {
            addMenuItem(40, Material.NETHER_STAR, "Complete Character Creation",
                    new String[]{
                            "",
                            "§c⚠ Character creation incomplete!",
                            "",
                            data.hasChosenRace() ? "§a✓ Race selected" : "§c✗ Choose your race",
                            data.hasChosenBloodline() ? "§a✓ Bloodline selected" : "§c✗ Choose your bloodline",
                            "",
                            "§e» Click to continue creation «"
                    }, "CHARACTER_CREATION");
        }

        // Quick actions
        addQuickActions();

        // Fill background
        fillBackground();
    }

    private void addQuickActions() {
        // Quick action buttons at the bottom
        addMenuItem(45, Material.BOOK, "Help & Commands",
                new String[]{
                        "",
                        "§fView available commands and help",
                        "",
                        "§a/rpg help §7- Command list",
                        "§a/guild list §7- Available guilds",
                        "§a/shop create §7- Create a shop",
                        "",
                        "§a» Click for detailed help «"
                }, "HELP");

        addMenuItem(46, Material.REDSTONE, "Quick Stats",
                new String[]{
                        "",
                        "§fQuick stat overview",
                        "",
                        "§6Total Stats: §f" + getTotalStats(),
                        "§6Total Skills: §f" + getTotalSkillLevels(),
                        "§6Character Level: §f" + getCharacterLevel(),
                        "",
                        "§7Click stats button for details"
                }, "INFO");

        addMenuItem(47, Material.CLOCK, "Play Time",
                new String[]{
                        "",
                        "§fYour character timeline",
                        "",
                        "§6Join Date: §f" + getJoinDateString(),
                        "§6Days Played: §f" + getDaysPlayed(),
                        "§6Status: §f" + getCharacterStatus(),
                        "",
                        "§7Click profile for full timeline"
                }, "INFO");

        addMenuItem(53, Material.BARRIER, "Close Menu",
                new String[]{
                        "",
                        "§fClose this menu",
                        "",
                        "§7You can reopen with /rpg"
                }, "CLOSE");
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

    private void addMenuItem(int slot, Material material, String name, String[] loreArray, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "✦ " + name + " ✦");

        List<String> lore = new ArrayList<>();
        for (String line : loreArray) {
            lore.add(ChatColor.translateAlternateColorCodes('§', line));
        }
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + action);

        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
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

        if (lastLine.contains("PROFILE")) {
            clicker.closeInventory();
            new PlayerProfileGUI(plugin, clicker).open();

        } else if (lastLine.contains("STATS")) {
            clicker.closeInventory();
            new PlayerStatsGUI(plugin, clicker).open();

        } else if (lastLine.contains("SKILLS")) {
            clicker.closeInventory();
            new PlayerSkillsGUI(plugin, clicker).open();

        } else if (lastLine.contains("GUILD")) {
            clicker.closeInventory();
            new GuildManagementGUI(plugin, clicker).open();

        } else if (lastLine.contains("SHOP")) {
            clicker.closeInventory();
            new ShopManagementGUI(plugin, clicker).open();

        } else if (lastLine.contains("RAVENPETS")) {
            clicker.closeInventory();
            showRavenPetsIntegration(clicker);

        } else if (lastLine.contains("CHARACTER_CREATION")) {
            clicker.closeInventory();
            new CharacterCreationGUI(plugin, clicker).open();

        } else if (lastLine.contains("HELP")) {
            clicker.closeInventory();
            showHelp(clicker);

        } else if (lastLine.contains("CLOSE")) {
            clicker.closeInventory();

        } else if (lastLine.contains("INFO")) {
            // Just informational, do nothing
        }
    }

    private void showRavenPetsIntegration(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenPets Integration" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Your RPG character enhances your raven companion! ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Active Bonuses:");

        // Calculate bonuses
        int intelligence = data.getStat("intelligence");
        int luck = data.getStat("luck");
        double expMultiplier = 1.0 + (intelligence - 10) * 0.02 + (luck - 10) * 0.01;

        player.sendMessage(ChatColor.GREEN + "  • Intelligence Bonus: " + ChatColor.WHITE + "+" + String.format("%.1f%%", (intelligence - 10) * 2) + " Raven EXP");
        player.sendMessage(ChatColor.GREEN + "  • Luck Bonus: " + ChatColor.WHITE + "+" + String.format("%.1f%%", (luck - 10) * 1) + " Raven EXP");
        player.sendMessage(ChatColor.GREEN + "  • Skill Average Bonus: " + ChatColor.WHITE + "+" + getAverageSkillLevel(data) + "% Raven EXP");
        player.sendMessage(ChatColor.GREEN + "  • Total EXP Multiplier: " + ChatColor.GOLD + String.format("%.1fx", expMultiplier));

        if (data.getSelectedRace() != null) {
            player.sendMessage(ChatColor.GREEN + "  • " + data.getSelectedRace().getDisplayName() + " Racial Bonus");
        }

        if (data.getSelectedBloodline() != null) {
            player.sendMessage(ChatColor.GREEN + "  • " + data.getSelectedBloodline().getDisplayName() + " Heritage Bonus");
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "Use /raven to manage your companion!");
        player.sendMessage(ChatColor.YELLOW + "Level up your RPG character to boost your raven!");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenRpg Commands" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Character Commands:");
        player.sendMessage(ChatColor.AQUA + "  /rpg" + ChatColor.WHITE + " - Open main RPG menu");
        player.sendMessage(ChatColor.AQUA + "  /rpg race" + ChatColor.WHITE + " - Choose your race");
        player.sendMessage(ChatColor.AQUA + "  /rpg bloodline" + ChatColor.WHITE + " - Choose your bloodline");
        player.sendMessage(ChatColor.AQUA + "  /rpg stats" + ChatColor.WHITE + " - View your stats");
        player.sendMessage(ChatColor.AQUA + "  /rpg profile" + ChatColor.WHITE + " - View your profile");
        player.sendMessage(ChatColor.AQUA + "  /rpg skills" + ChatColor.WHITE + " - View your skills");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Guild Commands:");
        player.sendMessage(ChatColor.AQUA + "  /guild" + ChatColor.WHITE + " - Open guild management");
        player.sendMessage(ChatColor.AQUA + "  /guild list" + ChatColor.WHITE + " - List all guilds");
        player.sendMessage(ChatColor.AQUA + "  /guild create <name>" + ChatColor.WHITE + " - Create a guild");
        player.sendMessage(ChatColor.AQUA + "  /guild join <name>" + ChatColor.WHITE + " - Join a guild");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Shop Commands:");
        player.sendMessage(ChatColor.AQUA + "  /shop" + ChatColor.WHITE + " - Open shop management");
        player.sendMessage(ChatColor.AQUA + "  /shop create <name>" + ChatColor.WHITE + " - Create a shop");
        player.sendMessage(ChatColor.AQUA + "  /shop list" + ChatColor.WHITE + " - List your shops");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═════════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private int getTotalStats() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        return data.getStat("strength") + data.getStat("agility") + data.getStat("intelligence") +
                data.getStat("vitality") + data.getStat("luck");
    }

    private int getTotalSkillLevels() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        return data.getSkill("combat") + data.getSkill("mining") + data.getSkill("woodcutting") +
                data.getSkill("fishing") + data.getSkill("crafting") + data.getSkill("trading");
    }

    private int getAverageSkillLevel(PlayerData data) {
        return getTotalSkillLevels() / 6;
    }

    private int getCharacterLevel() {
        return (getTotalStats() + getTotalSkillLevels()) / 10;
    }

    private String getJoinDateString() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(new java.util.Date(data.getJoinDate()));
    }

    private long getDaysPlayed() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        return (System.currentTimeMillis() - data.getJoinDate()) / (1000 * 60 * 60 * 24);
    }

    private String getCharacterStatus() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        if (!data.isFullyInitialized()) {
            return ChatColor.RED + "Incomplete";
        }

        if (data.getCurrentGuild() != null && !data.getOwnedShops().isEmpty()) {
            return ChatColor.GOLD + "Elite Member";
        } else if (data.getCurrentGuild() != null) {
            return ChatColor.GREEN + "Guild Member";
        } else if (!data.getOwnedShops().isEmpty()) {
            return ChatColor.BLUE + "Entrepreneur";
        } else {
            return ChatColor.YELLOW + "Adventurer";
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