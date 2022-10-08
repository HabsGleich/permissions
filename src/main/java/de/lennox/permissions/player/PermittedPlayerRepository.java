package de.lennox.permissions.player;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.PermittedPlayer;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Contains all permitted players, players which leave the server are removed from cache to save
 * memory.
 *
 * <p>The player cache is never being invalidated, it is only being updated.
 *
 * <p>Additionally, this repository contains quick access to a group cache which instantaneously
 * grants access to a players group. This is required for permission checks.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
public class PermittedPlayerRepository {
  private final Map<UUID, PermittedPlayer> cachedPlayers = new HashMap<>();
  private final Map<UUID, String> playerGroups = new HashMap<>();

  /**
   * Gets a permitted player by the players uuid from cache or database.
   *
   * <p>Automatically creates a new permitted player if there is none
   *
   * @param uuid The player uuid
   * @return The future permitted player
   */
  public CompletableFuture<PermittedPlayer> getPermittedPlayer(UUID uuid) {
    CompletableFuture<PermittedPlayer> playerFuture = new CompletableFuture<>();
    if (cachedPlayers.containsKey(uuid)) {
      playerFuture.complete(cachedPlayers.get(uuid));
    } else {
      PlayerPermissionPlugin.getSingleton()
          .getPermissionDriver()
          .queryPlayerById(uuid)
          .whenCompleteAsync(
              (optionalPlayer, t) -> {
                // Create new player if there is none in the database
                if (optionalPlayer.isEmpty()) {
                  PermittedPlayer player =
                      PlayerPermissionPlugin.getSingleton()
                          .getPermissionDriver()
                          .createPermittedPlayer(uuid)
                          .join();
                  cachedPlayers.put(uuid, player);
                  playerGroups.put(uuid, "");
                  playerFuture.complete(player);
                  return;
                }

                PermittedPlayer permittedPlayer = optionalPlayer.get();
                cachedPlayers.put(uuid, permittedPlayer);
                playerGroups.put(uuid, permittedPlayer.getGroup());
                playerFuture.complete(permittedPlayer);
              });
    }
    return playerFuture;
  }

  /**
   * Gets a permitted player by the players uuid from cache. No queries are executed for
   * instantaneous access.
   *
   * @param uuid The player uuid
   * @return The optional permitted player
   */
  public Optional<PermittedPlayer> getPermittedPlayerNoQuery(UUID uuid) {
    return Optional.ofNullable(cachedPlayers.get(uuid));
  }

  /**
   * Updates the cached group of a player
   *
   * @param uuid The player uuid
   * @param group The new group
   */
  public void updatePlayerGroupCache(UUID uuid, String group) {
    playerGroups.put(uuid, group);
  }

  /**
   * Removes a permitted player from the cache
   *
   * @param uuid The uuid
   */
  public void invalidate(UUID uuid) {
    cachedPlayers.remove(uuid);
    playerGroups.remove(uuid);
  }
}
