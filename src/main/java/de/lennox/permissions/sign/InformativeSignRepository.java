package de.lennox.permissions.sign;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.InformativeSign;

import java.util.ArrayList;
import java.util.List;
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
public class InformativeSignRepository {
  private final List<InformativeSign> cachedSigns = new ArrayList<>();

  /**
   * Builds an initial cache with all currently in-database persistent signs
   *
   * <p>The cache is usually also updates after block placement / destruction to save database
   * queries
   *
   * @since 1.0.0
   */
  public void buildInitialCache() {
    Logger logger = PlayerPermissionPlugin.getSingleton().getLogger();
    PlayerPermissionPlugin.getSingleton()
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
              cachedSigns.addAll(signs);

              logger.log(Level.INFO, "Cached " + signs.size() + " sign(s) on start-up!");
            });
  }
}
