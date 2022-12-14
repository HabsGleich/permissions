package de.lennox.permissions.sign;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.InformativeSign;
import lombok.Getter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all cached informative signs.
 *
 * <p>The sign cache is never being invalidated, it is only being updated.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
public class InformativeSignRepository {
  private final Map<Integer, InformativeSign> cachedSigns = new HashMap<>();

  /**
   * Builds an initial cache with all currently in-database persistent signs
   *
   * <p>The cache is usually also updates after block placement / destruction to save database
   * queries
   *
   * @since 1.0.0
   */
  public void buildInitialCache() {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    Logger logger = permissions.getLogger();
    permissions
        .getSignDriver()
        .queryAllSigns()
        .whenCompleteAsync(
            (optionalSigns, t) -> {
              // Notify server administrator if signs could not be fetched
              if (optionalSigns.isEmpty()) {
                logger.log(Level.WARNING, "Could not cache signs as loading from database failed!");
                return;
              }

              List<InformativeSign> signs = optionalSigns.get();
              for (InformativeSign sign : signs) {
                Optional<Location> optionalLocation = sign.getBlockLocation();

                // Only add if location exists on startup
                if (optionalLocation.isPresent()) {
                  Location location = optionalLocation.get();
                  cachedSigns.put(location.hashCode(), sign);
                }
              }

              logger.log(Level.INFO, "Cached " + signs.size() + " sign(s) on start-up!");
            });
  }

  /**
   * Registers a new sign in the cache
   *
   * @param sign The informative sign
   * @since 1.0.0
   */
  public void register(InformativeSign sign) {
    Optional<Location> optionalLocation = sign.getBlockLocation();

    // Only add if location exists
    if (optionalLocation.isPresent()) {
      Location location = optionalLocation.get();
      cachedSigns.put(location.hashCode(), sign);
    }
  }

  /**
   * Removes a sign from the cache
   *
   * @param location The sign location
   * @since 1.0.0
   */
  public void invalidate(Location location) {
    cachedSigns.remove(location.hashCode());
  }
}
