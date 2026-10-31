package expense.dao;

import expense.database.DBConnection;
import expense.model.User;

import java.sql.*;

/**
 * UserDAO – handles all database operations for the `users` table.
 *
 * Operations:
 *   registerUser  – INSERT a new user
 *   loginUser     – SELECT + verify credentials
 *   usernameExists – check for duplicate usernames
 */
public class UserDAO {

    /**
     * Registers a new user in the database.
     *
     * @return the generated user_id, or -1 if registration fails.
     */
    public int registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] registerUser error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Validates login credentials.
     *
     * @return the matching User object, or null if credentials are wrong.
     */
    public User loginUser(String username, String password) {
        String sql = "SELECT user_id, username, password FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loginUser error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns true if the username is already taken.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[UserDAO] usernameExists error: " + e.getMessage());
        }
        return false;
    }
}
