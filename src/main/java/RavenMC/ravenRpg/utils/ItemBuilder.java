package RavenMC.ravenRpg.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if (meta != null) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(loreList);
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (meta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        if (meta != null) {
            List<String> existingLore = meta.getLore();
            if (existingLore == null) {
                existingLore = new ArrayList<>();
            }
            for (String line : lore) {
                existingLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(existingLore);
        }
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (meta != null && glow) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }

    // Helper methods for creating specific RPG items
    public static ItemStack createRacialAbilityItem(String raceName) {
        Material material;
        String displayName;
        String[] lore;

        switch (raceName.toUpperCase()) {
            case "HUMAN":
                material = Material.IRON_SWORD;
                displayName = "&6✦ Human Leadership ✦";
                lore = new String[]{
                        "",
                        "&fActivate your natural leadership",
                        "&fabilities to inspire nearby allies.",
                        "",
                        "&7Right-click to use",
                        "&7Cooldown: 5 minutes"
                };
                break;
            case "ORK":
                material = Material.NETHERITE_AXE;
                displayName = "&c✦ Ork Battle Fury ✦";
                lore = new String[]{
                        "",
                        "&fEnter a berserker rage,",
                        "&fincreasing damage and speed.",
                        "",
                        "&7Right-click to use",
                        "&7Cooldown: 3 minutes"
                };
                break;
            case "ELF":
                material = Material.BOW;
                displayName = "&a✦ Elven Nature Magic ✦";
                lore = new String[]{
                        "",
                        "&fChannel the power of nature",
                        "&fto heal and protect yourself.",
                        "",
                        "&7Right-click to use",
                        "&7Cooldown: 4 minutes"
                };
                break;
            case "VAMPIRE":
                material = Material.REDSTONE;
                displayName = "&5✦ Vampire Blood Magic ✦";
                lore = new String[]{
                        "",
                        "&fDrain life from nearby enemies",
                        "&fto restore your own health.",
                        "",
                        "&7Right-click to use",
                        "&7Cooldown: 6 minutes"
                };
                break;
            default:
                material = Material.STICK;
                displayName = "&7✦ Unknown Ability ✦";
                lore = new String[]{"", "&7This ability is not yet available."};
        }

        return new ItemBuilder(material)
                .setName(displayName)
                .setLore(lore)
                .setGlow(true)
                .build();
    }

    public static ItemStack createGuildBanner(String guildName, String guildType) {
        return new ItemBuilder(Material.MAGENTA_BANNER)
                .setName("&6✦ " + guildName + " Guild Banner ✦")
                .setLore(
                        "",
                        "&fType: &e" + guildType,
                        "",
                        "&aRight-click to view guild info",
                        "&7Use /guild for management"
                )
                .setGlow(true)
                .build();
    }

    public static ItemStack createShopCreationTool() {
        return new ItemBuilder(Material.EMERALD)
                .setName("&a✦ Shop Creation Tool ✦")
                .setLore(
                        "",
                        "&fRight-click on a block to mark",
                        "&fa location for your new shop.",
                        "",
                        "&7Then use /shop create <n>"
                )
                .setGlow(true)
                .build();
    }
}