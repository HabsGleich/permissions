package de.lennox.permissions.database.model;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.locale.LocalizationRepository;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * Contains data about an informative sign which shows info about a players rank
 *
 * @since 1.0.0
 * @author Lennox
 */
@Data
public class InformativeSign {
  private final int x, y, z;
  private final String world;

  /**
   * Gets the sign lines for the given permitted player and bukkit player in the provided language
   *
   * <p>Format:<br>
   * - player_name<br>
   * - Group: group_name<br>
   * - expires at<br>
   * - expiry_date<br>
   *
   * @param player The player
   * @param bukkitPlayer The bukkit player
   * @param locale The target language
   * @return The sign lines
   * @since 1.0.0
   */
  public List<Component> getSignComponents(
      PermittedPlayer player, Player bukkitPlayer, String locale) {
    LocalizationRepository localization = PlayerPermissionPlugin.getSingleton().getLocalization();
    String groupName = player.getGroup().isEmpty() ? "Default" : player.getGroup();

    return List.of(
        Component.text(bukkitPlayer.getName(), NamedTextColor.GRAY),
        Component.text(
            String.format("%s: %s", localization.getMessage(locale, "group"), groupName),
            NamedTextColor.AQUA),
        Component.text(localization.getMessage(locale, "expires_at"), NamedTextColor.AQUA),
        Component.text(
            player.getExpiresAt() != -1 ? player.parseExpiryDate() : "Never", NamedTextColor.AQUA));
  }

  /**
   * Gets the block at the location of the informative sign
   *
   * @return The optional block
   * @since 1.0.0
   */
  public Optional<Location> getBlockLocation() {
    World bukkitWorld = Bukkit.getWorld(world);
    if (bukkitWorld == null) {
      return Optional.empty();
    }

    return Optional.of(new Location(bukkitWorld, x, y, z));
  }
}
