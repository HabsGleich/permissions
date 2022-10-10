package de.lennox.permissions.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Basic in-memory storage of player language selection, language selection is <b>not</b> saved on
 * server restart
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PlayerLanguageRepository {
  private final Map<UUID, String> languageSelectionMap = new HashMap<>();

  /**
   * Stores the language setting of a given player
   *
   * @param player The player
   * @param language The language setting
   * @since 1.0.0
   */
  public void store(UUID player, String language) {
    languageSelectionMap.put(player, language);
  }

  /**
   * Removes the player from the selection cache
   *
   * @param player The player
   * @since 1.0.0
   */
  public void invalidate(UUID player) {
    languageSelectionMap.remove(player);
  }

  /**
   * Returns the custom language if the player has chosen any, otherwise the default language
   * english will be returned.
   *
   * @param player The player
   * @return The chosen language
   * @since 1.0.0
   */
  public String get(UUID player) {
    // Return english as default language if no specific setting has been made
    if (!languageSelectionMap.containsKey(player)) {
      return "en";
    }

    return languageSelectionMap.get(player);
  }
}
