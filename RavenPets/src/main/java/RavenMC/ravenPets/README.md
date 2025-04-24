# RavenPets

A Minecraft plugin that adds raven companion pets with progression and abilities for your server.

## Overview

RavenPets adds a progression-based companion system to your Minecraft server. Players can obtain a raven companion that grows in power as they level up, evolving through five distinct tiers:

1. **Novice Raven** (Levels 1-10) - A small, purple-tinged bird that follows players and provides basic assistance.
2. **Adept Raven** (Levels 11-25) - Slightly larger with more pronounced purple hues and basic magical abilities.
3. **Expert Raven** (Levels 26-50) - Features distinctive purple feathers and glowing eyes with combat assistance abilities.
4. **Master Raven** (Levels 51-75) - A majestic bird with dark purple feathers and arcane runes capable of powerful magic.
5. **Legendary Raven** (Levels 76-100) - The ultimate evolution with ethereal purple wings and reality-altering abilities.

## Features

- **Progressive Companion System**: Ravens evolve through 5 tiers, gaining new abilities as they level up
- **Custom Abilities**: Each tier unlocks new abilities like inventory storage, flight, resource detection, and more
- **Home Locations**: Ravens can remember and teleport players to saved locations
- **Combat Assistance**: Higher-tier ravens provide combat bonuses and support
- **Crafting Recipes**: Custom crafting recipes for raven upgrades and items
- **Visual Effects**: Distinctive appearances and particle effects for each tier
- **Server Integration**: Works with Citizens, LuckPerms, and PlaceholderAPI

## Installation

1. Ensure your server is running Minecraft 1.20.1 or higher
2. Download the RavenPets.jar file
3. Place the file in your server's `plugins` folder
4. Restart your server
5. Edit the configuration files as needed (found in `plugins/RavenPets/`)

## Dependencies

- **Required**:
    - Spigot/Paper 1.20.1+
- **Optional**:
    - Citizens (for NPC integration)
    - LuckPerms (for permission and meta integration)
    - PlaceholderAPI (for placeholders support)

## Commands

### Player Commands

- `/ravenpet` or `/raven` or `/rp` - Main command
- `/ravenpet help` - Shows help information
- `/ravenpet spawn` - Summons your raven
- `/ravenpet despawn` - Dismisses your raven
- `/ravenpet info` - Shows information about your raven
- `/ravenpet abilities` - Shows your raven's abilities
- `/ravenpet upgrade` - Upgrades your raven (if requirements are met)
- `/ravenpet name <name>` - Renames your raven
- `/ravenpet home add` - Adds a home location
- `/ravenpet home list` - Lists your home locations
- `/ravenpet home tp <id>` - Teleports to a home location

### Admin Commands

- `/ravenpet addxp <player> <amount>` - Adds XP to a player's raven
- `/ravenpet setlevel <player> <level>` - Sets a player's raven level
- `/ravenpet settier <player> <tier>` - Sets a player's raven tier
- `/ravenpet reset <player>` - Resets a player's raven data
- `/ravenpet reload` - Reloads the plugin configuration

## Permissions

- `ravenpets.use` - Allows use of basic raven commands
- `ravenpets.upgrade` - Allows upgrading ravens
- `ravenpets.abilities` - Allows using raven abilities
- `ravenpets.admin` - Grants access to admin commands

## Upgrading Ravens

To upgrade a raven to the next tier, players must:

1. Reach the minimum level requirement for the tier
2. Collect the required amount of Raven XP
3. Complete tier-specific challenges:

### Adept Raven (Tier II) Requirements
- Reach Level 10
- Collect 1000 Raven XP
- Craft a Raven Amulet
- Feed your raven 5 Enchanted Golden Apples

### Expert Raven (Tier III) Requirements
- Reach Level 25
- Collect 5000 Raven XP
- Defeat the Enhanced Ender Dragon
- Craft a Raven Emblem
- Complete 15 daily quests

### Master Raven (Tier IV) Requirements
- Reach Level 50
- Collect 15000 Raven XP
- Complete the Void Citadel dungeon
- Craft a Raven Talisman
- Discover all 8 hidden shrines

### Legendary Raven (Tier V) Requirements
- Reach Level 75
- Collect 50000 Raven XP
- Defeat the Void Lord raid boss
- Craft a Legendary Raven Crown
- Complete all 30 server achievements
- Find all 12 Cosmic Fragments

## PlaceholderAPI Support

RavenPets provides the following placeholders:

- `%ravenpets_name%` - Raven's name
- `%ravenpets_level%` - Raven's level
- `%ravenpets_xp%` - Current Raven XP
- `%ravenpets_xp_required%` - XP required for next level
- `%ravenpets_xp_progress%` - XP progress as a percentage
- `%ravenpets_tier%` - Raven's tier name
- `%ravenpets_tier_roman%` - Raven's tier in Roman numerals
- `%ravenpets_inventory_slots%` - Available inventory slots
- `%ravenpets_flight_duration%` - Flight duration
- `%ravenpets_detection_radius%` - Detection radius
- `%ravenpets_is_active%` - Whether the raven is active
- `%ravenpets_home_count%` - Number of home locations
- `%ravenpets_max_homes%` - Maximum number of home locations
- `%ravenpets_progress_bar%` - A visual progress bar for XP
- `%ravenpets_tier_color%` - Color code for the raven's tier
- `%ravenpets_has_ability_<ability>%` - Whether the raven has a specific ability

## Configuration

The plugin provides extensive configuration options in the following files:

- `config.yml` - General plugin settings
- `messages.yml` - All plugin messages and texts

## Database

RavenPets can use either SQLite (default) or MySQL:

- **SQLite**: Used by default, stores data in `plugins/RavenPets/ravens.db`
- **MySQL**: Can be configured in the config.yml file

## Support and Contact

If you encounter any issues or have suggestions for improvements, please:
1. Check the configuration and permissions
2. Report issues at [GitHub Issues](https://github.com/ravenmc/ravenpets/issues)
3. Contact us on Discord: [RavenMC Discord](https://discord.gg/ravenmc)

## License

RavenPets is licensed under the MIT License. See the LICENSE file for details.