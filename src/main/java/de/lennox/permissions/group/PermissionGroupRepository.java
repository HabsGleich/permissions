package de.lennox.permissions.group;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.PermissionGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all cached permission groups.
 *
 * <p>The group cache is never being invalidated, it is only being updated.
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
public class PermissionGroupRepository {
  private final Map<String, PermissionGroup> cachedGroups = new HashMap<>();

  @Setter private PermissionGroup defaultGroup;

  /**
   * Builds an initial cache with all currently in-database persistent permission groups
   *
   * <p>The cache is usually also updated after command executions to save database queries
   *
   * @since 1.0.0
   */
  public void buildInitialCache() {
    Logger logger = PlayerPermissionPlugin.getSingleton().getLogger();
    PlayerPermissionPlugin.getSingleton()
        .getPermissionDriver()
        .queryAllGroups()
        .whenCompleteAsync(
            (optionalGroups, t) -> {
              // Notify server administrator if groups could not be fetched
              if (optionalGroups.isEmpty()) {
                logger.log(
                    Level.WARNING, "Could not cache groups as loading from database failed!");
                return;
              }

              List<PermissionGroup> groups = optionalGroups.get();
              for (PermissionGroup group : groups) {
                if (group.isDefaultGroup()) {
                  defaultGroup = group;
                }

                cachedGroups.put(group.getName(), group);
              }

              logger.log(Level.INFO, "Cached " + groups.size() + " group(s) on start-up!");
            });
  }

  /**
   * Gets a group from the cache if present. If no group can be found in-cache it will be directly
   * received from the database and added to the cache
   *
   * @param name The group name
   * @return The future optional group
   * @since 1.0.0
   */
  public CompletableFuture<Optional<PermissionGroup>> getGroup(String name) {
    CompletableFuture<Optional<PermissionGroup>> groupFuture = new CompletableFuture<>();
    if (cachedGroups.containsKey(name)) {
      groupFuture.complete(Optional.of(cachedGroups.get(name)));
    } else {
      PlayerPermissionPlugin.getSingleton()
          .getPermissionDriver()
          .queryGroupByName(name)
          .whenCompleteAsync(
              (optionalGroup, t) -> {
                // Complete with empty group if no group could be found
                if (optionalGroup.isEmpty()) {
                  groupFuture.complete(Optional.empty());
                  return;
                }

                PermissionGroup databaseGroup = optionalGroup.get();
                cachedGroups.put(databaseGroup.getName(), databaseGroup);
                groupFuture.complete(Optional.of(databaseGroup));
              });
    }
    return groupFuture;
  }

  /**
   * Gets a group from the cache, no queries are executed here to provide instantaneous access to
   * cached groups (usually all groups are cached unless they are directly added by a third party)
   *
   * @param name The group name
   * @return The optional group
   * @since 1.0.0
   */
  public Optional<PermissionGroup> getGroupNoQuery(String name) {
    return Optional.ofNullable(cachedGroups.get(name));
  }

  /**
   * Returns the default group which is set internally, the option will be empty if no default group
   * was found or set
   *
   * @return The optional default group
   * @since 1.0.0
   */
  public Optional<PermissionGroup> getDefaultGroup() {
    return Optional.ofNullable(defaultGroup);
  }

  /**
   * Returns if the group cache has a group with this name saved
   *
   * @param name The name
   * @return Contained or not
   * @since 1.0.0
   */
  public boolean hasGroup(String name) {
    return cachedGroups.containsKey(name);
  }

  /**
   * Invalidates the cache for the given group name
   *
   * @param name The group name
   * @since 1.0.0
   */
  public void invalidate(String name) {
    cachedGroups.remove(name);
  }
}
