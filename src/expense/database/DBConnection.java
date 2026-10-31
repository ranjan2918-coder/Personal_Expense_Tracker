package expense.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection – central JDBC connection manager.
 *
 * HOW TO CONFIGURE:
 *   Edit the three constants below to match your MySQL installation:
 *     DB_URL      – keep as-is unless MySQL runs on a different port
 *     DB_USER     – your MySQL username (default: root)
 *     DB_PASSWORD – your MySQL password
 *
 * Only one Connection object is shared across the application (singleton).
 * Call DBConnection.getConnection() wherever a connection is needed.
 */
public class DBConnection {

    // ── Database credentials – change only these three values ──
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/personal_expense_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "root"; // <-- change to your MySQL password

    private static Connection connection = null;

    // Private constructor – this class should never be instantiated
    private DBConnection() { }

    /**
     * Returns the shared Connection, creating it on first call.
     *
     * @return a live java.sql.Connection
     * @throws SQLException if the driver is missing or credentials are wrong
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the MySQL JDBC driver (required for Java 8 compatibility)
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("[DBConnection] Connected to MySQL database successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC Driver not found.\n" +
                    "Add mysql-connector-j-*.jar to your Eclipse build path.\n" +
                    "See setup instructions in SQL_SETUP.sql.", e);
            }
        }
        return connection;
    }

    /**
     * Closes the shared connection gracefully.
     * Call this only when the application exits.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DBConnection] Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Quick connectivity test – returns true if the DB is reachable.
     */
    public static boolean testConnection() {
        try {
            Connection c = getConnection();
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
