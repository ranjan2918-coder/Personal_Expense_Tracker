package expense.gui;

import expense.exception.InvalidExpenseException;
import expense.service.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Form screen to add a new expense.
 * Validates all inputs and shows clear success / error messages.
 */
public class AddExpensePanel extends JPanel {

    private final ExpenseService service;
    private final ExpenseTrackerGUI frame;

    private JTextField titleField;
    private JComboBox<String> categoryBox;
    private JTextField customCategoryField;
    private JTextField amountField;
    private JTextField dateField;
    private JLabel messageLabel;

    private static final String[] CATEGORIES = {
            "Food", "Travel", "Bills", "Shopping", "Entertainment",
            "Health", "Education", "Rent", "Other"
    };

    public AddExpensePanel(ExpenseService service, ExpenseTrackerGUI frame) {
        this.service = service;
        this.frame = frame;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        buildUI();
    }

    private void buildUI() {
        // Title
        JPanel top = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(ComponentFactory.titleLabel("Add New Expense"));
        add(top, BorderLayout.NORTH);

        // Form card
        JPanel card = ComponentFactory.cardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 14, 10, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Title
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Expense Title *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        titleField = ComponentFactory.textField(22);
        card.add(titleField, gbc);
        gbc.weightx = 0;

        // Category
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Category *"), gbc);
        gbc.gridx = 1;
        categoryBox = ComponentFactory.comboBox(CATEGORIES);
        card.add(categoryBox, gbc);

        // Custom category (shown when "Other" selected)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel customLbl = ComponentFactory.label("Custom Category");
        card.add(customLbl, gbc);
        gbc.gridx = 1;
        customCategoryField = ComponentFactory.textField(22);
        customCategoryField.setEnabled(false);
        card.add(customCategoryField, gbc);

        categoryBox.addActionListener(e -> {
            boolean isOther = "Other".equals(categoryBox.getSelectedItem());
            customCategoryField.setEnabled(isOther);
            if (!isOther) customCategoryField.setText("");
        });

        // Amount
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Amount (\u20B9) *"), gbc);
        gbc.gridx = 1;
        amountField = ComponentFactory.textField(22);
        card.add(amountField, gbc);

        // Date
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Date (yyyy-MM-dd) *"), gbc);
        gbc.gridx = 1;
        dateField = ComponentFactory.textField(22);
        dateField.setText(LocalDate.now().toString());
        card.add(dateField, gbc);

        // Message
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(AppTheme.FONT_LABEL);
        card.add(messageLabel, gbc);
        gbc.gridwidth = 1;

        // Buttons
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnRow = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton addBtn = ComponentFactory.accentButton("Add Expense");
        addBtn.setPreferredSize(new Dimension(160, 38));
        addBtn.addActionListener(e -> addExpense());

        JButton clearBtn = ComponentFactory.dangerButton("Clear");
        clearBtn.setPreferredSize(new Dimension(120, 38));
        clearBtn.addActionListener(e -> clearFields());

        JButton backBtn = ComponentFactory.primaryButton("\u2190 Dashboard");
        backBtn.setPreferredSize(new Dimension(140, 38));
        backBtn.addActionListener(e -> frame.showScreen(ExpenseTrackerGUI.DASHBOARD));

        btnRow.add(addBtn);
        btnRow.add(clearBtn);
        btnRow.add(backBtn);
        card.add(btnRow, gbc);

        // Centre the card
        JPanel wrapper = ComponentFactory.darkPanel(new GridBagLayout());
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private void addExpense() {
        try {
            String title = titleField.getText().trim();
            String category;
            if ("Other".equals(categoryBox.getSelectedItem())) {
                category = customCategoryField.getText().trim();
            } else {
                category = (String) categoryBox.getSelectedItem();
            }

            String amtText = amountField.getText().trim();
            if (amtText.isEmpty()) {
                showMessage("Please enter an amount.", true);
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amtText);
            } catch (NumberFormatException ex) {
                showMessage("Amount must be a valid number.", true);
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim());
            } catch (DateTimeParseException ex) {
                showMessage("Date must be in yyyy-MM-dd format.", true);
                return;
            }

            service.addExpense(title, category, amount, date);
            showMessage("Expense added successfully!", false);
            clearFields();

        } catch (InvalidExpenseException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    private void clearFields() {
        titleField.setText("");
        categoryBox.setSelectedIndex(0);
        customCategoryField.setText("");
        customCategoryField.setEnabled(false);
        amountField.setText("");
        dateField.setText(LocalDate.now().toString());
        messageLabel.setText(" ");
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setForeground(isError ? AppTheme.DANGER : AppTheme.ACCENT);
    }
}
