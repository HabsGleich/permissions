package de.lennox.permissions.database.result;

import lombok.AllArgsConstructor;
import lombok.Data;

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
  private final UUID uuid;
  private String rank;
  private long expiresAt;
}
