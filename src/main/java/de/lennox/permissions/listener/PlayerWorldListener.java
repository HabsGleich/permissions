package de.lennox.permissions.listener;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.InformativeSign;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

/**
 * Listens to all block placements and block breaks a player makes to create / remove informative
 * signs
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PlayerWorldListener implements Listener {

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    Block block = event.getBlock();

    // Only register sign places
    if (block.getType().name().endsWith("_SIGN")) {
      PlayerPermissionPlugin.getSingleton()
          .getSignRepository()
          .register(
              new InformativeSign(
                  block.getX(),
                  block.getY(),
                  block.getZ(),
                  block.getWorld().getName()));
    }
  }
}
