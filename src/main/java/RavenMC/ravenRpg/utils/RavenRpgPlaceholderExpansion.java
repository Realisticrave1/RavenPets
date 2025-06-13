package RavenMC.ravenRpg.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.OfflinePlayer;

public class RavenRpgPlaceholderExpansion extends PlaceholderExpansion {
    private RavenRpg plugin;

    public RavenRpgPlaceholderExpansion(RavenRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "RavenMC";
    }

    @Override
    public String getIdentifier() {
        return "ravenrpg";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline()) return "";

        PlayerData data = plugin.getDataManager().getPlayerData(player.getPlayer());

        switch (params.toLowerCase()) {
            // Race placeholders
            case "race":
                return data.getSelectedRace() != null ? data.getSelectedRace().getDisplayName() : "None";
            case "race_description":
                return data.getSelectedRace() != null ? data.getSelectedRace().getDescription() : "";

            // Bloodline placeholders
            case "bloodline":
                return data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDisplayName() : "None";
            case "bloodline_symbol":
                return data.getSelectedBloodline() != null ? data.getSelectedBloodline().getSymbol() : "";
            case "bloodline_description":
                return data.getSelectedBloodline() != null ? data.getSelectedBloodline().getDescription() : "";

            // Guild placeholders
            case "guild":
                if (data.getCurrentGuild() != null) {
                    Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
                    return guild != null ? guild.getDisplayName() : "None";
                }
                return "None";
            case "guild_type":
                if (data.getCurrentGuild() != null) {
                    Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
                    return guild != null ? guild.getType().getDisplayName() : "";
                }
                return "";
            case "guild_symbol":
                if (data.getCurrentGuild() != null) {
                    Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
                    return guild != null ? guild.getType().getSymbol() : "";
                }
                return "";
            case "guild_members":
                if (data.getCurrentGuild() != null) {
                    Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
                    return guild != null ? String.valueOf(guild.getMembers().size()) : "0";
                }
                return "0";

            // Stat placeholders
            case "strength":
                return String.valueOf(data.getStat("strength"));
            case "agility":
                return String.valueOf(data.getStat("agility"));
            case "intelligence":
                return String.valueOf(data.getStat("intelligence"));
            case "vitality":
                return String.valueOf(data.getStat("vitality"));
            case "luck":
                return String.valueOf(data.getStat("luck"));

            // Skill placeholders
            case "combat_level":
                return String.valueOf(data.getSkill("combat"));
            case "mining_level":
                return String.valueOf(data.getSkill("mining"));
            case "woodcutting_level":
                return String.valueOf(data.getSkill("woodcutting"));
            case "fishing_level":
                return String.valueOf(data.getSkill("fishing"));
            case "crafting_level":
                return String.valueOf(data.getSkill("crafting"));
            case "trading_level":
                return String.valueOf(data.getSkill("trading"));

            // Shop placeholders
            case "shops_owned":
                return String.valueOf(data.getOwnedShops().size());

            // Character status
            case "character_complete":
                return data.isFullyInitialized() ? "true" : "false";
            case "has_race":
                return data.hasChosenRace() ? "true" : "false";
            case "has_bloodline":
                return data.hasChosenBloodline() ? "true" : "false";

            default:
                return null;
        }
    }
}