package de.lennox.permissions.locale;

import de.lennox.permissions.PlayerPermissionPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the I18n localization equivalents for the configured languages
 *
 * @since 1.0.0
 * @author Lennox
 */
@Getter
public class LocalizationRepository {
  private static final String[] PROVIDED_BUNDLES = new String[] {"de", "en"};
  private final Map<String, LocalizationProvider> localeCache = new HashMap<>();

  /**
   * Loads all in the configuration provided localization bundles.
   *
   * @param config The plugin configuration
   * @since 1.0.0
   */
  @SneakyThrows
  public void load(FileConfiguration config) {
    extractLanguageBundles();

    Logger logger = PlayerPermissionPlugin.getSingleton().getLogger();
    //noinspection unchecked
    List<String> languages = (List<String>) config.getList("languages");
    // Stop loading of plugin if no languages are provided
    if (languages == null) {
      logger.log(
          Level.SEVERE,
          "Could not find languages attribute in config file, plugin cannot continue operating.");
      throw new IllegalStateException("Missing language attribute in config");
    }

    File languageBundleFolder = new File("plugins/PlayerPermissions/languages/");
    // Do not proceed loading if the language bundles are absent
    if (!languageBundleFolder.exists()) {
      logger.log(
          Level.SEVERE,
          "Could not find language bundle folder, please check the folder permissions");
      return;
    }

    ClassLoader languageBundleLoader =
        new URLClassLoader(new URL[] {languageBundleFolder.toURI().toURL()});
    for (String language : languages) {
      localeCache.put(
          language, LocalizationProvider.of(Locale.forLanguageTag(language), languageBundleLoader));
    }
  }

  /**
   * Extracts all provided language bundles from the jar file for easy modification
   *
   * @since 1.0.0
   */
  @SneakyThrows
  public void extractLanguageBundles() {
    String languageFileFormat = "plugins/PlayerPermissions/languages/permissions_%s.properties";
    String languageResourceFormat = "/permissions_%s.properties";
    for (String providedBundle : PROVIDED_BUNDLES) {
      InputStream resource =
          getClass().getResourceAsStream(String.format(languageResourceFormat, providedBundle));
      // Don't load the resource if unavailable
      if (resource == null) {
        PlayerPermissionPlugin.getSingleton()
            .getLogger()
            .log(
                Level.WARNING,
                "Could not find " + providedBundle + " language bundle in jar file, skipping...");
        continue;
      }

      File languageFile = new File(String.format(languageFileFormat, providedBundle));
      // Only extract if file doesn't exist
      if (languageFile.exists()) {
        continue;
      }
      //noinspection ResultOfMethodCallIgnored
      languageFile.getParentFile().mkdirs();
      FileOutputStream languageFileOutput = new FileOutputStream(languageFile);
      languageFileOutput.write(resource.readAllBytes());
      languageFileOutput.flush();
      languageFileOutput.close();
    }
  }

  /**
   * Gets a message for the given key and language
   *
   * @param lang The lang / language
   * @param key The message key
   * @return The localized string
   * @since 1.0.0
   */
  public String getMessage(String lang, String key) {
    // Return the key on invalid language
    if (!localeCache.containsKey(lang)) {
      return key;
    }

    return localeCache.get(lang).getMessage(key);
  }
}
