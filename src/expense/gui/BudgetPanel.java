package expense.gui;

import expense.service.ExpenseService;

import javax.swing.*;
import java.awt.*;

/**
 * Budget Management screen.
 * Shows current budget, monthly spending, remaining balance,
 * and allows the user to set a new monthly budget.
 */
public class BudgetPanel extends JPanel {

    private final ExpenseService service;
    private final ExpenseTrackerGUI frame;

    private JLabel currentBudgetLabel;
    private JLabel spentLabel;
    private JLabel remainingLabel;
    private JLabel statusLabel;
    private JTextField budgetField;
    private JLabel messageLabel;

    // Progress bar (custom painted)
    private double spentPercent = 0;
    private JPanel progressBar;

    public BudgetPanel(ExpenseService service, ExpenseTrackerGUI frame) {
        this.service = service;
        this.frame = frame;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        buildUI();
    }

    private void buildUI() {
        // ── Title ──
        JPanel top = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(ComponentFactory.titleLabel("Budget Management"));
        add(top, BorderLayout.NORTH);

        // ── Centre card ──
        JPanel card = ComponentFactory.cardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 16, 10, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Current budget
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Monthly Budget:"), gbc);
        gbc.gridx = 1;
        currentBudgetLabel = ComponentFactory.statLabel("\u20B90", AppTheme.PRIMARY);
        currentBudgetLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(currentBudgetLabel, gbc);

        // Spent
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Spent This Month:"), gbc);
        gbc.gridx = 1;
        spentLabel = ComponentFactory.statLabel("\u20B90", AppTheme.WARNING);
        spentLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(spentLabel, gbc);

        // Remaining
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Remaining:"), gbc);
        gbc.gridx = 1;
        remainingLabel = ComponentFactory.statLabel("\u20B90", AppTheme.ACCENT);
        remainingLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(remainingLabel, gbc);

        // Progress bar
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                // Track
                g2.setColor(AppTheme.BG_INPUT);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                // Filled portion
                int fillW = (int) (w * Math.min(spentPercent, 1.0));
                Color barColor = spentPercent > 1.0 ? AppTheme.DANGER
                        : spentPercent > 0.8 ? AppTheme.WARNING : AppTheme.ACCENT;
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fillW, h, 14, 14);
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(AppTheme.FONT_BUTTON);
                String pctText = String.format("%.0f%%", spentPercent * 100);
                int textW = g2.getFontMetrics().stringWidth(pctText);
                g2.drawString(pctText, (w - textW) / 2, h / 2 + 5);
                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(400, 28));
        progressBar.setOpaque(false);
        card.add(progressBar, gbc);

        // Status
        row++;
        gbc.gridy = row;
        statusLabel = ComponentFactory.label(" ");
        statusLabel.setFont(AppTheme.FONT_SUBTITLE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, gbc);

        // Separator
        row++;
        gbc.gridy = row;
        card.add(Box.createVerticalStrut(10), gbc);

        // Set new budget
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Set New Budget (\u20B9):"), gbc);
        gbc.gridx = 1;
        budgetField = ComponentFactory.textField(14);
        card.add(budgetField, gbc);

        // Buttons
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnRow = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton setBtn = ComponentFactory.accentButton("Set Budget");
        setBtn.addActionListener(e -> setBudget());
        JButton clearBtn = ComponentFactory.dangerButton("Clear");
        clearBtn.addActionListener(e -> { budgetField.setText(""); messageLabel.setText(" "); });

        btnRow.add(setBtn);
        btnRow.add(clearBtn);
        card.add(btnRow, gbc);

        // Message
        row++;
        gbc.gridy = row;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(AppTheme.FONT_LABEL);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(messageLabel, gbc);

        // Wrap card
        JPanel wrapper = ComponentFactory.darkPanel(new GridBagLayout());
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private void setBudget() {
        String txt = budgetField.getText().trim();
        if (txt.isEmpty()) {
            showMsg("Please enter a budget amount.", true);
            return;
        }
        try {
            double amount = Double.parseDouble(txt);
            service.setMonthlyBudget(amount);
            showMsg("Monthly budget updated to \u20B9" + String.format("%.2f", amount), false);
            budgetField.setText("");
            refresh();
        } catch (NumberFormatException ex) {
            showMsg("Enter a valid number.", true);
        } catch (IllegalArgumentException ex) {
            showMsg(ex.getMessage(), true);
        }
    }

    private void showMsg(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setForeground(isError ? AppTheme.DANGER : AppTheme.ACCENT);
    }

    /** Called when the screen becomes visible. */
    public void refresh() {
        double budget = service.getBudget().getMonthlyLimit();
        double spent = service.getCurrentMonthTotal();
        double remaining = budget - spent;

        currentBudgetLabel.setText(String.format("\u20B9%.2f", budget));
        spentLabel.setText(String.format("\u20B9%.2f", spent));
        remainingLabel.setText(String.format("\u20B9%.2f", remaining));
        remainingLabel.setForeground(remaining >= 0 ? AppTheme.ACCENT : AppTheme.DANGER);

        spentPercent = budget > 0 ? spent / budget : 0;
        progressBar.repaint();

        if (spent > budget) {
            statusLabel.setText("\u26A0 Budget Exceeded!");
            statusLabel.setForeground(AppTheme.DANGER);
        } else if (spent > budget * 0.8) {
            statusLabel.setText("\u26A0 Approaching Budget Limit");
            statusLabel.setForeground(AppTheme.WARNING);
        } else {
            statusLabel.setText("\u2714 Within Budget");
            statusLabel.setForeground(AppTheme.ACCENT);
        }
    }
}
