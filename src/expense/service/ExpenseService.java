package expense.service;

import expense.model.Expense;
import expense.model.Budget;
import expense.model.Reportable;
import expense.exception.InvalidExpenseException;
import expense.util.ExpenseFileManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseService implements Reportable {
    private List<Expense> expenses;
    private Budget budget;
    private int nextId;
    private ExpenseFileManager fileManager;

    public ExpenseService() {
        fileManager = new ExpenseFileManager();
        expenses = fileManager.loadExpenses();
        nextId = 1;
        for (Expense e : expenses) nextId = Math.max(nextId, e.getId() + 1);
        budget = new Budget(10000.0);
    }

    public void addExpense(String title, String category, double amount, LocalDate date)
            throws InvalidExpenseException {
        if (title == null || title.trim().isEmpty()) throw new InvalidExpenseException("Title cannot be empty.");
        if (category == null || category.trim().isEmpty()) throw new InvalidExpenseException("Category cannot be empty.");
        if (amount <= 0) throw new InvalidExpenseException("Amount must be greater than zero.");
        if (date == null || date.isAfter(LocalDate.now())) throw new InvalidExpenseException("Enter a valid date (not in the future).");
        expenses.add(new Expense(nextId++, title.trim(), category.trim(), amount, date));
        save();
    }

    public List<Expense> getAllExpenses() {
        return new ArrayList<Expense>(expenses);
    }

    public List<Expense> searchByCategory(String category) {
        return expenses.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public boolean updateExpense(int id, String title, String category, double amount, LocalDate date)
            throws InvalidExpenseException {
        Expense e = findById(id);
        if (e == null) return false;
        if (title == null || title.trim().isEmpty() || category == null || category.trim().isEmpty())
            throw new InvalidExpenseException("Title and category cannot be empty.");
        if (amount <= 0 || date == null || date.isAfter(LocalDate.now()))
            throw new InvalidExpenseException("Enter a valid amount and date.");
        e.setTitle(title.trim());
        e.setCategory(category.trim());
        e.setAmount(amount);
        e.setDate(date);
        save();
        return true;
    }

    public boolean deleteExpense(int id) {
        boolean removed = expenses.removeIf(e -> e.getId() == id);
        if (removed) save();
        return removed;
    }

    public Expense findById(int id) {
        for (Expense e : expenses) if (e.getId() == id) return e;
        return null;
    }

    public List<Expense> sortByAmount() {
        return expenses.stream()
                .sorted(Comparator.comparingDouble(Expense::getAmount))
                .collect(Collectors.toList());
    }

    public List<Expense> sortByDate() {
        List<Expense> result = getAllExpenses();
        Collections.sort(result);
        return result;
    }

    @Override
    public double calculateTotal() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public double calculateMonthlyTotal(YearMonth month) {
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .mapToDouble(Expense::getAmount).sum();
    }

    public Map<String, Double> categorySummary() {
        Map<String, Double> summary = new TreeMap<String, Double>();
        for (Expense e : expenses) {
            String key = e.getCategory();
            summary.put(key, summary.getOrDefault(key, 0.0) + e.getAmount());
        }
        return summary;
    }

    public Budget getBudget() { return budget; }

    public void setMonthlyBudget(double amount) {
        budget.setMonthlyLimit(amount);
    }

    public double getCurrentMonthTotal() {
        return calculateMonthlyTotal(YearMonth.now());
    }

    @Override
    public void displayReport() {
        System.out.println("\\n========== EXPENSE REPORT ==========");
        System.out.printf("Overall total : ₹%.2f%n", calculateTotal());
        System.out.printf("This month    : ₹%.2f%n", getCurrentMonthTotal());
        System.out.println("\\nCategory-wise totals:");
        Map<String, Double> summary = categorySummary();
        if (summary.isEmpty()) System.out.println("No expenses recorded.");
        summary.forEach((category, total) ->
                System.out.printf("%-18s ₹%.2f%n", category, total));
        System.out.println("====================================");
    }

    public void save() {
        try {
            fileManager.saveExpenses(expenses);
        } catch (IOException e) {
            System.out.println("Unable to save expenses: " + e.getMessage());
        }
    }
}
