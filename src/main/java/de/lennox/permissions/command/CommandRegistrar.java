package de.lennox.permissions.command;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.lennox.permissions.command.brigadier.PermsBrigadierCommand;
import de.lennox.permissions.command.brigadier.RankBrigadierCommand;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Registers the brigadier command(s) on server start-up
 *
 * @since 1.0.0
 * @author Lennox
 */
@RequiredArgsConstructor
public class CommandRegistrar implements Listener {
  private final Plugin plugin;

  /**
   * Creates the brigadier command(s) for the permission system
   *
   * @since 1.0.0
   */
  public void setup() {
    registerBrigadierCommand(new PermsBrigadierCommand());
    registerBrigadierCommand(new RankBrigadierCommand());
  }

  /**
   * Registers a given brigadier command
   *
   * @param command The command
   * @since 1.0.0
   */
  private void registerBrigadierCommand(Command command) {
    insertBrigadierCommand(command.getLabel(), command.createBrigadierLiteral());
  }

  /**
   * Inserts the given brigadier command with label in the command map of the server.
   *
   * <p>Automatically syncs commands after insertion
   *
   * @param label The command label / name
   * @param command The brigadier command
   * @since 1.0.0
   */
  private void insertBrigadierCommand(
      String label, Consumer<LiteralArgumentBuilder<CommandSourceStack>> command) {
    PaperBrigadierCommand paperBrigadierCommand = new PaperBrigadierCommand(label, command, plugin);
    Server server = plugin.getServer();
    server.getCommandMap().register(plugin.getName(), paperBrigadierCommand);
    ((CraftServer) server).syncCommands();
  }

  /**
   * Handles the registration of new brigadier commands after insertion to command map
   *
   * @param event The command registration event
   * @since 1.0.0
   */
  @EventHandler
  @SuppressWarnings({"UnstableApiUsage", "unchecked", "rawtypes"})
  private void onCommandRegistered(CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
    if (!(event.getCommand() instanceof PaperBrigadierCommand paperBrigadierCommand)) {
      return;
    }
    LiteralArgumentBuilder<CommandSourceStack> node =
        LiteralArgumentBuilder.literal(event.getCommandLabel());
    paperBrigadierCommand.command().accept(node);
    event.setLiteral((LiteralCommandNode) node.build());
  }
}
