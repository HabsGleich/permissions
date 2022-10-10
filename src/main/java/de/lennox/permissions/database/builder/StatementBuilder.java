package de.lennox.permissions.database.builder;

import com.google.common.base.Preconditions;
import de.lennox.permissions.PlayerPermissionPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Builder for easy creation and execution of jdbc connection statements
 *
 * @since 1.0.0
 * @author Lennox
 */
public class StatementBuilder {
  private final List<Object> parameters = new ArrayList<>();
  private final Connection connection;
  private String sql;

  public StatementBuilder(Connection connection) {
    this.connection = connection;
  }

  /**
   * Creates a new statement builder for the given jdbc connection
   *
   * @param connection The jdbc connection
   * @return The statement builder
   * @since 1.0.0
   */
  public static StatementBuilder forConnection(Connection connection) {
    return new StatementBuilder(connection);
  }

  /**
   * Sets the sql statement
   *
   * @param sql The sql statement
   * @return The current builder
   * @since 1.0.0
   */
  public StatementBuilder withSql(String sql) {
    this.sql = sql;
    return this;
  }

  /**
   * Attaches a singular parameter to the parameter list
   *
   * @param obj The parameter
   * @return The current builder
   * @since 1.0.0
   */
  public StatementBuilder withParameter(Object obj) {
    parameters.add(obj);
    return this;
  }

  /**
   * Attaches multiple parameters to the parameter list
   *
   * @param objs The object vararg
   * @return The current builder
   * @since 1.0.0
   */
  public StatementBuilder withParameters(Object... objs) {
    Collections.addAll(parameters, objs);
    return this;
  }

  /**
   * Executes a normal sql statement without data return value
   *
   * @return The success state
   * @since 1.0.0
   */
  public boolean execute() {
    Preconditions.checkNotNull(sql, "Please provide an sql statement to execute!");
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      // Attach all parameters
      for (int i = 0; i < parameters.size(); i++) {
        statement.setObject(i + 1, parameters.get(i));
      }
      // Execute the statement
      return statement.execute();
    } catch (SQLException e) {
      PlayerPermissionPlugin.getSingleton()
          .getLogger()
          .log(Level.SEVERE, "Failed to create new jdbc statement!", e);
      return false;
    }
  }

  /**
   * Executes a query sql statement with data as return value in form of a ResultSet
   *
   * @return The optional result set
   * @since 1.0.0
   * @see ResultSet
   */
  public Optional<ResultSet> executeQuery() {
    Preconditions.checkNotNull(sql, "Please provide an sql statement to execute!");
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      // Attach all parameters
      for (int i = 0; i < parameters.size(); i++) {
        statement.setObject(i + 1, parameters.get(i));
      }
      // Execute the statement
      return Optional.of(statement.executeQuery());
    } catch (SQLException e) {
      PlayerPermissionPlugin.getSingleton()
          .getLogger()
          .log(Level.SEVERE, "Failed to create new jdbc query statement!", e);
      return Optional.empty();
    }
  }
}
