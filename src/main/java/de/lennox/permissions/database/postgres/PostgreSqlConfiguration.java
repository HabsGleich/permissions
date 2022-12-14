package de.lennox.permissions.database.postgres;

import lombok.Builder;
import lombok.Data;

/**
 * PostgreSQL authentication data class
 *
 * @since 1.0.0
 * @author Lennox
 */
@Data
@Builder
public class PostgreSqlConfiguration {
  private String host;
  private String database;
  private String user;
  private String password;
  private long expirationThreshold;

  /**
   * Creates a jdbc connection string from the host and database provided in the configuration
   *
   * @return The jdbc connection string
   */
  public String createJdbcUri() {
    return "jdbc:postgresql://" + host + "/" + database;
  }
}
