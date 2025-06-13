package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopManager {
    private RavenRpg plugin;
    private Map<UUID, PlayerShop> shops;
    private File shopFile;

    public ShopManager(RavenRpg plugin) {
        this.plugin = plugin;
        this.shops = new HashMap<>();
        this.shopFile = new File(plugin.getDataFolder(), "shops.yml");
        loadShops();
    }

    public boolean createShop(Player player, String shopName, ShopType type) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Check max shops per player
        int maxShops = plugin.getConfig().getInt("shop.max-shops-per-player", 3);
        if (data.getOwnedShops().size() >= maxShops) {
            player.sendMessage("§c✦ You can only own " + maxShops + " shops! ✦");
            return false;
        }

        // Check creation cost
        double creationCost = plugin.getConfig().getDouble("shop.creation-cost", 500.0);
        if (plugin.getEconomy().getBalance(player) < creationCost) {
            player.sendMessage("§c✦ You need $" + creationCost + " to create a shop! ✦");
            return false;
        }

        PlayerShop shop = new PlayerShop(player.getUniqueId(), shopName, player.getLocation(), type);
        shops.put(shop.getShopId(), shop);
        data.getOwnedShops().add(shop.getShopId().toString());

        plugin.getEconomy().withdrawPlayer(player, creationCost);

        player.sendMessage("§a✦ Shop '" + shopName + "' created successfully! ✦");
        player.sendMessage("§6✦ Type: " + type.getDisplayName() + " ✦");

        saveShops();
        plugin.getDataManager().savePlayerData(data);
        return true;
    }

    public PlayerShop getShop(UUID shopId) {
        return shops.get(shopId);
    }

    public List<PlayerShop> getPlayerShops(Player player) {
        List<PlayerShop> playerShops = new ArrayList<>();
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        for (String shopIdString : data.getOwnedShops()) {
            try {
                UUID shopId = UUID.fromString(shopIdString);
                PlayerShop shop = shops.get(shopId);
                if (shop != null) {
                    playerShops.add(shop);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid shop ID for player " + player.getName() + ": " + shopIdString);
            }
        }

        return playerShops;
    }

    public Collection<PlayerShop> getAllShops() {
        return shops.values();
    }

    private void loadShops() {
        // Implementation for loading shops from file
        // Similar to guild loading but for shops
    }

    public void saveShops() {
        // Implementation for saving shops to file
        // Similar to guild saving but for shops
    }

    public void saveAllShops() {
        saveShops();
    }
}