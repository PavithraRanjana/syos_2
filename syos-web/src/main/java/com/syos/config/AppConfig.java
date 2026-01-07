package com.syos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application-wide configuration holder.
 * Loads and provides access to application properties.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "application.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    static {
        loadProperties();
    }

    private AppConfig() {
        // Prevent instantiation
    }

    /**
     * Loads properties from the configuration file.
     */
    private static synchronized void loadProperties() {
        if (loaded) return;

        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded application configuration from {}", CONFIG_FILE);
                loaded = true;
            } else {
                logger.warn("Configuration file {} not found, using defaults", CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.error("Error loading configuration file: {}", e.getMessage());
        }
    }

    /**
     * Gets a string property.
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a string property with a default value.
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property.
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for {}: {}", key, value);
            return defaultValue;
        }
    }

    /**
     * Gets a long property.
     */
    public static long getLong(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for {}: {}", key, value);
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Checks if development mode is enabled.
     */
    public static boolean isDevMode() {
        return getBoolean("dev.mode", false);
    }

    /**
     * Gets the application name.
     */
    public static String getAppName() {
        return get("app.name", "SYOS Retail Management System");
    }

    /**
     * Gets the application version.
     */
    public static String getAppVersion() {
        return get("app.version", "2.0.0");
    }

    /**
     * Gets the session timeout in minutes.
     */
    public static int getSessionTimeoutMinutes() {
        return getInt("session.timeout.minutes", 30);
    }

    /**
     * Gets the default reorder threshold.
     */
    public static int getDefaultReorderThreshold() {
        return getInt("inventory.reorder.threshold", 50);
    }

    /**
     * Gets the expiry warning threshold in days.
     */
    public static int getExpiryWarningDays() {
        return getInt("inventory.expiry.warning.days", 30);
    }

    /**
     * Gets the critical expiry threshold in days.
     */
    public static int getExpiryCriticalDays() {
        return getInt("inventory.expiry.critical.days", 7);
    }

    /**
     * Reloads configuration from file.
     */
    public static synchronized void reload() {
        properties.clear();
        loaded = false;
        loadProperties();
        logger.info("Configuration reloaded");
    }
}
