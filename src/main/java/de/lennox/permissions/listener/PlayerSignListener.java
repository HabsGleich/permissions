package de.lennox.permissions.listener;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.InformativeSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

/**
 * Listens to all block placements and block breaks a player makes to create / remove informative
 * signs
 *
 * @since 1.0.0
 * @author Lennox
 */
public class PlayerSignListener implements Listener {

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    Block block = event.getBlock();

    List<Component> lines = event.lines();
    String firstLine = PlainTextComponentSerializer.plainText().serialize(lines.get(0));
    // Only modify signs with %showRank% in first line
    if (!firstLine.contains("%showRank%")) {
      return;
    }

    int x = block.getX();
    int y = block.getY();
    int z = block.getZ();
    String worldName = block.getWorld().getName();
    PlayerPermissionPlugin.getSingleton().getSignDriver().createSign(x, y, z, worldName);
    PlayerPermissionPlugin.getSingleton()
        .getSignRepository()
        .register(new InformativeSign(x, y, z, worldName));
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();
    Block block = event.getBlock();

    // Only invalidate signs
    if (block.getState() instanceof Sign) {
      permissions
          .getSignDriver()
          .deleteSign(
              new InformativeSign(
                  block.getX(), block.getY(), block.getZ(), block.getWorld().getName()));
      permissions.getSignRepository().invalidate(event.getBlock().getLocation());
    }
  }
}
