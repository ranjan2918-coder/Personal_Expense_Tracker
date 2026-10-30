package expense.gui;

import expense.service.ExpenseService;

import javax.swing.*;
import java.awt.*;

/**
 * Main GUI class for Personal Expense Tracker.
 *
 * This is the entry point of the application – right-click this file in
 * Eclipse and choose "Run As -> Java Application" to launch.
 *
 * The window uses a CardLayout to switch between screens:
 *   Dashboard | Add Expense | View All | Search | Monthly Report | Budget
 *
 * All screens are connected to the existing ExpenseService backend.
 */
public class ExpenseTrackerGUI extends JFrame {

    private final ExpenseService service;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    // Screen instances (kept so we can refresh them)
    private DashboardPanel dashboardPanel;
    private AddExpensePanel addExpensePanel;
    private ViewExpensesPanel viewExpensesPanel;
    private SearchExpensePanel searchExpensePanel;
    private MonthlyReportPanel monthlyReportPanel;
    private BudgetPanel budgetPanel;

    // Screen name constants used by all panels
    public static final String DASHBOARD = "DASHBOARD";
    public static final String ADD       = "ADD";
    public static final String VIEW      = "VIEW";
    public static final String SEARCH    = "SEARCH";
    public static final String REPORT    = "REPORT";
    public static final String BUDGET    = "BUDGET";

    // ================================================================
    //  MAIN METHOD – Run this class as "Run As -> Java Application"
    // ================================================================
    public static void main(String[] args) {
        // Ensure all Swing work happens on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AppTheme.applyGlobalDefaults();
            new ExpenseTrackerGUI();
        });
    }

    // ================================================================
    //  Constructor – builds the entire GUI window
    // ================================================================
    public ExpenseTrackerGUI() {
        super("Personal Expense Tracker");

        // Create the shared backend service (loads saved expenses from file)
        this.service = new ExpenseService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null); // centre on screen

        // CardLayout lets us switch between screens in the same window
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(AppTheme.BG_DARK);

        buildScreens();
        buildNavBar();

        add(mainPanel, BorderLayout.CENTER);

        // Show the dashboard first
        showScreen(DASHBOARD);
        setVisible(true);

        System.out.println("Personal Expense Tracker GUI started successfully.");
        System.out.println("Loaded " + service.getAllExpenses().size() + " saved expense(s).");
    }

    // ──────────────────── Build all screen panels ────────────────────

    private void buildScreens() {
        dashboardPanel     = new DashboardPanel(service, this);
        addExpensePanel    = new AddExpensePanel(service, this);
        viewExpensesPanel  = new ViewExpensesPanel(service, this);
        searchExpensePanel = new SearchExpensePanel(service, this);
        monthlyReportPanel = new MonthlyReportPanel(service, this);
        budgetPanel        = new BudgetPanel(service, this);

        mainPanel.add(dashboardPanel,     DASHBOARD);
        mainPanel.add(addExpensePanel,    ADD);
        mainPanel.add(viewExpensesPanel,  VIEW);
        mainPanel.add(searchExpensePanel, SEARCH);
        mainPanel.add(monthlyReportPanel, REPORT);
        mainPanel.add(budgetPanel,        BUDGET);
    }

    // ──────────────────── Top navigation bar ────────────────────

    private void buildNavBar() {
        JPanel nav = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.PRIMARY));

        JButton btnDash   = ComponentFactory.primaryButton("Dashboard");
        JButton btnAdd    = ComponentFactory.accentButton("Add Expense");
        JButton btnView   = ComponentFactory.primaryButton("View Expenses");
        JButton btnSearch = ComponentFactory.primaryButton("Search");
        JButton btnReport = ComponentFactory.primaryButton("Monthly Report");
        JButton btnBudget = ComponentFactory.warningButton("Budget");
        JButton btnExit   = ComponentFactory.dangerButton("Exit");

        btnDash.addActionListener(e   -> showScreen(DASHBOARD));
        btnAdd.addActionListener(e    -> showScreen(ADD));
        btnView.addActionListener(e   -> showScreen(VIEW));
        btnSearch.addActionListener(e -> showScreen(SEARCH));
        btnReport.addActionListener(e -> showScreen(REPORT));
        btnBudget.addActionListener(e -> showScreen(BUDGET));
        btnExit.addActionListener(e   -> exitApplication());

        nav.add(btnDash);
        nav.add(btnAdd);
        nav.add(btnView);
        nav.add(btnSearch);
        nav.add(btnReport);
        nav.add(btnBudget);
        nav.add(btnExit);

        add(nav, BorderLayout.NORTH);
    }

    // ──────────────────── Screen navigation ────────────────────

    /**
     * Switches to the specified screen and refreshes its data.
     * Called by all panel classes when the user clicks navigation buttons.
     */
    public void showScreen(String name) {
        switch (name) {
            case DASHBOARD: dashboardPanel.refresh();     break;
            case VIEW:      viewExpensesPanel.refresh();   break;
            case SEARCH:    searchExpensePanel.refresh();   break;
            case REPORT:    monthlyReportPanel.refresh();   break;
            case BUDGET:    budgetPanel.refresh();         break;
        }
        cardLayout.show(mainPanel, name);
    }

    // ──────────────────── Exit ────────────────────

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?\nAll expenses will be saved.",
                "Exit Application",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            service.save();
            System.out.println("Expenses saved. Goodbye!");
            System.exit(0);
        }
    }
}
