package expense.gui;

import expense.model.Expense;
import expense.service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Monthly Expense Report screen.
 * User enters a month (yyyy-MM) and sees a detailed breakdown with totals.
 */
public class MonthlyReportPanel extends JPanel {

    private final ExpenseService service;
    private final ExpenseTrackerGUI frame;

    private JTextField monthField;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel summaryLabel;
    private JPanel categoryBreakdown;

    private static final String[] COLUMNS = {"ID", "Title", "Category", "Amount (\u20B9)", "Date"};

    public MonthlyReportPanel(ExpenseService service, ExpenseTrackerGUI frame) {
        this.service = service;
        this.frame = frame;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        buildUI();
    }

    private void buildUI() {
        // ── Header ──
        JPanel top = ComponentFactory.darkPanel(new BorderLayout());
        top.add(ComponentFactory.titleLabel("Monthly Expense Report"), BorderLayout.WEST);

        JPanel controls = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.add(ComponentFactory.label("Month (yyyy-MM): "));
        monthField = ComponentFactory.textField(10);
        monthField.setText(YearMonth.now().toString());
        controls.add(monthField);
        JButton genBtn = ComponentFactory.accentButton("Generate");
        genBtn.addActionListener(e -> generateReport());
        controls.add(genBtn);
        top.add(controls, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ── Centre – split: table + category breakdown ──
        JPanel centre = ComponentFactory.darkPanel(new BorderLayout(0, 8));

        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = ComponentFactory.styledTable(tableModel);
        centre.add(ComponentFactory.tableScrollPane(table), BorderLayout.CENTER);

        // Category breakdown card
        categoryBreakdown = ComponentFactory.cardPanel();
        categoryBreakdown.setLayout(new BoxLayout(categoryBreakdown, BoxLayout.Y_AXIS));
        JScrollPane catScroll = new JScrollPane(categoryBreakdown);
        catScroll.setPreferredSize(new Dimension(0, 140));
        catScroll.getViewport().setBackground(AppTheme.BG_CARD);
        catScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR));
        centre.add(catScroll, BorderLayout.SOUTH);

        add(centre, BorderLayout.CENTER);

        // ── Summary footer ──
        JPanel footer = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        summaryLabel = ComponentFactory.label("Select a month and click Generate.");
        footer.add(summaryLabel);
        add(footer, BorderLayout.SOUTH);

        monthField.addActionListener(e -> generateReport());
    }

    private void generateReport() {
        String text = monthField.getText().trim();
        YearMonth month;
        try {
            month = YearMonth.parse(text);
        } catch (DateTimeParseException ex) {
            summaryLabel.setText("Invalid month format. Use yyyy-MM (e.g., 2026-10).");
            summaryLabel.setForeground(AppTheme.DANGER);
            return;
        }

        // Filter expenses for the selected month
        List<Expense> all = service.getAllExpenses();
        List<Expense> monthly = all.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .collect(Collectors.toList());

        // Load into table
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        double total = 0;
        for (Expense e : monthly) {
            tableModel.addRow(new Object[]{
                    e.getId(), e.getTitle(), e.getCategory(),
                    String.format("%.2f", e.getAmount()), e.getDate().format(fmt)
            });
            total += e.getAmount();
        }

        // Summary
        double budget = service.getBudget().getMonthlyLimit();
        String status = total > budget ? " | \u26A0 BUDGET EXCEEDED!" : "";
        summaryLabel.setText(String.format("Month: %s | Expenses: %d | Total: \u20B9%.2f | Budget: \u20B9%.2f%s",
                month, monthly.size(), total, budget, status));
        summaryLabel.setForeground(total > budget ? AppTheme.DANGER : AppTheme.ACCENT);

        // Category breakdown
        categoryBreakdown.removeAll();
        categoryBreakdown.add(ComponentFactory.subtitleLabel("Category Breakdown – " + month));
        categoryBreakdown.add(Box.createVerticalStrut(6));

        Map<String, Double> catMap = monthly.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)));

        if (catMap.isEmpty()) {
            categoryBreakdown.add(ComponentFactory.label("  No expenses for this month."));
        } else {
            for (Map.Entry<String, Double> entry : catMap.entrySet()) {
                JPanel row = ComponentFactory.darkPanel(new BorderLayout());
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.add(ComponentFactory.label("  " + entry.getKey()), BorderLayout.WEST);
                JLabel val = ComponentFactory.label(String.format("\u20B9%.2f  ", entry.getValue()));
                val.setForeground(AppTheme.ACCENT);
                row.add(val, BorderLayout.EAST);
                categoryBreakdown.add(row);
            }
        }
        categoryBreakdown.revalidate();
        categoryBreakdown.repaint();
    }

    /** Called when the screen becomes visible. */
    public void refresh() {
        // Auto-generate for current month
        monthField.setText(YearMonth.now().toString());
        generateReport();
    }
}
