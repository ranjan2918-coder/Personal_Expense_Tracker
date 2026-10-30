package expense.gui;

import expense.exception.InvalidExpenseException;
import expense.model.Expense;
import expense.service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * View All Expenses screen – table with Update, Delete and Sort buttons.
 */
public class ViewExpensesPanel extends JPanel {

    private final ExpenseService service;
    private final ExpenseTrackerGUI frame;

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel totalLabel;
    private JLabel countLabel;

    private static final String[] COLUMNS = {"ID", "Title", "Category", "Amount (\u20B9)", "Date"};

    public ViewExpensesPanel(ExpenseService service, ExpenseTrackerGUI frame) {
        this.service = service;
        this.frame = frame;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        buildUI();
    }

    private void buildUI() {
        // ── Header row ──
        JPanel header = ComponentFactory.darkPanel(new BorderLayout());
        header.add(ComponentFactory.titleLabel("All Expenses"), BorderLayout.WEST);

        JPanel statsPanel = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        countLabel = ComponentFactory.label("Records: 0");
        totalLabel = ComponentFactory.label("Total: \u20B90.00");
        totalLabel.setForeground(AppTheme.ACCENT);
        statsPanel.add(countLabel);
        statsPanel.add(totalLabel);
        header.add(statsPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Table ──
        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = ComponentFactory.styledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);

        add(ComponentFactory.tableScrollPane(table), BorderLayout.CENTER);

        // ── Action buttons ──
        JPanel actions = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));

        JButton updateBtn = ComponentFactory.primaryButton("Update Selected");
        updateBtn.addActionListener(e -> updateSelected());

        JButton deleteBtn = ComponentFactory.dangerButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton sortDateBtn = ComponentFactory.primaryButton("Sort by Date");
        sortDateBtn.addActionListener(e -> loadData(service.sortByDate()));

        JButton sortAmtBtn = ComponentFactory.primaryButton("Sort by Amount");
        sortAmtBtn.addActionListener(e -> loadData(service.sortByAmount()));

        JButton refreshBtn = ComponentFactory.accentButton("\u21BB Refresh");
        refreshBtn.addActionListener(e -> refresh());

        actions.add(updateBtn);
        actions.add(deleteBtn);
        actions.add(sortDateBtn);
        actions.add(sortAmtBtn);
        actions.add(refreshBtn);

        add(actions, BorderLayout.SOUTH);
    }

    /** Reload table with the given list. */
    private void loadData(List<Expense> expenses) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        double total = 0;
        for (Expense e : expenses) {
            tableModel.addRow(new Object[]{
                    e.getId(), e.getTitle(), e.getCategory(),
                    String.format("%.2f", e.getAmount()), e.getDate().format(fmt)
            });
            total += e.getAmount();
        }
        countLabel.setText("Records: " + expenses.size());
        totalLabel.setText(String.format("Total: \u20B9%.2f", total));
    }

    /** Public refresh called when navigating to this screen. */
    public void refresh() {
        loadData(service.getAllExpenses());
    }

    // ──────────── Update flow ────────────

    private void updateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Expense existing = service.findById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Expense not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build update dialog
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBackground(AppTheme.BG_CARD);
        JTextField tTitle = ComponentFactory.textField(18);
        tTitle.setText(existing.getTitle());
        JTextField tCat = ComponentFactory.textField(18);
        tCat.setText(existing.getCategory());
        JTextField tAmt = ComponentFactory.textField(18);
        tAmt.setText(String.valueOf(existing.getAmount()));
        JTextField tDate = ComponentFactory.textField(18);
        tDate.setText(existing.getDate().toString());

        form.add(ComponentFactory.label("Title"));  form.add(tTitle);
        form.add(ComponentFactory.label("Category")); form.add(tCat);
        form.add(ComponentFactory.label("Amount")); form.add(tAmt);
        form.add(ComponentFactory.label("Date (yyyy-MM-dd)")); form.add(tDate);

        int result = JOptionPane.showConfirmDialog(this, form, "Update Expense #" + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amount;
            try {
                amount = Double.parseDouble(tAmt.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Amount must be a valid number.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate date;
            try {
                date = LocalDate.parse(tDate.getText().trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Date must be in yyyy-MM-dd format.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean updated = service.updateExpense(id,
                    tTitle.getText().trim(), tCat.getText().trim(), amount, date);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Expense updated successfully.");
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed – ID not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (InvalidExpenseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────── Delete flow ────────────

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete expense #" + id + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (service.deleteExpense(id)) {
                JOptionPane.showMessageDialog(this, "Expense deleted.");
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "ID not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
