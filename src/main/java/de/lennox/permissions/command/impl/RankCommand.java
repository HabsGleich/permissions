package de.lennox.permissions.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.lennox.permissions.PlayerPermissionPlugin;
import de.lennox.permissions.command.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public class RankCommand extends Command {
  public RankCommand() {
    super("rank");
  }

  /**
   * Creates the main brigadier rank command
   *
   * <p>First gets the player from cache then sends him his rank information.
   *
   * @return The command
   * @since 1.0.0
   */
  @Override
  public Consumer<LiteralArgumentBuilder<CommandSourceStack>> createBrigadierLiteral() {
    return literal ->
        literal.executes(
            context -> {
              CommandSender sender = context.getSource().getBukkitSender();

              if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                PlayerPermissionPlugin.getSingleton()
                    .getPlayerRepository()
                    .getPermittedPlayer(player.getUniqueId())
                    .whenCompleteAsync(
                        (permittedPlayer, t) -> {
                          String groupName =
                              permittedPlayer.getGroup().isEmpty()
                                  ? "Default"
                                  : permittedPlayer.getGroup();
                          long expirationTime = permittedPlayer.getExpiresAt();

                          // Display expiring message if date is set
                          if (expirationTime != -1) {
                            sender.sendMessage(
                                Component.text(
                                    String.format(
                                        getLocalizedMessage(uuid, "command.rank.info.expiring"),
                                        groupName,
                                        permittedPlayer.parseExpiryDate()),
                                    NamedTextColor.GRAY));
                          } else {
                            sender.sendMessage(
                                Component.text(
                                    String.format(
                                        getLocalizedMessage(uuid, "command.rank.info"), groupName),
                                    NamedTextColor.GRAY));
                          }
                        });
              }
              return 1;
            });
  }
}
