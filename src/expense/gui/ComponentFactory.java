package expense.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Factory helper that creates pre-styled Swing components.
 * Every screen re-uses these methods so the whole app looks consistent.
 */
public final class ComponentFactory {

    private ComponentFactory() { }

    // ───────────────────── Buttons ─────────────────────

    /**
     * Creates a styled button with rounded feel, hover effect and custom colours.
     */
    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));

        Color hoverColor = bgColor.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); btn.repaint(); }
            @Override
            public void mouseExited(MouseEvent e)  { btn.setBackground(bgColor); btn.repaint(); }
        });
        return btn;
    }

    public static JButton primaryButton(String text)  { return createButton(text, AppTheme.PRIMARY); }
    public static JButton accentButton(String text)    { return createButton(text, AppTheme.ACCENT); }
    public static JButton dangerButton(String text)    { return createButton(text, AppTheme.DANGER); }
    public static JButton warningButton(String text)   { return createButton(text, AppTheme.WARNING); }

    // ───────────────────── Labels ─────────────────────

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_TITLE);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_SUBTITLE);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel statLabel(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(AppTheme.FONT_STAT);
        lbl.setForeground(color);
        return lbl;
    }

    // ───────────────────── Text fields ─────────────────────

    public static JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        styleField(field);
        return field;
    }

    public static JTextField textField() {
        return textField(18);
    }

    private static void styleField(JTextField field) {
        field.setFont(AppTheme.FONT_FIELD);
        field.setBackground(AppTheme.BG_INPUT);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setCaretColor(AppTheme.TEXT_PRIMARY);
        field.setBorder(AppTheme.FIELD_BORDER);
    }

    // ───────────────────── Combo boxes ─────────────────────

    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppTheme.FONT_FIELD);
        cb.setBackground(AppTheme.BG_INPUT);
        cb.setForeground(AppTheme.TEXT_PRIMARY);
        return cb;
    }

    // ───────────────────── Panels ─────────────────────

    /** A dark card-like panel with a border. */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(AppTheme.BG_CARD);
        panel.setBorder(AppTheme.CARD_BORDER);
        return panel;
    }

    /** A dark panel with no border. */
    public static JPanel darkPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(AppTheme.BG_DARK);
        return panel;
    }

    /** A dark panel with no border and the given layout. */
    public static JPanel darkPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(AppTheme.BG_DARK);
        return panel;
    }

    // ───────────────────── Tables ─────────────────────

    /**
     * Creates a styled, non-editable table with alternating row colours.
     */
    public static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? AppTheme.TABLE_ROW : AppTheme.TABLE_ROW_ALT);
                } else {
                    c.setBackground(AppTheme.PRIMARY);
                }
                c.setForeground(AppTheme.TEXT_PRIMARY);
                return c;
            }
        };
        table.setFont(AppTheme.FONT_TABLE);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.TABLE_ROW);
        table.setGridColor(AppTheme.BORDER_COLOR);
        table.setRowHeight(30);
        table.setSelectionBackground(AppTheme.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppTheme.TABLE_HEADER);
        header.setForeground(AppTheme.TEXT_PRIMARY);
        header.setFont(AppTheme.FONT_TABLE_HD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.PRIMARY));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(AppTheme.TABLE_HEADER);
        headerRenderer.setForeground(AppTheme.TEXT_PRIMARY);
        headerRenderer.setFont(AppTheme.FONT_TABLE_HD);
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        header.setDefaultRenderer(headerRenderer);

        return table;
    }

    /** Wraps a table inside a dark scroll pane. */
    public static JScrollPane tableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(AppTheme.BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR));
        return sp;
    }
}
