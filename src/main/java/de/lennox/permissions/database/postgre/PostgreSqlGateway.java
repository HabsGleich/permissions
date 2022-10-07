package de.lennox.permissions.database.postgre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
  public void setup() {
    try {
      DriverManager.registerDriver(new Driver());
      // Establish a connection to the database
      this.connection =
          DriverManager.getConnection(
              config.createJdbcUri(), config.getUser(), config.getPassword());
    } catch (SQLException e) {
      System.err.println(
          "Unable to connect to the provided database, are you sure your credentials are correct? The plugin cannot function properly!");
      throw new RuntimeException(e);
    }
    // Prepare all tables
    prepareTables();
  }

  /**
   * Prepares all tables if not existing in the database
   *
   * @since 1.0.0
   */
  private void prepareTables() {
    try {
      // Create permission groups table
      connection
          .createStatement()
          .execute(
              """
        CREATE TABLE IF NOT EXISTS permission_groups(
          "name" VARCHAR PRIMARY KEY NOT NULL,
          prefix VARCHAR NOT NULL,
          "default" BOOLEAN NOT NULL
        )
        """);
      // Create permitted players table
      connection
          .createStatement()
          .execute(
              """
        CREATE TABLE IF NOT EXISTS permitted_players(
          id CHAR(36) PRIMARY KEY NOT NULL,
          "group" VARCHAR NOT NULL,
          expiration_date BIGINT NOT NULL
        )
        """);
      // Create permission list table
      connection
          .createStatement()
          .execute(
              """
        CREATE TABLE IF NOT EXISTS group_permissions(
          "group" VARCHAR NOT NULL,
          permission VARCHAR NOT NULL,
          denied BOOLEAN NOT NULL
        )
        """);
    } catch (SQLException e) {
      System.err.println(
          "Something went horribly wrong while trying to prepare the database tables. The plugin cannot function properly!");
      throw new RuntimeException(e);
    }
  }
}
