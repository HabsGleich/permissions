package de.lennox.permissions.listener;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.PermissionGroup;
import de.lennox.permissions.database.model.PermittedPlayer;
import de.lennox.permissions.group.PermissionGroupRepository;
import de.lennox.permissions.permission.PermissibleBaseInjector;
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

/**
 * Listens to player login, join and quit to create and invalidate caches.
 *
 * <p>It additionally modifies the join message to append the prefix of the player.
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PlayerStateListener implements Listener {
  private final PermissibleBaseInjector injector = new PermissibleBaseInjector();

  @EventHandler
  private void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    UUID player = event.getUniqueId();
    PermittedPlayer permittedPlayer =
        permissions.getPlayerRepository().getPermittedPlayer(player).join();

    // Automatically assign player to default group on rank expire
    if (permittedPlayer.isRankExpired()) {
      permissions.getPermissionDriver().updatePlayerGroup(player, "", -1);
      permittedPlayer.setGroup("");
      permittedPlayer.setExpiresAt(-1);
    }
  }

  @EventHandler
  private void onPlayerJoin(PlayerJoinEvent event) {
    event.joinMessage(Component.empty());

    Player player = event.getPlayer();
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    PermissionGroupRepository groups = permissions.getGroupRepository();

    injector.injectIntoPlayer(player);
    permissions
        .getPlayerRepository()
        .getPermittedPlayer(player.getUniqueId())
        .whenCompleteAsync(
            (permittedPlayer, t) -> {
              String playerRankName = permittedPlayer.getGroup();
              boolean useDefaultRank = playerRankName.isEmpty();
              // Use default rank if player rank is empty
              Optional<PermissionGroup> playerRank =
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
    UUID uuid = event.getPlayer().getUniqueId();
    PlayerPermissionPlugin.getSingleton().getPlayerLanguageRepository().invalidate(uuid);
  }
}
