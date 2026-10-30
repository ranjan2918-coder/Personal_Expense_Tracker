package expense.model;

public class Budget {
    private double monthlyLimit;

    public Budget(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getMonthlyLimit() { return monthlyLimit; }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit <= 0) throw new IllegalArgumentException("Budget must be greater than zero.");
        this.monthlyLimit = monthlyLimit;
    }
}
