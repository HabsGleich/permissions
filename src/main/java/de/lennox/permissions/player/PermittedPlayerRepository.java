package de.lennox.permissions.player;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.result.PermittedPlayerResult;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Contains all permitted players, players which leave the server are removed from cache to save
 * memory.
 *
 * <p>The player cache is never being invalidated, it is only being updated.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
public class PermittedPlayerRepository {
  private final Map<UUID, PermittedPlayerResult> cachedPlayers = new HashMap<>();

  /**
   * Gets a permitted player by the players uuid from cache or database.
   *
   * <p>Automatically creates a new permitted player if there is none
   *
   * @param uuid The player uuid
   * @return The future permitted player
   */
  public CompletableFuture<PermittedPlayerResult> getPermittedPlayer(UUID uuid) {
    CompletableFuture<PermittedPlayerResult> playerFuture = new CompletableFuture<>();
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
                  PermittedPlayerResult player =
                      PlayerPermissionPlugin.getSingleton()
                          .getPermissionDriver()
                          .createPermittedUser(uuid)
                          .join();
                  playerFuture.complete(player);
                  return;
                }

                playerFuture.complete(optionalPlayer.get());
              });
    }
    return playerFuture;
  }

  /**
   * Removes a permitted player from the cache
   *
   * @param uuid The uuid
   */
  public void remove(UUID uuid) {
    cachedPlayers.remove(uuid);
  }
}
