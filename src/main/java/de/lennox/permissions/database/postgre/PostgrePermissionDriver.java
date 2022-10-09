package de.lennox.permissions.database.postgre;

import de.lennox.permissions.database.PermissionDriver;
import de.lennox.permissions.database.builder.StatementBuilder;
import de.lennox.permissions.database.model.PermissionGroup;
import de.lennox.permissions.database.model.PermittedPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.Tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles all database queries, updates and deletions
 *
 * <p>All statements are executed asynchronously
 *
 * @author Lennox
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class PostgrePermissionDriver implements PermissionDriver {
  private final ExecutorService databaseThreadPool = Executors.newCachedThreadPool();
  private final PostgreSqlGateway gateway;

  /**
   * Queries a player from the database by his uuid
   *
   * @param uuid The player uuid
   * @return The future optional permitted player
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<Optional<PermittedPlayer>> queryPlayerById(UUID uuid) {
    CompletableFuture<Optional<PermittedPlayer>> playerFuture = new CompletableFuture<>();
    databaseThreadPool.execute(
        () -> {
          try {
            Optional<ResultSet> optionalResult =
                StatementBuilder.forConnection(getConnection())
                    .withSql("SELECT * FROM permitted_players WHERE id = ?")
                    .withParameter(uuid.toString())
                    .executeQuery();
            // Complete with empty player if query didn't succeed
            if (optionalResult.isEmpty()) {
              playerFuture.complete(Optional.empty());
              return;
            }

            ResultSet result = optionalResult.get();
            // Return empty result if there is no player with this name
            if (!result.next()) {
              playerFuture.complete(Optional.empty());
              return;
            }

            String rank = result.getString("group");
            long expirationDate = result.getLong("expiration_date");

            playerFuture.complete(Optional.of(new PermittedPlayer(uuid, rank, expirationDate)));
          } catch (SQLException e) {
            System.err.println(
                "Failed to read player query result! For precise details see the stacktrace below.");
            e.printStackTrace();
            // Complete with empty player on failure
            playerFuture.complete(Optional.empty());
          }
        });
    return playerFuture;
  }

  /**
   * Queries all groups from the database
   *
   * @return The future optional list of permission groups
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<Optional<List<PermissionGroup>>> queryAllGroups() {
    CompletableFuture<Optional<List<PermissionGroup>>> groupListFuture = new CompletableFuture<>();
    databaseThreadPool.execute(
        () -> {
          try {
            Optional<ResultSet> optionalResult =
                StatementBuilder.forConnection(getConnection())
                    .withSql("SELECT * FROM permission_groups")
                    .executeQuery();
            // Complete with empty group if query didn't succeed
            if (optionalResult.isEmpty()) {
              groupListFuture.complete(Optional.empty());
              return;
            }

            ResultSet result = optionalResult.get();
            // Construct the groups from query result
            List<PermissionGroup> groups = new ArrayList<>();
            while (result.next()) {
              String groupName = result.getString("name");
              String prefix = result.getString("prefix");
              boolean defaultGroup = result.getBoolean("default");

              Optional<Tuple<List<String>, List<String>>> optionalPermissions =
                  queryGroupPermissions(groupName).join();
              // Insert non-permitted group if no permission could be received
              if (optionalPermissions.isEmpty()) {
                groups.add(
                    new PermissionGroup(
                        groupName, prefix, defaultGroup, new ArrayList<>(), new ArrayList<>()));
                return;
              }

              Tuple<List<String>, List<String>> permissions = optionalPermissions.get();
              groups.add(
                  new PermissionGroup(
                      groupName, prefix, defaultGroup, permissions.getA(), permissions.getB()));
            }
            groupListFuture.complete(Optional.of(groups));
          } catch (SQLException e) {
            System.err.println(
                "Failed to read query result! For precise details read stacktrace below.");
            e.printStackTrace();
            groupListFuture.complete(Optional.empty());
          }
        });
    return groupListFuture;
  }

  /**
   * Queries a group from the database by its name
   *
   * @param name The name
   * @return The future optional permission group
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<Optional<PermissionGroup>> queryGroupByName(String name) {
    CompletableFuture<Optional<PermissionGroup>> groupFuture = new CompletableFuture<>();
    databaseThreadPool.execute(
        () -> {
          try {
            Optional<ResultSet> optionalResult =
                StatementBuilder.forConnection(getConnection())
                    .withSql("SELECT * FROM permission_groups WHERE \"name\" = ?")
                    .withParameter(name)
                    .executeQuery();
            // Complete with empty group if query didn't succeed
            if (optionalResult.isEmpty()) {
              groupFuture.complete(Optional.empty());
              return;
            }

            ResultSet result = optionalResult.get();
            // Return empty result if there is no group with this name
            if (!result.next()) {
              groupFuture.complete(Optional.empty());
              return;
            }
            String prefix = result.getString("prefix");
            boolean defaultGroup = result.getBoolean("default");

            queryGroupPermissions(name)
                .whenComplete(
                    (optionalPermissions, t) -> {
                      // Complete with non-permitted group if no permission could be received
                      if (optionalPermissions.isEmpty()) {
                        groupFuture.complete(
                            Optional.of(
                                new PermissionGroup(
                                    name,
                                    prefix,
                                    defaultGroup,
                                    new ArrayList<>(),
                                    new ArrayList<>())));
                        return;
                      }

                      Tuple<List<String>, List<String>> permissions = optionalPermissions.get();
                      groupFuture.complete(
                          Optional.of(
                              new PermissionGroup(
                                  name,
                                  prefix,
                                  defaultGroup,
                                  permissions.getA(),
                                  permissions.getB())));
                    });
          } catch (SQLException e) {
            System.err.println(
                "Failed to read query result! For precise details read stacktrace below.");
            e.printStackTrace();
            groupFuture.complete(Optional.empty());
          }
        });
    return groupFuture;
  }

  /**
   * Queries the permissions of a given group
   *
   * @param name The name
   * @return Tuple which contains Object A (list of allowed permissions) and Object B (list of
   *     denied permissions)
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<Optional<Tuple<List<String>, List<String>>>> queryGroupPermissions(
      String name) {
    CompletableFuture<Optional<Tuple<List<String>, List<String>>>> permissionFuture =
        new CompletableFuture<>();
    databaseThreadPool.execute(
        () -> {
          try {
            List<String> allowed = new ArrayList<>();
            List<String> denied = new ArrayList<>();
            Optional<ResultSet> optionalAllowedPerms =
                StatementBuilder.forConnection(getConnection())
                    .withSql(
                        "SELECT * FROM group_permissions WHERE denied = FALSE AND \"group\" = ?")
                    .withParameter(name)
                    .executeQuery();
            Optional<ResultSet> optionalDeniedPerms =
                StatementBuilder.forConnection(getConnection())
                    .withSql(
                        "SELECT * FROM group_permissions WHERE denied = TRUE AND \"group\" = ?")
                    .withParameter(name)
                    .executeQuery();

            // Complete with empty permissions if query didn't succeed
            if (optionalAllowedPerms.isEmpty() || optionalDeniedPerms.isEmpty()) {
              permissionFuture.complete(Optional.empty());
              return;
            }

            // Add all permissions to corresponding type of permission
            ResultSet allowedPerms = optionalAllowedPerms.get();
            ResultSet deniedPerms = optionalDeniedPerms.get();
            while (allowedPerms.next()) {
              allowed.add(allowedPerms.getString("permission"));
            }
            while (deniedPerms.next()) {
              denied.add(deniedPerms.getString("permission"));
            }

            permissionFuture.complete(Optional.of(new Tuple<>(allowed, denied)));
          } catch (SQLException e) {
            System.err.println(
                "Failed to read query result! For precise details read stacktrace below.");
            e.printStackTrace();
            permissionFuture.complete(Optional.empty());
          }
        });
    return permissionFuture;
  }

  /**
   * Creates a new group with the given name
   *
   * @param name The name
   * @since 1.0.0
   */
  @Override
  public void createGroup(String name) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql("INSERT INTO permission_groups VALUES(?, ?, ?)")
                .withParameters(name, "", false)
                .execute());
  }

  /**
   * Sets a group as default and updates the old default group as non-default
   *
   * @param name The new default group name
   */
  @Override
  public void makeGroupDefault(String name) {
    databaseThreadPool.execute(
        () -> {
          // Make old default group non-default
          StatementBuilder.forConnection(getConnection())
              .withSql("UPDATE permission_groups SET \"default\" = FALSE WHERE \"default\" = TRUE")
              .execute();
          // Make new group default
          StatementBuilder.forConnection(getConnection())
              .withSql("UPDATE permission_groups SET \"default\" = TRUE WHERE \"name\" = ?")
              .withParameter(name)
              .execute();
        });
  }

  /**
   * Adds an allowed or denied permission to a given group
   *
   * @param name The group name
   * @param permission The permission
   * @param denied The denied state
   * @since 1.0.0
   */
  @Override
  public void addPermissionToGroup(String name, String permission, boolean denied) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql("INSERT INTO group_permissions VALUES(?, ?, ?)")
                .withParameters(name, permission, denied)
                .execute());
  }

  /**
   * Removes an allowed or denied permission from a given group
   *
   * @param name The group name
   * @param permission The permission
   * @param denied The denied state
   * @since 1.0.0
   */
  @Override
  public void removePermissionFromGroup(String name, String permission, boolean denied) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql(
                    "DELETE FROM group_permissions WHERE \"group\" = ? AND permission = ? AND denied = ?")
                .withParameters(name, permission, denied)
                .execute());
  }

  /**
   * Deletes a group by its name
   *
   * @param name The name
   * @since 1.0.0
   */
  @Override
  public void deleteGroup(String name) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql("DELETE FROM permission_groups WHERE \"name\" = ?")
                .withParameter(name)
                .execute());
  }

  /**
   * Updates a given groups prefix
   *
   * @param name The group name
   * @param prefix The new prefix
   */
  @Override
  public void updateGroupPrefix(String name, String prefix) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql("UPDATE permission_groups SET prefix = ? WHERE \"name\" = ?")
                .withParameters(prefix, name)
                .execute());
  }

  /**
   * Creates a new permitted player with the given uuid, automatically assigns the default group if
   * present to the player with no expiration date
   *
   * @param uuid The player uuid
   * @return The completable created player
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<PermittedPlayer> createPermittedPlayer(UUID uuid) {
    CompletableFuture<PermittedPlayer> playerFuture = new CompletableFuture<>();
    databaseThreadPool.execute(
        () -> {
          StatementBuilder.forConnection(getConnection())
              .withSql("INSERT INTO permitted_players VALUES(?, ?, ?)")
              .withParameters(uuid.toString(), "", -1)
              .execute();

          playerFuture.complete(new PermittedPlayer(uuid, "", -1));
        });
    return playerFuture;
  }

  /**
   * Updates a players permission group for the given time
   *
   * @param uuid The player uuid
   * @param name The group name
   * @param time The expiration date as UNIX timestamp
   * @since 1.0.0
   */
  @Override
  public void updatePlayerGroup(UUID uuid, String name, long time) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql(
                    "UPDATE permitted_players SET \"group\" = ?, expiration_date = ? WHERE id = ?")
                .withParameters(name, time, uuid.toString())
                .execute());
  }

  private Connection getConnection() {
    return gateway.getConnection();
  }
}
