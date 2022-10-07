package de.lennox.permissions.listener;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.result.PermissionGroupResult;
import de.lennox.permissions.group.PermissionGroupRepository;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * Listens to all chat messages being sent by players.
 *
 * <p>Modifies the message with a custom format which contains the rank prefix of the player.
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PlayerChatListener implements Listener {
  @EventHandler
  private void onAsyncChat(AsyncChatEvent event) {
    event.setCancelled(true);

    Component original = event.originalMessage();
    Player player = event.getPlayer();
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    PermissionGroupRepository groups = permissions.getGroupRepository();

    permissions
        .getPlayerRepository()
        .getPermittedPlayer(player.getUniqueId())
        .whenCompleteAsync(
            (permittedPlayer, t) -> {
              String playerRankName = permittedPlayer.getRank();
              boolean useDefaultRank = playerRankName.isEmpty();
              // Use default rank if player rank is empty
              Optional<PermissionGroupResult> playerRank =
                  useDefaultRank
                      ? groups.getDefaultGroup()
                      : groups.getGroup(playerRankName).join();

              // Send normal chat message if player has no rank or prefix
              if (playerRank.isEmpty() || playerRank.get().getPrefix().isEmpty()) {
                Bukkit.broadcast(
                    Component.text(String.format("%s > ", player.getName())).append(original));
                return;
              }
              Bukkit.broadcast(
                  Component.text(
                          String.format(
                              "[%s] %s > ", playerRank.get().getPrefix(), player.getName()))
                      .append(original));
            });
  }
}
