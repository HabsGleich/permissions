package de.lennox.permissions.player;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.result.PermittedPlayerResult;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;

/**
 * Handles the automatic expiration of players currently on the server. Expiration is checked every
 * 5 seconds asynchronously.
 *
 * @since 1.0.0
 * @author Lennox
 */
public class AutomaticRankAssigner {
  /**
   * Creates the assigning task through the bukkit scheduler
   *
   * @since 1.0.0
   */
  public void createTask() {
    Bukkit.getScheduler()
        .runTaskTimerAsynchronously(
            PlayerPermissionPlugin.getSingleton(), this::checkPlayersForExpiry, 20 * 5, 20 * 5);
  }

  /**
   * Checks all players for an expiry of a rank, default rank is applied on expiration
   *
   * @since 1.0.0
   */
  private void checkPlayersForExpiry() {
    Map<UUID, PermittedPlayerResult> players =
        PlayerPermissionPlugin.getSingleton().getPlayerRepository().getCachedPlayers();

    for (PermittedPlayerResult permittedPlayer : players.values()) {
      // Set to default group if expired
      if (permittedPlayer.isRankExpired()) {
        PlayerPermissionPlugin.getSingleton()
            .getPermissionDriver()
            .updateUserGroup(permittedPlayer.getUuid(), "", -1);
        permittedPlayer.setRank("");
        permittedPlayer.setExpiresAt(-1);
      }
    }
  }
}
