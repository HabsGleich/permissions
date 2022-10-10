package de.lennox.permissions.command.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.command.Command;
import de.lennox.permissions.command.TimeInputFormatter;
import de.lennox.permissions.database.PermissionDriver;
import de.lennox.permissions.database.model.PermissionGroup;
import de.lennox.permissions.database.model.PermittedPlayer;
import de.lennox.permissions.player.PermittedPlayerRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.StringArgumentType.*;

/**
 * The /perms brigadier command implementation
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PermsBrigadierCommand extends Command {
  public PermsBrigadierCommand() {
    super("perms");
  }

  /**
   * Creates the main brigadier permissions command and attaches its sub commands
   *
   * @return The command
   * @since 1.0.0
   */
  @Override
  public Consumer<LiteralArgumentBuilder<CommandSourceStack>> createBrigadierLiteral() {
    return literal ->
        literal
            .requires(stack -> stack.getBukkitSender().hasPermission("permissions.command.perms"))
            .then(createLanguageSubCommand())
            .then(createUserSubCommands())
            .then(createGroupSubCommands());
  }

  /**
   * Creates the language selection sub command
   *
   * <p>Additionally handles auto-completion for language types by adding all cached languages
   *
   * @return The language sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createLanguageSubCommand() {
    return literal("language")
        .then(
            argument("lang", string())
                .requires(
                    stack ->
                        stack.getBukkitSender().hasPermission("permissions.command.perms.lang"))
                .suggests(
                    (ctx, builder) -> {
                      for (String language :
                          PlayerPermissionPlugin.getSingleton()
                              .getLocalization()
                              .getLocaleCache()
                              .keySet()) {
                        builder.suggest(language);
                      }
                      return builder.buildFuture();
                    })
                .executes(
                    context -> {
                      CommandSender sender = context.getSource().getBukkitSender();
                      String lang = context.getArgument("lang", String.class);

                      // Only process player execution
                      if (sender instanceof Player player) {
                        UUID uuid = player.getUniqueId();
                        PlayerPermissionPlugin.getSingleton()
                            .getPlayerLanguageRepository()
                            .store(uuid, lang);
                        sender.sendMessage(
                            Component.text(
                                getLocalizedMessage(uuid, "command.perms.language.success"),
                                NamedTextColor.AQUA));
                      }
                      return 1;
                    }));
  }

  // region user sub commands
  /**
   * Creates the user sub commands which contains functionality for modifying users
   *
   * @return The user sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createUserSubCommands() {
    return literal("player")
        .then(
            argument("name", string())
                .then(createUserGroupCommand())
                .then(createUserInfoSubCommand()));
  }

  // region user info sub command
  /**
   * Creates the user group sub-command which is used to set the group of a player
   *
   * @return The user info sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createUserGroupCommand() {
    return literal("group")
        .requires(
            stack ->
                stack.getBukkitSender().hasPermission("permissions.command.perms.player.group"))
        .then(
            literal("set")
                .then(
                    argument("groupName", word())
                        .then(
                            argument("expiresIn", greedyString())
                                .executes(
                                    context -> {
                                      updateUserGroup(
                                          context,
                                          System.currentTimeMillis()
                                              + TimeInputFormatter.parseTimeInput(
                                                  context.getArgument("expiresIn", String.class)));
                                      return 1;
                                    }))
                        .executes(
                            context -> {
                              updateUserGroup(context, -1);
                              return 1;
                            })));
  }

  /**
   * Internally updates a players permission group for the given time
   *
   * @param context The command execution context
   * @param time The run-time of the rank
   * @since 1.0.0
   */
  private void updateUserGroup(CommandContext<CommandSourceStack> context, long time) {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    PermittedPlayerRepository playerRepository = permissions.getPlayerRepository();
    CommandSender sender = context.getSource().getBukkitSender();
    String name = context.getArgument("name", String.class);
    String groupName = context.getArgument("groupName", String.class);
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
    UUID playerId = offlinePlayer.getUniqueId();

    if (sender instanceof Player player) {
      UUID uuid = player.getUniqueId();
      permissions
          .getPermissionDriver()
          .queryPlayerById(playerId)
          .whenCompleteAsync(
              (optionalPlayer, t) -> {
                // Notify sender that the player could not be found
                if (optionalPlayer.isEmpty()) {
                  sender.sendMessage(
                      Component.text(
                          getLocalizedMessage(uuid, "command.perms.player_not_found"),
                          NamedTextColor.RED));
                  return;
                }

                permissions.getPermissionDriver().updatePlayerGroup(playerId, groupName, time);
                // Update cache if player is online
                if (offlinePlayer.isOnline()) {
                  playerRepository
                      .getPermittedPlayer(playerId)
                      .whenComplete(
                          (permittedPlayer, throwable) -> {
                            permittedPlayer.setGroup(groupName);
                            permittedPlayer.setExpiresAt(time);
                          });
                }
                sender.sendMessage(
                    Component.text(
                        getLocalizedMessage(uuid, "command.perms.set_group.success"),
                        NamedTextColor.AQUA));
              });
    }
  }
  // endregion

  // region user info sub command
  /**
   * Creates the user info sub-command which sends the executor information about the supplied user
   *
   * @return The user info sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createUserInfoSubCommand() {
    return literal("info")
        .requires(
            stack -> stack.getBukkitSender().hasPermission("permissions.command.perms.player.info"))
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String name = context.getArgument("name", String.class);

              if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                PlayerPermissionPlugin.getSingleton()
                    .getPermissionDriver()
                    .queryPlayerById(Bukkit.getOfflinePlayer(name).getUniqueId())
                    .whenCompleteAsync(
                        (optionalPlayer, t) -> {
                          // Notify player that the requested player could not be found
                          if (optionalPlayer.isEmpty()) {
                            sender.sendMessage(
                                Component.text(
                                    getLocalizedMessage(uuid, "command.perms.player_not_found"),
                                    NamedTextColor.RED));
                            return;
                          }

                          PermittedPlayer permittedPlayer = optionalPlayer.get();
                          sender.sendMessage(permittedPlayer.parseInfoComponent(player, name));
                        });
              }

              return 1;
            });
  }
  // endregion
  // endregion

  // region group sub commands
  /**
   * Creates the group sub command which contains functionality for modifying groups
   *
   * <p>Additionally handles auto-completion for group names by retrieving all group names from the
   * group cache.
   *
   * @return The group sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupSubCommands() {
    return literal("group")
        .then(
            argument("name", word())
                .suggests(
                    (ctx, builder) -> {
                      for (String groupName :
                          PlayerPermissionPlugin.getSingleton()
                              .getGroupRepository()
                              .getCachedGroups()
                              .keySet()) {
                        builder.suggest(groupName);
                      }
                      return builder.buildFuture();
                    })
                .then(createGroupCreateSubCommand())
                .then(createGroupDeleteSubCommand())
                .then(createGroupDefaultSubCommand())
                .then(createGroupInfoSubCommand())
                .then(createGroupPrefixSubCommand())
                .then(createGroupPermissionsSubCommand()));
  }

  // region group create sub command
  /**
   * Creates the group create sub-command which is used to create a new group
   *
   * @return The group create sub-command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupCreateSubCommand() {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    return literal("create")
        .requires(
            stack ->
                stack.getBukkitSender().hasPermission("permissions.command.perms.group.create"))
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                // Notify player that group is already existing
                if (permissions.getGroupRepository().hasGroup(groupName)) {
                  sender.sendMessage(
                      Component.text(
                          getLocalizedMessage(uuid, "command.perms.already_exists"),
                          NamedTextColor.RED));
                  return 1;
                }

                permissions.getPermissionDriver().createGroup(groupName);
                sender.sendMessage(
                    Component.text(
                        getLocalizedMessage(uuid, "command.perms.create.success"),
                        NamedTextColor.AQUA));
              }
              return 1;
            });
  }
  // endregion

  // region group delete sub command
  /**
   * Creates the group delete sub-command which is used to delete an existing group
   *
   * @return The group delete sub-command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupDeleteSubCommand() {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    return literal("delete")
        .requires(
            stack ->
                stack.getBukkitSender().hasPermission("permissions.command.perms.group.delete"))
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                // Notify player that group is not existing
                if (!permissions.getGroupRepository().hasGroup(groupName)) {
                  sender.sendMessage("command.perms.group_not_found");
                  sender.sendMessage(
                      Component.text(
                          getLocalizedMessage(uuid, "command.perms.group_not_found"),
                          NamedTextColor.RED));
                  return 1;
                }

                permissions.getPermissionDriver().deleteGroup(groupName);
                permissions.getGroupRepository().invalidate(groupName);
                sender.sendMessage(
                    Component.text(
                        getLocalizedMessage(uuid, "command.perms.delete.success"),
                        NamedTextColor.AQUA));
              }
              return 1;
            });
  }
  // endregion

  // region group default sub command
  /**
   * Creates the group default sub-command which is used to mark a group as default group
   *
   * @return The group delete sub-command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupDefaultSubCommand() {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    return literal("default")
        .requires(
            stack ->
                stack.getBukkitSender().hasPermission("permissions.command.perms.group.default"))
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                permissions
                    .getGroupRepository()
                    .getGroup(groupName)
                    .whenCompleteAsync(
                        (optionalGroup, t) -> {
                          // Notify player that group is not existing
                          if (optionalGroup.isEmpty()) {
                            sender.sendMessage(
                                Component.text(
                                    getLocalizedMessage(uuid, "command.perms.group_not_found"),
                                    NamedTextColor.RED));
                            return;
                          }

                          permissions.getPermissionDriver().makeGroupDefault(groupName);
                          permissions.getGroupRepository().setDefaultGroup(optionalGroup.get());
                          sender.sendMessage(
                              Component.text(
                                  getLocalizedMessage(uuid, "command.perms.default.success"),
                                  NamedTextColor.AQUA));
                        });
              }
              return 1;
            });
  }
  // endregion

  // region group prefix sub command
  /**
   * Creates the group prefix sub-command which is used to set the prefix of a group
   *
   * @return The group prefix sub-command
   * @since 1.0.0
   */
  public LiteralArgumentBuilder<CommandSourceStack> createGroupPrefixSubCommand() {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    return literal("prefix")
        .requires(
            stack ->
                stack.getBukkitSender().hasPermission("permissions.command.perms.group.prefix"))
        .then(
            argument("prefix", string())
                .executes(
                    context -> {
                      CommandSender sender = context.getSource().getBukkitSender();
                      String groupName = context.getArgument("name", String.class);
                      String prefix = context.getArgument("prefix", String.class);

                      if (sender instanceof Player player) {
                        UUID uuid = player.getUniqueId();
                        permissions
                            .getGroupRepository()
                            .getGroup(groupName)
                            .whenCompleteAsync(
                                (optionalGroup, t) -> {
                                  // Notify player that group is not existing
                                  if (optionalGroup.isEmpty()) {
                                    sender.sendMessage(
                                        Component.text(
                                            getLocalizedMessage(
                                                uuid, "command.perms.group_not_found"),
                                            NamedTextColor.AQUA));
                                    return;
                                  }

                                  PermissionGroup group = optionalGroup.get();
                                  permissions
                                      .getPermissionDriver()
                                      .updateGroupPrefix(groupName, prefix);
                                  group.setPrefix(prefix);
                                  sender.sendMessage(
                                      Component.text(
                                          getLocalizedMessage(uuid, "command.perms.prefix.success"),
                                          NamedTextColor.AQUA));
                                });
                      }
                      return 1;
                    }));
  }
  // endregion

  // region group info sub command
  /**
   * Creates the group info sub-command which sends the executor information about the supplied rank
   *
   * @return The group info sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupInfoSubCommand() {
    return literal("info")
        .requires(
            stack -> stack.getBukkitSender().hasPermission("permissions.command.perms.group.info"))
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                PlayerPermissionPlugin.getSingleton()
                    .getGroupRepository()
                    .getGroup(groupName)
                    .whenCompleteAsync(
                        (optionalGroup, t) -> {
                          // Notify player that group is not existing
                          if (optionalGroup.isEmpty()) {
                            sender.sendMessage(
                                Component.text(
                                    getLocalizedMessage(uuid, "command.perms.group_not_found"),
                                    NamedTextColor.RED));
                            return;
                          }

                          PermissionGroup group = optionalGroup.get();
                          sender.sendMessage(group.parseInfoComponent(player));
                        });
              }
              return 1;
            });
  }
  // endregion

  // region group permission sub commands
  /**
   * Creates the group permission sub-command which is used to manage permissions a group has
   * assigned
   *
   * @return The group permission sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupPermissionsSubCommand() {
    return literal("permissions")
        .requires(
            stack ->
                stack.getBukkitSender().hasPermission("permissions.command.perms.group.permission"))
        .then(createGroupPermissionsAllowedCommand())
        .then(createGroupPermissionsDeniedCommand());
  }

  /**
   * Creates the group permission sub-command for <b>allowed</b> permissions which is used to manage
   * allowed permissions of a group
   *
   * @return The group permission allowed sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupPermissionsAllowedCommand() {
    return literal("allowed")
        .then(
            literal("add")
                .then(
                    argument("permission", greedyString())
                        .executes(
                            context -> {
                              updateGroupPermission(context, false, true);
                              return 1;
                            })))
        .then(
            literal("remove")
                .then(
                    argument("permission", greedyString())
                        .executes(
                            context -> {
                              updateGroupPermission(context, false, false);
                              return 1;
                            })));
  }

  /**
   * Creates the group permission sub-command for <b>denied</b> permissions which is used to manage
   * denied permissions of a group
   *
   * @return The group permission denied sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createGroupPermissionsDeniedCommand() {
    return literal("denied")
        .then(
            literal("add")
                .then(
                    argument("permission", greedyString())
                        .executes(
                            context -> {
                              updateGroupPermission(context, true, true);
                              return 1;
                            })))
        .then(
            literal("remove")
                .then(
                    argument("permission", greedyString())
                        .executes(
                            context -> {
                              updateGroupPermission(context, true, false);
                              return 1;
                            })));
  }

  /**
   * Internal method to update the group permissions for a given group after command execution
   *
   * @param context The command execution context
   * @param denied Whether it's a denied or allowed permission
   * @param add Whether it should get added or removed
   * @since 1.0.0
   */
  private void updateGroupPermission(
      CommandContext<CommandSourceStack> context, boolean denied, boolean add) {
    CommandSender sender = context.getSource().getBukkitSender();
    String groupName = context.getArgument("name", String.class);
    String permission = context.getArgument("permission", String.class);
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    PermissionDriver driver = permissions.getPermissionDriver();

    if (sender instanceof Player player) {
      UUID uuid = player.getUniqueId();
      permissions
          .getGroupRepository()
          .getGroup(groupName)
          .whenCompleteAsync(
              (optionalGroup, t) -> {
                // Notify player that group is not existing
                if (optionalGroup.isEmpty()) {
                  sender.sendMessage(
                      Component.text(
                          getLocalizedMessage(uuid, "command.perms.group_not_found"),
                          NamedTextColor.RED));
                  return;
                }

                PermissionGroup group = optionalGroup.get();
                List<String> list =
                    denied ? group.getDeniedPermissions() : group.getAllowedPermissions();
                group.invalidate(permission);
                if (add) {
                  driver.addPermissionToGroup(groupName, permission, denied);
                  list.add(permission);
                } else {
                  driver.removePermissionFromGroup(groupName, permission, denied);
                  list.remove(permission);
                }
                sender.sendMessage(
                    Component.text(
                        getLocalizedMessage(uuid, "command.perms.change.success"),
                        NamedTextColor.AQUA));
              });
    }
  }
  // endregion
  // endregion
}
