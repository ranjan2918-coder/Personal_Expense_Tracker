package expense.gui;

import expense.model.Expense;
import expense.service.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Dashboard screen – shows summary statistics and category breakdown at a glance.
 */
public class DashboardPanel extends JPanel {

    private final ExpenseService service;
    private final ExpenseTrackerGUI frame;

    // Stat labels we update on refresh
    private JLabel totalLabel;
    private JLabel monthLabel;
    private JLabel countLabel;
    private JLabel budgetStatusLabel;
    private JPanel categoryPanel;

    public DashboardPanel(ExpenseService service, ExpenseTrackerGUI frame) {
        this.service = service;
        this.frame = frame;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        buildUI();
    }

    private void buildUI() {
        // ── Title row ──
        JPanel topRow = ComponentFactory.darkPanel(new BorderLayout());
        topRow.add(ComponentFactory.titleLabel("Dashboard"), BorderLayout.WEST);
        JLabel dateLabel = ComponentFactory.label(
                "Today: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topRow.add(dateLabel, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        // ── Stat cards ──
        JPanel statsRow = ComponentFactory.darkPanel(new GridLayout(1, 4, 14, 0));

        totalLabel       = ComponentFactory.statLabel("₹0.00", AppTheme.ACCENT);
        monthLabel       = ComponentFactory.statLabel("₹0.00", AppTheme.PRIMARY);
        countLabel       = ComponentFactory.statLabel("0",      AppTheme.TEXT_PRIMARY);
        budgetStatusLabel = ComponentFactory.statLabel("—",     AppTheme.WARNING);

        statsRow.add(createStatCard("Total Expenses",  totalLabel));
        statsRow.add(createStatCard("This Month",      monthLabel));
        statsRow.add(createStatCard("No. of Records",  countLabel));
        statsRow.add(createStatCard("Budget Status",   budgetStatusLabel));

        // ── Category breakdown ──
        categoryPanel = ComponentFactory.cardPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));

        JPanel centre = ComponentFactory.darkPanel(new BorderLayout(0, 14));
        centre.add(statsRow, BorderLayout.NORTH);
        centre.add(new JScrollPane(categoryPanel) {{
            getViewport().setBackground(AppTheme.BG_CARD);
            setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR));
        }}, BorderLayout.CENTER);

        add(centre, BorderLayout.CENTER);

        // ── Quick-action buttons ──
        JPanel actions = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        JButton addBtn = ComponentFactory.accentButton("+ Add Expense");
        addBtn.addActionListener(e -> frame.showScreen(ExpenseTrackerGUI.ADD));
        JButton viewBtn = ComponentFactory.primaryButton("View All");
        viewBtn.addActionListener(e -> frame.showScreen(ExpenseTrackerGUI.VIEW));
        JButton reportBtn = ComponentFactory.primaryButton("Monthly Report");
        reportBtn.addActionListener(e -> frame.showScreen(ExpenseTrackerGUI.REPORT));
        actions.add(addBtn);
        actions.add(viewBtn);
        actions.add(reportBtn);
        add(actions, BorderLayout.SOUTH);
    }

    /** Creates a dark card with a heading and a value label. */
    private JPanel createStatCard(String heading, JLabel valueLabel) {
        JPanel card = ComponentFactory.cardPanel();
        card.setLayout(new BorderLayout(0, 6));
        JLabel headLbl = ComponentFactory.label(heading);
        headLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(headLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Called whenever the dashboard becomes visible. */
    public void refresh() {
        List<Expense> all = service.getAllExpenses();
        double total = service.calculateTotal();
        double month = service.getCurrentMonthTotal();
        double limit = service.getBudget().getMonthlyLimit();

        totalLabel.setText(String.format("\u20B9%.2f", total));
        monthLabel.setText(String.format("\u20B9%.2f", month));
        countLabel.setText(String.valueOf(all.size()));

        if (month > limit) {
            budgetStatusLabel.setText("EXCEEDED");
            budgetStatusLabel.setForeground(AppTheme.DANGER);
        } else {
            double remaining = limit - month;
            budgetStatusLabel.setText(String.format("\u20B9%.0f left", remaining));
            budgetStatusLabel.setForeground(AppTheme.ACCENT);
        }

        // Category breakdown
        categoryPanel.removeAll();
        JLabel catTitle = ComponentFactory.subtitleLabel("Category-wise Breakdown");
        catTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryPanel.add(catTitle);
        categoryPanel.add(Box.createVerticalStrut(8));

        Map<String, Double> summary = service.categorySummary();
        if (summary.isEmpty()) {
            JLabel noData = ComponentFactory.label("No expenses recorded yet.");
            noData.setAlignmentX(Component.LEFT_ALIGNMENT);
            categoryPanel.add(noData);
        } else {
            for (Map.Entry<String, Double> entry : summary.entrySet()) {
                JPanel row = ComponentFactory.darkPanel(new BorderLayout());
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

                JLabel catName = ComponentFactory.label("  " + entry.getKey());
                JLabel catVal  = ComponentFactory.label(String.format("\u20B9%.2f  ", entry.getValue()));
                catVal.setForeground(AppTheme.ACCENT);
                catVal.setHorizontalAlignment(SwingConstants.RIGHT);

                // Simple bar
                double pct = (total > 0) ? entry.getValue() / total : 0;
                JPanel bar = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int barWidth = (int) (getWidth() * pct);
                        g2.setColor(AppTheme.PRIMARY);
                        g2.fillRoundRect(0, 6, barWidth, 10, 5, 5);
                        g2.setColor(AppTheme.BORDER_COLOR);
                        g2.drawRoundRect(0, 6, getWidth() - 1, 10, 5, 5);
                    }
                };
                bar.setOpaque(false);
                bar.setPreferredSize(new Dimension(200, 22));

                row.add(catName, BorderLayout.WEST);
                row.add(bar,     BorderLayout.CENTER);
                row.add(catVal,  BorderLayout.EAST);
                categoryPanel.add(row);
            }
        }
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }
}
