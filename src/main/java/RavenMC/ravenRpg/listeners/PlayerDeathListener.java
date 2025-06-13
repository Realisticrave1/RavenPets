package RavenMC.ravenRpg.listeners;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private RavenRpg plugin;

    public PlayerDeathListener(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        if (!data.isFullyInitialized()) return;

        // Apply racial death effects
        switch (data.getSelectedRace()) {
            case VAMPIRE:
                // Vampires have a chance to avoid death penalties
                if (Math.random() < 0.2) { // 20% chance
                    player.sendMessage(ChatColor.DARK_RED + "✦ Your vampiric nature protects you from death! ✦");
                    event.setKeepInventory(true);
                    event.setKeepLevel(true);
                    event.getDrops().clear();
                }
                break;
            case HUMAN:
                // Humans have resilience
                if (Math.random() < 0.1) { // 10% chance
                    player.sendMessage(ChatColor.YELLOW + "✦ Your human resilience saves some items! ✦");
                    // Keep 50% of items
                    int itemsToKeep = event.getDrops().size() / 2;
                    while (event.getDrops().size() > itemsToKeep) {
                        event.getDrops().remove(0);
                    }
                }
                break;
            case ELF:
                // Elves have nature's protection
                if (Math.random() < 0.15) { // 15% chance
                    player.sendMessage(ChatColor.GREEN + "✦ Nature's spirits guide your soul back! ✦");
                    // Reduce experience loss
                    event.setDroppedExp(event.getDroppedExp() / 2);
                }
                break;
            case ORK:
                // Orks have honor in death
                player.sendMessage(ChatColor.RED + "✦ You died with honor! Your strength will be remembered! ✦");
                // No special effect, but a cool message
                break;
        }

        // Guild support
        if (data.getCurrentGuild() != null) {
            notifyGuildOfDeath(player, data);
        }
    }

    private void notifyGuildOfDeath(Player player, PlayerData data) {
        // Notify guild members of the death
        var guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
        if (guild != null) {
            String deathMessage = guild.getType().getSymbol() + " " + ChatColor.RED +
                    "[Guild] " + ChatColor.WHITE + player.getName() + " has fallen in battle!";

            for (java.util.UUID memberUUID : guild.getMembers()) {
                Player member = plugin.getServer().getPlayer(memberUUID);
                if (member != null && member.isOnline() && !member.equals(player)) {
                    member.sendMessage(deathMessage);
                }
            }
        }
    }
}