package expense.service;

import expense.model.Budget;
import expense.model.Expense;
import expense.model.Reportable;
import expense.exception.InvalidExpenseException;
import expense.dao.ExpenseDAO;
import expense.dao.BudgetDAO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * ExpenseService – business-logic layer.
 *
 * All persistence is now handled via MySQL through ExpenseDAO / BudgetDAO.
 * The existing public API (method signatures) is preserved so that all GUI
 * panels continue to compile and work without changes.
 *
 * User context (userId) is set once after login via setCurrentUser().
 */
public class ExpenseService implements Reportable {

    // ── DAO layer ──
    private final ExpenseDAO expenseDAO;
    private final BudgetDAO  budgetDAO;

    // ── Currently logged-in user ──
    private int currentUserId = 1; // default for backward compatibility

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
        this.budgetDAO  = new BudgetDAO();
    }

    // ─────────────────────────────────────────
    //  User context
    // ─────────────────────────────────────────

    /** Called by LoginPanel after a successful login / registration. */
    public void setCurrentUser(int userId) {
        this.currentUserId = userId;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    // ─────────────────────────────────────────
    //  ADD
    // ─────────────────────────────────────────

    /** Original signature – delegates with empty description. */
    public void addExpense(String title, String category, double amount, LocalDate date)
            throws InvalidExpenseException {
        addExpense(title, category, amount, "", date);
    }

    /** Extended signature with description. */
    public void addExpense(String title, String category, double amount,
                           String description, LocalDate date)
            throws InvalidExpenseException {
        if (title == null || title.trim().isEmpty())
            throw new InvalidExpenseException("Title cannot be empty.");
        if (category == null || category.trim().isEmpty())
            throw new InvalidExpenseException("Category cannot be empty.");
        if (amount <= 0)
            throw new InvalidExpenseException("Amount must be greater than zero.");
        if (date == null || date.isAfter(LocalDate.now()))
            throw new InvalidExpenseException("Enter a valid date (not in the future).");

        int id = expenseDAO.addExpense(currentUserId, title, amount, category, description, date);
        if (id < 0) throw new InvalidExpenseException("Database error: could not save expense.");
    }

    // ─────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────

    public List<Expense> getAllExpenses() {
        return expenseDAO.getAllExpenses(currentUserId);
    }

    public Expense findById(int id) {
        return expenseDAO.findById(currentUserId, id);
    }

    public List<Expense> searchByCategory(String category) {
        return expenseDAO.searchByCategory(currentUserId, category);
    }

    public List<Expense> searchByKeyword(String keyword) {
        return expenseDAO.searchByKeyword(currentUserId, keyword);
    }

    public List<Expense> getExpensesByMonth(int month, int year) {
        return expenseDAO.getExpensesByMonth(currentUserId, month, year);
    }

    // ─────────────────────────────────────────
    //  SORT
    // ─────────────────────────────────────────

    public List<Expense> sortByAmount() {
        return expenseDAO.getSortedByAmount(currentUserId);
    }

    public List<Expense> sortByDate() {
        return expenseDAO.getSortedByDate(currentUserId);
    }

    // ─────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────

    /** Original signature – updates without changing description. */
    public boolean updateExpense(int id, String title, String category,
                                 double amount, LocalDate date)
            throws InvalidExpenseException {
        Expense existing = findById(id);
        String desc = (existing != null) ? existing.getDescription() : "";
        return updateExpense(id, title, category, amount, desc, date);
    }

    /** Extended signature including description. */
    public boolean updateExpense(int id, String title, String category,
                                 double amount, String description, LocalDate date)
            throws InvalidExpenseException {
        if (title == null || title.trim().isEmpty() || category == null || category.trim().isEmpty())
            throw new InvalidExpenseException("Title and category cannot be empty.");
        if (amount <= 0 || date == null || date.isAfter(LocalDate.now()))
            throw new InvalidExpenseException("Enter a valid amount and date.");
        return expenseDAO.updateExpense(currentUserId, id, title, amount, category, description, date);
    }

    // ─────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────

    public boolean deleteExpense(int id) {
        return expenseDAO.deleteExpense(currentUserId, id);
    }

    // ─────────────────────────────────────────
    //  TOTALS / SUMMARY (Reportable)
    // ─────────────────────────────────────────

    @Override
    public double calculateTotal() {
        return expenseDAO.getGrandTotal(currentUserId);
    }

    public double calculateMonthlyTotal(YearMonth month) {
        return expenseDAO.getMonthlyTotal(currentUserId, month.getMonthValue(), month.getYear());
    }

    public double getCurrentMonthTotal() {
        return calculateMonthlyTotal(YearMonth.now());
    }

    /** Returns a sorted map of category -> total amount. */
    public Map<String, Double> categorySummary() {
        Map<String, Double> summary = new TreeMap<String, Double>();
        List<Object[]> rows = expenseDAO.getCategorySummary(currentUserId);
        for (Object[] row : rows) {
            summary.put((String) row[0], (Double) row[1]);
        }
        return summary;
    }

    // ─────────────────────────────────────────
    //  BUDGET
    // ─────────────────────────────────────────

    public Budget getBudget() {
        YearMonth now = YearMonth.now();
        return budgetDAO.getOverallBudget(currentUserId, now.getMonthValue(), now.getYear());
    }

    public void setMonthlyBudget(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Budget must be greater than zero.");
        YearMonth now = YearMonth.now();
        budgetDAO.setOrUpdateBudget(currentUserId, "Overall", amount,
                now.getMonthValue(), now.getYear());
    }

    public void setMonthlyBudget(String category, double amount, int month, int year) {
        if (amount <= 0) throw new IllegalArgumentException("Budget must be greater than zero.");
        budgetDAO.setOrUpdateBudget(currentUserId, category, amount, month, year);
    }

    public double getBudgetForMonth(String category, int month, int year) {
        Budget b = budgetDAO.getBudget(currentUserId, category, month, year);
        return (b != null) ? b.getMonthlyLimit() : 0.0;
    }

    // ─────────────────────────────────────────
    //  Reportable.displayReport
    // ─────────────────────────────────────────

    @Override
    public void displayReport() {
        System.out.println("\n========== EXPENSE REPORT ==========");
        System.out.printf("Overall total : \u20B9%.2f%n", calculateTotal());
        System.out.printf("This month    : \u20B9%.2f%n", getCurrentMonthTotal());
        System.out.println("\nCategory-wise totals:");
        Map<String, Double> summary = categorySummary();
        if (summary.isEmpty()) System.out.println("No expenses recorded.");
        for (Map.Entry<String, Double> e : summary.entrySet())
            System.out.printf("%-18s \u20B9%.2f%n", e.getKey(), e.getValue());
        System.out.println("====================================");
    }

    /**
     * No-op – file saving is replaced by DB persistence.
     * Kept so existing callers (exitApplication, console app) don't break.
     */
    public void save() {
        // Data is already persisted in MySQL; nothing to do here.
        System.out.println("[ExpenseService] Data persisted in MySQL.");
    }
}
