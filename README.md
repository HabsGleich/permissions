# Player Permissions

A basic permission system, for simple usage or for learning purposes, use as you like.
<br>
This was my first time using PostgreSQL and Brigadier, if you find any mistakes or ways to improve the code feel free to
create an issue or pull
request.

## Features

- **Basic Group System**
    - Player can only have one group
    - Groups can be given temporarily to a player
    - Groups can be set as default
    - Groups can have a simple prefix and name
- **Language System**
    - You can add custom languages
    - Players can choose a language for their commands
    - Two pre-made languages (English, German)
    - Player selection currently **not** saved, discarded after restart
- **Database**
    - Currently only PostgreSQL is supported
- **Signs**
    - Server owners can place signs on the server where players can view their rank and the expiry time live

## Permissions

Below you can find all permissions and what they are for

- `permissions.command.perms` -> Access to the /perms command
- `permissions.command.perms.lang` -> Access to change language
- `permissions.command.perms.player.group` -> Access to change a players group
- `permissions.command.perms.player.info` -> Access to view information about a player
- `permissions.command.perms.group.create` -> Access to create a new group
- `permissions.command.perms.group.delete` -> Access to delete an existing group
- `permissions.command.perms.group.default` -> Access to make a group the default
- `permissions.command.perms.group.prefix` -> Access to change a groups prefix
- `permissions.command.perms.group.info` -> Access to view information about a group
- `permissions.command.perms.group.permission` -> Access to modify group permissions

## TODO

- [x] Actually implement PermissibleBase for perms to work
- [x] Automatic rank expiry while in-game (currently only on join)
- [x] Re-work permissions for Perms command
- [ ] Make /rank work
- [ ] Signs
- [ ] Unit Tests

## Code Credits

- Paper Test Plugin (https://github.com/PaperMC/paperweight-test-plugin/)
    - Brigadier Spigot implementation setup (PaperBrigadierCommand)
    - Base project setup (build.gradle.kts)

## Code Conventions

This project uses the Google Java Codestyle with **2 spaces**
<br>
If you contribute please make sure your changes are in correct format.

## License

This project is licensed under MIT, feel free to use this in your projects.