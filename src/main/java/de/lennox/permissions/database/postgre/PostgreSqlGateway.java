package de.lennox.permissions.database.postgre;

import de.lennox.permissions.database.builder.StatementBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Connection gateway to the postgresql database
 *
 * @author Lennox
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class PostgreSqlGateway {
  private final PostgreSqlConfiguration config;
  private Connection connection;

  /**
   * Connects to the postgresql database and prepares all tables after successful connection
   *
   * @since 1.0.0
   */
  @SneakyThrows
  public void setup() {
    DriverManager.registerDriver(new Driver());
    // Establish a connection to the database
    this.connection =
        DriverManager.getConnection(config.createJdbcUri(), config.getUser(), config.getPassword());
    // Prepare all tables
    prepareTables();
  }

  /**
   * Prepares all tables if not existing in the database
   *
   * @since 1.0.0
   */
  private void prepareTables() {
    // Create permission groups table
    StatementBuilder.forConnection(connection)
        .withSql(
            """
                CREATE TABLE IF NOT EXISTS permission_groups(
                  "name" VARCHAR PRIMARY KEY NOT NULL,
                  prefix VARCHAR NOT NULL,
                  "default" BOOLEAN NOT NULL
                )
            """)
        .execute();
    // Create permitted players table
    StatementBuilder.forConnection(connection)
        .withSql(
            """
                CREATE TABLE IF NOT EXISTS permitted_players(
                  id CHAR(36) PRIMARY KEY NOT NULL,
                  "group" VARCHAR NOT NULL,
                  expiration_date BIGINT NOT NULL
                )
            """)
        .execute();
    // Create permission list table
    StatementBuilder.forConnection(connection)
        .withSql(
            """
               CREATE TABLE IF NOT EXISTS group_permissions(
                 "group" VARCHAR NOT NULL,
                 permission VARCHAR NOT NULL,
                 denied BOOLEAN NOT NULL
               )
            """)
        .execute();
  }
}
