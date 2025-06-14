package RavenMC.ravenRpg.managers;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.WorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class EventManager {
    private RavenRpg plugin;
    private List<WorldEvent> activeEvents;
    private BukkitTask eventScheduler;
    private Random random;

    public EventManager(RavenRpg plugin) {
        this.plugin = plugin;
        this.activeEvents = new ArrayList<>();
        this.random = new Random();
        startEventScheduler();
    }

    private void startEventScheduler() {
        int frequency = plugin.getConfig().getInt("events.world-events.frequency", 3600);

        eventScheduler = new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getConfig().getBoolean("events.world-events.enabled", true)) {
                    triggerRandomEvent();
                }
            }
        }.runTaskTimer(plugin, 20L * frequency, 20L * frequency);
    }

    private void triggerRandomEvent() {
        // Remove expired events
        activeEvents.removeIf(event -> event.isExpired());

        // Don't start new event if one is already active
        if (!activeEvents.isEmpty()) return;

        WorldEvent.EventType[] eventTypes = WorldEvent.EventType.values();
        WorldEvent.EventType selectedType = eventTypes[random.nextInt(eventTypes.length)];

        WorldEvent event = new WorldEvent(selectedType);
        activeEvents.add(event);

        // Broadcast event start
        Bukkit.broadcastMessage(ChatColor.YELLOW + "✦ ═══════════════════════════════════════ ✦");
        Bukkit.broadcastMessage(event.getStartMessage());
        Bukkit.broadcastMessage(ChatColor.GRAY + "Duration: " + (event.getDuration() / 60) + " minutes");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "✦ ═══════════════════════════════════════ ✦");

        // Schedule event end
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeEvents.contains(event)) {
                    activeEvents.remove(event);
                    Bukkit.broadcastMessage(event.getEndMessage());
                }
            }
        }.runTaskLater(plugin, 20L * event.getDuration());
    }

    public boolean isEventActive(WorldEvent.EventType type) {
        return activeEvents.stream().anyMatch(event -> event.getType() == type);
    }

    public List<WorldEvent> getActiveEvents() {
        return new ArrayList<>(activeEvents);
    }

    public void stopEventScheduler() {
        if (eventScheduler != null) {
            eventScheduler.cancel();
        }
    }
}