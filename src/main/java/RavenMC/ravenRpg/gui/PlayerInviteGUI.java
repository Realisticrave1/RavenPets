package RavenMC.ravenRpg.gui;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import java.util.UUID;

public class PlayerInviteGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;
    private int currentPage = 0;
    private List<OfflinePlayer> invitablePlayers;

    public PlayerInviteGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.GREEN + "✦ " + ChatColor.BOLD + "Invite Player" + ChatColor.RESET + ChatColor.GREEN + " ✦");

        loadInvitablePlayers();
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void loadInvitablePlayers() {
        invitablePlayers = new ArrayList<>();
        Guild playerGuild = plugin.getGuildManager().getPlayerGuild(player);

        if (playerGuild == null) return;

        // Get all players who have ever joined the server
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            // Skip if player is null or has never joined
            if (offlinePlayer.getName() == null || !offlinePlayer.hasPlayedBefore()) continue;

            // Skip if player is already in our guild
            if (playerGuild.isMember(offlinePlayer.getUniqueId())) continue;

            invitablePlayers.add(offlinePlayer);
        }
    }

    private void setupGUI() {
        // Clear inventory
        inventory.clear();

        Guild guild = plugin.getGuildManager().getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            return;
        }

        int startIndex = currentPage * 36; // 36 slots for players per page
        int endIndex = Math.min(startIndex + 36, invitablePlayers.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            OfflinePlayer offlinePlayer = invitablePlayers.get(i);
            String playerName = offlinePlayer.getName();
            boolean isOnline = offlinePlayer.isOnline();

            // Check if player is in another guild
            PlayerData targetData = plugin.getDataManager().getPlayerData(offlinePlayer.getUniqueId(), playerName);
            String currentGuild = targetData.getCurrentGuild();

            ItemStack playerItem = new ItemStack(isOnline ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE);
            ItemMeta playerMeta = playerItem.getItemMeta();

            String statusColor = isOnline ? ChatColor.GREEN + "●" : ChatColor.GRAY + "●";
            playerMeta.setDisplayName(statusColor + " " + ChatColor.YELLOW + playerName);

            List<String> playerLore = new ArrayList<>();
            playerLore.add("");
            playerLore.add(ChatColor.WHITE + "Status: " + (isOnline ? ChatColor.GREEN + "Online" : ChatColor.GRAY + "Offline"));

            if (currentGuild != null) {
                Guild otherGuild = plugin.getGuildManager().getGuild(currentGuild);
                if (otherGuild != null) {
                    playerLore.add(ChatColor.RED + "Guild: " + otherGuild.getDisplayName());
                    playerLore.add(ChatColor.RED + "Cannot invite - already in guild");
                } else {
                    playerLore.add(ChatColor.GREEN + "Guild: None");
                    playerLore.add(ChatColor.GREEN + "» Click to invite «");
                }
            } else {
                playerLore.add(ChatColor.GREEN + "Guild: None");
                playerLore.add("");
                playerLore.add(ChatColor.GREEN + "» Click to invite «");
            }

            playerLore.add("");
            playerLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + offlinePlayer.getUniqueId().toString());

            playerMeta.setLore(playerLore);
            playerItem.setItemMeta(playerMeta);
            inventory.setItem(slot, playerItem);
            slot++;
        }

        // Navigation buttons
        if (currentPage > 0) {
            ItemStack prevBtn = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevBtn.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "« Previous Page");
            List<String> prevLore = new ArrayList<>();
            prevLore.add("");
            prevLore.add(ChatColor.GRAY + "Go to page " + currentPage);
            prevLore.add("");
            prevLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "PREV");
            prevMeta.setLore(prevLore);
            prevBtn.setItemMeta(prevMeta);
            inventory.setItem(45, prevBtn);
        }

        if ((currentPage + 1) * 36 < invitablePlayers.size()) {
            ItemStack nextBtn = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextBtn.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page »");
            List<String> nextLore = new ArrayList<>();
            nextLore.add("");
            nextLore.add(ChatColor.GRAY + "Go to page " + (currentPage + 2));
            nextLore.add("");
            nextLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "NEXT");
            nextMeta.setLore(nextLore);
            nextBtn.setItemMeta(nextMeta);
            inventory.setItem(53, nextBtn);
        }

        // Back button
        ItemStack backBtn = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Guild Management");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add(ChatColor.GRAY + "Return to guild management");
        backLore.add("");
        backLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "BACK");
        backMeta.setLore(backLore);
        backBtn.setItemMeta(backMeta);
        inventory.setItem(49, backBtn);

        // Page info
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        pageMeta.setDisplayName(ChatColor.AQUA + "Page " + (currentPage + 1));
        List<String> pageLore = new ArrayList<>();
        pageLore.add("");
        pageLore.add(ChatColor.WHITE + "Showing players " + ((currentPage * 36) + 1) + "-" + Math.min((currentPage + 1) * 36, invitablePlayers.size()));
        pageLore.add(ChatColor.WHITE + "Total players: " + invitablePlayers.size());
        pageMeta.setLore(pageLore);
        pageInfo.setItemMeta(pageMeta);
        inventory.setItem(46, pageInfo);

        // Fill empty slots
        fillBackground();
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        // Fill navigation area
        for (int i = 36; i < 54; i++) {
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
            new GuildManagementGUI(plugin, clicker).open();
            return;
        }

        if (lastLine.contains("PREV")) {
            currentPage = Math.max(0, currentPage - 1);
            setupGUI();
            return;
        }

        if (lastLine.contains("NEXT")) {
            if ((currentPage + 1) * 36 < invitablePlayers.size()) {
                currentPage++;
                setupGUI();
            }
            return;
        }

        // Check if it's a player UUID
        try {
            String uuidString = lastLine.replace(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC, "");
            UUID targetUUID = UUID.fromString(uuidString);

            // Check if player can be invited (not in a guild)
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
            PlayerData targetData = plugin.getDataManager().getPlayerData(targetUUID, target.getName());

            if (targetData.getCurrentGuild() != null) {
                clicker.sendMessage(ChatColor.RED + "✦ " + target.getName() + " is already in a guild! ✦");
                return;
            }

            if (plugin.getGuildManager().invitePlayer(clicker, target)) {
                clicker.closeInventory();
            }
        } catch (IllegalArgumentException e) {
            // Not a valid UUID, ignore
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