package expense.service;

import expense.dao.BudgetDAO;
import expense.dao.ExpenseDAO;
import expense.exception.InvalidExpenseException;
import expense.model.Budget;
import expense.model.Expense;
import expense.model.Reportable;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for expense and budget operations.
 *
 * UPDATED: All persistence now goes through ExpenseDAO / BudgetDAO (MariaDB via JDBC).
 * The file-based ExpenseFileManager is no longer used.
 *
 * All existing public method signatures are preserved so that GUI panels
 * continue to compile without changes.
 */
public class ExpenseService implements Reportable {

    private final ExpenseDAO expenseDAO;
    private final BudgetDAO  budgetDAO;

    // In-memory budget fallback for the GUI (used when no DB budget exists yet)
    private Budget  inMemoryBudget;

    public ExpenseService() {
        this.expenseDAO    = new ExpenseDAO();
        this.budgetDAO     = new BudgetDAO();
        this.inMemoryBudget = new Budget(10000.0); // sensible default
    }

    // ================================================================
    //  Expense CRUD
    // ================================================================

    public void addExpense(String title, String category, double amount, LocalDate date)
            throws InvalidExpenseException {
        addExpense(title, category, amount, "", date);
    }

    public void addExpense(String title, String category, double amount,
                           String description, LocalDate date)
            throws InvalidExpenseException {
        validate(title, category, amount, date);
        try {
            Expense e = new Expense(0, title.trim(), category.trim(), amount,
                    description == null ? "" : description.trim(), date);
            expenseDAO.addExpense(e);
        } catch (SQLException ex) {
            throw new InvalidExpenseException("Database error: " + ex.getMessage());
        }
    }

    public List<Expense> getAllExpenses() {
        try {
            return expenseDAO.getAllExpenses();
        } catch (SQLException ex) {
            System.err.println("[Service] getAllExpenses error: " + ex.getMessage());
            return new ArrayList<Expense>();
        }
    }

    public Expense findById(int id) {
        try {
            return expenseDAO.getExpenseById(id);
        } catch (SQLException ex) {
            System.err.println("[Service] findById error: " + ex.getMessage());
            return null;
        }
    }

    public List<Expense> searchByCategory(String category) {
        try {
            return expenseDAO.getExpensesByCategory(category);
        } catch (SQLException ex) {
            System.err.println("[Service] searchByCategory error: " + ex.getMessage());
            return new ArrayList<Expense>();
        }
    }

    public boolean updateExpense(int id, String title, String category, double amount, LocalDate date)
            throws InvalidExpenseException {
        return updateExpense(id, title, category, amount, "", date);
    }

    public boolean updateExpense(int id, String title, String category, double amount,
                                 String description, LocalDate date)
            throws InvalidExpenseException {
        Expense existing = findById(id);
        if (existing == null) return false;

        if (title    == null || title.trim().isEmpty() ||
            category == null || category.trim().isEmpty())
            throw new InvalidExpenseException("Title and category cannot be empty.");
        if (amount <= 0 || date == null || date.isAfter(LocalDate.now()))
            throw new InvalidExpenseException("Enter a valid amount and date.");

        Expense updated = new Expense(id, title.trim(), category.trim(), amount,
                description == null ? "" : description.trim(), date);
        try {
            return expenseDAO.updateExpense(updated);
        } catch (SQLException ex) {
            throw new InvalidExpenseException("Database error: " + ex.getMessage());
        }
    }

    public boolean deleteExpense(int id) {
        try {
            return expenseDAO.deleteExpense(id);
        } catch (SQLException ex) {
            System.err.println("[Service] deleteExpense error: " + ex.getMessage());
            return false;
        }
    }

    // ================================================================
    //  Sorting helpers (GUI uses these)
    // ================================================================

    public List<Expense> sortByAmount() {
        return getAllExpenses().stream()
                .sorted(Comparator.comparingDouble(Expense::getAmount))
                .collect(Collectors.toList());
    }

    public List<Expense> sortByDate() {
        List<Expense> result = getAllExpenses();
        Collections.sort(result);
        return result;
    }

    // ================================================================
    //  Aggregates / summaries
    // ================================================================

    @Override
    public double calculateTotal() {
        try {
            return expenseDAO.getTotalAmount();
        } catch (SQLException ex) {
            System.err.println("[Service] calculateTotal error: " + ex.getMessage());
            return 0.0;
        }
    }

    public double calculateMonthlyTotal(YearMonth month) {
        try {
            return expenseDAO.getMonthlyTotal(month.getYear(), month.getMonthValue());
        } catch (SQLException ex) {
            System.err.println("[Service] calculateMonthlyTotal error: " + ex.getMessage());
            return 0.0;
        }
    }

    public double getCurrentMonthTotal() {
        return calculateMonthlyTotal(YearMonth.now());
    }

    public Map<String, Double> categorySummary() {
        Map<String, Double> summary = new TreeMap<String, Double>();
        for (Expense e : getAllExpenses()) {
            summary.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return summary;
    }

    // ================================================================
    //  Budget
    // ================================================================

    public Budget getBudget() {
        // Return the latest budget from DB if available; else use in-memory default
        try {
            List<Budget> budgets = budgetDAO.getAllBudgets();
            if (!budgets.isEmpty()) return budgets.get(0);
        } catch (SQLException ex) {
            System.err.println("[Service] getBudget error: " + ex.getMessage());
        }
        return inMemoryBudget;
    }

    public void setMonthlyBudget(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Budget must be greater than zero.");
        inMemoryBudget.setMonthlyLimit(amount);

        // Also persist to DB for the current month
        try {
            int m = LocalDate.now().getMonthValue();
            int y = LocalDate.now().getYear();
            Budget existing = budgetDAO.getBudgetByCategoryMonthYear("General", m, y);
            if (existing == null) {
                budgetDAO.addBudget(new Budget(0, "General", amount, m, y));
            } else {
                existing.setMonthlyLimit(amount);
                budgetDAO.updateBudget(existing);
            }
        } catch (SQLException ex) {
            System.err.println("[Service] setMonthlyBudget DB error: " + ex.getMessage());
        }
    }

    // ================================================================
    //  Reportable interface
    // ================================================================

    @Override
    public void displayReport() {
        System.out.println("\n========== EXPENSE REPORT ==========");
        System.out.printf("Overall total : \u20B9%.2f%n", calculateTotal());
        System.out.printf("This month    : \u20B9%.2f%n", getCurrentMonthTotal());
        System.out.println("\nCategory-wise totals:");
        Map<String, Double> summary = categorySummary();
        if (summary.isEmpty()) System.out.println("No expenses recorded.");
        summary.forEach((cat, total) -> System.out.printf("%-18s \u20B9%.2f%n", cat, total));
        System.out.println("====================================");
    }

    /**
     * Legacy compatibility method – previously saved to file.
     * Now a no-op because every write goes directly to the DB.
     */
    public void save() {
        // No-op: all writes are already committed to the database
    }

    // ================================================================
    //  Private validation helper
    // ================================================================

    private void validate(String title, String category, double amount, LocalDate date)
            throws InvalidExpenseException {
        if (title    == null || title.trim().isEmpty())    throw new InvalidExpenseException("Title cannot be empty.");
        if (category == null || category.trim().isEmpty()) throw new InvalidExpenseException("Category cannot be empty.");
        if (amount <= 0)     throw new InvalidExpenseException("Amount must be greater than zero.");
        if (date == null || date.isAfter(LocalDate.now()))
            throw new InvalidExpenseException("Enter a valid date (not in the future).");
    }
}
