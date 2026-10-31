package expense.gui;

import expense.model.Expense;
import expense.service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Search Expenses by Category screen.
 */
public class SearchExpensePanel extends JPanel {

    private final ExpenseService service;
    private final ExpenseTrackerGUI frame;

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel resultLabel;

    private static final String[] COLUMNS = {"ID", "Title", "Category", "Amount (₹)", "Date", "Description"};

    public SearchExpensePanel(ExpenseService service, ExpenseTrackerGUI frame) {
        this.service = service;
        this.frame = frame;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        buildUI();
    }

    private void buildUI() {
        // ── Title ──
        JPanel top = ComponentFactory.darkPanel(new BorderLayout());
        top.add(ComponentFactory.titleLabel("Search by Category"), BorderLayout.WEST);

        // Search bar
        JPanel searchBar = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchField = ComponentFactory.textField(16);
        JButton searchCatBtn = ComponentFactory.primaryButton("By Category");
        searchCatBtn.addActionListener(e -> performCategorySearch());
        JButton searchKwBtn = ComponentFactory.accentButton("By Keyword");
        searchKwBtn.addActionListener(e -> performKeywordSearch());
        JButton clearBtn = ComponentFactory.dangerButton("Clear");
        clearBtn.addActionListener(e -> clearSearch());
        searchBar.add(ComponentFactory.label("Search: "));
        searchBar.add(searchField);
        searchBar.add(searchCatBtn);
        searchBar.add(searchKwBtn);
        searchBar.add(clearBtn);
        top.add(searchBar, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ── Table ──
        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = ComponentFactory.styledTable(tableModel);
        add(ComponentFactory.tableScrollPane(table), BorderLayout.CENTER);

        // ── Result info ──
        JPanel bottom = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        resultLabel = ComponentFactory.label("Enter a category and click Search.");
        bottom.add(resultLabel);
        add(bottom, BorderLayout.SOUTH);

        // Allow pressing Enter in the search field
        searchField.addActionListener(e -> performCategorySearch());
    }

    private void performCategorySearch() {
        String category = searchField.getText().trim();
        if (category.isEmpty()) {
            resultLabel.setText("Please enter a category name.");
            resultLabel.setForeground(AppTheme.WARNING);
            return;
        }
        List<Expense> results = service.searchByCategory(category);
        loadData(results);
        if (results.isEmpty()) {
            resultLabel.setText("No expenses found for category: \"" + category + "\"");
            resultLabel.setForeground(AppTheme.WARNING);
        } else {
            double sum = results.stream().mapToDouble(Expense::getAmount).sum();
            resultLabel.setText(String.format("Category '%s': %d expense(s) | Total: ₹%.2f",
                    category, results.size(), sum));
            resultLabel.setForeground(AppTheme.ACCENT);
        }
    }

    private void performKeywordSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            resultLabel.setText("Please enter a keyword to search.");
            resultLabel.setForeground(AppTheme.WARNING);
            return;
        }
        List<Expense> results = service.searchByKeyword(keyword);
        loadData(results);
        if (results.isEmpty()) {
            resultLabel.setText("No expenses found for keyword: \"" + keyword + "\"");
            resultLabel.setForeground(AppTheme.WARNING);
        } else {
            double sum = results.stream().mapToDouble(Expense::getAmount).sum();
            resultLabel.setText(String.format("Keyword '%s': %d expense(s) | Total: ₹%.2f",
                    keyword, results.size(), sum));
            resultLabel.setForeground(AppTheme.ACCENT);
        }
    }

    private void loadData(List<Expense> expenses) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (Expense e : expenses) {
            tableModel.addRow(new Object[]{
                    e.getId(), e.getTitle(), e.getCategory(),
                    String.format("%.2f", e.getAmount()),
                    e.getDate().format(fmt),
                    e.getDescription()
            });
        }
    }

    private void clearSearch() {
        searchField.setText("");
        tableModel.setRowCount(0);
        resultLabel.setText("Enter a category and click Search.");
        resultLabel.setForeground(AppTheme.TEXT_SECONDARY);
    }

    /** Called when the screen becomes visible. */
    public void refresh() {
        // Keep existing search results; user can clear if needed
    }
}
