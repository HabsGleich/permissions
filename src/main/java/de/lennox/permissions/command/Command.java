package de.lennox.permissions.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import de.lennox.permissions.PlayerPermissionPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * The parental command class for all created brigadier commands.
 *
 * @author Lennox
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public abstract class Command {
  private final String label;

  /**
   * Creates the main brigadier command handle literal
   *
   * @return The literal consumer
   * @since 1.0.0
   */
  public abstract Consumer<LiteralArgumentBuilder<CommandSourceStack>> createBrigadierLiteral();

  /**
   * Creates a new literal builder from the given label
   *
   * @param label The label
   * @return The literal builder
   * @since 1.0.0
   */
  protected LiteralArgumentBuilder<CommandSourceStack> literal(String label) {
    return LiteralArgumentBuilder.literal(label);
  }

  /**
   * Creates a new argument builder from the given label and argument type
   *
   * @param label The label
   * @param type The type of argument
   * @return The argument builder
   * @since 1.0.0
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected RequiredArgumentBuilder<CommandSourceStack, String> argument(
      String label, ArgumentType type) {
    return RequiredArgumentBuilder.<CommandSourceStack, String>argument(label, type);
  }

  /**
   * Resolves a players language and returns the localized message
   *
   * @param player The player
   * @param key The message key
   * @return The localized message
   * @since 1.0.0
   */
  protected String getLocalizedMessage(UUID player, String key) {
    PlayerPermissionPlugin permissions = PlayerPermissionPlugin.getSingleton();

    return permissions
        .getLocalization()
        .getMessage(permissions.getPlayerLanguageRepository().get(player), key);
  }
}
