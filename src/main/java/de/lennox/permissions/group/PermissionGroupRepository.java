package de.lennox.permissions.group;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.result.PermissionGroupResult;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all cached permission groups, caches are invalidated for after 15 minutes for every
 * group individually or if another source requests invalidation (e.g. command execution)
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
public class PermissionGroupRepository {
  private final Cache<String, PermissionGroupResult> cachedGroups =
      CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();

  @Setter private PermissionGroupResult defaultGroup;

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

              List<PermissionGroupResult> groups = optionalGroups.get();
              for (PermissionGroupResult group : groups) {
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
  public CompletableFuture<Optional<PermissionGroupResult>> getGroup(String name) {
    CompletableFuture<Optional<PermissionGroupResult>> groupFuture = new CompletableFuture<>();
    PermissionGroupResult group = cachedGroups.getIfPresent(name);
    if (group != null) {
      groupFuture.complete(Optional.of(group));
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

                PermissionGroupResult databaseGroup = optionalGroup.get();
                cachedGroups.put(databaseGroup.getName(), databaseGroup);
                groupFuture.complete(Optional.of(databaseGroup));
              });
    }
    return groupFuture;
  }

  /**
   * Returns the default group which is set internally, the option will be empty if no default group
   * was found or set
   *
   * @return The optional default group
   * @since 1.0.0
   */
  public Optional<PermissionGroupResult> getDefaultGroup() {
    return Optional.ofNullable(defaultGroup);
  }

  /**
   * Returns if the group cache has a group with this name saved
   *
   * @param name The name
   * @return Contained or not
   */
  public boolean hasGroup(String name) {
    return cachedGroups.getIfPresent(name) != null;
  }

  /**
   * Invalidates the cache for the given group name
   *
   * @param name The group name
   * @since 1.0.0
   */
  public void invalidate(String name) {
    cachedGroups.invalidate(name);
  }
}
