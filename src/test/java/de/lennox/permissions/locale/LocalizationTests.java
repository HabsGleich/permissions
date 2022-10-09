package de.lennox.permissions.locale;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalizationTests {

  @Test
  public void testGermanLocale() {
    LocalizationProvider provider =
        LocalizationProvider.of(Locale.GERMAN, getClass().getClassLoader());

    assertEquals(
        provider.getMessage("command.perms.player_not_found"),
        "Der angegebene Spieler konnte nicht gefunden werden");
    assertEquals(
        provider.getMessage("command.perms.set_group.success"),
        "Die Gruppe des angegebenen Spielers wurde erfolgreich gesetzt");
    assertEquals(
        provider.getMessage("command.perms.create.success"),
        "Die Gruppe wurde erfolgreich erstellt");
  }

  @Test
  public void testEnglishLocale() {
    LocalizationProvider provider =
        LocalizationProvider.of(Locale.ENGLISH, getClass().getClassLoader());

    assertEquals(
        provider.getMessage("command.perms.player_not_found"),
        "The given player could not be found");
    assertEquals(
        provider.getMessage("command.perms.set_group.success"),
        "The group of the provided player has been set successfully");
    assertEquals(
        provider.getMessage("command.perms.create.success"),
        "The group has been created successfully");
  }
}
