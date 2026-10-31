package expense.model;

/**
 * Represents a monthly budget record stored in the database.
 * Extended from the original single-limit class to include
 * category, month, and year (matching the DB schema).
 */
public class Budget {

    private int    id;
    private String category;
    private double monthlyLimit;
    private int    month;
    private int    year;

    // ----------------------------------------------------------------
    // Full constructor (used by DAO)
    // ----------------------------------------------------------------
    public Budget(int id, String category, double monthlyLimit, int month, int year) {
        this.id           = id;
        this.category     = category;
        this.monthlyLimit = monthlyLimit;
        this.month        = month;
        this.year         = year;
    }

    // ----------------------------------------------------------------
    // Backward-compatible single-arg constructor (used by ExpenseService)
    // ----------------------------------------------------------------
    public Budget(double monthlyLimit) {
        this(0, "General", monthlyLimit, java.time.LocalDate.now().getMonthValue(),
                java.time.LocalDate.now().getYear());
    }

    // ----------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------
    public int    getId()           { return id; }
    public String getCategory()     { return category; }
    public double getMonthlyLimit() { return monthlyLimit; }
    public int    getMonth()        { return month; }
    public int    getYear()         { return year; }

    // ----------------------------------------------------------------
    // Setters
    // ----------------------------------------------------------------
    public void setCategory(String category)       { this.category = category; }
    public void setMonthlyLimit(double limit) {
        if (limit <= 0) throw new IllegalArgumentException("Budget must be greater than zero.");
        this.monthlyLimit = limit;
    }
    public void setMonth(int month)   { this.month = month; }
    public void setYear(int year)     { this.year  = year;  }
}
