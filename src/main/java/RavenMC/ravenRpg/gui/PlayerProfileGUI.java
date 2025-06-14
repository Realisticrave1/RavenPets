package RavenMC.ravenRpg.gui;

import RavenMC.ravenRpg.RavenRpg;
import RavenMC.ravenRpg.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerProfileGUI implements Listener {
    private RavenRpg plugin;
    private Player player;
    private Inventory inventory;
    private boolean isRegistered = false;

    public PlayerProfileGUI(RavenRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + "✦ " + ChatColor.BOLD + "Character Profile" + ChatColor.RESET + ChatColor.DARK_PURPLE + " ✦");
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
    }

    private void setupGUI() {
        PlayerData data = plugin.getDataManager().getPlayerData(player);

        // Main character info
        addCharacterInfo(data);

        // Race information
        if (data.getSelectedRace() != null) {
            addRaceInfo(data.getSelectedRace());
        }

        // Bloodline information
        if (data.getSelectedBloodline() != null) {
            addBloodlineInfo(data.getSelectedBloodline());
        }

        // Guild information
        if (data.getCurrentGuild() != null) {
            addGuildInfo(data);
        }

        // Character statistics
        addCharacterStats(data);

        // Achievement summary
        addAchievements(data);

        // Shop ownership
        addShopInfo(data);

        // Play time and join date
        addPlayTimeInfo(data);

        // Back button
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to RPG Menu");
        List<String> backLore = new ArrayList<>();
        backLore.add("");
        backLore.add(ChatColor.GRAY + "Return to main RPG menu");
        backLore.add("");
        backLore.add(ChatColor.DARK_GRAY + "" + ChatColor.MAGIC + "BACK");
        backMeta.setLore(backLore);
        backBtn.setItemMeta(backMeta);
        inventory.setItem(49, backBtn);

        // Fill background
        fillBackground();
    }

    private void addCharacterInfo(PlayerData data) {
        Material material = getCharacterMaterial(data);
        ItemStack characterItem = new ItemStack(material);
        ItemMeta characterMeta = characterItem.getItemMeta();
        characterMeta.setDisplayName(ChatColor.GOLD + "✦ " + ChatColor.BOLD + player.getName() + ChatColor.RESET + ChatColor.GOLD + " ✦");

        List<String> characterLore = new ArrayList<>();
        characterLore.add("");
        characterLore.add(ChatColor.YELLOW + "Character Information:");
        characterLore.add(ChatColor.WHITE + "  Name: " + ChatColor.AQUA + player.getName());

        if (data.getSelectedRace() != null) {
            characterLore.add(ChatColor.WHITE + "  Race: " + ChatColor.GREEN + data.getSelectedRace().getDisplayName());
        }

        if (data.getSelectedBloodline() != null) {
            characterLore.add(ChatColor.WHITE + "  Bloodline: " + data.getSelectedBloodline().getSymbol() + " " + ChatColor.LIGHT_PURPLE + data.getSelectedBloodline().getDisplayName());
        }

        if (data.getCurrentGuild() != null) {
            Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
            if (guild != null) {
                characterLore.add(ChatColor.WHITE + "  Guild: " + guild.getType().getSymbol() + " " + ChatColor.YELLOW + guild.getDisplayName());

                if (guild.isLeader(player.getUniqueId())) {
                    characterLore.add(ChatColor.WHITE + "  Rank: " + ChatColor.GOLD + "Leader");
                } else if (guild.isOfficer(player.getUniqueId())) {
                    characterLore.add(ChatColor.WHITE + "  Rank: " + ChatColor.BLUE + "Officer");
                } else {
                    characterLore.add(ChatColor.WHITE + "  Rank: " + ChatColor.GRAY + "Member");
                }
            }
        } else {
            characterLore.add(ChatColor.WHITE + "  Guild: " + ChatColor.GRAY + "None");
        }

        characterLore.add("");
        characterLore.add(ChatColor.GOLD + "Character Status: " + getCharacterStatus(data));

        characterMeta.setLore(characterLore);
        characterItem.setItemMeta(characterMeta);
        inventory.setItem(22, characterItem);
    }

    private void addRaceInfo(Race race) {
        ItemStack raceItem = new ItemStack(getRaceMaterial(race));
        ItemMeta raceMeta = raceItem.getItemMeta();
        raceMeta.setDisplayName(ChatColor.GREEN + "✦ " + race.getDisplayName() + " Heritage ✦");

        List<String> raceLore = new ArrayList<>();
        raceLore.add("");
        raceLore.add(ChatColor.WHITE + race.getDescription());
        raceLore.add("");
        raceLore.add(ChatColor.YELLOW + "Racial Traits:");

        // Show stat bonuses
        race.getStatBonuses().forEach((stat, bonus) -> {
            if (bonus != 0) {
                String color = bonus > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "";
                raceLore.add(ChatColor.WHITE + "  " + capitalize(stat) + ": " + color + bonus);
            }
        });

        raceLore.add("");
        raceLore.add(ChatColor.GOLD + "Special Abilities:");
        raceLore.add(ChatColor.GRAY + "  • Racial combat bonuses");
        raceLore.add(ChatColor.GRAY + "  • Unique passive effects");
        raceLore.add(ChatColor.GRAY + "  • Cultural benefits");

        raceMeta.setLore(raceLore);
        raceItem.setItemMeta(raceMeta);
        inventory.setItem(20, raceItem);
    }

    private void addBloodlineInfo(Bloodline bloodline) {
        ItemStack bloodlineItem = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta bloodlineMeta = bloodlineItem.getItemMeta();
        bloodlineMeta.setDisplayName(bloodline.getSymbol() + " " + ChatColor.LIGHT_PURPLE + bloodline.getDisplayName() + " " + bloodline.getSymbol());

        List<String> bloodlineLore = new ArrayList<>();
        bloodlineLore.add("");
        bloodlineLore.add(ChatColor.WHITE + bloodline.getDescription());
        bloodlineLore.add("");
        bloodlineLore.add(ChatColor.YELLOW + "Bloodline Abilities:");

        for (String ability : bloodline.getAbilities()) {
            bloodlineLore.add(ChatColor.LIGHT_PURPLE + "  ✦ " + ChatColor.WHITE + ability);
        }

        bloodlineLore.add("");
        bloodlineLore.add(ChatColor.GOLD + "Heritage Benefits:");
        bloodlineLore.add(ChatColor.GRAY + "  • Access to bloodline quests");
        bloodlineLore.add(ChatColor.GRAY + "  • Special crafting recipes");
        bloodlineLore.add(ChatColor.GRAY + "  • Unique social interactions");

        bloodlineMeta.setLore(bloodlineLore);
        bloodlineItem.setItemMeta(bloodlineMeta);
        inventory.setItem(24, bloodlineItem);
    }

    private void addGuildInfo(PlayerData data) {
        Guild guild = plugin.getGuildManager().getGuild(data.getCurrentGuild());
        if (guild == null) return;

        ItemStack guildItem = new ItemStack(Material.MAGENTA_BANNER);
        ItemMeta guildMeta = guildItem.getItemMeta();
        guildMeta.setDisplayName(guild.getType().getSymbol() + " " + ChatColor.YELLOW + guild.getDisplayName());

        List<String> guildLore = new ArrayList<>();
        guildLore.add("");
        guildLore.add(ChatColor.WHITE + guild.getDescription());
        guildLore.add("");
        guildLore.add(ChatColor.YELLOW + "Guild Information:");
        guildLore.add(ChatColor.WHITE + "  Type: " + guild.getType().getDisplayName());
        guildLore.add(ChatColor.WHITE + "  Level: " + guild.getLevel());
        guildLore.add(ChatColor.WHITE + "  Members: " + guild.getMembers().size());
        guildLore.add(ChatColor.WHITE + "  Treasury: " + ChatColor.GREEN + "$" + String.format("%.2f", guild.getTreasury()));
        guildLore.add("");

        if (guild.isLeader(player.getUniqueId())) {
            guildLore.add(ChatColor.GOLD + "✦ You are the Guild Leader ✦");
        } else if (guild.isOfficer(player.getUniqueId())) {
            guildLore.add(ChatColor.BLUE + "✦ You are a Guild Officer ✦");
        } else {
            guildLore.add(ChatColor.GRAY + "✦ You are a Guild Member ✦");
        }

        guildMeta.setLore(guildLore);
        guildItem.setItemMeta(guildMeta);
        inventory.setItem(37, guildItem);
    }

    private void addCharacterStats(PlayerData data) {
        ItemStack statsItem = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName(ChatColor.AQUA + "✦ Character Statistics ✦");

        List<String> statsLore = new ArrayList<>();
        statsLore.add("");
        statsLore.add(ChatColor.YELLOW + "Core Stats:");
        statsLore.add(ChatColor.WHITE + "  Strength: " + getStatColor(data.getStat("strength")) + data.getStat("strength"));
        statsLore.add(ChatColor.WHITE + "  Agility: " + getStatColor(data.getStat("agility")) + data.getStat("agility"));
        statsLore.add(ChatColor.WHITE + "  Intelligence: " + getStatColor(data.getStat("intelligence")) + data.getStat("intelligence"));
        statsLore.add(ChatColor.WHITE + "  Vitality: " + getStatColor(data.getStat("vitality")) + data.getStat("vitality"));
        statsLore.add(ChatColor.WHITE + "  Luck: " + getStatColor(data.getStat("luck")) + data.getStat("luck"));
        statsLore.add("");
        statsLore.add(ChatColor.YELLOW + "Skill Summary:");

        String[] skills = {"combat", "mining", "woodcutting", "fishing", "crafting", "trading"};
        for (String skill : skills) {
            int level = data.getSkill(skill);
            statsLore.add(ChatColor.WHITE + "  " + capitalize(skill) + ": " + getSkillColor(level) + "Level " + level);
        }

        statsMeta.setLore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inventory.setItem(38, statsItem);
    }

    private void addAchievements(PlayerData data) {
        ItemStack achievementItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta achievementMeta = achievementItem.getItemMeta();
        achievementMeta.setDisplayName(ChatColor.GOLD + "✦ Achievements & Milestones ✦");

        List<String> achievementLore = new ArrayList<>();
        achievementLore.add("");
        achievementLore.add(ChatColor.YELLOW + "Character Achievements:");

        // Check various achievements
        if (data.isFullyInitialized()) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Character Creation Complete");
        }

        if (data.getCurrentGuild() != null) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Guild Member");
        }

        if (!data.getOwnedShops().isEmpty()) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Shop Owner");
        }

        // Skill achievements
        int totalSkillLevels = getTotalSkillLevels(data);
        if (totalSkillLevels >= 60) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Skill Apprentice (60 levels)");
        }
        if (totalSkillLevels >= 150) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Skill Journeyman (150 levels)");
        }
        if (totalSkillLevels >= 300) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Skill Expert (300 levels)");
        }

        // Stat achievements
        int totalStats = getTotalStats(data);
        if (totalStats >= 75) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Stat Specialist (75 points)");
        }
        if (totalStats >= 100) {
            achievementLore.add(ChatColor.GREEN + "  ✓ Stat Expert (100 points)");
        }

        achievementLore.add("");
        achievementLore.add(ChatColor.GRAY + "More achievements coming soon!");

        achievementMeta.setLore(achievementLore);
        achievementItem.setItemMeta(achievementMeta);
        inventory.setItem(39, achievementItem);
    }

    private void addShopInfo(PlayerData data) {
        ItemStack shopItem = new ItemStack(Material.EMERALD);
        ItemMeta shopMeta = shopItem.getItemMeta();
        shopMeta.setDisplayName(ChatColor.GREEN + "✦ Business Ventures ✦");

        List<String> shopLore = new ArrayList<>();
        shopLore.add("");
        shopLore.add(ChatColor.YELLOW + "Shop Ownership:");
        shopLore.add(ChatColor.WHITE + "  Shops Owned: " + ChatColor.GREEN + data.getOwnedShops().size());

        if (data.getOwnedShops().isEmpty()) {
            shopLore.add(ChatColor.GRAY + "  No shops owned yet");
            shopLore.add("");
            shopLore.add(ChatColor.AQUA + "Create your first shop with /shop create!");
        } else {
            shopLore.add("");
            shopLore.add(ChatColor.GOLD + "Business Status:");
            shopLore.add(ChatColor.GREEN + "  ✓ Entrepreneur");

            if (data.getOwnedShops().size() >= 3) {
                shopLore.add(ChatColor.GREEN + "  ✓ Business Mogul");
            }

            shopLore.add("");
            shopLore.add(ChatColor.AQUA + "Use /shop to manage your businesses!");
        }

        shopMeta.setLore(shopLore);
        shopItem.setItemMeta(shopMeta);
        inventory.setItem(40, shopItem);
    }

    private void addPlayTimeInfo(PlayerData data) {
        ItemStack timeItem = new ItemStack(Material.CLOCK);
        ItemMeta timeMeta = timeItem.getItemMeta();
        timeMeta.setDisplayName(ChatColor.YELLOW + "✦ Character Timeline ✦");

        List<String> timeLore = new ArrayList<>();
        timeLore.add("");
        timeLore.add(ChatColor.YELLOW + "Character History:");

        // Join date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        Date joinDate = new Date(data.getJoinDate());
        timeLore.add(ChatColor.WHITE + "  Joined: " + ChatColor.AQUA + dateFormat.format(joinDate));

        // Days since joining
        long daysSinceJoin = (System.currentTimeMillis() - data.getJoinDate()) / (1000 * 60 * 60 * 24);
        timeLore.add(ChatColor.WHITE + "  Days Played: " + ChatColor.GREEN + daysSinceJoin);

        timeLore.add("");
        timeLore.add(ChatColor.YELLOW + "Character Development:");

        // Character completion date
        if (data.isFullyInitialized()) {
            timeLore.add(ChatColor.GREEN + "  ✓ Character creation completed");
        }

        // Current session info
        timeLore.add(ChatColor.WHITE + "  Current Session: " + ChatColor.YELLOW + "Active");
        timeLore.add(ChatColor.WHITE + "  Last Seen: " + ChatColor.GREEN + "Now");

        timeMeta.setLore(timeLore);
        timeItem.setItemMeta(timeMeta);
        inventory.setItem(41, timeItem);
    }

    private Material getCharacterMaterial(PlayerData data) {
        if (data.getSelectedRace() == null) return Material.PLAYER_HEAD;

        switch (data.getSelectedRace()) {
            case HUMAN: return Material.IRON_HELMET;
            case ORK: return Material.NETHERITE_HELMET;
            case ELF: return Material.LEATHER_HELMET;
            case VAMPIRE: return Material.CHAINMAIL_HELMET;
            default: return Material.PLAYER_HEAD;
        }
    }

    private Material getRaceMaterial(Race race) {
        switch (race) {
            case HUMAN: return Material.IRON_INGOT;
            case ORK: return Material.NETHERITE_AXE;
            case ELF: return Material.BOW;
            case VAMPIRE: return Material.REDSTONE;
            default: return Material.STICK;
        }
    }

    private String getCharacterStatus(PlayerData data) {
        if (!data.isFullyInitialized()) {
            return ChatColor.RED + "Incomplete";
        }

        if (data.getCurrentGuild() != null && !data.getOwnedShops().isEmpty()) {
            return ChatColor.GOLD + "Elite Member";
        } else if (data.getCurrentGuild() != null) {
            return ChatColor.GREEN + "Guild Member";
        } else if (!data.getOwnedShops().isEmpty()) {
            return ChatColor.BLUE + "Entrepreneur";
        } else {
            return ChatColor.YELLOW + "Adventurer";
        }
    }

    private String getStatColor(int statValue) {
        if (statValue >= 20) return ChatColor.GOLD + "";
        if (statValue >= 15) return ChatColor.GREEN + "";
        if (statValue >= 10) return ChatColor.YELLOW + "";
        return ChatColor.WHITE + "";
    }

    private String getSkillColor(int level) {
        if (level >= 75) return ChatColor.GOLD + "";
        if (level >= 50) return ChatColor.BLUE + "";
        if (level >= 25) return ChatColor.GREEN + "";
        if (level >= 10) return ChatColor.YELLOW + "";
        return ChatColor.WHITE + "";
    }

    private int getTotalStats(PlayerData data) {
        return data.getStat("strength") + data.getStat("agility") + data.getStat("intelligence") +
                data.getStat("vitality") + data.getStat("luck");
    }

    private int getTotalSkillLevels(PlayerData data) {
        return data.getSkill("combat") + data.getSkill("mining") + data.getSkill("woodcutting") +
                data.getSkill("fishing") + data.getSkill("crafting") + data.getSkill("trading");
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void fillBackground() {
        ItemStack glass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (!meta.hasLore()) return;

        List<String> lore = meta.getLore();
        if (lore.isEmpty()) return;

        String lastLine = lore.get(lore.size() - 1);

        if (lastLine.contains("BACK")) {
            clicker.closeInventory();
            new RpgMainGUI(plugin, clicker).open();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            cleanup();
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void cleanup() {
        if (isRegistered) {
            HandlerList.unregisterAll(this);
            isRegistered = false;
        }
    }
}