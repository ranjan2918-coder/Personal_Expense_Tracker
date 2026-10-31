package expense.model;

/**
 * Budget model – represents a monthly budget for a category or overall.
 * Extended to carry DB-level fields (budget_id, user_id, category, month, year).
 */
public class Budget {
    private int    budgetId;
    private int    userId;
    private String category;
    private double monthlyLimit;
    private int    month;
    private int    year;

    // ── Legacy constructor used by ExpenseService (file-based mode) ──
    public Budget(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
        this.category     = "Overall";
    }

    // ── Full constructor used by BudgetDAO when reading from DB ──
    public Budget(int budgetId, int userId, String category,
                  double monthlyLimit, int month, int year) {
        this.budgetId     = budgetId;
        this.userId       = userId;
        this.category     = category;
        this.monthlyLimit = monthlyLimit;
        this.month        = month;
        this.year         = year;
    }

    public int    getBudgetId()    { return budgetId; }
    public int    getUserId()      { return userId; }
    public String getCategory()    { return category; }
    public double getMonthlyLimit(){ return monthlyLimit; }
    public int    getMonth()       { return month; }
    public int    getYear()        { return year; }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit <= 0) throw new IllegalArgumentException("Budget must be greater than zero.");
        this.monthlyLimit = monthlyLimit;
    }
    public void setCategory(String category) { this.category = category; }
    public void setMonth(int month)          { this.month = month; }
    public void setYear(int year)            { this.year = year; }
    public void setUserId(int userId)        { this.userId = userId; }
}
