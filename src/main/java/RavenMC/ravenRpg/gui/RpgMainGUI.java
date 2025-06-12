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

        // Character Profile
        addMenuItem(20, getCharacterMaterial(data), "Character Profile",
                new String[]{
                        "",
                        "View your character information",
                        "",
                        "§6Race: §f" + (data.getSelectedRace() != null ? data.getSelectedRace().getDisplayName() : "None"),
                        "§6Bloodline: §f" + (data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDisplayName() : "None"),
                        "",
                        "§a» Click to view details «"
                }, "PROFILE");

        // Stats & Skills
        addMenuItem(21, Material.DIAMOND_SWORD, "Stats & Skills",
                new String[]{
                        "",
                        "View and manage your stats",
                        "",
                        "§6Strength: §f" + data.getStat("strength"),
                        "§6Agility: §f" + data.getStat("agility"),
                        "§6Intelligence: §f" + data.getStat("intelligence"),
                        "",
                        "§a» Click to view all stats «"
                }, "STATS");

        // Guild Management
        addMenuItem(22, Material.MAGENTA_BANNER, "Guild Management",
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
        addMenuItem(23, Material.EMERALD, "Shop Management",
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
            addMenuItem(24, Material.COAL_BLOCK, "RavenPets Integration",
                    new String[]{
                            "",
                            "§5✦ RavenPets Integration Active ✦",
                            "",
                            "§fYour raven gains bonuses from",
                            "§fyour RPG character progression!",
                            "",
                            "§a» Click for details «"
                    }, "RAVENPETS");
        }

        // Fill background
        fillBackground();
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
            showProfile(clicker);

        } else if (lastLine.contains("STATS")) {
            clicker.closeInventory();
            showStats(clicker);

        } else if (lastLine.contains("GUILD")) {
            clicker.closeInventory();
            new GuildManagementGUI(plugin, clicker).open();

        } else if (lastLine.contains("SHOP")) {
            clicker.closeInventory();
            new ShopManagementGUI(plugin, clicker).open();

        } else if (lastLine.contains("RAVENPETS")) {
            clicker.closeInventory();
            showRavenPetsIntegration(clicker);
        }
    }

    private void showProfile(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "Character Profile" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Name: " + ChatColor.WHITE + player.getName());

        if (data.getSelectedRace() != null) {
            player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Race: " + ChatColor.WHITE + data.getSelectedRace().getDisplayName());
            player.sendMessage(ChatColor.GRAY + "  " + data.getSelectedRace().getDescription());
        }

        if (data.getSelectedBloodline() != null) {
            player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Bloodline: " + data.getSelectedBloodline().getSymbol() + " " + ChatColor.WHITE + data.getSelectedBloodline().getDisplayName());
            player.sendMessage(ChatColor.GRAY + "  " + data.getSelectedBloodline().getDescription());
        }

        if (data.getCurrentGuild() != null) {
            Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
            if (guild != null) {
                player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Guild: " + guild.getType().getSymbol() + " " + ChatColor.WHITE + guild.getDisplayName());
            }
        }

        player.sendMessage(ChatColor.GOLD + "▸ " + ChatColor.YELLOW + "Shops Owned: " + ChatColor.WHITE + data.getOwnedShops().size());
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═════════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showStats(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════ " + ChatColor.BOLD + "Character Stats" + ChatColor.RESET + ChatColor.GOLD + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Base Stats:");
        player.sendMessage(ChatColor.AQUA + "  Strength: " + ChatColor.WHITE + data.getStat("strength"));
        player.sendMessage(ChatColor.AQUA + "  Agility: " + ChatColor.WHITE + data.getStat("agility"));
        player.sendMessage(ChatColor.AQUA + "  Intelligence: " + ChatColor.WHITE + data.getStat("intelligence"));
        player.sendMessage(ChatColor.AQUA + "  Vitality: " + ChatColor.WHITE + data.getStat("vitality"));
        player.sendMessage(ChatColor.AQUA + "  Luck: " + ChatColor.WHITE + data.getStat("luck"));
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Skills:");
        player.sendMessage(ChatColor.GREEN + "  Combat: " + ChatColor.WHITE + "Level " + data.getSkill("combat"));
        player.sendMessage(ChatColor.GREEN + "  Mining: " + ChatColor.WHITE + "Level " + data.getSkill("mining"));
        player.sendMessage(ChatColor.GREEN + "  Woodcutting: " + ChatColor.WHITE + "Level " + data.getSkill("woodcutting"));
        player.sendMessage(ChatColor.GREEN + "  Fishing: " + ChatColor.WHITE + "Level " + data.getSkill("fishing"));
        player.sendMessage(ChatColor.GREEN + "  Crafting: " + ChatColor.WHITE + "Level " + data.getSkill("crafting"));
        player.sendMessage(ChatColor.GREEN + "  Trading: " + ChatColor.WHITE + "Level " + data.getSkill("trading"));
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ ══════════════════════════════════ ✦");
        player.sendMessage("");
    }

    private void showRavenPetsIntegration(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════ " + ChatColor.BOLD + "RavenPets Integration" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ═══════ ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Your raven is enhanced by your RPG character! ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Active Bonuses:");
        player.sendMessage(ChatColor.GREEN + "  • Raven gains experience from RPG activities");
        player.sendMessage(ChatColor.GREEN + "  • Racial bonuses affect raven abilities");
        player.sendMessage(ChatColor.GREEN + "  • Bloodline powers enhance raven magic");
        player.sendMessage(ChatColor.GREEN + "  • Guild membership provides raven benefits");
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "Use /raven to manage your companion!");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ ═══════════════════════════════════════ ✦");
        player.sendMessage("");
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