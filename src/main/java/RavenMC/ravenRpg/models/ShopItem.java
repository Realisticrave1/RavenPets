package RavenMC.ravenRpg.models;

import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private ItemStack item;
    private double buyPrice;
    private double sellPrice;
    private int stock;
    private int maxStock;
    private boolean canBuy;
    private boolean canSell;

    public ShopItem(ItemStack item, double buyPrice, double sellPrice, int stock) {
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.maxStock = stock;
        this.canBuy = buyPrice > 0;
        this.canSell = sellPrice > 0;
    }

    // Getters and setters
    public ItemStack getItem() { return item; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public int getStock() { return stock; }
    public int getMaxStock() { return maxStock; }
    public boolean canBuy() { return canBuy && stock > 0; }
    public boolean canSell() { return canSell; }

    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; this.canBuy = buyPrice > 0; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; this.canSell = sellPrice > 0; }
    public void setStock(int stock) { this.stock = Math.max(0, stock); }
    public void setMaxStock(int maxStock) { this.maxStock = maxStock; }

    public boolean removeStock(int amount) {
        if (stock >= amount) {
            stock -= amount;
            return true;
        }
        return false;
    }

    public void addStock(int amount) {
        stock = Math.min(maxStock, stock + amount);
    }
}