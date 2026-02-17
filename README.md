<p align="center">
  <img width="256" height="256" alt="NanoGuard Logo" src="https://github.com/user-attachments/assets/9ea95b77-0f19-429c-83a3-748947c8cb99" />
</p>

# 🛡️ NanoGuard

NanoGuard is a lightweight and simple Anti-VPN plugin for your Minecraft Network, designed to block players using proxies, VPNs, or Tor.

## ✨ Features

- **Cross-Platform:** Works seamlessly with both PaperMC and Velocity.
- **VPN/Proxy Detection:** Utilizes the [proxycheck.io](https://proxycheck.io) API to effectively identify and block malicious connections.
- **Database Integration:** Logs all detected players and their IP addresses to a MySQL database for tracking and review.
- **Whitelist System:** Easily whitelist players by username, UUID, or IP address to bypass checks.
- **Configurable Notifications:** In-game alerts for staff when a player is blocked, with detailed information available on hover.
- **Customizable Messages:** Tailor all player-facing messages, including the kick screen, to match your server's theme.
- **Discord Integration:** Optional Discord webhook support to send notifications to a designated channel.

## Compatibility

- **PaperSpigot**: 1.21.1 and newer
- **Velocity**: 3.5.0 and newer

## ⚙️ Installation

1.  Download the appropriate JAR file for your server platform (PaperMC or Velocity) from the releases page.
2.  Place the `NanoGuard-*.jar` file into the `plugins/` directory of your server.
3.  Restart your server. This will generate the default configuration files in the `plugins/NanoGuard/` directory.
4.  Open `config.json` and configure the plugin to your liking, especially the `database` section and your `api_key`.

## 🔧 Configuration

The main configuration is located in `plugins/NanoGuard/config.json`.

```json
{
  "prefix": "<b><white>Nano<red>Guard</red></white></b> <gray>→</gray> ",
  "notifyEnabled": true,
  "restServer": {
    "enabled": true,
    "port": 7922
  },
  "api_key": "",
  "database": {
    "enabled": true,
    "host": "localhost",
    "port": 3306,
    "database": "nanoguard",
    "username": "root",
    "password": "password"
  },
  "messages": {
    "no_permissions": "You do not have permission to use this command.",
    "kick_message": "\n<red>You have been kicked for using a VPN or proxy.</red>\n<gray>If you believe this is a mistake, please contact the server staff.</gray>\n",
    "notify_message": "%prefix% <b><red>%player%</red></b> <gray> was detected using a proxy or a vpn! <i>Hover for more information.</i></gray>",
    "notify_enabled": "<#aaaaaa>Notifications are <green>enabled</green></#aaaaaa>",
    "notify_disabled": "<#aaaaaa>Notifications are <red>disabled</red></#aaaaaa>",
    "reloaded": "<gray>The config was <#aaaaaa>successfully</#aaaaaa> reloaded!</gray>"
  },
  "permissions": {
    "bypass": "nanoguard.bypass",
    "notify": "nanoguard.notify",
    "command": "nanoguard.command"
  },
  "discord": {
    "webhook_enabled": false,
    "webhook_url": "https://discord.com/api/webhooks/your_webhook_url"
  }
}
```

**Key Settings:**
- **`api_key`**: Your API key from [proxycheck.io](https://proxycheck.io). While the plugin can work without it, using a key provides better performance and reliability.
- **`database`**: Configure your MySQL database credentials here. Set `enabled` to `false` if you don't want to use database logging.
- **`messages`**: Customize all user-facing messages using MiniMessage format.
- **`permissions`**: Change the permission nodes used by the plugin.

## <caption> Commands & Permissions

The following commands are available for managing NanoGuard.

| Command                               | Description                                         | Permission              |
| ------------------------------------- | --------------------------------------------------- | ----------------------- |
| `/nanoguard`                          | Displays basic plugin information and commands.     | `nanoguard.command`     |
| `/nanoguard notify`                   | Toggles VPN detection notifications for yourself.   | `nanoguard.command`     |
| `/nanoguard blocked`                  | Lists all IPs flagged and stored in the database.   | `nanoguard.command`     |
| `/nanoguard whitelist list`           | Shows all whitelisted IPs, UUIDs, and names.        | `nanoguard.command`     |
| `/nanoguard whitelist add <value>`    | Adds a player (by name, UUID, or IP) to the whitelist. | `nanoguard.command`     |
| `/nanoguard whitelist remove <value>` | Removes a player from the whitelist.                | `nanoguard.command`     |
| `/nanoguard reload`                   | Reloads the plugin's configuration.                 | `nanoguard.command`     |

### Permissions

- **`nanoguard.command`**: Grants access to all `/nanoguard` subcommands.
- **`nanoguard.notify`**: Allows a player to receive notifications when a user is blocked.
- **`nanoguard.bypass`**: Exempts a player from all VPN checks.
