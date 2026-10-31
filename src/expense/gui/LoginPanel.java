package expense.gui;

import expense.dao.UserDAO;
import expense.model.User;
import expense.service.ExpenseService;
import expense.database.DBConnection;

import javax.swing.*;
import java.awt.*;

/**
 * LoginPanel – shown before the main tracker GUI.
 * Handles both Login and Registration in one screen via a tab switch.
 *
 * On successful login / registration:
 *   1. Sets the logged-in userId on the ExpenseService.
 *   2. Hides this panel.
 *   3. Shows the main ExpenseTrackerGUI window.
 */
public class LoginPanel extends JFrame {

    private final UserDAO        userDAO;
    private final ExpenseService service;

    // Login widgets
    private JTextField  loginUserField;
    private JPasswordField loginPassField;
    private JLabel      loginMsgLabel;

    // Register widgets
    private JTextField  regUserField;
    private JPasswordField regPassField;
    private JPasswordField regConfirmField;
    private JLabel      regMsgLabel;

    public LoginPanel(ExpenseService service) {
        super("Personal Expense Tracker – Login");
        this.service = service;
        this.userDAO = new UserDAO();

        AppTheme.applyGlobalDefaults();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 420);
        setMinimumSize(new Dimension(420, 380));
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);

        // ── App title ──
        JLabel title = ComponentFactory.titleLabel("Personal Expense Tracker");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel titlePanel = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        titlePanel.add(title);
        root.add(titlePanel, BorderLayout.NORTH);

        // ── Tabbed pane: Login / Register ──
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(AppTheme.BG_DARK);
        tabs.setForeground(AppTheme.TEXT_PRIMARY);
        tabs.setFont(AppTheme.FONT_BUTTON);
        tabs.addTab("Login",    buildLoginTab());
        tabs.addTab("Register", buildRegisterTab());

        root.add(tabs, BorderLayout.CENTER);

        // ── Footer hint ──
        JLabel hint = ComponentFactory.label("Powered by MySQL + JDBC  |  Java Swing");
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        hint.setForeground(AppTheme.TEXT_SECONDARY);
        JPanel footer = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        footer.add(hint);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }

    // ──────────────────── Login tab ────────────────────

    private JPanel buildLoginTab() {
        JPanel card = ComponentFactory.cardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 16, 10, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Username
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        loginUserField = ComponentFactory.textField(18);
        loginUserField.setName("loginUsername");
        card.add(loginUserField, gbc);
        gbc.weightx = 0;

        // Password
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Password:"), gbc);
        gbc.gridx = 1;
        loginPassField = new JPasswordField(18);
        loginPassField.setBackground(AppTheme.BG_INPUT);
        loginPassField.setForeground(AppTheme.TEXT_PRIMARY);
        loginPassField.setCaretColor(AppTheme.TEXT_PRIMARY);
        loginPassField.setBorder(AppTheme.FIELD_BORDER);
        loginPassField.setFont(AppTheme.FONT_FIELD);
        loginPassField.setName("loginPassword");
        card.add(loginPassField, gbc);

        // Message
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        loginMsgLabel = new JLabel(" ");
        loginMsgLabel.setFont(AppTheme.FONT_LABEL);
        card.add(loginMsgLabel, gbc);

        // Buttons
        row++;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnRow = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton loginBtn = ComponentFactory.accentButton("Login");
        loginBtn.setPreferredSize(new Dimension(140, 38));
        loginBtn.setName("btnLogin");
        loginBtn.addActionListener(e -> doLogin());

        btnRow.add(loginBtn);
        card.add(btnRow, gbc);

        // Allow Enter key
        loginPassField.addActionListener(e -> doLogin());
        loginUserField.addActionListener(e -> doLogin());

        JPanel wrapper = ComponentFactory.darkPanel(new GridBagLayout());
        wrapper.add(card);
        return wrapper;
    }

    // ──────────────────── Register tab ────────────────────

    private JPanel buildRegisterTab() {
        JPanel card = ComponentFactory.cardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 16, 10, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Username
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        regUserField = ComponentFactory.textField(18);
        regUserField.setName("regUsername");
        card.add(regUserField, gbc);
        gbc.weightx = 0;

        // Password
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Password:"), gbc);
        gbc.gridx = 1;
        regPassField = new JPasswordField(18);
        stylePasswordField(regPassField, "regPassword");
        card.add(regPassField, gbc);

        // Confirm Password
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        card.add(ComponentFactory.label("Confirm Password:"), gbc);
        gbc.gridx = 1;
        regConfirmField = new JPasswordField(18);
        stylePasswordField(regConfirmField, "regConfirm");
        card.add(regConfirmField, gbc);

        // Message
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        regMsgLabel = new JLabel(" ");
        regMsgLabel.setFont(AppTheme.FONT_LABEL);
        card.add(regMsgLabel, gbc);

        // Button
        row++;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnRow = ComponentFactory.darkPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton regBtn = ComponentFactory.accentButton("Create Account");
        regBtn.setPreferredSize(new Dimension(160, 38));
        regBtn.setName("btnRegister");
        regBtn.addActionListener(e -> doRegister());

        btnRow.add(regBtn);
        card.add(btnRow, gbc);

        JPanel wrapper = ComponentFactory.darkPanel(new GridBagLayout());
        wrapper.add(card);
        return wrapper;
    }

    // ──────────────────── Actions ────────────────────

    private void doLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showLoginMsg("Username and password are required.", true);
            return;
        }

        User user = userDAO.loginUser(username, password);
        if (user != null) {
            showLoginMsg("Login successful! Loading...", false);
            service.setCurrentUser(user.getUserId());
            openMainWindow(user.getUsername());
        } else {
            showLoginMsg("Invalid username or password.", true);
            loginPassField.setText("");
        }
    }

    private void doRegister() {
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword());
        String confirm  = new String(regConfirmField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showRegMsg("All fields are required.", true);
            return;
        }
        if (username.length() < 3) {
            showRegMsg("Username must be at least 3 characters.", true);
            return;
        }
        if (!password.equals(confirm)) {
            showRegMsg("Passwords do not match.", true);
            return;
        }
        if (password.length() < 4) {
            showRegMsg("Password must be at least 4 characters.", true);
            return;
        }
        if (userDAO.usernameExists(username)) {
            showRegMsg("Username already taken. Choose another.", true);
            return;
        }

        int newId = userDAO.registerUser(username, password);
        if (newId > 0) {
            showRegMsg("Account created! You can now log in.", false);
            service.setCurrentUser(newId);
            openMainWindow(username);
        } else {
            showRegMsg("Registration failed. Try again.", true);
        }
    }

    private void openMainWindow(String username) {
        SwingUtilities.invokeLater(() -> {
            dispose(); // close login window
            new ExpenseTrackerGUI(service, username);
        });
    }

    // ──────────────────── Helpers ────────────────────

    private void showLoginMsg(String msg, boolean isError) {
        loginMsgLabel.setText(msg);
        loginMsgLabel.setForeground(isError ? AppTheme.DANGER : AppTheme.ACCENT);
    }

    private void showRegMsg(String msg, boolean isError) {
        regMsgLabel.setText(msg);
        regMsgLabel.setForeground(isError ? AppTheme.DANGER : AppTheme.ACCENT);
    }

    private void stylePasswordField(JPasswordField f, String name) {
        f.setBackground(AppTheme.BG_INPUT);
        f.setForeground(AppTheme.TEXT_PRIMARY);
        f.setCaretColor(AppTheme.TEXT_PRIMARY);
        f.setBorder(AppTheme.FIELD_BORDER);
        f.setFont(AppTheme.FONT_FIELD);
        f.setName(name);
    }
}
