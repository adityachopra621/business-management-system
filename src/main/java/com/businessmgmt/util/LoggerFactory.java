package com.businessmgmt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Factory for obtaining configured {@link Logger} instances.
 * Loads logging configuration from log.properties on the classpath.
 */
public class LoggerFactory {

    private static boolean initialized = false;

    private LoggerFactory() {
        // Utility class — no instantiation
    }

    /**
     * Initializes the logging system from log.properties.
     * Safe to call multiple times; initialization happens only once.
     */
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        try (InputStream is = LoggerFactory.class.getResourceAsStream("/log.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            } else {
                System.err.println("WARNING: log.properties not found on classpath. Using JVM defaults.");
            }
        } catch (IOException e) {
            System.err.println("WARNING: Failed to load log.properties: " + e.getMessage());
        }
        initialized = true;
    }

    /**
     * Returns a Logger for the given class, configured from log.properties.
     *
     * @param clazz the class requesting the logger
     * @return a configured Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        initialize();
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Returns a Logger with the given name, configured from log.properties.
     *
     * @param name the logger name
     * @return a configured Logger instance
     */
    public static Logger getLogger(String name) {
        initialize();
        return Logger.getLogger(name);
    }
}
