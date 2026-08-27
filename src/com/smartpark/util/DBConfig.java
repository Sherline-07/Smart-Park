package com.smartpark.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * DBConfig loads application and database settings from config/db.properties.
 */
public class DBConfig {
    private static final Properties props = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Try loading from file system first
        File configFile = new File("config/db.properties");
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                return;
            } catch (Exception e) {
                System.err.println("Warning: Could not read config/db.properties: " + e.getMessage());
            }
        }
        
        // Try relative to parent
        File altConfig = new File("../config/db.properties");
        if (altConfig.exists()) {
            try (InputStream in = new FileInputStream(altConfig)) {
                props.load(in);
                return;
            } catch (Exception e) {
                // Ignore
            }
        }

        // Set safe defaults
        props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "root");
        props.setProperty("db.fallback.enabled", "true");
        props.setProperty("db.fallback.driver", "org.h2.Driver");
        props.setProperty("db.fallback.url", "jdbc:h2:./smart_parking_db;MODE=MySQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE");
        props.setProperty("db.fallback.user", "sa");
        props.setProperty("db.fallback.password", "");
        props.setProperty("pricing.2w.base", "20.0");
        props.setProperty("pricing.4w.base", "40.0");
        props.setProperty("pricing.occupancy.threshold", "80.0");
        props.setProperty("pricing.2w.surge", "30.0");
        props.setProperty("pricing.4w.surge", "60.0");
        props.setProperty("server.port", "8080");
    }

    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static String getDbDriver() {
        return props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static String getDbUrl() {
        return props.getProperty("db.url", "jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getDbUser() {
        return props.getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        return props.getProperty("db.password", "root");
    }

    public static boolean isFallbackEnabled() {
        return Boolean.parseBoolean(props.getProperty("db.fallback.enabled", "true"));
    }

    public static String getFallbackDriver() {
        return props.getProperty("db.fallback.driver", "org.h2.Driver");
    }

    public static String getFallbackUrl() {
        return props.getProperty("db.fallback.url", "jdbc:h2:./smart_parking_db;MODE=MySQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE");
    }

    public static String getFallbackUser() {
        return props.getProperty("db.fallback.user", "sa");
    }

    public static String getFallbackPassword() {
        return props.getProperty("db.fallback.password", "");
    }

    public static double get2WBaseRate() {
        return Double.parseDouble(props.getProperty("pricing.2w.base", "20.0"));
    }

    public static double get4WBaseRate() {
        return Double.parseDouble(props.getProperty("pricing.4w.base", "40.0"));
    }

    public static double getOccupancyThreshold() {
        return Double.parseDouble(props.getProperty("pricing.occupancy.threshold", "80.0"));
    }

    public static double get2WSurgeRate() {
        return Double.parseDouble(props.getProperty("pricing.2w.surge", "30.0"));
    }

    public static double get4WSurgeRate() {
        return Double.parseDouble(props.getProperty("pricing.4w.surge", "60.0"));
    }

    public static int getServerPort() {
        return Integer.parseInt(props.getProperty("server.port", "8080"));
    }
}
