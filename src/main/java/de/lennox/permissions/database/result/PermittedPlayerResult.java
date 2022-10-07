package de.lennox.permissions.database.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Contains data about the permitted player, including its group and date of expiry.
 *
 * <p>An empty rank means the fallback default rank will be used internally, this way we don't have
 * to update all users on change of default rank. Even if this is really rare.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Data
@AllArgsConstructor
public class PermittedPlayerResult {
  private static final SimpleDateFormat EXPIRE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  private final UUID uuid;
  private String rank;
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
    return expiresAt != 1 && System.currentTimeMillis() > expiresAt;
  }
}
