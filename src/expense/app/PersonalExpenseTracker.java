package expense.app;

import expense.model.Expense;
import expense.service.ExpenseService;
import expense.exception.InvalidExpenseException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class PersonalExpenseTracker {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ExpenseService service = new ExpenseService();

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("       PERSONAL EXPENSE TRACKER");
        System.out.println("======================================");
        System.out.println("Loaded " + service.getAllExpenses().size() + " saved expense(s).");
        runMenu();
    }

    private static void runMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\\n--------------- MAIN MENU ---------------");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Search by Category");
            System.out.println("4. Update Expense");
            System.out.println("5. Delete Expense");
            System.out.println("6. Sort Expenses");
            System.out.println("7. Expense Report");
            System.out.println("8. Set Monthly Budget");
            System.out.println("9. Check Monthly Budget");
            System.out.println("10. Monthly Summary");
            System.out.println("0. Exit");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: addExpense(); break;
                case 2: viewExpenses(service.getAllExpenses()); break;
                case 3: searchExpense(); break;
                case 4: updateExpense(); break;
                case 5: deleteExpense(); break;
                case 6: sortMenu(); break;
                case 7: service.displayReport(); break;
                case 8: setBudget(); break;
                case 9: checkBudget(); break;
                case 10: monthlySummary(); break;
                case 0:
                    service.save();
                    running = false;
                    System.out.println("Your expenses have been saved. Goodbye!");
                    break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private static void addExpense() {
        try {
            System.out.println("\\n--- Add Expense ---");
            String title = readText("Expense title: ");
            String category = readText("Category (Food/Travel/Bills/etc.): ");
            double amount = readDouble("Amount: ");
            LocalDate date = readDate("Date (yyyy-MM-dd): ");
            service.addExpense(title, category, amount, date);
            System.out.println("Expense added successfully.");
            checkBudget();
        } catch (InvalidExpenseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void searchExpense() {
        String category = readText("Enter category: ");
        viewExpenses(service.searchByCategory(category));
    }

    private static void updateExpense() {
        int id = readInt("Enter expense ID to update: ");
        if (service.findById(id) == null) {
            System.out.println("Expense ID not found.");
            return;
        }
        try {
            String title = readText("New title: ");
            String category = readText("New category: ");
            double amount = readDouble("New amount: ");
            LocalDate date = readDate("New date (yyyy-MM-dd): ");
            if (service.updateExpense(id, title, category, amount, date))
                System.out.println("Expense updated successfully.");
        } catch (InvalidExpenseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteExpense() {
        int id = readInt("Enter expense ID to delete: ");
        System.out.println(service.deleteExpense(id) ?
                "Expense deleted successfully." : "Expense ID not found.");
    }

    private static void sortMenu() {
        System.out.println("1. Sort by date");
        System.out.println("2. Sort by amount");
        int choice = readInt("Choose sorting method: ");
        if (choice == 1) viewExpenses(service.sortByDate());
        else if (choice == 2) viewExpenses(service.sortByAmount());
        else System.out.println("Invalid choice.");
    }

    private static void setBudget() {
        double amount = readDouble("Enter monthly budget: ₹");
        try {
            service.setMonthlyBudget(amount);
            System.out.println("Monthly budget updated.");
            checkBudget();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void checkBudget() {
        double spent = service.getCurrentMonthTotal();
        double limit = service.getBudget().getMonthlyLimit();
        System.out.printf("Current month spending: ₹%.2f / ₹%.2f%n", spent, limit);
        if (spent > limit) System.out.println("WARNING: Monthly budget exceeded!");
        else System.out.printf("Remaining budget: ₹%.2f%n", limit - spent);
    }

    private static void monthlySummary() {
        String value = readText("Enter month (yyyy-MM): ");
        try {
            YearMonth month = YearMonth.parse(value);
            System.out.printf("Total expenses for %s: ₹%.2f%n",
                    month, service.calculateMonthlyTotal(month));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid month. Use yyyy-MM.");
        }
    }

    private static void viewExpenses(List<Expense> expenses) {
        System.out.println("\\n================ EXPENSE LIST ================");
        System.out.printf("%-5s %-20s %-15s %-11s %s%n",
                "ID", "Title", "Category", "Amount", "Date");
        System.out.println("--------------------------------------------------------------");
        if (expenses.isEmpty()) System.out.println("No expenses found.");
        else for (Expense e : expenses) System.out.println(e);
    }

    private static String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static int readInt(String message) {
        while (true) {
            try { return Integer.parseInt(readText(message)); }
            catch (NumberFormatException e) { System.out.println("Enter a valid whole number."); }
        }
    }

    private static double readDouble(String message) {
        while (true) {
            try { return Double.parseDouble(readText(message)); }
            catch (NumberFormatException e) { System.out.println("Enter a valid amount."); }
        }
    }

    private static LocalDate readDate(String message) {
        while (true) {
            try { return LocalDate.parse(readText(message)); }
            catch (DateTimeParseException e) { System.out.println("Use date format yyyy-MM-dd."); }
        }
    }
}
