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

public class GuildManagementGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public GuildManagementGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.GOLD + "✦ " + ChatColor.BOLD + "Guild Management" + ChatColor.RESET + ChatColor.GOLD + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);
        Guild playerGuild = plugin.getGuildManager().getPlayerGuild(player);

        if (playerGuild != null) {
            // Player is in a guild - show guild info and management options
            showGuildInfo(playerGuild);
        } else {
            // Player is not in a guild - show available guilds and creation option
            showAvailableGuilds();
        }

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

    private void showGuildInfo(Guild guild) {
        // Guild info item
        ItemStack guildItem = new ItemStack(getMaterialForGuildType(guild.getType()));
        ItemMeta guildMeta = guildItem.getItemMeta();
        guildMeta.setDisplayName(guild.getType().getSymbol() + " " + ChatColor.GOLD + guild.getDisplayName() + " " + guild.getType().getSymbol());

        List<String> guildLore = new ArrayList<>();
        guildLore.add("");
        guildLore.add(ChatColor.WHITE + guild.getDescription());
        guildLore.add("");
        guildLore.add(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + guild.getType().getDisplayName());
        guildLore.add(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + guild.getMembers().size());
        guildLore.add(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + guild.getLevel());
        guildLore.add(ChatColor.YELLOW + "Treasury: " + ChatColor.GREEN + "$" + String.format("%.2f", guild.getTreasury()));

        Player leader = Bukkit.getPlayer(guild.getLeader());
        guildLore.add(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE +
                (leader != null ? leader.getName() : "Offline"));

        guildMeta.setLore(guildLore);
        guildItem.setItemMeta(guildMeta);
        inventory.setItem(22, guildItem);

        // Guild management buttons
        if (guild.isLeader(player.getUniqueId())) {
            // Leader options
            addGuildButton(19, Material.REDSTONE, "Disband Guild",
                    new String[]{"", "§cPermanently disband this guild", "", "§c» Click to disband «"}, "DISBAND");

            addGuildButton(20, Material.EMERALD, "Invite Player",
                    new String[]{"", "§aInvite a player to join", "", "§a» Click to open invite menu «"}, "INVITE");

            addGuildButton(24, Material.DIAMOND, "Promote Member",
                    new String[]{"", "§bPromote a member to officer", "", "§b» Click to manage members «"}, "PROMOTE");

            addGuildButton(25, Material.BARRIER, "Kick Member",
                    new String[]{"", "§cRemove a member from guild", "", "§c» Click to manage members «"}, "KICK");
        }

        // Officer options
        if (guild.isOfficer(player.getUniqueId()) && !guild.isLeader(player.getUniqueId())) {
            addGuildButton(20, Material.EMERALD, "Invite Player",
                    new String[]{"", "§aInvite a player to join", "", "§a» Click to open invite menu «"}, "INVITE");

            addGuildButton(25, Material.BARRIER, "Kick Member",
                    new String[]{"", "§cRemove a member from guild", "", "§c» Click to manage members «"}, "KICK");
        }

        // Leave guild button (for non-leaders)
        if (!guild.isLeader(player.getUniqueId())) {
            addGuildButton(21, Material.BARRIER, "Leave Guild",
                    new String[]{"", "§cLeave " + guild.getDisplayName(), "", "§c» Click to leave «"}, "LEAVE");
        }

        // Guild chat button
        addGuildButton(23, Material.PAPER, "Guild Chat",
                new String[]{"", "§aSend a message to guild", "", "§a» Use /guild chat <message> «"}, "INFO");
    }

    private void showAvailableGuilds() {
        // Create guild button
        addGuildButton(13, Material.NETHER_STAR, "Create New Guild",
                new String[]{"", "§aCreate your own guild", "", "§6Cost: §f$1000", "", "§a» Use /guild create <name> «"}, "INFO");

        // Show available guild types
        int slot = 19;
        for (GuildType type : GuildType.values()) {
            ItemStack typeItem = new ItemStack(getMaterialForGuildType(type));
            ItemMeta typeMeta = typeItem.getItemMeta();
            typeMeta.setDisplayName(type.getSymbol() + " " + ChatColor.YELLOW + type.getDisplayName());

            List<String> typeLore = new ArrayList<>();
            typeLore.add("");
            typeLore.add(ChatColor.WHITE + type.getDescription());
            typeLore.add("");
            typeLore.add(ChatColor.GRAY + "Specialties:");
            for (String specialty : type.getSpecialties()) {
                typeLore.add(ChatColor.GRAY + "  • " + specialty);
            }
            typeLore.add("");

            // Count guilds of this type
            int guildCount = 0;
            for (Guild guild : plugin.getGuildManager().getAllGuilds()) {
                if (guild.getType() == type) {
                    guildCount++;
                }
            }
            typeLore.add(ChatColor.YELLOW + "Active Guilds: " + ChatColor.WHITE + guildCount);

            typeMeta.setLore(typeLore);
            typeItem.setItemMeta(typeMeta);
            inventory.setItem(slot, typeItem);
            slot++;
        }
    }

    private void addGuildButton(int slot, Material material, String name, String[] loreArray, String action) {
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

    private Material getMaterialForGuildType(GuildType type) {
        switch (type) {
            case MERCHANT_BAZAAR: return Material.EMERALD;
            case MINING_DEPTHS: return Material.IRON_PICKAXE;
            case FISHING_DOCKS: return Material.FISHING_ROD;
            case LUMBER_YARDS: return Material.IRON_AXE;
            case BATTLE_ARENA: return Material.IRON_SWORD;
            default: return Material.MAGENTA_BANNER;
        }
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

        } else if (lastLine.contains("LEAVE")) {
            plugin.getGuildManager().leaveGuild(clicker);
            clicker.closeInventory();

        } else if (lastLine.contains("DISBAND")) {
            Guild guild = plugin.getGuildManager().getPlayerGuild(clicker);
            if (guild != null && guild.isLeader(clicker.getUniqueId())) {
                plugin.getGuildManager().disbandGuild(clicker);
                clicker.closeInventory();
            }

        } else if (lastLine.contains("INVITE")) {
            clicker.closeInventory();
            new PlayerInviteGUI(plugin, clicker).open();

        } else if (lastLine.contains("PROMOTE")) {
            clicker.closeInventory();
            new GuildMemberManagementGUI(plugin, clicker, "promote").open();

        } else if (lastLine.contains("KICK")) {
            clicker.closeInventory();
            new GuildMemberManagementGUI(plugin, clicker, "kick").open();

        } else if (lastLine.contains("INFO")) {
            // Just informational, do nothing
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