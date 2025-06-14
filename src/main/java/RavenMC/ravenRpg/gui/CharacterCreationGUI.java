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

public class CharacterCreationGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public CharacterCreationGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27,
                ChatColor.DARK_PURPLE + "✦ " + ChatColor.BOLD + "Character Creation" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Welcome message item
        ItemStack welcomeItem = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta welcomeMeta = welcomeItem.getItemMeta();
        welcomeMeta.setDisplayName(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "Welcome to RavenRpg!" + ChatColor.RESET + ChatColor.GOLD + " ✦");
        List<String> welcomeLore = new ArrayList<>();
        welcomeLore.add("");
        welcomeLore.add(ChatColor.WHITE + "Create your character by choosing:");
        welcomeLore.add(ChatColor.YELLOW + "  1. Your Race (affects stats)");
        welcomeLore.add(ChatColor.YELLOW + "  2. Your Bloodline (affects abilities)");
        welcomeLore.add("");
        welcomeLore.add(ChatColor.GRAY + "Each choice is permanent, so choose wisely!");
        welcomeMeta.setLore(welcomeLore);
        welcomeItem.setItemMeta(welcomeMeta);
        inventory.setItem(13, welcomeItem);

        // Race selection button
        ItemStack raceItem = new ItemStack(data.hasChosenRace() ? Material.EMERALD : Material.DIAMOND);
        ItemMeta raceMeta = raceItem.getItemMeta();
        raceMeta.setDisplayName(ChatColor.AQUA + "✦ Choose Your Race ✦");
        List<String> raceLore = new ArrayList<>();
        raceLore.add("");
        if (data.hasChosenRace()) {
            raceLore.add(ChatColor.GREEN + "✓ Selected: " + data.getSelectedRace().getDisplayName());
            raceLore.add(ChatColor.WHITE + "  " + data.getSelectedRace().getDescription());
        } else {
            raceLore.add(ChatColor.WHITE + "Select from 4 unique races:");
            raceLore.add(ChatColor.YELLOW + "• Human - Balanced and adaptable");
            raceLore.add(ChatColor.YELLOW + "• Ork - Strength and honor");
            raceLore.add(ChatColor.YELLOW + "• Elf - Grace and wisdom");
            raceLore.add(ChatColor.YELLOW + "• Vampire - Dark eternal power");
            raceLore.add("");
            raceLore.add(ChatColor.GREEN + "» Click to choose your race «");
        }
        raceLore.add("");
        raceLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "RACE_SELECTION");
        raceMeta.setLore(raceLore);
        raceItem.setItemMeta(raceMeta);
        inventory.setItem(11, raceItem);

        // Bloodline selection button
        ItemStack bloodlineItem = new ItemStack(data.hasChosenBloodline() ? Material.EMERALD : Material.REDSTONE);
        ItemMeta bloodlineMeta = bloodlineItem.getItemMeta();
        bloodlineMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ Choose Your Bloodline ✦");
        List<String> bloodlineLore = new ArrayList<>();
        bloodlineLore.add("");
        if (data.hasChosenBloodline()) {
            bloodlineLore.add(ChatColor.GREEN + "✓ Selected: " + data.getSelectedBloodline().getDisplayName());
            bloodlineLore.add(ChatColor.WHITE + "  " + data.getSelectedBloodline().getDescription());
        } else if (!data.hasChosenRace()) {
            bloodlineLore.add(ChatColor.RED + "⚠ Choose your race first!");
        } else {
            bloodlineLore.add(ChatColor.WHITE + "Join one of four bloodlines:");
            bloodlineLore.add(ChatColor.YELLOW + "• Human Stronghold");
            bloodlineLore.add(ChatColor.YELLOW + "• Ork Warcamp");
            bloodlineLore.add(ChatColor.YELLOW + "• Elven Grove");
            bloodlineLore.add(ChatColor.YELLOW + "• Vampire Crypt");
            bloodlineLore.add("");
            bloodlineLore.add(ChatColor.GREEN + "» Click to choose your bloodline «");
        }
        bloodlineLore.add("");
        bloodlineLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "BLOODLINE_SELECTION");
        bloodlineMeta.setLore(bloodlineLore);
        bloodlineItem.setItemMeta(bloodlineMeta);
        inventory.setItem(15, bloodlineItem);

        // Complete button (only if both chosen)
        if (data.isFullyInitialized()) {
            ItemStack completeItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta completeMeta = completeItem.getItemMeta();
            completeMeta.setDisplayName(ChatColor.GREEN + "✦ " + ChatColor.BOLD + "Complete Character Creation" + ChatColor.RESET + ChatColor.GREEN + " ✦");
            List<String> completeLore = new ArrayList<>();
            completeLore.add("");
            completeLore.add(ChatColor.WHITE + "Your character is ready!");
            completeLore.add(ChatColor.YELLOW + "Race: " + data.getSelectedRace().getDisplayName());
            completeLore.add(ChatColor.YELLOW + "Bloodline: " + data.getSelectedBloodline().getDisplayName());
            completeLore.add("");
            completeLore.add(ChatColor.GREEN + "» Click to begin your adventure! «");
            completeLore.add("");
            completeLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "COMPLETE");
            completeMeta.setLore(completeLore);
            completeItem.setItemMeta(completeMeta);
            inventory.setItem(22, completeItem);
        }

        // Fill background
        fillBackground();
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
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

        if (lastLine.contains("RACE_SELECTION")) {
            clicker.closeInventory();
            new RaceSelectionGUI(plugin, clicker).open();

        } else if (lastLine.contains("BLOODLINE_SELECTION")) {
            PlayerData data = plugin.getDataManager().getPlayerData(clicker);
            if (!data.hasChosenRace()) {
                clicker.sendMessage(ChatColor.RED + "✦ You must choose a race first! ✦");
                return;
            }
            clicker.closeInventory();
            new BloodlineSelectionGUI(plugin, clicker).open();

        } else if (lastLine.contains("COMPLETE")) {
            clicker.closeInventory();
            clicker.sendMessage("");
            clicker.sendMessage(ChatColor.GREEN + "✦ ═══════ " + ChatColor.BOLD + "Character Creation Complete!" + ChatColor.RESET + ChatColor.GREEN + " ═══════ ✦");
            clicker.sendMessage("");
            clicker.sendMessage(ChatColor.GOLD + "✦ Welcome to the world of RavenRpg! ✦");
            clicker.sendMessage(ChatColor.AQUA + "✦ Use /rpg help to see available commands ✦");
            clicker.sendMessage(ChatColor.YELLOW + "✦ Join a guild with /guild list ✦");
            clicker.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Create shops with /shop create ✦");
            clicker.sendMessage("");
            clicker.sendMessage(ChatColor.GREEN + "✦ ══════════════════════════════════════════ ✦");
            clicker.sendMessage("");
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