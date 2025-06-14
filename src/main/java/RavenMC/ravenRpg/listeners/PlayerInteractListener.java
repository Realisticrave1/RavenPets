package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
    private RavenRpg plugin;

    public PlayerInteractListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // Check for special RPG items
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String itemName = item.getItemMeta().getDisplayName();

            // Guild banner interaction
            if (itemName.contains("Guild Banner") &&
                    (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

                event.setCancelled(true);
                handleGuildBannerUse(player);
            }

            // Shop creation tool
            if (itemName.contains("Shop Creation Tool") &&
                    (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

                event.setCancelled(true);
                handleShopCreation(player, event);
            }

            // Racial ability items
            if (itemName.contains("Racial Ability") &&
                    (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

                event.setCancelled(true);
                handleRacialAbility(player, itemName);
            }
        }
    }

    private void handleGuildBannerUse(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (data.getCurrentGuild() != null) {
            player.sendMessage(ChatColor.GOLD + "✦ " +
                    plugin.getGuildManager().getGuild(data.getCurrentGuild()).getDisplayName() + " ✦");
            player.sendMessage(ChatColor.YELLOW + "✦ Use /guild for guild management ✦");
        } else {
            player.sendMessage(ChatColor.RED + "✦ You are not in a guild! ✦");
            player.sendMessage(ChatColor.AQUA + "✦ Use /guild list to find one to join ✦");
        }
    }

    private void handleShopCreation(Player player, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player);
        int maxShops = plugin.getConfig().getInt("shop.max-shops-per-player", 3);

        if (data.getOwnedShops().size() >= maxShops) {
            player.sendMessage(ChatColor.RED + "✦ You can only own " + maxShops + " shops! ✦");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "✦ Shop location marked! ✦");
        player.sendMessage(ChatColor.YELLOW + "✦ Use /shop create <n> to create your shop here ✦");

        // Store the location for the player (you could implement this)
        // For now, just show the message
    }

    private void handleRacialAbility(Player player, String itemName) {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (data.getSelectedRace() == null) {
            player.sendMessage(ChatColor.RED + "✦ You haven't chosen a race yet! ✦");
            return;
        }

        // Different abilities based on race
        switch (data.getSelectedRace()) {
            case HUMAN:
                // Human leadership ability
                player.sendMessage(ChatColor.YELLOW + "✦ Leadership Aura activated! ✦");
                // Implement actual effect
                break;
            case ORK:
                // Ork battle fury
                player.sendMessage(ChatColor.RED + "✦ Battle Fury activated! ✦");
                // Implement actual effect
                break;
            case ELF:
                // Elf nature magic
                player.sendMessage(ChatColor.GREEN + "✦ Nature Magic activated! ✦");
                // Implement actual effect
                break;
            case VAMPIRE:
                // Vampire blood magic
                player.sendMessage(ChatColor.DARK_RED + "✦ Blood Magic activated! ✦");
                // Implement actual effect
                break;
        }
    }
}