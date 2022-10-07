package de.lennox.permissions.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.lennox.permissions.command.Command;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

public class RankCommand extends Command {
  public RankCommand() {
    super("rank");
  }

  @Override
  public Consumer<LiteralArgumentBuilder<CommandSourceStack>> createBrigadierLiteral() {
    return literal ->
        literal.executes(
            context -> {
              return 1;
            });
  }
}
