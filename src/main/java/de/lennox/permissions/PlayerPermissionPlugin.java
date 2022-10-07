package de.lennox.permissions;

import de.lennox.permissions.command.CommandRegistrar;
import de.lennox.permissions.database.PermissionDriver;
import de.lennox.permissions.database.postgre.PostgreSqlConfiguration;
import de.lennox.permissions.database.postgre.PostgreSqlGateway;
import de.lennox.permissions.group.PermissionGroupRepository;
import de.lennox.permissions.listener.PlayerChatListener;
import de.lennox.permissions.listener.PlayerStateListener;
import de.lennox.permissions.local.MessageLocalization;
import de.lennox.permissions.player.PermittedPlayerRepository;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;

@Getter
public class PlayerPermissionPlugin extends JavaPlugin {
  @Getter private static PlayerPermissionPlugin singleton;
  private PermittedPlayerRepository playerRepository;
  private PermissionGroupRepository groupRepository;
  private CommandRegistrar commandRegistrar;
  private PermissionDriver permissionDriver;
  private MessageLocalization localization;

  @Override
  public void onLoad() {
    singleton = this;
  }

  @Override
  public void onEnable() {
    this.commandRegistrar = new CommandRegistrar(this);
    PostgreSqlGateway postgreSqlGateway =
        new PostgreSqlGateway(
            PostgreSqlConfiguration.builder()
                .host("127.0.0.1:5432")
                .database("permissions")
                .user("postgres")
                .password("123")
                .build());
    this.permissionDriver = new PermissionDriver(postgreSqlGateway);
    this.playerRepository = new PermittedPlayerRepository();
    this.groupRepository = new PermissionGroupRepository();
    this.localization = MessageLocalization.ofLocale(Locale.GERMANY);

    List.of(commandRegistrar, new PlayerChatListener(), new PlayerStateListener())
        .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

    postgreSqlGateway.setup();
    this.commandRegistrar.setup();
    this.groupRepository.buildInitialCache();
  }

  @Override
  public void onDisable() {}
}
