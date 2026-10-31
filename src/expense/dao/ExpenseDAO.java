package expense.dao;

import expense.database.DBConnection;
import expense.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ExpenseDAO – complete CRUD + search/filter operations for the `expenses` table.
 *
 * All SQL queries use PreparedStatement for safety.
 * Every method scopes results to the logged-in user_id.
 */
public class ExpenseDAO {

    // ──────────────────────── CREATE ────────────────────────

    /**
     * Inserts a new expense row and returns the generated expense_id.
     * Returns -1 if the insert fails.
     */
    public int addExpense(int userId, String title, double amount, String category,
                          String description, LocalDate date) {
        String sql = "INSERT INTO expenses (user_id, title, amount, category, description, expense_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, title.trim());
            ps.setDouble(3, amount);
            ps.setString(4, category.trim());
            ps.setString(5, description != null ? description.trim() : "");
            ps.setDate(6, Date.valueOf(date));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] addExpense error: " + e.getMessage());
        }
        return -1;
    }

    // ──────────────────────── READ ────────────────────────

    /**
     * Returns all expenses for a given user, ordered by date descending.
     */
    public List<Expense> getAllExpenses(int userId) {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? ORDER BY expense_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getAllExpenses error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds a single expense by its primary key.
     */
    public Expense findById(int userId, int expenseId) {
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? AND expense_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, expenseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns expenses matching the given category (case-insensitive).
     */
    public List<Expense> searchByCategory(int userId, String category) {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? AND LOWER(category) = LOWER(?) " +
                     "ORDER BY expense_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category.trim());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] searchByCategory error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Full-text search across title, category, and description.
     */
    public List<Expense> searchByKeyword(int userId, String keyword) {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? " +
                     "AND (LOWER(title) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) " +
                     "OR LOWER(description) LIKE LOWER(?)) ORDER BY expense_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setInt(1, userId);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] searchByKeyword error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns expenses for a specific month and year.
     */
    public List<Expense> getExpensesByMonth(int userId, int month, int year) {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? " +
                     "AND MONTH(expense_date) = ? AND YEAR(expense_date) = ? " +
                     "ORDER BY expense_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getExpensesByMonth error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns the total amount spent in a given month / year.
     */
    public double getMonthlyTotal(int userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses " +
                     "WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getMonthlyTotal error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Returns the grand total of all expenses for a user.
     */
    public double getGrandTotal(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getGrandTotal error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Returns category-wise totals as a simple 2-column result set mapped to String->Double.
     */
    public List<Object[]> getCategorySummary(int userId) {
        List<Object[]> rows = new ArrayList<Object[]>();
        String sql = "SELECT category, SUM(amount) AS total FROM expenses " +
                     "WHERE user_id = ? GROUP BY category ORDER BY category";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new Object[]{ rs.getString("category"), rs.getDouble("total") });
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getCategorySummary error: " + e.getMessage());
        }
        return rows;
    }

    /**
     * Returns all expenses sorted by amount ascending.
     */
    public List<Expense> getSortedByAmount(int userId) {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? ORDER BY amount ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getSortedByAmount error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all expenses sorted by date ascending.
     */
    public List<Expense> getSortedByDate(int userId) {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT expense_id, title, category, amount, description, expense_date " +
                     "FROM expenses WHERE user_id = ? ORDER BY expense_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] getSortedByDate error: " + e.getMessage());
        }
        return list;
    }

    // ──────────────────────── UPDATE ────────────────────────

    /**
     * Updates an existing expense record. Returns true if exactly one row was updated.
     */
    public boolean updateExpense(int userId, int expenseId, String title, double amount,
                                 String category, String description, LocalDate date) {
        String sql = "UPDATE expenses SET title=?, amount=?, category=?, description=?, expense_date=? " +
                     "WHERE expense_id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title.trim());
            ps.setDouble(2, amount);
            ps.setString(3, category.trim());
            ps.setString(4, description != null ? description.trim() : "");
            ps.setDate(5, Date.valueOf(date));
            ps.setInt(6, expenseId);
            ps.setInt(7, userId);

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] updateExpense error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────── DELETE ────────────────────────

    /**
     * Deletes an expense by its ID. Returns true if the row was removed.
     */
    public boolean deleteExpense(int userId, int expenseId) {
        String sql = "DELETE FROM expenses WHERE expense_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] deleteExpense error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────── Helper ────────────────────────

    /** Maps a ResultSet row to an Expense object. */
    private Expense mapRow(ResultSet rs) throws SQLException {
        int      id          = rs.getInt("expense_id");
        String   title       = rs.getString("title");
        String   category    = rs.getString("category");
        double   amount      = rs.getDouble("amount");
        String   description = rs.getString("description");
        LocalDate date       = rs.getDate("expense_date").toLocalDate();
        return new Expense(id, title, category, amount, description, date);
    }
}
