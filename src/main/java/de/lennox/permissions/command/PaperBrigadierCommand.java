package de.lennox.permissions.command;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.command.VanillaCommandWrapper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class PaperBrigadierCommand extends Command implements PluginIdentifiableCommand {
  private final Consumer<LiteralArgumentBuilder<CommandSourceStack>> command;
  private final Plugin plugin;

  public PaperBrigadierCommand(
      String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> command, Plugin plugin) {
    super(name);
    this.command = command;
    this.plugin = plugin;
  }

  @Override
  public boolean execute(
      @Nullable CommandSender sender, @Nullable String commandLabel, String[] args) {
    // Make sure the command execution is valid
    Preconditions.checkNotNull(sender, "Command sender can't be null!");
    Preconditions.checkNotNull(commandLabel, "Command label can't be null!");

    String joined = String.join(" ", args);
    String argsString = joined.isBlank() ? "" : " " + joined;
    ((CraftServer) Bukkit.getServer())
        .getServer()
        .getCommands()
        .performPrefixedCommand(
            VanillaCommandWrapper.getListener(sender), commandLabel + argsString, commandLabel);
    return true;
  }

  @Override
  public @NotNull List<String> tabComplete(
      @Nullable CommandSender sender,
      @Nullable String alias,
      @Nullable String[] args,
      @Nullable Location location) {
    // Make sure the tab completion is valid
    Preconditions.checkNotNull(sender, "Command sender can't be null!");

    String joined = String.join(" ", args);
    String argsString = joined.isBlank() ? "" : joined;
    CommandDispatcher<CommandSourceStack> dispatcher =
        ((CraftServer) Bukkit.getServer()).getServer().getCommands().getDispatcher();
    ParseResults<CommandSourceStack> results =
        dispatcher.parse(
            new StringReader(alias + " " + argsString), VanillaCommandWrapper.getListener(sender));
    return dispatcher
        .getCompletionSuggestions(results)
        .thenApply(result -> result.getList().stream().map(Suggestion::getText).toList())
        .join();
  }

  @NotNull
  @Override
  public Plugin getPlugin() {
    return plugin;
  }

  public Consumer<LiteralArgumentBuilder<CommandSourceStack>> command() {
    return command;
  }
}
