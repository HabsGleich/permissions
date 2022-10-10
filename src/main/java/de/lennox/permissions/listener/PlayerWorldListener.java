package de.lennox.permissions.listener;

import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.database.model.InformativeSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
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
public class PlayerWorldListener implements Listener {

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    Block block = event.getBlock();

    List<Component> lines = event.lines();
    String firstLine = PlainTextComponentSerializer.plainText().serialize(lines.get(0));
    // Only modify signs with %showRank% in first line
    if (!firstLine.contains("%showRank%")) {
      return;
    }

    PlayerPermissionPlugin.getSingleton()
        .getSignRepository()
        .register(
            new InformativeSign(
                block.getX(), block.getY(), block.getZ(), block.getWorld().getName()));
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
  }
}
