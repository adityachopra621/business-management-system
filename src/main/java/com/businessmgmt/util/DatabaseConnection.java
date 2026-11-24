package com.businessmgmt.util;

import com.businessmgmt.exception.DatabaseException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Singleton JDBC connection manager.
 * Reads configuration from application.properties and provides
 * connections to the PostgreSQL database.
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;

    private final String url;
    private final String user;
    private final String password;
    private final List<Connection> connectionPool;
    private static final int POOL_SIZE = 5;

    private DatabaseConnection() {
        AppConfig config = AppConfig.getInstance();
        this.url = config.get("db.url", "jdbc:postgresql://localhost:5432/business_mgmt");
        this.user = config.get("db.user", "postgres");
        this.password = config.get("db.password", "postgres");
        this.connectionPool = new ArrayList<>();
        logger.info("DatabaseConnection initialized with URL: " + url);
    }

    /**
     * Returns the singleton instance.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Static accessor for getting a database connection.
     *
     * @return a valid JDBC Connection
     * @throws DatabaseException if the connection cannot be established
     */
    public static Connection getConnection() throws DatabaseException {
        return getInstance().getConnectionInternal();
    }

    /**
     * Gets a connection from the pool or creates a new one.
     *
     * @return a valid JDBC Connection
     * @throws DatabaseException if the connection cannot be established
     */
    public synchronized Connection getConnectionInternal() throws DatabaseException {
        try {
            // Try to reuse a pooled connection
            while (!connectionPool.isEmpty()) {
                Connection conn = connectionPool.remove(connectionPool.size() - 1);
                if (conn != null && !conn.isClosed() && conn.isValid(2)) {
                    return conn;
                }
            }
            // Create a new connection
            Connection conn = DriverManager.getConnection(url, user, password);
            logger.fine("New database connection created.");
            return conn;
        } catch (SQLException e) {
            logger.severe("Failed to get database connection: " + e.getMessage());
            throw new DatabaseException("Failed to connect to database", e);
        }
    }

    /**
     * Returns a connection to the pool for reuse.
     *
     * @param conn the connection to return
     */
    public synchronized void releaseConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed() && conn.isValid(2)) {
                if (connectionPool.size() < POOL_SIZE) {
                    connectionPool.add(conn);
                } else {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            logger.warning("Error releasing connection: " + e.getMessage());
        }
    }

    /**
     * Initializes the database by executing the schema.sql script.
     *
     * @throws DatabaseException if schema initialization fails
     */
    public void initializeDatabase() throws DatabaseException {
        logger.info("Initializing database schema...");
        try (InputStream is = getClass().getResourceAsStream("/db/schema.sql")) {
            if (is == null) {
                throw new DatabaseException("Schema file not found: /db/schema.sql");
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.startsWith("--")) {
                        sb.append(line).append("\n");
                    }
                }
            }

            // Split by semicolons and execute each statement
            String[] statements = sb.toString().split(";");
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException ex) {
                            logger.warning("Notice executing statement: " + ex.getMessage());
                        }
                    }
                }
            }

            logger.info("Database schema initialized successfully.");
        } catch (SQLException e) {
            logger.severe("Failed to initialize database schema: " + e.getMessage());
            throw new DatabaseException("Failed to initialize database schema", e);
        } catch (Exception e) {
            logger.severe("Error reading schema file: " + e.getMessage());
            throw new DatabaseException("Error reading schema file", e);
        }
    }

    /**
     * Closes all pooled connections and resets the singleton.
     */
    public synchronized void close() {
        logger.info("Closing all database connections...");
        for (Connection conn : connectionPool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.warning("Error closing connection: " + e.getMessage());
            }
        }
        connectionPool.clear();
        instance = null;
        logger.info("All database connections closed.");
    }
}
