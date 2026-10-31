package expense.dao;

import expense.database.DBConnection;
import expense.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BudgetDAO – CRUD operations for the `budgets` table.
 *
 * Each budget row stores: user_id, category, monthly_limit, month, year.
 * An "Overall" budget covers the global monthly spending limit.
 */
public class BudgetDAO {

    // ──────────────────────── CREATE / UPSERT ────────────────────────

    /**
     * Sets (inserts or updates) a budget for a given category + month + year.
     * Uses MySQL's INSERT ... ON DUPLICATE KEY UPDATE pattern via unique index.
     *
     * @return the budget_id on success, -1 on failure.
     */
    public int setOrUpdateBudget(int userId, String category, double monthlyLimit,
                                  int month, int year) {
        // First check if a budget already exists for this user/category/month/year
        Budget existing = getBudget(userId, category, month, year);
        if (existing != null) {
            // Update
            String sql = "UPDATE budgets SET monthly_limit = ? " +
                         "WHERE user_id = ? AND category = ? AND month = ? AND year = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDouble(1, monthlyLimit);
                ps.setInt(2, userId);
                ps.setString(3, category);
                ps.setInt(4, month);
                ps.setInt(5, year);
                ps.executeUpdate();
                return existing.getBudgetId();
            } catch (SQLException e) {
                System.err.println("[BudgetDAO] updateBudget error: " + e.getMessage());
            }
        } else {
            // Insert
            String sql = "INSERT INTO budgets (user_id, category, monthly_limit, month, year) " +
                         "VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, userId);
                ps.setString(2, category);
                ps.setDouble(3, monthlyLimit);
                ps.setInt(4, month);
                ps.setInt(5, year);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) return keys.getInt(1);
                }
            } catch (SQLException e) {
                System.err.println("[BudgetDAO] insertBudget error: " + e.getMessage());
            }
        }
        return -1;
    }

    // ──────────────────────── READ ────────────────────────

    /**
     * Retrieves the budget for a specific category/month/year.
     * Returns null if none has been set.
     */
    public Budget getBudget(int userId, String category, int month, int year) {
        String sql = "SELECT budget_id, user_id, category, monthly_limit, month, year " +
                     "FROM budgets WHERE user_id = ? AND category = ? AND month = ? AND year = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setInt(3, month);
            ps.setInt(4, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] getBudget error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the "Overall" budget for the current month/year.
     * Returns a default Budget of 10000 if none is stored yet.
     */
    public Budget getOverallBudget(int userId, int month, int year) {
        Budget b = getBudget(userId, "Overall", month, year);
        return (b != null) ? b : new Budget(10000.0); // sensible default
    }

    /**
     * Returns all budgets set by a user for any month/year.
     */
    public List<Budget> getAllBudgets(int userId) {
        List<Budget> list = new ArrayList<Budget>();
        String sql = "SELECT budget_id, user_id, category, monthly_limit, month, year " +
                     "FROM budgets WHERE user_id = ? ORDER BY year DESC, month DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] getAllBudgets error: " + e.getMessage());
        }
        return list;
    }

    // ──────────────────────── DELETE ────────────────────────

    /**
     * Deletes a budget record by its ID.
     */
    public boolean deleteBudget(int budgetId) {
        String sql = "DELETE FROM budgets WHERE budget_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, budgetId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] deleteBudget error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────── Helper ────────────────────────

    private Budget mapRow(ResultSet rs) throws SQLException {
        return new Budget(
            rs.getInt("budget_id"),
            rs.getInt("user_id"),
            rs.getString("category"),
            rs.getDouble("monthly_limit"),
            rs.getInt("month"),
            rs.getInt("year")
        );
    }
}
