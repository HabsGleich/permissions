package de.lennox.permissions.database.model;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;
import java.util.UUID;

/**
 * Contains data about an informative sign which shows info about a players rank
 *
 * @since 1.0.0
 * @author Lennox
 */
@Data
public class InformativeSign {
  private final UUID playerId;
  private final int x, y, z;
  private final String world;

  /**
   * Gets the block at the location of the informative sign
   *
   * @return The optional block
   * @since 1.0.0
   */
  public Optional<Block> getBlock() {
    World bukkitWorld = Bukkit.getWorld(world);
    if (bukkitWorld == null) {
      return Optional.empty();
    }

    return Optional.of(bukkitWorld.getBlockAt(x, y, z));
  }
}
