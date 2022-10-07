package de.lennox.permissions.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.command.Command;
import de.lennox.permissions.database.PermissionDriver;
import de.lennox.permissions.database.result.PermissionGroupResult;
import de.lennox.permissions.database.result.PermittedPlayerResult;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.StringArgumentType.*;

public class PermsCommand extends Command {
  public PermsCommand() {
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
            .then(createUserSubCommands())
            .then(createGroupSubCommands());
  }

  // region user sub commands
  /**
   * Creates the user sub commands which contains functionality for modifying users
   *
   * @return The user sub command
   * @since 1.0.0
   */
  private LiteralArgumentBuilder<CommandSourceStack> createUserSubCommands() {
    return literal("user")
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
        .then(
            literal("set")
                .then(
                    argument("groupName", word())
                        .then(
                            argument("expiresIn", greedyString())
                                .executes(
                                    context -> {
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
    CommandSender sender = context.getSource().getBukkitSender();
    String name = context.getArgument("name", String.class);
    String groupName = context.getArgument("groupName", String.class);
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
    UUID playerId = offlinePlayer.getUniqueId();

    permissions
        .getPermissionDriver()
        .queryPlayerById(playerId)
        .whenCompleteAsync(
            (optionalPlayer, t) -> {
              // Notify sender that the player could not be found
              if (optionalPlayer.isEmpty()) {
                sender.sendMessage("permissions.command.perms.player.not_found");
                return;
              }

              permissions.getPermissionDriver().updateUserGroup(playerId, groupName, time);
              // Update cache if player is online
              if (offlinePlayer.isOnline()) {
                permissions
                    .getPlayerRepository()
                    .getPermittedPlayer(playerId)
                    .whenComplete(
                        (player, throwable) -> {
                          player.setRank(groupName);
                          player.setExpiresAt(time);
                        });
              }
              sender.sendMessage("permissions.command.perms.set_group.success");
            });
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
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String name = context.getArgument("name", String.class);

              PlayerPermissionPlugin.getSingleton()
                  .getPermissionDriver()
                  .queryPlayerById(Bukkit.getOfflinePlayer(name).getUniqueId())
                  .whenCompleteAsync(
                      (optionalPlayer, t) -> {
                        // Notify player that the requested player could not be found
                        if (optionalPlayer.isEmpty()) {
                          sender.sendMessage("permissions.command.perms.player.not_found");
                          return;
                        }

                        PermittedPlayerResult player = optionalPlayer.get();
                        sender.sendMessage(
                            "uuid:"
                                + player.getUuid()
                                + ", group: "
                                + player.getRank()
                                + ", expiresAt: "
                                + player.getExpiresAt());
                      });

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
                              .asMap()
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
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              // Notify player that group is already existing
              if (permissions.getGroupRepository().hasGroup(groupName)) {
                sender.sendMessage("permissions.command.perms.already_exists");
                return 1;
              }

              permissions.getPermissionDriver().createGroup(groupName);
              sender.sendMessage("permissions.command.perms.create.success");
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
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              // Notify player that group is not existing
              if (!permissions.getGroupRepository().hasGroup(groupName)) {
                sender.sendMessage("permissions.command.perms.group_not_found");
                return 1;
              }

              permissions.getPermissionDriver().deleteGroup(groupName);
              permissions.getGroupRepository().invalidateCache(groupName);
              sender.sendMessage("permissions.command.perms.delete.success");
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
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              permissions
                  .getGroupRepository()
                  .getGroup(groupName)
                  .whenCompleteAsync(
                      (optionalGroup, t) -> {
                        // Notify player that group is not existing
                        if (optionalGroup.isEmpty()) {
                          sender.sendMessage("permissions.command.perms.group_not_found");
                          return;
                        }

                        permissions.getPermissionDriver().makeGroupDefault(groupName);
                        sender.sendMessage("permissions.command.perms.prefix.success");
                      });
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
        .then(
            argument("prefix", string())
                .executes(
                    context -> {
                      CommandSender sender = context.getSource().getBukkitSender();
                      String groupName = context.getArgument("name", String.class);
                      String prefix = context.getArgument("prefix", String.class);

                      permissions
                          .getGroupRepository()
                          .getGroup(groupName)
                          .whenCompleteAsync(
                              (optionalGroup, t) -> {
                                // Notify player that group is not existing
                                if (optionalGroup.isEmpty()) {
                                  sender.sendMessage("permissions.command.perms.group_not_found");
                                  return;
                                }

                                PermissionGroupResult group = optionalGroup.get();
                                permissions
                                    .getPermissionDriver()
                                    .updateGroupPrefix(groupName, prefix);
                                group.setPrefix(prefix);
                                sender.sendMessage("permissions.command.perms.prefix.success");
                              });
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
        .executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();
              String groupName = context.getArgument("name", String.class);

              PlayerPermissionPlugin.getSingleton()
                  .getGroupRepository()
                  .getGroup(groupName)
                  .whenCompleteAsync(
                      (optionalGroup, t) -> {
                        // Notify player that group is not existing
                        if (optionalGroup.isEmpty()) {
                          sender.sendMessage("permissions.command.perms.group_not_found");
                          return;
                        }

                        PermissionGroupResult group = optionalGroup.get();
                        sender.sendMessage(
                            "name: " + group.getName() + " prefix: " + group.getPrefix());
                      });
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
                    argument("permission", string())
                        .executes(
                            context -> {
                              updateGroupPermission(context, false, true);
                              return 1;
                            })))
        .then(
            literal("remove")
                .then(
                    argument("permission", string())
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
                    argument("permission", string())
                        .executes(
                            context -> {
                              updateGroupPermission(context, true, true);
                              return 1;
                            })))
        .then(
            literal("remove")
                .then(
                    argument("permission", string())
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
   */
  private void updateGroupPermission(
      CommandContext<CommandSourceStack> context, boolean denied, boolean add) {
    CommandSender sender = context.getSource().getBukkitSender();
    String groupName = context.getArgument("name", String.class);
    String permission = context.getArgument("permission", String.class);
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();

    permissions
        .getGroupRepository()
        .getGroup(groupName)
        .whenCompleteAsync(
            (optionalGroup, t) -> {
              // Notify player that group is not existing
              if (optionalGroup.isEmpty()) {
                sender.sendMessage("permissions.command.perms.group_not_found");
                return;
              }

              PermissionGroupResult group = optionalGroup.get();
              PermissionDriver driver = permissions.getPermissionDriver();
              List<String> list =
                  denied ? group.getDeniedPermissions() : group.getAllowedPermissions();
              if (add) {
                driver.addPermissionToGroup(groupName, permission, denied);
                list.add(permission);
              } else {
                driver.removePermissionFromGroup(groupName, permission, denied);
                list.remove(permission);
              }
              sender.sendMessage("permission.command.perms.change.success");
            });
  }
  // endregion
  // endregion
}
