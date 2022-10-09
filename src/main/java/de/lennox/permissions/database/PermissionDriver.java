package de.lennox.permissions.database;

import de.lennox.permissions.database.model.PermissionGroup;
import de.lennox.permissions.database.model.PermittedPlayer;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for all permission database executions
 *
 * <p>All statements must be executed asynchronously
 *
 * @author Lennox
 * @since 1.0.0
 */
public interface PermissionDriver {
  /**
   * Queries a player from the database by his uuid
   *
   * @param uuid The player uuid
   * @return The future optional permitted player
   * @since 1.0.0
   */
  CompletableFuture<Optional<PermittedPlayer>> queryPlayerById(UUID uuid);

  /**
   * Queries all groups from the database
   *
   * @return The future optional list of permission groups
   * @since 1.0.0
   */
  CompletableFuture<Optional<List<PermissionGroup>>> queryAllGroups();

  /**
   * Queries a group from the database by its name
   *
   * @param name The name
   * @return The future optional permission group
   * @since 1.0.0
   */
  CompletableFuture<Optional<PermissionGroup>> queryGroupByName(String name);

  /**
   * Queries the permissions of a given group
   *
   * @param name The name
   * @return Tuple which contains Object A (list of allowed permissions) and Object B (list of
   *     denied permissions)
   * @since 1.0.0
   */
  CompletableFuture<Optional<Tuple<List<String>, List<String>>>> queryGroupPermissions(String name);

  /**
   * Creates a new group with the given name
   *
   * @param name The name
   * @since 1.0.0
   */
  void createGroup(String name);

  /**
   * Sets a group as default and updates the old default group as non-default
   *
   * @param name The new default group name
   */
  void makeGroupDefault(String name);

  /**
   * Adds an allowed or denied permission to a given group
   *
   * @param name The group name
   * @param permission The permission
   * @param denied The denied state
   * @since 1.0.0
   */
  void addPermissionToGroup(String name, String permission, boolean denied);

  /**
   * Removes an allowed or denied permission from a given group
   *
   * @param name The group name
   * @param permission The permission
   * @param denied The denied state
   * @since 1.0.0
   */
  void removePermissionFromGroup(String name, String permission, boolean denied);

  /**
   * Deletes a group by its name
   *
   * @param name The name
   * @since 1.0.0
   */
  void deleteGroup(String name);

  /**
   * Updates a given groups prefix
   *
   * @param name The group name
   * @param prefix The new prefix
   */
  void updateGroupPrefix(String name, String prefix);

  /**
   * Creates a new permitted player with the given uuid, automatically assigns the default group if
   * present to the player with no expiration date
   *
   * @param uuid The player uuid
   * @return The completable created player
   * @since 1.0.0
   */
  CompletableFuture<PermittedPlayer> createPermittedPlayer(UUID uuid);

  /**
   * Updates a players permission group for the given time
   *
   * @param uuid The player uuid
   * @param name The group name
   * @param time The expiration date as UNIX timestamp
   * @since 1.0.0
   */
  void updatePlayerGroup(UUID uuid, String name, long time);
}
