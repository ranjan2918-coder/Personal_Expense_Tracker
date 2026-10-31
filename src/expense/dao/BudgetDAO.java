package expense.dao;

import expense.database.DatabaseConnection;
import expense.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'budgets' table.
 * All queries use PreparedStatement.
 */
public class BudgetDAO {

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------
    /** Inserts a new budget row and returns the generated ID. */
    public int addBudget(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (category, monthly_limit, month, year) "
                   + "VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, budget.getCategory());
            ps.setDouble(2, budget.getMonthlyLimit());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------
    /** Returns all budgets ordered by year and month descending. */
    public List<Budget> getAllBudgets() throws SQLException {
        List<Budget> list = new ArrayList<Budget>();
        String sql = "SELECT id, category, monthly_limit, month, year "
                   + "FROM budgets ORDER BY year DESC, month DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Returns a single budget by ID, or null. */
    public Budget getBudgetById(int id) throws SQLException {
        String sql = "SELECT id, category, monthly_limit, month, year FROM budgets WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Returns the budget for a specific category, month, and year (or null).
     * Useful when checking whether a budget already exists before inserting.
     */
    public Budget getBudgetByCategoryMonthYear(String category, int month, int year)
            throws SQLException {
        String sql = "SELECT id, category, monthly_limit, month, year FROM budgets "
                   + "WHERE LOWER(category) = LOWER(?) AND month = ? AND year = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    /** Updates an existing budget row. Returns true if a row was affected. */
    public boolean updateBudget(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET category=?, monthly_limit=?, month=?, year=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, budget.getCategory());
            ps.setDouble(2, budget.getMonthlyLimit());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());
            ps.setInt(5, budget.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------
    /** Deletes a budget by ID. Returns true if a row was deleted. */
    public boolean deleteBudget(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------
    /** Returns the total monthly_limit for the given month/year across all categories. */
    public double getTotalBudget(int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monthly_limit), 0) FROM budgets "
                   + "WHERE month = ? AND year = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // ----------------------------------------------------------------
    // Private mapper
    // ----------------------------------------------------------------
    private Budget mapRow(ResultSet rs) throws SQLException {
        return new Budget(
                rs.getInt("id"),
                rs.getString("category"),
                rs.getDouble("monthly_limit"),
                rs.getInt("month"),
                rs.getInt("year"));
    }
}
