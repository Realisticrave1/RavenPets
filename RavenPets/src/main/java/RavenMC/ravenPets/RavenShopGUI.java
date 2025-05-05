package RavenMC.ravenPets;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RavenShopGUI implements Listener {
    private final RavenPets plugin;
    private final Map<UUID, Long> xpBoostExpiry = new HashMap<>();
    private final Map<UUID, Long> coinBoostExpiry = new HashMap<>();

    public RavenShopGUI(RavenPets plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Schedule task to check for expired boosts every minute
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkExpiredBoosts, 1200L, 1200L);
    }

    public void openShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§5§lRaven Shop");

        // XP Boost
        ItemStack xpBoost = createShopItem(
                Material.EXPERIENCE_BOTTLE,
                "§b§lXP Boost",
                "§7Increases XP gain by 50% for 1 hour",
                "§7Cost: §e" + plugin.getConfig().getInt("shop.xp-boost.cost") + " Raven Coins"
        );
        gui.setItem(10, xpBoost);

        // Coin Boost
        ItemStack coinBoost = createShopItem(
                Material.GOLD_INGOT,
                "§e§lCoin Boost",
                "§7Increases coin gain by 50% for 1 hour",
                "§7Cost: §e" + plugin.getConfig().getInt("shop.coin-boost.cost") + " Raven Coins"
        );
        gui.setItem(12, coinBoost);

        // Custom Colors
        ItemStack customColors = createShopItem(
                Material.PURPLE_DYE,
                "§d§lCustom Raven Colors",
                "§7Unlock custom color options for your raven",
                "§7Cost: §e" + plugin.getConfig().getInt("shop.custom-colors.cost") + " Raven Coins"
        );
        gui.setItem(14, customColors);

        // Custom Particles
        ItemStack customParticles = createShopItem(
                Material.BLAZE_POWDER,
                "§6§lCustom Particles",
                "§7Add particle effects to your raven",
                "§7Cost: §e" + plugin.getConfig().getInt("shop.custom-particles.cost") + " Raven Coins"
        );
        gui.setItem(16, customParticles);

        // Raven Tier Info
        PlayerRaven raven = plugin.getRavenManager().getRaven(player);
        ItemStack tierInfo = createInfoItem(
                Material.BOOK,
                "§5§lYour Raven",
                "§7Tier: §d" + raven.getTier(),
                "§7Level: §d" + raven.getLevel(),
                "§7Element: §d" + raven.getElementType()
        );
        gui.setItem(4, tierInfo);

        // Coin Balance
        int coins = plugin.getCoinManager().getCoins(player);
        ItemStack coinBalance = createInfoItem(
                Material.EMERALD,
                "§e§lRaven Coins",
                "§7Balance: §e" + coins + " coins"
        );
        gui.setItem(22, coinBalance);

        // Fill empty slots with gray glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createShopItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String... lore) {
        return createShopItem(material, name, lore);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals("§5§lRaven Shop")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        int coins = plugin.getCoinManager().getCoins(player);

        switch (event.getSlot()) {
            case 10: // XP Boost
                handleXpBoostPurchase(player, coins);
                break;
            case 12: // Coin Boost
                handleCoinBoostPurchase(player, coins);
                break;
            case 14: // Custom Colors
                handleCustomColorsPurchase(player, coins);
                break;
            case 16: // Custom Particles
                handleCustomParticlesPurchase(player, coins);
                break;
        }

        // Update coin balance display
        Inventory gui = event.getInventory();
        int updatedCoins = plugin.getCoinManager().getCoins(player);
        ItemStack coinBalance = createInfoItem(
                Material.EMERALD,
                "§e§lRaven Coins",
                "§7Balance: §e" + updatedCoins + " coins"
        );
        gui.setItem(22, coinBalance);
    }

    private void handleXpBoostPurchase(Player player, int coins) {
        int cost = plugin.getConfig().getInt("shop.xp-boost.cost");

        if (coins < cost) {
            player.sendMessage("§cYou don't have enough Raven Coins to purchase this!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Check if already active
        if (xpBoostExpiry.containsKey(player.getUniqueId()) &&
                xpBoostExpiry.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage("§cYou already have an active XP boost!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Purchase successful
        plugin.getCoinManager().removeCoins(player, cost);

        // Apply boost
        int duration = plugin.getConfig().getInt("shop.xp-boost.duration");
        xpBoostExpiry.put(player.getUniqueId(), System.currentTimeMillis() + (duration * 1000));

        player.sendMessage("§aYou have purchased an XP boost for 1 hour!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void handleCoinBoostPurchase(Player player, int coins) {
        int cost = plugin.getConfig().getInt("shop.coin-boost.cost");

        if (coins < cost) {
            player.sendMessage("§cYou don't have enough Raven Coins to purchase this!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Check if already active
        if (coinBoostExpiry.containsKey(player.getUniqueId()) &&
                coinBoostExpiry.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage("§cYou already have an active Coin boost!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Purchase successful
        plugin.getCoinManager().removeCoins(player, cost);

        // Apply boost
        int duration = plugin.getConfig().getInt("shop.coin-boost.duration");
        coinBoostExpiry.put(player.getUniqueId(), System.currentTimeMillis() + (duration * 1000));

        player.sendMessage("§aYou have purchased a Coin boost for 1 hour!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void handleCustomColorsPurchase(Player player, int coins) {
        int cost = plugin.getConfig().getInt("shop.custom-colors.cost");

        if (coins < cost) {
            player.sendMessage("§cYou don't have enough Raven Coins to purchase this!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Implementation of custom colors would go here
        // For now, just take the coins
        plugin.getCoinManager().removeCoins(player, cost);

        player.sendMessage("§aYou have purchased custom raven colors!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void handleCustomParticlesPurchase(Player player, int coins) {
        int cost = plugin.getConfig().getInt("shop.custom-particles.cost");

        if (coins < cost) {
            player.sendMessage("§cYou don't have enough Raven Coins to purchase this!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Implementation of custom particles would go here
        // For now, just take the coins
        plugin.getCoinManager().removeCoins(player, cost);

        player.sendMessage("§aYou have purchased custom raven particles!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void checkExpiredBoosts() {
        long currentTime = System.currentTimeMillis();

        // Check XP boosts
        xpBoostExpiry.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTime) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    player.sendMessage("§cYour XP boost has expired!");
                }
                return true;
            }
            return false;
        });

        // Check Coin boosts
        coinBoostExpiry.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTime) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    player.sendMessage("§cYour Coin boost has expired!");
                }
                return true;
            }
            return false;
        });
    }

    public boolean hasXpBoost(Player player) {
        return xpBoostExpiry.containsKey(player.getUniqueId()) &&
                xpBoostExpiry.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public boolean hasCoinBoost(Player player) {
        return coinBoostExpiry.containsKey(player.getUniqueId()) &&
                coinBoostExpiry.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public double getXpMultiplier(Player player) {
        return hasXpBoost(player) ?
                plugin.getConfig().getDouble("shop.xp-boost.multiplier", 1.5) : 1.0;
    }

    public double getCoinMultiplier(Player player) {
        return hasCoinBoost(player) ?
                plugin.getConfig().getDouble("shop.coin-boost.multiplier", 1.5) : 1.0;
    }
}