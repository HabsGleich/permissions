# Player Permissions

A basic permission system, for simple or for learning purposes, use as you like.
<br>
This was my first time using PostgreSQL and Brigadier, if you find any mistakes or ways to improve the code feel free to create an issue or pull
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

## TODO
- [ ] Actually implement PermissibleBase for perms to work
- [ ] Automatic rank expiry while in-game (currently only on join)
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