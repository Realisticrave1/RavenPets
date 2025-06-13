package RavenMC.ravenRpg.models;

import org.bukkit.ChatColor;

public class WorldEvent {
    private EventType type;
    private long startTime;
    private int duration; // in seconds

    public enum EventType {
        DOUBLE_EXPERIENCE("Double Experience", 1800,
                ChatColor.GOLD + "✦ Double Experience Event Active! Gain 2x EXP from all activities! ✦",
                ChatColor.YELLOW + "✦ Double Experience Event has ended! ✦"),

        GUILD_WARS("Guild Wars", 3600,
                ChatColor.RED + "✦ Guild Wars have begun! Compete for glory and rewards! ✦",
                ChatColor.RED + "✦ Guild Wars have ended! Check your guild's performance! ✦"),

        MERCHANT_FESTIVAL("Merchant Festival", 2400,
                ChatColor.GREEN + "✦ Merchant Festival! All player shops have 20% discounts! ✦",
                ChatColor.GREEN + "✦ Merchant Festival has ended! Thank you for participating! ✦"),

        RARE_SPAWNS("Rare Creature Activity", 1800,
                ChatColor.LIGHT_PURPLE + "✦ Rare creatures are more active! Higher chance for special drops! ✦",
                ChatColor.LIGHT_PURPLE + "✦ Rare creature activity has returned to normal! ✦");

        private final String name;
        private final int defaultDuration;
        private final String startMessage;
        private final String endMessage;

        EventType(String name, int defaultDuration, String startMessage, String endMessage) {
            this.name = name;
            this.defaultDuration = defaultDuration;
            this.startMessage = startMessage;
            this.endMessage = endMessage;
        }

        public String getName() { return name; }
        public int getDefaultDuration() { return defaultDuration; }
        public String getStartMessage() { return startMessage; }
        public String getEndMessage() { return endMessage; }
    }

    public WorldEvent(EventType type) {
        this.type = type;
        this.startTime = System.currentTimeMillis();
        this.duration = type.getDefaultDuration();
    }

    public EventType getType() { return type; }
    public long getStartTime() { return startTime; }
    public int getDuration() { return duration; }
    public String getStartMessage() { return type.getStartMessage(); }
    public String getEndMessage() { return type.getEndMessage(); }

    public boolean isExpired() {
        return (System.currentTimeMillis() - startTime) > (duration * 1000L);
    }

    public long getTimeRemaining() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = (duration * 1000L) - elapsed;
        return Math.max(0, remaining / 1000L);
    }
}