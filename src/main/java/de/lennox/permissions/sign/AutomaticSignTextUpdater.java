package de.lennox.permissions.sign;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.InformativeSign;
import de.lennox.permissions.database.model.PermittedPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Automatic updates for all informative signs on the world
 *
 * @since 1.0.0
 * @author Lennox
 */
public class AutomaticSignTextUpdater {

  /**
   * Creates the updating task
   *
   * @since 1.0.0
   */
  public void createTask() {
    Bukkit.getScheduler()
        .runTaskTimerAsynchronously(
            PlayerPermissionPlugin.getSingleton(), this::updateInformativeSigns, 20, 20);
  }

  /**
   * Updates all informative signs in the world
   *
   * @since 1.0.0
   */
  private void updateInformativeSigns() {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    permissions
        .getSignRepository()
        .getCachedSigns()
        .values()
        .forEach(
            sign -> {
              Optional<Location> optionalLocation = sign.getBlockLocation();
              // Don't do anything if location could not be created
              if (optionalLocation.isEmpty()) {
                return;
              }

              Location location = optionalLocation.get();
              for (Player player : Bukkit.getOnlinePlayers()) {
                Optional<PermittedPlayer> optionalPermittedPlayer =
                    permissions
                        .getPlayerRepository()
                        .getPermittedPlayerNoQuery(player.getUniqueId());
                // Only update if rank information is available
                if (optionalPermittedPlayer.isEmpty()) {
                  return;
                }

                PermittedPlayer permittedPlayer = optionalPermittedPlayer.get();
                String locale = permissions.getPlayerLanguageRepository().get(player.getUniqueId());
                player.sendSignChange(
                    location, sign.getSignComponents(permittedPlayer, player, locale));
              }
            });
  }
}
