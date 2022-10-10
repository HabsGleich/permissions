package de.lennox.permissions.database;

import de.lennox.permissions.database.model.InformativeSign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for all informative sign database executions
 *
 * <p>All statements must be executed asynchronously
 *
 * @since 1.0.0
 * @author Lennox
 */
public interface SignDriver {

  /**
   * Queries all signs from the database
   *
   * @return The future optional list of informative signs
   * @since 1.0.0
   */
  CompletableFuture<Optional<List<InformativeSign>>> queryAllSigns();

  /**
   * Creates a new informative sign of a given player
   *
   * @param x The x position
   * @param y The y position
   * @param z The z position
   * @param world The world
   * @since 1.0.0
   */
  void createSign(int x, int y, int z, String world);

  /**
   * Deletes a given informative sign from the database
   *
   * @param sign The informative sign
   * @since 1.0.0
   */
  void deleteSign(InformativeSign sign);
}
