package de.lennox.permissions.permission;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * Processes the "injection" of the custom permissible base {@link SimplePermissibleBase} which is
 * used to do custom permission checks
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PermissibleBaseInjector {
  private static final String PLAYER_CLASS_NAME =
      "org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity";
  private static final String PLAYER_PERMISSION_FIELD = "perm";

  /**
   * "Injects" the custom permissible base into the perm field
   *
   * @param player The player
   * @since 1.0.0
   */
  @SneakyThrows
  public void injectIntoPlayer(Player player) {
    Class<?> playerClass = Class.forName(PLAYER_CLASS_NAME);
    // Make sure the proper class was queried
    if (!playerClass.isAssignableFrom(player.getClass())) {
      return;
    }

    Field permField = playerClass.getDeclaredField(PLAYER_PERMISSION_FIELD);
    permField.setAccessible(true);
    PermissibleBase oldPermissible = (PermissibleBase) permField.get(player);
    PermissibleBase newPermissible = new SimplePermissibleBase(player);

    copyPermissibleContentOf(oldPermissible, newPermissible);
    permField.set(player, newPermissible);
  }

  /**
   * Copies the contents from the previous permissible base to the new custom permissible base
   *
   * @param old The old permissible base
   * @param current The new permissible base
   * @since 1.0.0
   */
  @SneakyThrows
  private void copyPermissibleContentOf(PermissibleBase old, PermissibleBase current) {
    // Resolve the attachment field
    Field attachmentField = PermissibleBase.class.getDeclaredField("attachments");
    attachmentField.setAccessible(true);
    //noinspection unchecked
    List<Object> attachments = (List<Object>) attachmentField.get(current);
    attachments.clear();

    attachments.addAll((Collection<?>) attachmentField.get(old));
    current.recalculatePermissions();
  }
}
