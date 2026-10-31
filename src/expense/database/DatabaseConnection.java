package expense.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single shared JDBC Connection to the MariaDB database.
 *
 * Usage:
 *   Connection conn = DatabaseConnection.getConnection();
 *
 * Call DatabaseConnection.close() when the application shuts down.
 */
public final class DatabaseConnection {

    private static Connection connection = null;

    private DatabaseConnection() { }

    /**
     * Returns the existing connection or opens a new one.
     * Thread-safe for single-threaded desktop / embedded server use.
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DatabaseConfig.DRIVER_CLASS);
                connection = DriverManager.getConnection(
                        DatabaseConfig.URL,
                        DatabaseConfig.USERNAME,
                        DatabaseConfig.PASSWORD);
                System.out.println("[DB] Connected to " + DatabaseConfig.DB_NAME
                        + " on " + DatabaseConfig.HOST + ":" + DatabaseConfig.PORT);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MariaDB JDBC driver not found. "
                    + "Add mariadb-java-client-*.jar to the project build path.\n"
                    + "Download from: https://mariadb.com/downloads/connectors/",
                    e);
        }
        return connection;
    }

    /** Closes the shared connection gracefully. */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
