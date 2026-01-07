package com.syos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for HikariCP database connection pool.
 * Replaces the Singleton pattern from the CLI application with a proper connection pool.
 */
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);
    private static final String CONFIG_FILE = "application.properties";

    private static volatile HikariDataSource dataSource;
    private static final Object lock = new Object();

    private DataSourceConfig() {
        // Prevent instantiation
    }

    /**
     * Gets the shared DataSource instance (thread-safe lazy initialization).
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }

    /**
     * Creates a new HikariDataSource with configuration from properties file.
     */
    private static HikariDataSource createDataSource() {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();

        // Database connection settings
        config.setJdbcUrl(props.getProperty("db.url",
            "jdbc:mysql://localhost:3306/syos_grocery_store?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"));
        config.setUsername(props.getProperty("db.username", "root"));
        config.setPassword(props.getProperty("db.password", ""));
        config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

        // HikariCP pool settings
        config.setMaximumPoolSize(Integer.parseInt(
            props.getProperty("hikari.maximumPoolSize", "20")));
        config.setMinimumIdle(Integer.parseInt(
            props.getProperty("hikari.minimumIdle", "5")));
        config.setConnectionTimeout(Long.parseLong(
            props.getProperty("hikari.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(
            props.getProperty("hikari.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(
            props.getProperty("hikari.maxLifetime", "1800000")));
        config.setLeakDetectionThreshold(Long.parseLong(
            props.getProperty("hikari.leakDetectionThreshold", "60000")));

        // Pool name for logging
        config.setPoolName(props.getProperty("hikari.poolName", "SyosHikariPool"));

        // MySQL-specific optimizations
        config.addDataSourceProperty("cachePrepStmts",
            props.getProperty("hikari.dataSource.cachePrepStmts", "true"));
        config.addDataSourceProperty("prepStmtCacheSize",
            props.getProperty("hikari.dataSource.prepStmtCacheSize", "250"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit",
            props.getProperty("hikari.dataSource.prepStmtCacheSqlLimit", "2048"));
        config.addDataSourceProperty("useServerPrepStmts",
            props.getProperty("hikari.dataSource.useServerPrepStmts", "true"));

        logger.info("Creating HikariCP DataSource with pool size: {} - {}",
            config.getMinimumIdle(), config.getMaximumPoolSize());

        return new HikariDataSource(config);
    }

    /**
     * Creates a DataSource with specific connection parameters (for testing).
     */
    public static HikariDataSource createDataSource(String jdbcUrl, String username,
                                                     String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setPoolName("SyosTestPool");

        return new HikariDataSource(config);
    }

    /**
     * Loads properties from the configuration file.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DataSourceConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                logger.info("Loaded configuration from {}", CONFIG_FILE);
            } else {
                logger.warn("Configuration file {} not found, using defaults", CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.warn("Error loading configuration file: {}", e.getMessage());
        }
        return props;
    }

    /**
     * Closes the DataSource and releases all connections.
     * Should be called when the application shuts down.
     */
    public static void closeDataSource() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                logger.info("Closing HikariCP DataSource");
                dataSource.close();
                dataSource = null;
            }
        }
    }

    /**
     * Gets pool statistics for monitoring.
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Pool Stats - Active: %d, Idle: %d, Waiting: %d, Total: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                dataSource.getHikariPoolMXBean().getTotalConnections()
            );
        }
        return "DataSource not initialized";
    }
}
