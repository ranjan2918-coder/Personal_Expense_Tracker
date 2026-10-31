package expense.database;

/**
 * Central database configuration.
 * Change only this file to update connection settings.
 */
public final class DatabaseConfig {

    // XAMPP MariaDB defaults
    public static final String HOST     = "localhost";
    public static final int    PORT     = 3306;
    public static final String DB_NAME  = "expense_tracker";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";          // XAMPP default has no password

    // Full JDBC URL for MariaDB
    public static final String URL =
            "jdbc:mariadb://" + HOST + ":" + PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // JDBC driver class name
    public static final String DRIVER_CLASS = "org.mariadb.jdbc.Driver";

    private DatabaseConfig() { /* prevent instantiation */ }
}
