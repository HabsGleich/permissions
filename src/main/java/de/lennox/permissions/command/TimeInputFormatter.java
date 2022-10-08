package de.lennox.permissions.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * Formats time inputs (e.g. 30d 2m 27s)
 *
 * @since 1.0.0
 * @author Lennox
 */
public class TimeInputFormatter {

  /**
   * Parses the time input and converts it to a unix time offset
   *
   * @param input The text input
   * @return Unix time offset
   * @since 1.0.0
   */
  public static long parseTimeInput(String input) {
    long offset = 0;

    for (String timeStatement : input.split(" ")) {
      // A valid time statement can't be less than 2 characters
      if (timeStatement.length() < 2) {
        continue;
      }

      TemporalType type =
          TemporalType.ofIdentifier(timeStatement.charAt(timeStatement.length() - 1));
      // If invalid type was passed continue with parsing next statements
      if (type == null) {
        continue;
      }

      int amountProvided = Integer.parseInt(timeStatement.substring(0, timeStatement.length() - 1));
      offset += type.getUnit().toMillis(amountProvided);
    }
    return offset;
  }

  /**
   * All different supported temporal types
   *
   * @since 1.0.0
   */
  @Getter
  @AllArgsConstructor
  public enum TemporalType {
    DAYS('d', TimeUnit.DAYS),
    MINUTES('m', TimeUnit.MINUTES),
    SECONDS('s', TimeUnit.SECONDS);

    final char identifier;
    final TimeUnit unit;

    /**
     * Gets an input type by identifier char
     *
     * @param identifier The identifier char
     * @return The input type or null
     */
    public static TemporalType ofIdentifier(char identifier) {
      for (TemporalType value : values()) {
        if (value.identifier == identifier) {
          return value;
        }
      }
      return null;
    }
  }
}
