package RavenMC.ravenRpg.models;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class PlayerShop {
    private UUID shopId;
    private UUID owner;
    private String shopName;
    private Location location;
    private ShopType type;
    private Map<ItemStack, ShopItem> items;
    private double earnings;
    private boolean isOpen;
    private long createdDate;
    private Map<String, Object> settings;

    public PlayerShop(UUID owner, String shopName, Location location, ShopType type) {
        this.shopId = UUID.randomUUID();
        this.owner = owner;
        this.shopName = shopName;
        this.location = location;
        this.type = type;
        this.items = new HashMap<>();
        this.earnings = 0.0;
        this.isOpen = true;
        this.createdDate = System.currentTimeMillis();
        this.settings = new HashMap<>();
        initializeSettings();
    }

    private void initializeSettings() {
        settings.put("allowVisitors", true);
        settings.put("autoRestock", false);
        settings.put("taxRate", 0.05);
        settings.put("maxItems", 54);
    }

    // Getters and setters
    public UUID getShopId() { return shopId; }
    public UUID getOwner() { return owner; }
    public String getShopName() { return shopName; }
    public Location getLocation() { return location; }
    public ShopType getType() { return type; }
    public Map<ItemStack, ShopItem> getItems() { return items; }
    public double getEarnings() { return earnings; }
    public boolean isOpen() { return isOpen; }
    public long getCreatedDate() { return createdDate; }
    public Map<String, Object> getSettings() { return settings; }

    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setOpen(boolean open) { this.isOpen = open; }
    public void addEarnings(double amount) { this.earnings += amount; }
}