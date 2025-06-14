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

public class ShopManagementGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public ShopManagementGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.GREEN + "✦ " + ChatColor.BOLD + "Shop Management" + ChatColor.RESET + ChatColor.GREEN + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        List<PlayerShop> playerShops = plugin.getShopManager().getPlayerShops(player);

        if (playerShops.isEmpty()) {
            // No shops - show creation options
            showShopCreationOptions();
        } else {
            // Has shops - show them
            showPlayerShops(playerShops);
        }

        // Create shop button
        addShopButton(49, Material.NETHER_STAR, "Create New Shop",
                new String[]{"", "§aCreate a new shop", "", "§6Cost: §f$500", "", "§a» Use /shop create <name> «"}, "INFO");

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
        inventory.setItem(53, backBtn);

        // Fill background
        fillBackground();
    }

    private void showShopCreationOptions() {
        // Show shop types
        int slot = 19;
        for (ShopType type : ShopType.values()) {
            ItemStack typeItem = new ItemStack(getMaterialForShopType(type));
            ItemMeta typeMeta = typeItem.getItemMeta();
            typeMeta.setDisplayName(type.getSymbol() + " " + ChatColor.YELLOW + type.getDisplayName());

            List<String> typeLore = new ArrayList<>();
            typeLore.add("");
            typeLore.add(ChatColor.WHITE + "A " + type.getDisplayName().toLowerCase() + " specializing in");
            typeLore.add(ChatColor.WHITE + "various " + type.name().toLowerCase().replace("_", " ") + " items");
            typeLore.add("");
            typeLore.add(ChatColor.GRAY + "Create with: /shop create <name> " + type.name().toLowerCase());

            typeMeta.setLore(typeLore);
            typeItem.setItemMeta(typeMeta);
            inventory.setItem(slot, typeItem);

            slot++;
            if (slot == 26) slot = 28; // Skip to next row
        }
    }

    private void showPlayerShops(List<PlayerShop> shops) {
        int slot = 10;
        for (PlayerShop shop : shops) {
            if (slot >= 44) break; // Don't overflow

            ItemStack shopItem = new ItemStack(getMaterialForShopType(shop.getType()));
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(shop.getType().getSymbol() + " " + ChatColor.GOLD + shop.getShopName());

            List<String> shopLore = new ArrayList<>();
            shopLore.add("");
            shopLore.add(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + shop.getType().getDisplayName());
            shopLore.add(ChatColor.YELLOW + "Status: " + (shop.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Closed"));
            shopLore.add(ChatColor.YELLOW + "Items: " + ChatColor.WHITE + shop.getItems().size());
            shopLore.add(ChatColor.YELLOW + "Earnings: " + ChatColor.GREEN + "$" + String.format("%.2f", shop.getEarnings()));
            shopLore.add("");
            shopLore.add(ChatColor.GRAY + "Created: " + new java.util.Date(shop.getCreatedDate()).toString().substring(0, 10));
            shopLore.add("");
            shopLore.add(ChatColor.AQUA + "» Click to manage shop «");
            shopLore.add("");
            shopLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + shop.getShopId().toString());

            shopMeta.setLore(shopLore);
            shopItem.setItemMeta(shopMeta);
            inventory.setItem(slot, shopItem);

            slot++;
            if (slot == 17) slot = 19; // Skip to next row
            if (slot == 26) slot = 28; // Skip to next row
            if (slot == 35) slot = 37; // Skip to next row
        }
    }

    private void addShopButton(int slot, Material material, String name, String[] loreArray, String action) {
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

    private Material getMaterialForShopType(ShopType type) {
        switch (type) {
            case GENERAL: return Material.CHEST;
            case WEAPONS: return Material.IRON_SWORD;
            case ARMOR: return Material.IRON_CHESTPLATE;
            case POTIONS: return Material.BREWING_STAND;
            case FOOD: return Material.BREAD;
            case MATERIALS: return Material.CRAFTING_TABLE;
            case RARE: return Material.DIAMOND;
            case CUSTOM: return Material.ENDER_CHEST;
            default: return Material.CHEST;
        }
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
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

        } else if (lastLine.contains("INFO")) {
            // Just informational, do nothing

        } else {
            // Check if it's a shop ID (for managing specific shops)
            try {
                String shopId = lastLine.replace(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC, "");
                if (shopId.length() == 36) { // UUID length
                    clicker.closeInventory();
                    clicker.sendMessage(ChatColor.YELLOW + "✦ Shop management features coming soon! ✦");
                    clicker.sendMessage(ChatColor.AQUA + "✦ Use commands to manage your shop for now ✦");
                }
            } catch (Exception e) {
                // Not a shop ID, ignore
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