package de.lennox.permissions.database.model;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.locale.LocalizationRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.*;
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
  private final Set<String> setCache = new HashSet<>();

  /**
   * Checks if a group has the given permission
   *
   * @param permission The permission
   * @return The state of permission
   * @since 1.0.0
   */
  public boolean hasPermission(String permission) {
    if (stateCache.containsKey(permission)) {
      return stateCache.get(permission);
    }

    for (String perm : allowedPermissions) {
      Pattern pattern = getPattern(perm);
      if (pattern.matcher(permission).matches()) {
        stateCache.put(permission, true);
        setCache.add(permission);
        return true;
      }
    }

    for (String perm : deniedPermissions) {
      Pattern pattern = getPattern(perm);
      if (pattern.matcher(permission).matches()) {
        stateCache.put(permission, false);
        setCache.add(permission);
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
   * @since 1.0.0
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
   * @since 1.0.0
   */
  public String createRegexFromInput(String input) {
    return input.replace(".", "\\.").replace("*", "(.*)");
  }

  /**
   * Invalidates a permission for the group
   *
   * @param permission The permission
   * @since 1.0.0
   */
  public void invalidate(String permission) {
    stateCache.remove(permission);
    setCache.remove(permission);
  }

  /**
   * Parses the information about the group to a component
   *
   * @param player The player the message is parsed for
   * @return The component
   * @since 1.0.0
   */
  public Component parseInfoComponent(Player player) {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    LocalizationRepository localization = permissions.getLocalization();
    String language = permissions.getPlayerLanguageRepository().get(player.getUniqueId());

    return Component.text(
            String.format(localization.getMessage(language, "command.perms.group.header"), name)
                + "\n",
            NamedTextColor.AQUA)
        .append(
            Component.text(
                String.format(" - %s: %s\n", localization.getMessage(language, "prefix"), prefix),
                NamedTextColor.GRAY))
        .append(
            Component.text(
                String.format(
                    " - %s: %s", localization.getMessage(language, "default"), defaultGroup),
                NamedTextColor.GRAY));
  }
}
