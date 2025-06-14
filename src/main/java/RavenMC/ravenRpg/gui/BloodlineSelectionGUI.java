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

public class BloodlineSelectionGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public BloodlineSelectionGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27,
                ChatColor.LIGHT_PURPLE + "✦ " + ChatColor.BOLD + "Choose Your Bloodline" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        // Human Stronghold
        addBloodlineItem(10, Material.IRON_SWORD, Bloodline.HUMAN_STRONGHOLD);

        // Ork Warcamp
        addBloodlineItem(12, Material.NETHERITE_SWORD, Bloodline.ORK_WARCAMP);

        // Elven Grove
        addBloodlineItem(14, Material.OAK_LEAVES, Bloodline.ELVEN_GROVE);

        // Vampire Crypt
        addBloodlineItem(16, Material.WITHER_SKELETON_SKULL, Bloodline.VAMPIRE_CRYPT);

        // Back button
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Character Creation");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add(ChatColor.GRAY + "Return to character creation menu");
        backLore.add("");
        backLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "BACK");
        backMeta.setLore(backLore);
        backBtn.setItemMeta(backMeta);
        inventory.setItem(22, backBtn);

        // Fill background
        fillBackground();
    }

    private void addBloodlineItem(int slot, Material material, Bloodline bloodline) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(bloodline.getSymbol() + " " + ChatColor.LIGHT_PURPLE + bloodline.getDisplayName() + " " + bloodline.getSymbol());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.WHITE + bloodline.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Special Abilities:");

        for (String ability : bloodline.getAbilities()) {
            lore.add(ChatColor.WHITE + "  • " + ability);
        }

        lore.add("");
        lore.add(ChatColor.GREEN + "» Click to join this bloodline «");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + bloodline.name());

        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
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
            new CharacterCreationGUI(plugin, clicker).open();
            return;
        }

        // Check for bloodline selection
        for (Bloodline bloodline : Bloodline.values()) {
            if (lastLine.contains(bloodline.name())) {
                plugin.getBloodlineManager().selectBloodline(clicker, bloodline);
                clicker.closeInventory();

                // Character creation complete, show completion message
                PlayerData data = plugin.getDataManager().getPlayerData(clicker);
                if (data.isFullyInitialized()) {
                    // Show completion message after a delay
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        clicker.sendMessage("");
                        clicker.sendMessage(ChatColor.GREEN + "✦ ═══════ " + ChatColor.BOLD + "Character Creation Complete!" + ChatColor.RESET + ChatColor.GREEN + " ═══════ ✦");
                        clicker.sendMessage("");
                        clicker.sendMessage(ChatColor.GOLD + "✦ Welcome to the world of RavenRpg! ✦");
                        clicker.sendMessage(ChatColor.AQUA + "✦ Use /rpg to open your character menu ✦");
                        clicker.sendMessage(ChatColor.YELLOW + "✦ Join a guild with /guild list ✦");
                        clicker.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Create shops with /shop create ✦");
                        clicker.sendMessage("");
                        clicker.sendMessage(ChatColor.GREEN + "✦ ══════════════════════════════════════════ ✦");
                        clicker.sendMessage("");
                    }, 20L);
                }
                return;
            }
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