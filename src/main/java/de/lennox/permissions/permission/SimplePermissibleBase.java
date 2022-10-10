package de.lennox.permissions.permission;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.PermittedPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Custom implementation of the bukkit permissible base.
 *
 * <p>Used to overwrite permissions of a player, if a permission is not set the fallback permissions
 * will be used.
 *
 * @since 1.0.0
 * @author Lennox
 */
public class SimplePermissibleBase extends PermissibleBase {
  private final Player player;

  public SimplePermissibleBase(Player player) {
    super(player);
    this.player = player;
  }

  @Override
  public boolean hasPermission(@NotNull String name) {
    return hasPermission0(name);
  }

  @Override
  public boolean hasPermission(@NotNull Permission perm) {
    String name = perm.getName().toLowerCase(Locale.ENGLISH);
    return hasPermission0(name);
  }

  @Override
  public boolean isPermissionSet(@NotNull String name) {
    return isPermissionSet0(name, false);
  }

  @Override
  public boolean isPermissionSet(@NotNull Permission perm) {
    String name = perm.getName().toLowerCase(Locale.ENGLISH);
    return isPermissionSet0(name, false);
  }

  /**
   * Checks if the player has the given permission
   *
   * @param name The permission
   * @return The permission state
   * @since 1.0.0
   */
  private boolean hasPermission0(String name) {
    UUID uuid = player.getUniqueId();

    Optional<PermittedPlayer> optionalPlayer =
        PlayerPermissionPlugin.getSingleton().getPlayerRepository().getPermittedPlayerNoQuery(uuid);
    // Use fallback hasPermission if player is not cached yet
    if (optionalPlayer.isEmpty()) {
      return super.hasPermission(name);
    }

    // If the permission isn't set fallback will be used
    if (!isPermissionSet0(name, true)) {
      return super.hasPermission(name);
    }
    return optionalPlayer.get().hasPermission(name);
  }

  /**
   * Checks if the permission is set for the player
   *
   * @param name The permission
   * @param onlyIntern Defines if only group permissions should be checked, not the fallback
   * @return The set status
   * @since 1.0.0
   */
  private boolean isPermissionSet0(String name, boolean onlyIntern) {
    UUID uuid = player.getUniqueId();

    Optional<PermittedPlayer> optionalPlayer =
        PlayerPermissionPlugin.getSingleton().getPlayerRepository().getPermittedPlayerNoQuery(uuid);
    // Use fallback isPermissionSet if player is not cached yet
    boolean fallbackPermissionSet = super.isPermissionSet(name);
    if (optionalPlayer.isEmpty()) {
      return fallbackPermissionSet;
    }

    boolean permissionSet = optionalPlayer.get().isPermissionSet(name);
    // If permission is set in fallback use fallback
    if (!permissionSet && fallbackPermissionSet && !onlyIntern) {
      return true;
    }

    return permissionSet;
  }
}
