package de.lennox.permissions.player;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.result.PermittedPlayerResult;
import org.bukkit.Bukkit;

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
            PlayerPermissionPlugin.getSingleton(),
            () -> {
              for (PermittedPlayerResult permittedPlayer :
                  PlayerPermissionPlugin.getSingleton()
                      .getPlayerRepository()
                      .getCachedPlayers()
                      .values()) {

                // Set to default group if expired
                if (permittedPlayer.isRankExpired()) {
                  PlayerPermissionPlugin.getSingleton()
                      .getPermissionDriver()
                      .updateUserGroup(permittedPlayer.getUuid(), "", -1);
                  permittedPlayer.setRank("");
                  permittedPlayer.setExpiresAt(-1);
                }
              }
            },
            20 * 5,
            20 * 5);
  }
}
