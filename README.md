# Easyspeak
A lightweight (only around ~55 KB!) chat management plugin designed to give server admins control over player communication without the added headache.

## Features
- Fully customizable chat formatting with [PlaceholderAPI support](https://modrinth.com/plugin/placeholderapi)
- Permission assignable colored chat using [traditional Minecraft color codes](https://minecraft.wiki/w/Formatting_codes)
- Adjustable slow mode duration (in configuration & in-game command) with permission-based bypass
- Ability to enable or disable chat
- Private messaging with ability for players to ignore others.
- Instant chat clearing.
- Chat history (SQLite for single-server, MySQL/MariaDB for Bungee/Velocity networks)
  - Including a context viewer which allows you to see what was being said in chat at the time of the original message.

## Dependencies
- Required Plugins:
  - [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi)
  - [SignedVelocity](https://modrinth.com/plugin/signedvelocity)
- Libraries (downloaded at runtime)
  - [HikariCP](https://mvnrepository.com/artifact/com.zaxxer/HikariCP)
  - [mariadb-java-client](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)
  - [H2](https://mvnrepository.com/artifact/com.h2database/h2)

## Preview
![Formatted chat, simple](https://cdn.modrinth.com/data/cached_images/ea9813935df1e7e932e72ffc8cb4c965e63fdaf1.png)
![Chat history view](https://cdn.modrinth.com/data/cached_images/4582975f08f94e207aae62e25efd7fe2dd1aedaa.png)
![Chat context view](https://cdn.modrinth.com/data/cached_images/c874f4d7619ae301973d477abf170451d2fc690f.png)

## Commands & Permissions
| Command                                                  | Aliases                  | Description                                                                                                                      | Permission                       |
|----------------------------------------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------|
| /easyspeakreload                                         | esreload                 | Reload the Easyspeak configuration file.                                                                                         | easyspeak.reload                 |
| /togglechat                                              | tc                       | Toggle chat on or off.                                                                                                           | easyspeak.togglechat             |
| /slowchat <seconds>                                      | slow                     | Set or disable slow mode in chat.                                                                                                | easyspeak.slowchat               |
| /clearchat                                               | cc                       | Clears the chat instantly.                                                                                                       | easyspeak.clearchat              |
| /chathistory <player> [page]<br>/chathistory -c <messageId> | chist                    | View a player's chat history across the entire database. The `c` tick is to view context of a certain message by its ID number. | easyspeak.chathistory            |
| /pm <player> <message>                                   | m, w, msg, whisper, tell | Sends a message to another player. Will not reach the other player if ignored.                                                   | easyspeak.pm (default: true)     |
| /reply <message>                                         | r                        | Sends a message to the last player the sender had an interaction with.                                                           | easyspeak.reply (default: true)  |
| /ignore <player>                                         | _(none)_                 | Ignores (or unignores) a player.                                                                                                 | easyspeak.ignore (default: true) |

## Other Permissions
| Permission                        | Intended For               |
|-----------------------------------|----------------------------|
| easyspeak.slowchat.bypass         | Admins<br>Staff            |
| easyspeak.clearchat.bypass        | Admins<br>Staff            |
| easyspeak.togglechat.bypass       | Admins<br>Staff            |
| easyspeak.colors (default: false) | Players<br>Players w/ Rank |

## What version is supported?
This is currently built on the 1.21 Paper API version.
I have not implemented multi-version compilation as the things I'm using it for do not need it, but you're welcome to contribute on the GitHub page.

## Contribution
Feel free to contribute at this project's GitHub page, located at [https://github.com/kodalee/easyspeak](https://github.com/kodalee/easyspeak)
