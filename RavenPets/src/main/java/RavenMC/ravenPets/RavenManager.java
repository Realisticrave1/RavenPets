package RavenMC.ravenPets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class RavenManager {
    private final RavenPets plugin;
    private final Map<UUID, PlayerRaven> playerRavens;

    public RavenManager(RavenPets plugin) {
        this.plugin = plugin;
        this.playerRavens = new HashMap<>();

        // Load all ravens from database/config
        loadAllRavens();
    }

    public void loadAllRavens() {
        // TODO: Load ravens from database/config
    }

    public void saveAllRavens() {
        // TODO: Save ravens to database/config
    }

    public PlayerRaven getRaven(Player player) {
        return getRaven(player.getUniqueId());
    }

    public PlayerRaven getRaven(UUID playerId) {
        return playerRavens.computeIfAbsent(playerId, id -> new PlayerRaven(playerId));
    }

    public void spawnRaven(Player player) {
        PlayerRaven raven = getRaven(player);
        raven.spawn(player.getLocation());
    }

    public void despawnRaven(Player player) {
        PlayerRaven raven = getRaven(player);
        raven.despawn();
    }

    public boolean hasRaven(Player player) {
        return playerRavens.containsKey(player.getUniqueId()) &&
                getRaven(player).isSpawned();
    }
}