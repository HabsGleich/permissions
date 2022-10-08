package de.lennox.permissions.database.model;

import it.unimi.dsi.fastutil.Hash;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Contains data about a permission group. Additionally used to check for permissions with regex.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Data
@AllArgsConstructor
public class PermissionGroup {
  private final String name;
  private String prefix;
  private boolean defaultGroup;
  private final List<String> allowedPermissions;
  private final List<String> deniedPermissions;

  // Static to share patterns between groups
  private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();
  private final Map<String, Boolean> stateCache = new HashMap<>();

  /**
   * Checks if a group has the given permission
   *
   * @param permission The permission
   * @return The state of permission
   */
  public boolean hasPermission(String permission) {
    if (stateCache.containsKey(permission)) {
      return stateCache.get(permission);
    }

    for (String perm : allowedPermissions) {
      Pattern pattern = getPattern(perm);
      if (pattern.matcher(permission).matches()) {
        stateCache.put(permission, true);
        return true;
      }
    }

    for (String perm : deniedPermissions) {
      Pattern pattern = getPattern(perm);
      if (pattern.matcher(permission).matches()) {
        stateCache.put(permission, false);
        return false;
      }
    }
    return false;
  }

  /**
   * Creates a new regex pattern from the given input
   *
   * @param input The input
   * @return The regex pattern
   */
  @SneakyThrows
  public Pattern getPattern(String input) {
    if (PATTERN_CACHE.containsKey(input)) {
      return PATTERN_CACHE.get(input);
    }

    String regex = createRegexFromInput(input);
    Pattern pattern;
    try {
      pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException ex) {
      // Re-try with quoting after pattern parsing failed
      pattern = Pattern.compile(Pattern.quote(regex), Pattern.CASE_INSENSITIVE);
    }
    PATTERN_CACHE.put(input, pattern);
    return pattern;
  }

  /**
   * Creates regex from a standard expression (e.g. minecraft.command.*)
   *
   * @param input The standard input
   * @return The regex expression
   */
  public String createRegexFromInput(String input) {
    return input.replace(".", "\\.").replace("*", "(.*)");
  }

  /**
   * Invalidates a permission for the group
   *
   * @param permission The permission
   */
  public void invalidate(String permission) {
    stateCache.remove(permission);
  }
}
