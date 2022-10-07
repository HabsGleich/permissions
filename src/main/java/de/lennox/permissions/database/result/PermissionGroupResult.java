package de.lennox.permissions.database.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PermissionGroupResult {
  private final String name;
  private String prefix;
  private boolean defaultGroup;
  private final List<String> allowedPermissions;
  private final List<String> deniedPermissions;
}
