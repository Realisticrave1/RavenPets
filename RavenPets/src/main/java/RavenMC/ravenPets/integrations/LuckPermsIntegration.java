package RavenMC.ravenPets.integrations;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.entity.Player;

public class LuckPermsIntegration {

    private final RavenPets plugin;
    private final LuckPerms luckPerms;

    public LuckPermsIntegration(RavenPets plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
    }

    /**
     * Set a player's raven tier as a meta value in LuckPerms
     *
     * @param player The player to set the meta for
     * @param tier The raven tier
     */
    public void setRavenTierMeta(Player player, RavenTier tier) {
        // Get the user
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }

        // Create a meta node
        MetaNode node = MetaNode.builder("ravenpets.tier", tier.name())
                .build();

        // Clear existing nodes with the same key
        user.data().clear(node1 ->
                node1.getKey().equals("meta") &&
                        node1 instanceof MetaNode &&
                        ((MetaNode) node1).getMetaKey().equals("ravenpets.tier")
        );

        // Add the new meta node
        user.data().add(node);

        // Save the user
        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Set a player's raven level as a meta value in LuckPerms
     *
     * @param player The player to set the meta for
     * @param level The raven level
     */
    public void setRavenLevelMeta(Player player, int level) {
        // Get the user
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }

        // Create a meta node
        MetaNode node = MetaNode.builder("ravenpets.level", String.valueOf(level))
                .build();

        // Clear existing nodes with the same key
        user.data().clear(node1 ->
                node1.getKey().equals("meta") &&
                        node1 instanceof MetaNode &&
                        ((MetaNode) node1).getMetaKey().equals("ravenpets.level")
        );

        // Add the new meta node
        user.data().add(node);

        // Save the user
        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Update all metadata for a player's raven
     *
     * @param player The player to update metadata for
     */
    public void updateRavenMetadata(Player player) {
        Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
        if (raven == null) {
            return;
        }

        setRavenTierMeta(player, raven.getTier());
        setRavenLevelMeta(player, raven.getLevel());

        // Set legendary status for special perks
        if (raven.getTier() == RavenTier.LEGENDARY) {
            setLegendaryStatus(player, true);
        } else {
            setLegendaryStatus(player, false);
        }
    }

    /**
     * Set a player's legendary status for special perks
     *
     * @param player The player to set the status for
     * @param isLegendary Whether the player has a legendary raven
     */
    public void setLegendaryStatus(Player player, boolean isLegendary) {
        // Get the user
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }

        // Create a meta node
        MetaNode node = MetaNode.builder("ravenpets.legendary", String.valueOf(isLegendary))
                .build();

        // Clear existing nodes with the same key
        user.data().clear(node1 ->
                node1.getKey().equals("meta") &&
                        node1 instanceof MetaNode &&
                        ((MetaNode) node1).getMetaKey().equals("ravenpets.legendary")
        );

        // Add the new meta node
        user.data().add(node);

        // Save the user
        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Add server title for legendary raven owners
     *
     * @param player The player to set the title for
     * @param title The title to set
     */
    public void setRavenTitle(Player player, String title) {
        // Get the user
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }

        // Create a meta node for prefix (title)
        MetaNode prefixNode = MetaNode.builder("prefix", "§5[" + title + "] ")
                .build();

        // Clear existing prefix nodes
        user.data().clear(node1 ->
                node1.getKey().equals("meta") &&
                        node1 instanceof MetaNode &&
                        ((MetaNode) node1).getMetaKey().equals("prefix") &&
                        ((MetaNode) node1).getMetaValue().startsWith("§5[")
        );

        // Add the new prefix node
        user.data().add(prefixNode);

        // Save the user
        luckPerms.getUserManager().saveUser(user);
    }
}