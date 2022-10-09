package de.lennox.permissions.command;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeInputFormatterTests {
  @Test
  public void testSingleInput() {
    assertEquals(TimeInputFormatter.parseTimeInput("30d"), TimeUnit.DAYS.toMillis(30));
    assertEquals(TimeInputFormatter.parseTimeInput("15m"), TimeUnit.MINUTES.toMillis(15));
    assertEquals(TimeInputFormatter.parseTimeInput("5s"), TimeUnit.SECONDS.toMillis(5));
  }

  @Test
  public void testMultipleInputs() {
    assertEquals(
        TimeInputFormatter.parseTimeInput("30d 15m 5s"),
        TimeUnit.DAYS.toMillis(30) + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(5));
    assertEquals(
        TimeInputFormatter.parseTimeInput("15m 5s"),
        TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(5));
  }
}
