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

public class GuildMemberManagementGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;
    private String action; // "promote" or "kick"

    public GuildMemberManagementGUI(RavenRpg plugin, Player player, String action) {
        this.plugin = plugin;
        this.player = player;
        this.action = action;

        String title = action.equals("promote") ? "Promote Member" : "Kick Member";
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.GOLD + "✦ " + ChatColor.BOLD + title + ChatColor.RESET + ChatColor.GOLD + " ✦");

        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            return;
        }

        int slot = 10;
        for (UUID memberUUID : guild.getMembers()) {
            if (slot >= 44) break; // Don't overflow

            // Skip self
            if (memberUUID.equals(player.getUniqueId())) continue;

            // For promote: skip if already officer or leader
            if (action.equals("promote") && (guild.isOfficer(memberUUID) || guild.isLeader(memberUUID))) {
                continue;
            }

            // For kick: leaders can kick anyone except themselves, officers can kick regular members
            if (action.equals("kick")) {
                boolean canKick = false;
                if (guild.isLeader(player.getUniqueId())) {
                    canKick = !guild.isLeader(memberUUID); // Can't kick other leaders (shouldn't happen anyway)
                } else if (guild.isOfficer(player.getUniqueId())) {
                    canKick = !guild.isOfficer(memberUUID) && !guild.isLeader(memberUUID);
                }
                if (!canKick) continue;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            boolean isOnline = offlinePlayer.isOnline();

            ItemStack memberItem = new ItemStack(isOnline ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
            ItemMeta memberMeta = memberItem.getItemMeta();

            String statusColor = isOnline ? ChatColor.GREEN + "●" : ChatColor.RED + "●";
            memberMeta.setDisplayName(statusColor + " " + ChatColor.YELLOW + playerName);

            List<String> memberLore = new ArrayList<>();
            memberLore.add("");
            memberLore.add(ChatColor.WHITE + "Status: " + (isOnline ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline"));

            if (guild.isOfficer(memberUUID)) {
                memberLore.add(ChatColor.GOLD + "Rank: Officer");
            } else {
                memberLore.add(ChatColor.GRAY + "Rank: Member");
            }

            memberLore.add("");

            if (action.equals("promote")) {
                memberLore.add(ChatColor.GREEN + "» Click to promote to Officer «");
            } else {
                memberLore.add(ChatColor.RED + "» Click to kick from guild «");
            }

            memberLore.add("");
            memberLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + memberUUID.toString());

            memberMeta.setLore(memberLore);
            memberItem.setItemMeta(memberMeta);
            inventory.setItem(slot, memberItem);

            slot++;
            if (slot == 17) slot = 19; // Skip to next row
            if (slot == 26) slot = 28; // Skip to next row
            if (slot == 35) slot = 37; // Skip to next row
        }

        // Back button
        ItemStack backBtn = new ItemStack(Material.ARROW);
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

        // Fill background
        fillBackground();
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
            new GuildManagementGUI(plugin, clicker).open();
            return;
        }

        // Check if it's a member UUID
        try {
            String uuidString = lastLine.replace(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC, "");
            UUID memberUUID = UUID.fromString(uuidString);

            Guild guild = plugin.getGuildManager().getPlayerGuild(clicker);
            if (guild == null) return;

            if (action.equals("promote")) {
                if (plugin.getGuildManager().promoteMember(clicker, memberUUID)) {
                    clicker.closeInventory();
                } else {
                    // Refresh GUI to update display
                    setupGUI();
                }
            } else if (action.equals("kick")) {
                if (plugin.getGuildManager().kickMember(clicker, memberUUID)) {
                    clicker.closeInventory();
                } else {
                    // Refresh GUI to update display
                    setupGUI();
                }
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