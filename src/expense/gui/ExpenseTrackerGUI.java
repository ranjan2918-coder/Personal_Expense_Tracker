package expense.gui;

import expense.database.DBConnection;
import expense.service.ExpenseService;

import javax.swing.*;
import java.awt.*;

/**
 * Main GUI class for Personal Expense Tracker.
 *
 * Launch sequence:
 *   main() -> LoginPanel -> ExpenseTrackerGUI (after successful login)
 *
 * The window uses a CardLayout to switch between screens:
 *   Dashboard | Add Expense | View All | Search | Monthly Report | Budget
 *
 * All screens are connected to the ExpenseService backend (MySQL via JDBC).
 */
public class ExpenseTrackerGUI extends JFrame {

    private final ExpenseService service;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    // Screen instances (kept so we can refresh them)
    private DashboardPanel    dashboardPanel;
    private AddExpensePanel   addExpensePanel;
    private ViewExpensesPanel viewExpensesPanel;
    private SearchExpensePanel searchExpensePanel;
    private MonthlyReportPanel monthlyReportPanel;
    private BudgetPanel       budgetPanel;

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
        SwingUtilities.invokeLater(() -> {
            AppTheme.applyGlobalDefaults();
            // Check DB connectivity first; show friendly error if it fails
            ExpenseService svc = new ExpenseService();
            if (!DBConnection.testConnection()) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to MySQL database.\n\n" +
                    "Please check:\n" +
                    "  1. MySQL server is running.\n" +
                    "  2. Database 'personal_expense_tracker' exists.\n" +
                    "  3. Username/password in DBConnection.java are correct.\n" +
                    "  4. mysql-connector-j-*.jar is on your build path.\n\n" +
                    "See SQL_SETUP.sql for setup instructions.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new LoginPanel(svc);
        });
    }

    // ================================================================
    //  Constructor called by LoginPanel after authentication
    // ================================================================
    public ExpenseTrackerGUI(ExpenseService service, String username) {
        super("Personal Expense Tracker  —  Welcome, " + username);
        this.service = service;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });

        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(AppTheme.BG_DARK);

        buildScreens();
        buildNavBar(username);

        add(mainPanel, BorderLayout.CENTER);

        showScreen(DASHBOARD);
        setVisible(true);

        System.out.println("Personal Expense Tracker GUI started for user: " + username);
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

    private void buildNavBar(String username) {
        JPanel nav = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.PRIMARY));

        JButton btnDash   = ComponentFactory.primaryButton("Dashboard");
        JButton btnAdd    = ComponentFactory.accentButton("Add Expense");
        JButton btnView   = ComponentFactory.primaryButton("View Expenses");
        JButton btnSearch = ComponentFactory.primaryButton("Search");
        JButton btnReport = ComponentFactory.primaryButton("Monthly Report");
        JButton btnBudget = ComponentFactory.warningButton("Budget");

        // User badge (right-aligned)
        JLabel userLbl = ComponentFactory.label("\uD83D\uDC64 " + username);
        userLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JButton btnExit = ComponentFactory.dangerButton("Logout");

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
        nav.add(Box.createHorizontalStrut(20));
        nav.add(userLbl);
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
            case VIEW:      viewExpensesPanel.refresh();  break;
            case SEARCH:    searchExpensePanel.refresh(); break;
            case REPORT:    monthlyReportPanel.refresh(); break;
            case BUDGET:    budgetPanel.refresh();        break;
        }
        cardLayout.show(mainPanel, name);
    }

    // ──────────────────── Exit / Logout ────────────────────

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout and exit?",
                "Logout / Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            DBConnection.closeConnection();
            System.out.println("Logged out. Goodbye!");
            System.exit(0);
        }
    }
}
