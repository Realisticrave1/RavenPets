# RavenPets

A feature-rich Minecraft 1.21 plugin that adds customizable elemental raven companions with unique abilities to your server.

![RavenPets Banner](https://via.placeholder.com/800x200?text=RavenPets)

## Overview

RavenPets allows players to own and customize magical ravens with different elemental powers. These ravens follow players around, can be leveled up, and provide special abilities based on their element type.

## Features

### Elemental Ravens
- **9 Element Types**: Fire, Water, Earth, Air, Lightning, Ice, Nature, Darkness, Light
- Each element has unique visuals, particles, and themed abilities
- Each raven can gain experience and level up
- Custom models use new 1.21 BlockDisplay entities

### Progression System
- Level up your raven from 1-100
- Five unique tiers: Novice, Adept, Expert, Master, and Legendary
- Each tier provides stronger abilities and unique benefits
- Experience gained from combat and mining

### Dual Ability System
- Every raven has a primary and secondary ability
- Primary ability provides buffs and enhancements
- Secondary ability has offensive or utility effects
- Abilities scale in power with raven's tier
- Cooldown system prevents ability spam

### Element Abilities

| Element | Primary Ability | Secondary Ability |
|---------|----------------|-------------------|
| Fire | Fire Aura - Grants fire resistance and strength | Fire Wave - Damages nearby enemies in a radius |
| Water | Aquatic Affinity - Grants water breathing and swimming abilities | Tidal Wave - Creates a wave that pushes and slows enemies |
| Earth | Stone Skin - Grants damage resistance and slow falling | Earth Shatter - Creates a shockwave that damages and slows enemies |
| Air | Wind Rush - Grants speed, jump boost, and air-based abilities | Cyclone - Creates a whirlwind that lifts enemies |
| Lightning | Lightning Speed - Grants increased speed and strength | Chain Lightning - Strikes enemies with chaining lightning |
| Ice | Frost Armor - Grants protection from damage and fire | Ice Storm - Creates a freezing storm that damages and slows enemies |
| Nature | Nature's Blessing - Grants regeneration and poison resistance | Entangling Roots - Summons vines that root enemies |
| Darkness | Shadow Cloak - Grants night vision and invisibility | Terror - Applies blindness and weakness to nearby entities |
| Light | Divine Blessing - Grants protection and healing abilities | Radiance - Heals allies and damages undead enemies |

### Economy & Shop System
- Earn and spend Raven Coins
- Purchase boosts, upgrades, and visual enhancements
- Buy XP and coin multipliers for faster progression

### Comprehensive GUI System
- Main dashboard for easy raven management
- Ability information with cooldown display
- Statistics tracking and level progression
- Customization options for visual enhancements

### PlaceholderAPI Support
- Use raven stats in other plugins
- Create dynamic scoreboard and chat displays
- Access all raven data through placeholders

## Commands

### Player Commands
- `/raven` - Opens the main dashboard GUI
- `/raven spawn` - Spawns your raven
- `/raven despawn` - Despawns your raven
- `/raven info` - Shows raven information
- `/raven rename <name>` - Renames your raven
- `/raven core` - Gives you your raven ability core
- `/raven shop` - Opens the raven shop
- `/raven abilities` - View your raven's abilities
- `/raven stats` - View detailed statistics
- `/raven customize` - Customize your raven's appearance
- `/raven coins` - Show your coin balance
- `/raven gui` - Opens the main GUI menu

### Admin Commands
- `/radmin` - Opens the admin dashboard
- `/radmin player <name>` - Manage a player's raven
- `/radmin killall` - Despawns all ravens
- `/radmin reload` - Reloads the configuration
- `/radmin setlevel <player> <level>` - Sets a player's raven level
- `/radmin setelement <player> <element>` - Changes a raven's element
- `/radmin addcoins <player> <amount>` - Gives raven coins to a player
- `/radmin reset <player>` - Resets a player's raven to default
- `/radmin stats` - Shows plugin statistics

## Using Abilities

1. Get your raven core with `/raven core`
2. Right-click to use your primary ability
3. Shift + Right-click to use your secondary ability
4. Wait for cooldowns to reset before using again

The ability effects and duration scale with your raven's tier.

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

- **Required**: Vault, Economy plugin (like EssentialsX)
- **Optional**: PlaceholderAPI (for placeholders)

## Configuration

The plugin is highly configurable through the `config.yml` file:

```yaml
# Database settings
database:
  type: sqlite # or mysql
  # MySQL settings (only needed if database.type is mysql)
  host: localhost
  port: 3306
  database: ravenpets
  username: root
  password: password

# General settings
auto-spawn-on-join: true
follow-player: true
follow-distance: 3.0
follow-teleport-distance: 20.0

# Experience settings
exp-per-mob-kill: 10
exp-per-player-kill: 50
exp-per-block-mine: 1
exp-multiplier: 1.0

# Economy settings
enable-economy: true
coin-per-mob-kill: 1
coin-per-player-kill: 5
coin-multiplier: 1.0

# Ability cooldowns and more...
```

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

## Future Plans

- Custom modeling system for unique raven appearances
- Enhanced particle effects and animations
- Raven combat and PvP system
- Raven breeding and evolution
- Additional elements and hybrid ravens

## Support

If you encounter any issues or have questions:
- Open an issue on GitHub
- Contact us on Discord: [Join RavenMC Discord](https://discord.example.com/raven)

## Credits

- **Plugin Development:** RavenMC Team
- **Concept & Design:** RavenMC Community
- **Special Thanks:** Minecraft 1.21 development team for the new BlockDisplay entities

---

© 2025 RavenMC. All rights reserved.