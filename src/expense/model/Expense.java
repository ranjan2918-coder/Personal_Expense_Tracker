package expense.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Expense implements Comparable<Expense> {
    private int id;
    private String title;
    private String category;
    private double amount;
    private LocalDate date;

    public Expense(int id, String title, String category, double amount, LocalDate date) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }

    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public int compareTo(Expense other) {
        return this.date.compareTo(other.date);
    }

    @Override
    public String toString() {
        return String.format("%-5d %-20s %-15s ₹%-10.2f %s",
                id, title, category, amount, date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }
}
