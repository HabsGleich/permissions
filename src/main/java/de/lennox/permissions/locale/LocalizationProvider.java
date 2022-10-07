package de.lennox.permissions.locale;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Access point for internationalized messages
 *
 * @since 1.0.0
 * @author Lennox
 */
@RequiredArgsConstructor
public class LocalizationProvider {
  private static final String BUNDLE_KEY = "permissions";
  private final ClassLoader loader;
  private final Locale locale;
  private ResourceBundle bundle;

  /**
   * Loads the bundle for the given locale
   *
   * @since 1.0.0
   */
  public void load() {
    this.bundle = ResourceBundle.getBundle(BUNDLE_KEY, locale, loader);
  }

  /**
   * Gets a message from the localization bundle with the given key
   *
   * @param key Message key
   * @return The localized message
   */
  public String getMessage(String key) {
    return this.bundle.getString(key);
  }

  /**
   * Creates a new message localization for the given locale
   *
   * @param locale The locale
   * @return The message localization
   */
  public static LocalizationProvider ofLocale(Locale locale, ClassLoader loader) {
    LocalizationProvider provider = new LocalizationProvider(loader, locale);
    provider.load();
    return provider;
  }
}
