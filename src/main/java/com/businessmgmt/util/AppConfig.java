package com.businessmgmt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Application configuration loader.
 * Loads properties from application.properties on the classpath
 * and provides typed access methods.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "/application.properties";
    private static AppConfig instance;

    private final Properties properties;

    private AppConfig() {
        properties = new Properties();
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                logger.info("Application configuration loaded from " + CONFIG_FILE);
            } else {
                logger.warning("Configuration file not found: " + CONFIG_FILE + ". Using defaults.");
            }
        } catch (IOException e) {
            logger.severe("Error loading configuration: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton instance.
     */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Gets a property value by key.
     *
     * @param key the property key
     * @return the value, or null if not found
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a property value by key with a default.
     *
     * @param key          the property key
     * @param defaultValue the default value if the key is missing
     * @return the value, or the default
     */
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property value.
     *
     * @param key          the property key
     * @param defaultValue the default value if the key is missing or invalid
     * @return the integer value
     */
    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                logger.warning("Invalid integer for key '" + key + "': " + value);
            }
        }
        return defaultValue;
    }

    /**
     * Gets a boolean property value.
     *
     * @param key          the property key
     * @param defaultValue the default value if the key is missing
     * @return the boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }
}
