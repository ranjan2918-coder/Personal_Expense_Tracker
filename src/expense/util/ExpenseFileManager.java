package expense.util;

import expense.model.Expense;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseFileManager {
    private static final String FILE_NAME = "expenses.txt";

    public void saveExpenses(List<Expense> expenses) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Expense e : expenses) {
                writer.write(e.getId() + "|" + clean(e.getTitle()) + "|" +
                        clean(e.getCategory()) + "|" + e.getAmount() + "|" + e.getDate());
                writer.newLine();
            }
        }
    }

    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<Expense>();
        File file = new File(FILE_NAME);
        if (!file.exists())
            return expenses;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length == 5) {
                    expenses.add(new Expense(Integer.parseInt(p[0]), p[1], p[2],
                            Double.parseDouble(p[3]), LocalDate.parse(p[4])));
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load saved expenses: " + e.getMessage());
        }
        return expenses;
    }

    private String clean(String value) {
        return value.replace("|", "/").replace("\n", " ");
    }
}
