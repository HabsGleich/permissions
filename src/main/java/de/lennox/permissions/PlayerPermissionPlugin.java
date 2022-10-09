package de.lennox.permissions;

import de.lennox.permissions.command.CommandRegistrar;
import de.lennox.permissions.database.PermissionDriver;
import de.lennox.permissions.database.SignDriver;
import de.lennox.permissions.database.postgre.PostgrePermissionDriver;
import de.lennox.permissions.database.postgre.PostgreSignDriver;
import de.lennox.permissions.database.postgre.PostgreSqlConfiguration;
import de.lennox.permissions.database.postgre.PostgreSqlGateway;
import de.lennox.permissions.group.PermissionGroupRepository;
import de.lennox.permissions.listener.PlayerChatListener;
import de.lennox.permissions.listener.PlayerStateListener;
import de.lennox.permissions.locale.LocalizationRepository;
import de.lennox.permissions.player.AutomaticRankAssigner;
import de.lennox.permissions.player.PermittedPlayerRepository;
import de.lennox.permissions.player.PlayerLanguageRepository;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
public class PlayerPermissionPlugin extends JavaPlugin {
  @Getter private static PlayerPermissionPlugin singleton;
  private PlayerLanguageRepository playerLanguageRepository;
  private PermittedPlayerRepository playerRepository;
  private PermissionGroupRepository groupRepository;
  private CommandRegistrar commandRegistrar;
  private PermissionDriver permissionDriver;
  private LocalizationRepository localization;
  private AutomaticRankAssigner automaticRankAssigner;
  private SignDriver signDriver;

  @Override
  public void onLoad() {
    singleton = this;
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();
    this.commandRegistrar = new CommandRegistrar(this);

    FileConfiguration config = getConfig();
    PostgreSqlGateway postgreSqlGateway =
        new PostgreSqlGateway(
            PostgreSqlConfiguration.builder()
                .host(config.getString("database.host"))
                .database(config.getString("database.database"))
                .user(config.getString("database.username"))
                .password(config.getString("database.password"))
                .build());
    this.permissionDriver = new PostgrePermissionDriver(postgreSqlGateway);
    this.signDriver = new PostgreSignDriver(postgreSqlGateway);

    this.playerRepository = new PermittedPlayerRepository();
    this.groupRepository = new PermissionGroupRepository();
    this.localization = new LocalizationRepository();
    this.playerLanguageRepository = new PlayerLanguageRepository();
    this.automaticRankAssigner = new AutomaticRankAssigner();

    List.of(commandRegistrar, new PlayerChatListener(), new PlayerStateListener())
        .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

    postgreSqlGateway.setup();
    this.commandRegistrar.setup();
    this.groupRepository.buildInitialCache();
    this.localization.load(config);
    this.automaticRankAssigner.createTask();
  }
}
