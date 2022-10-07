package de.lennox.permissions.database.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PermittedPlayerResult {
  private final UUID uuid;
  private String rank;
  private long expiresAt;
}
