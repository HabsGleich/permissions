package de.lennox.permissions.database.model;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.group.PermissionGroupRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Contains data about the permitted player, including its group and date of expiry.
 *
 * <p>An empty rank means the fallback default rank will be used internally, this way we don't have
 * to update all players on change of default rank. Even if this is really rare.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Data
@AllArgsConstructor
public class PermittedPlayer {
  private static final SimpleDateFormat EXPIRE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  private final UUID uuid;
  private String group;
  private long expiresAt;

  /**
   * Creates a parsed time string to display for when the rank expires
   *
   * @return The parsed time
   * @since 1.0.0
   */
  public String parseExpiryDate() {
    return EXPIRE_FORMATTER.format(new Date(expiresAt));
  }

  /**
   * Returns if the current rank is expired and should be replaced with the default group
   *
   * @return Rank expiry state
   * @since 1.0.0
   */
  public boolean isRankExpired() {
    return expiresAt != -1 && System.currentTimeMillis() > expiresAt;
  }

  /**
   * Uses the players group or default to check if the player has the permission requested.
   *
   * @param permission The permission
   * @return Permission state
   * @since 1.0.0
   */
  public boolean hasPermission(String permission) {
    Optional<PermissionGroup> optionalGroup = getPlayerGroup();
    // We can't do any permission checks without default or specified group
    if (optionalGroup.isEmpty()) {
      return false;
    }

    PermissionGroup group = optionalGroup.get();
    return group.hasPermission(permission);
  }

  /**
   * Uses the players group or default to check if a permission is set
   *
   * @param permission The permission
   * @return Permission set status
   * @since 1.0.0
   */
  public boolean isPermissionSet(String permission) {
    Optional<PermissionGroup> optionalGroup = getPlayerGroup();
    // We can't do any permission checks without default or specified group
    if (optionalGroup.isEmpty()) {
      return false;
    }

    PermissionGroup group = optionalGroup.get();
    if (group.getSetCache().contains(permission)) {
      return true;
    }
    // Re-try after checking for permission
    group.hasPermission(permission);
    return group.getSetCache().contains(permission);
  }

  /**
   * Gets the players cached group from memory
   *
   * @return The optional cached group
   * @since 1.0.0
   */
  private Optional<PermissionGroup> getPlayerGroup() {
    PermissionGroupRepository groups = PlayerPermissionPlugin.getSingleton().getGroupRepository();
    Optional<PermissionGroup> optionalGroup = groups.getGroupNoQuery(group);
    // Return default optional group if the group couldn't be found
    if (optionalGroup.isEmpty()) {
      return groups.getDefaultGroup();
    }
    return optionalGroup;
  }
}
