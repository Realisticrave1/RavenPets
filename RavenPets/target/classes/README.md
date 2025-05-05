# RavenPets

A feature-rich Minecraft plugin that adds customizable elemental raven companions with unique abilities to your server.

![RavenPets Logo](https://via.placeholder.com/150?text=RavenPets)

## Server Information
- **Server IP:** mc.ravenmc.online
- **Website:** www.ravenmc.online
- **Created by:** Realisticrave, Owner/Founder of RavenMC

## Overview

RavenPets allows players to own and customize magical ravens with different elemental powers. These ravens follow players around, can be leveled up, and provide special abilities based on their element type.

## Features

- **Elemental Ravens:** Choose from 9 element types (Fire, Water, Earth, Air, Lightning, Ice, Nature, Darkness, Light)
- **Progression System:** Level up your raven from Novice to Legendary tier
- **Dual Abilities:** Each raven has both primary and secondary abilities
- **Custom Visuals:** Unique particles and effects based on element type
- **Economy System:** Earn and spend Raven Coins for upgrades
- **Shop System:** Purchase boosts and visual enhancements
- **PlaceholderAPI Support:** Use raven stats in other plugins

## Commands

### Player Commands
- `/raven` - Opens the main dashboard GUI
- `/raven spawn` - Spawns your raven
- `/raven despawn` - Despawns your raven
- `/raven info` - Shows raven information
- `/raven rename <name>` - Renames your raven
- `/raven core` - Gives you your raven ability core
- `/raven shop` - Opens the raven shop

### Admin Commands
- `/radmin` - Opens the admin dashboard
- `/radmin player <name>` - Manage a player's raven
- `/radmin killall` - Despawns all ravens
- `/radmin reload` - Reloads the configuration
- `/radmin setlevel <player> <level>` - Sets a player's raven level
- `/radmin setelement <player> <element>` - Changes a raven's element
- `/radmin addcoins <player> <amount>` - Gives raven coins to a player
- `/radmin reset <player>` - Resets a player's raven to default

## Using Abilities

1. Get your raven core with `/raven core`
2. Right-click to use your primary ability
3. Shift + Right-click to use your secondary ability
4. Wait for cooldowns to reset before using again

## Permissions

- `ravenpets.use` - Basic access to ravens (default: true)
- `ravenpets.admin` - Admin commands access (default: op)
- `ravenpets.colornames` - Use color codes in raven names (default: op)
- `ravenpets.bypasscooldown` - Bypass ability cooldowns (default: op)
- `ravenpets.team` - Create raven teams that don't damage each other (default: op)

## Installation

1. Download the RavenPets.jar file
2. Place it in your server's plugins folder
3. Restart your server
4. Configure the plugin in the config.yml file
5. Make sure you have Vault and an economy plugin installed

## Dependencies

- Required: Vault, Economy plugin (like EssentialsX)
- Optional: PlaceholderAPI (for placeholders)

## PlaceholderAPI Variables

The following placeholders are available when PlaceholderAPI is installed:

| Placeholder | Description |
|-------------|-------------|
| `%ravenpets_name%` | Raven's name |
| `%ravenpets_level%` | Current raven level |
| `%ravenpets_tier%` | Current tier (NOVICE, ADEPT, etc.) |
| `%ravenpets_tier_formatted%` | Formatted tier with colors |
| `%ravenpets_element%` | Element type (FIRE, WATER, etc.) |
| `%ravenpets_element_formatted%` | Formatted element with colors |
| `%ravenpets_experience%` | Current experience points |
| `%ravenpets_experience_required%` | Experience needed for next level |
| `%ravenpets_experience_percentage%` | Percentage to next level |
| `%ravenpets_experience_bar%` | Visual XP progress bar |
| `%ravenpets_is_spawned%` | Whether raven is spawned (true/false) |
| `%ravenpets_is_spawned_formatted%` | Formatted spawned status (Yes/No with colors) |
| `%ravenpets_coins%` | Player's raven coins |
| `%ravenpets_ability_name%` | Name of primary ability |
| `%ravenpets_secondary_ability_name%` | Name of secondary ability |
| `%ravenpets_ability_cooldown%` | Remaining cooldown for primary ability |
| `%ravenpets_secondary_cooldown%` | Remaining cooldown for secondary ability |
| `%ravenpets_has_custom_colors%` | Whether player has custom colors (true/false) |
| `%ravenpets_has_custom_particles%` | Whether player has custom particles (true/false) |

## Support

If you encounter any issues or have questions, please visit our server or website:
- Server: mc.ravenmc.online
- Website: www.ravenmc.online

## Credits

- **Plugin Development:** Realisticrave
- **Plugin Testing:** RavenMC Community

---

© 2025 RavenMC. All rights reserved.