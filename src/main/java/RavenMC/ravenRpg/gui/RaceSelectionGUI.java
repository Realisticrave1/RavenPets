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

public class RaceSelectionGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public RaceSelectionGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27,
                ChatColor.AQUA + "✦ " + ChatColor.BOLD + "Choose Your Race" + ChatColor.RESET + ChatColor.AQUA + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        // Human
        addRaceItem(10, Material.IRON_INGOT, Race.HUMAN);

        // Ork
        addRaceItem(12, Material.NETHERITE_AXE, Race.ORK);

        // Elf
        addRaceItem(14, Material.BOW, Race.ELF);

        // Vampire
        addRaceItem(16, Material.REDSTONE, Race.VAMPIRE);

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

    private void addRaceItem(int slot, Material material, Race race) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "✦ " + race.getDisplayName() + " ✦");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.WHITE + race.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Stat Bonuses:");

        for (Map.Entry<String, Integer> bonus : race.getStatBonuses().entrySet()) {
            if (bonus.getValue() != 0) {
                String color = bonus.getValue() > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "";
                lore.add(ChatColor.WHITE + "  " + capitalize(bonus.getKey()) + ": " + color + bonus.getValue());
            }
        }

        lore.add("");
        lore.add(ChatColor.GREEN + "» Click to select this race «");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + race.name());

        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
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

        // Check for race selection
        for (Race race : Race.values()) {
            if (lastLine.contains(race.name())) {
                plugin.getRaceManager().selectRace(clicker, race);
                clicker.closeInventory();

                // Open bloodline selection if this was successful
                PlayerData data = plugin.getDataManager().getPlayerData(clicker);
                if (data.hasChosenRace() && !data.hasChosenBloodline()) {
                    new BloodlineSelectionGUI(plugin, clicker).open();
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

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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