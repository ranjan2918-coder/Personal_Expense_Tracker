package expense.dao;

import expense.database.DatabaseConnection;
import expense.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'expenses' table.
 * All queries use PreparedStatement to prevent SQL injection.
 */
public class ExpenseDAO {

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------
    /**
     * Inserts a new expense and returns the generated auto-increment ID.
     */
    public int addExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (title, amount, category, description, expense_date) "
                   + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setString(4, expense.getDescription());
            ps.setDate(5, Date.valueOf(expense.getExpenseDate()));
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
    /** Returns all expenses ordered by expense_date descending. */
    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT id, title, amount, category, description, expense_date "
                   + "FROM expenses ORDER BY expense_date DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Returns a single expense by primary key, or null if not found. */
    public Expense getExpenseById(int id) throws SQLException {
        String sql = "SELECT id, title, amount, category, description, expense_date "
                   + "FROM expenses WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Returns expenses that match the given category (case-insensitive). */
    public List<Expense> getExpensesByCategory(String category) throws SQLException {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT id, title, amount, category, description, expense_date "
                   + "FROM expenses WHERE LOWER(category) = LOWER(?) "
                   + "ORDER BY expense_date DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Returns expenses for a specific year-month. */
    public List<Expense> getExpensesByMonth(int year, int month) throws SQLException {
        List<Expense> list = new ArrayList<Expense>();
        String sql = "SELECT id, title, amount, category, description, expense_date "
                   + "FROM expenses WHERE YEAR(expense_date) = ? AND MONTH(expense_date) = ? "
                   + "ORDER BY expense_date DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    /** Updates an existing expense. Returns true if a row was affected. */
    public boolean updateExpense(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET title=?, amount=?, category=?, description=?, expense_date=? "
                   + "WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setString(4, expense.getDescription());
            ps.setDate(5, Date.valueOf(expense.getExpenseDate()));
            ps.setInt(6, expense.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------
    /** Deletes an expense by ID. Returns true if a row was deleted. */
    public boolean deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Aggregate helpers
    // ----------------------------------------------------------------
    /** Returns the sum of all expense amounts. */
    public double getTotalAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    /** Returns the total amount spent in the given year-month. */
    public double getMonthlyTotal(int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses "
                   + "WHERE YEAR(expense_date) = ? AND MONTH(expense_date) = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // ----------------------------------------------------------------
    // Private helper
    // ----------------------------------------------------------------
    private Expense mapRow(ResultSet rs) throws SQLException {
        int       id          = rs.getInt("id");
        String    title       = rs.getString("title");
        double    amount      = rs.getDouble("amount");
        String    category    = rs.getString("category");
        String    description = rs.getString("description");
        LocalDate date        = rs.getDate("expense_date").toLocalDate();
        return new Expense(id, title, category, amount, description, date);
    }
}
