package de.lennox.permissions.database.postgre;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.SignDriver;
import de.lennox.permissions.database.builder.StatementBuilder;
import de.lennox.permissions.database.model.InformativeSign;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
import java.util.logging.Level;

/**
 * Handles all database executions
 *
 * <p>All statements are executed asynchronously
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
@RequiredArgsConstructor
public class PostgreSignDriver implements SignDriver {
  private final ExecutorService databaseThreadPool = Executors.newCachedThreadPool();
  private final PostgreSqlGateway gateway;

  /**
   * Queries all signs from the database
   *
   * @return The future optional list of informative signs
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<Optional<List<InformativeSign>>> queryAllSigns() {
    CompletableFuture<Optional<List<InformativeSign>>> signFuture = new CompletableFuture<>();
    databaseThreadPool.execute(
        () -> {
          try {
            Optional<ResultSet> optionalResult =
                StatementBuilder.forConnection(getConnection())
                    .withSql("SELECT * FROM informative_signs")
                    .executeQuery();
            // Complete with empty signs if query didn't succeed
            if (optionalResult.isEmpty()) {
              signFuture.complete(Optional.empty());
              return;
            }

            ResultSet result = optionalResult.get();
            // Construct the signs from query result
            List<InformativeSign> signs = new ArrayList<>();
            while (result.next()) {
              UUID playerId = UUID.fromString(result.getString("player_id"));
              int x = result.getInt("x");
              int y = result.getInt("y");
              int z = result.getInt("z");
              String world = result.getString("world");

              signs.add(new InformativeSign(playerId, x, y, z, world));
            }
            signFuture.complete(Optional.of(signs));
          } catch (SQLException e) {
            PlayerPermissionPlugin.getSingleton()
                .getLogger()
                .log(Level.SEVERE, "Could not query informative signs!", e);
            signFuture.complete(Optional.empty());
          }
        });
    return signFuture;
  }

  /**
   * Creates a new informative sign of a given player
   *
   * @param playerId The player id
   * @param x The x position
   * @param y The y position
   * @param z The z position
   * @param world The world
   * @since 1.0.0
   */
  @Override
  public void createSign(UUID playerId, int x, int y, int z, String world) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql("INSERT INTO informative_signs VALUES(?, ?, ?, ?, ?)")
                .withParameters(playerId.toString(), x, y, z, world)
                .execute());
  }

  /**
   * Deletes a given informative sign from the database
   *
   * @param sign The informative sign
   * @since 1.0.0
   */
  @Override
  public void deleteSign(InformativeSign sign) {
    databaseThreadPool.execute(
        () ->
            StatementBuilder.forConnection(getConnection())
                .withSql(
                    "DELETE FROM informative_signs WHERE player_id = ? AND x = ? AND y = ? AND z = ? AND world = ?")
                .withParameters(
                    sign.getPlayerId().toString(),
                    sign.getX(),
                    sign.getY(),
                    sign.getZ(),
                    sign.getWorld())
                .execute());
  }

  private Connection getConnection() {
    return gateway.getConnection();
  }
}
