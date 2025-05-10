package RavenMC.ravenPets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class RavenCoinManager {
    private final RavenPets plugin;
    private final Map<UUID, Integer> playerCoins;

    public RavenCoinManager(RavenPets plugin) {
        this.plugin = plugin;
        this.playerCoins = new HashMap<>();

        // Load all coins from database/config
        loadAllCoins();
    }

    public void loadAllCoins() {
        // TODO: Load coins from database/config
    }

    public void saveAllCoins() {
        // TODO: Save coins to database/config
    }

    public int getCoins(Player player) {
        return getCoins(player.getUniqueId());
    }

    public int getCoins(UUID playerId) {
        return playerCoins.getOrDefault(playerId, 0);
    }

    public void addCoins(Player player, int amount) {
        addCoins(player.getUniqueId(), amount);
    }

    public void addCoins(UUID playerId, int amount) {
        int currentCoins = getCoins(playerId);
        playerCoins.put(playerId, currentCoins + amount);
    }

    public boolean removeCoins(Player player, int amount) {
        return removeCoins(player.getUniqueId(), amount);
    }

    public boolean removeCoins(UUID playerId, int amount) {
        int currentCoins = getCoins(playerId);

        if (currentCoins < amount) {
            return false;
        }

        playerCoins.put(playerId, currentCoins - amount);
        return true;
    }

    public boolean hasEnoughCoins(Player player, int amount) {
        return getCoins(player) >= amount;
    }
}