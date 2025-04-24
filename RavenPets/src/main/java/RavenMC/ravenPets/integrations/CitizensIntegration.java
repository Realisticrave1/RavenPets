package RavenMC.ravenPets.integrations;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.model.Raven;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class CitizensIntegration {

    private final RavenPets plugin;

    public CitizensIntegration(RavenPets plugin) {
        this.plugin = plugin;

        // Register traits with no-argument constructors
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RavenMasterTrait.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RavenTrainerTrait.class));

        // Create NPCs if configured
        if (plugin.getConfigManager().getConfig().getBoolean("citizens.create-npcs", false)) {
            createRavenNPCs();
        }
    }

    private void createRavenNPCs() {
        // Create Raven Master NPC
        if (plugin.getConfigManager().getConfig().getBoolean("citizens.create-raven-master", false)) {
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "§5Raven Master");
            npc.addTrait(RavenMasterTrait.class);

            // Set NPC location from config
            // This would typically use a location from the config
        }

        // Create Raven Trainer NPC
        if (plugin.getConfigManager().getConfig().getBoolean("citizens.create-raven-trainer", false)) {
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "§5Raven Trainer");
            npc.addTrait(RavenTrainerTrait.class);

            // Set NPC location from config
        }
    }

    public static class RavenMasterTrait extends Trait {

        private RavenPets plugin;

        // No-arguments constructor required by Citizens API
        public RavenMasterTrait() {
            super("ravenmaster");
            this.plugin = RavenPets.getInstance();
        }

        @EventHandler
        public void onRightClick(NPCRightClickEvent event) {
            if (event.getNPC() == this.getNPC()) {
                Player player = event.getClicker();

                // Open Raven Master interface
                openRavenMasterInterface(player);
            }
        }

        private void openRavenMasterInterface(Player player) {
            // This would open a GUI for raven upgrades and special items
            player.sendMessage("§5Welcome to the Raven Master!");
            player.sendMessage("§5I can help you upgrade your raven companion.");

            // Check if player has a raven
            Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
            if (raven == null) {
                player.sendMessage("§cYou don't have a raven companion yet. Complete the welcome tutorial to get one!");
                return;
            }

            // Show available upgrades
            player.sendMessage("§5Available upgrades for your " + raven.getTier().getName() + ":");

            // This would show different options based on the raven's tier
            // For demonstration, just show some example upgrades
            player.sendMessage("§5- §dRaven Amulet §7(Tier II)");
            player.sendMessage("§5- §dRaven Emblem §7(Tier III)");
            player.sendMessage("§5- §dRaven Talisman §7(Tier IV)");
            player.sendMessage("§5- §dLegendary Raven Crown §7(Tier V)");
        }
    }

    public static class RavenTrainerTrait extends Trait {

        private RavenPets plugin;

        // No-arguments constructor required by Citizens API
        public RavenTrainerTrait() {
            super("raventrainer");
            this.plugin = RavenPets.getInstance();
        }

        @EventHandler
        public void onRightClick(NPCRightClickEvent event) {
            if (event.getNPC() == this.getNPC()) {
                Player player = event.getClicker();

                // Open Raven Trainer interface
                openRavenTrainerInterface(player);
            }
        }

        private void openRavenTrainerInterface(Player player) {
            // This would open a GUI for raven skills and abilities
            player.sendMessage("§5Welcome to the Raven Trainer!");
            player.sendMessage("§5I can help your raven learn new abilities.");

            // Check if player has a raven
            Raven raven = plugin.getRavenManager().getRavenByPlayer(player.getUniqueId());
            if (raven == null) {
                player.sendMessage("§cYou don't have a raven companion yet. Complete the welcome tutorial to get one!");
                return;
            }

            // Show available abilities
            player.sendMessage("§5Available abilities for your " + raven.getTier().getName() + ":");

            // This would show different options based on the raven's tier
            // For demonstration, just show some example abilities
            player.sendMessage("§5- §dEnhanced Flight §7(500 XP)");
            player.sendMessage("§5- §dResource Detection §7(750 XP)");
            player.sendMessage("§5- §dCombat Support §7(1000 XP)");
            player.sendMessage("§5- §dAdvanced Crafting §7(1500 XP)");
        }
    }
}