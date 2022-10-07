package de.lennox.permissions.listener;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.result.PermissionGroupResult;
import de.lennox.permissions.group.PermissionGroupRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.UUID;

public class PlayerStateListener implements Listener {
  @EventHandler
  private void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
    UUID player = event.getUniqueId();

    // Let the player wait until his data is loaded
    PlayerPermissionPlugin.getSingleton().getPlayerRepository().getPermittedPlayer(player).join();

    // TODO: Handle rank expiration
  }

  @EventHandler
  private void onPlayerJoin(PlayerJoinEvent event) {
    // Will be replaced with custom message
    event.joinMessage(Component.empty());

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

              // Send normal join message if player has no rank or prefix
              if (playerRank.isEmpty() || playerRank.get().getPrefix().isEmpty()) {
                Bukkit.broadcast(
                    Component.text(
                        String.format("%s joined the game", player.getName()),
                        NamedTextColor.YELLOW));
                return;
              }
              Bukkit.broadcast(
                  Component.text(
                      String.format(
                          "[%s] %s joined the game",
                          playerRank.get().getPrefix(), player.getName()),
                      NamedTextColor.YELLOW));
            });
  }

  @EventHandler
  private void onPlayerQuit(PlayerQuitEvent event) {
    PlayerPermissionPlugin.getSingleton()
        .getPlayerRepository()
        .remove(event.getPlayer().getUniqueId());
  }
}
