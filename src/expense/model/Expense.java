package expense.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single expense record.
 * Extended to include 'description' and renamed 'date' -> 'expenseDate'
 * so the field names align with the database column names.
 *
 * The old constructor is kept for backward-compatibility with existing GUI code.
 */
public class Expense implements Comparable<Expense> {

    private int       id;
    private String    title;
    private String    category;
    private double    amount;
    private String    description;   // NEW – optional free-text note
    private LocalDate expenseDate;   // maps to column expense_date

    // ----------------------------------------------------------------
    // Full constructor (used by DAO when reading from DB)
    // ----------------------------------------------------------------
    public Expense(int id, String title, String category, double amount,
                   String description, LocalDate expenseDate) {
        this.id          = id;
        this.title       = title;
        this.category    = category;
        this.amount      = amount;
        this.description = description == null ? "" : description;
        this.expenseDate = expenseDate;
    }

    // ----------------------------------------------------------------
    // Backward-compatible 5-arg constructor (used by existing GUI/service)
    // ----------------------------------------------------------------
    public Expense(int id, String title, String category, double amount, LocalDate date) {
        this(id, title, category, amount, "", date);
    }

    // ----------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------
    public int       getId()          { return id; }
    public String    getTitle()       { return title; }
    public String    getCategory()    { return category; }
    public double    getAmount()      { return amount; }
    public String    getDescription() { return description; }
    public LocalDate getExpenseDate() { return expenseDate; }

    /** Alias kept for code that used .getDate() before the rename. */
    public LocalDate getDate()        { return expenseDate; }

    // ----------------------------------------------------------------
    // Setters
    // ----------------------------------------------------------------
    public void setTitle(String title)             { this.title = title; }
    public void setCategory(String category)       { this.category = category; }
    public void setAmount(double amount)           { this.amount = amount; }
    public void setDescription(String description) { this.description = description == null ? "" : description; }
    public void setExpenseDate(LocalDate date)     { this.expenseDate = date; }
    public void setDate(LocalDate date)            { this.expenseDate = date; }

    // ----------------------------------------------------------------
    // Comparable / toString
    // ----------------------------------------------------------------
    @Override
    public int compareTo(Expense other) {
        return this.expenseDate.compareTo(other.expenseDate);
    }

    @Override
    public String toString() {
        return String.format("%-5d %-20s %-15s \u20B9%-10.2f %s",
                id, title, category, amount,
                expenseDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }
}
