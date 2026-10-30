package expense.gui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Centralized theme constants used across every GUI screen.
 * Changing values here updates the entire application look.
 */
public final class AppTheme {

    // ---- Colour palette ----
    public static final Color PRIMARY       = new Color(41, 98, 255);     // Vibrant blue
    public static final Color PRIMARY_DARK   = new Color(25, 60, 160);
    public static final Color ACCENT         = new Color(0, 200, 83);     // Green accent
    public static final Color DANGER         = new Color(229, 57, 53);    // Red for delete / warnings
    public static final Color WARNING        = new Color(255, 179, 0);    // Amber warning
    public static final Color BG_DARK        = new Color(30, 30, 46);     // Dark background
    public static final Color BG_CARD        = new Color(45, 45, 65);     // Card background
    public static final Color BG_INPUT       = new Color(55, 55, 78);     // Text field fill
    public static final Color TEXT_PRIMARY   = new Color(230, 230, 250);  // Light text
    public static final Color TEXT_SECONDARY = new Color(160, 160, 190);  // Muted text
    public static final Color BORDER_COLOR   = new Color(70, 70, 100);    // Panel borders
    public static final Color TABLE_HEADER   = new Color(35, 35, 55);
    public static final Color TABLE_ROW_ALT  = new Color(40, 40, 58);
    public static final Color TABLE_ROW      = new Color(50, 50, 70);

    // ---- Fonts ----
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_FIELD    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT     = new Font("Segoe UI", Font.BOLD, 22);

    // ---- Borders ----
    public static final Border CARD_BORDER =
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(18, 18, 18, 18));

    public static final Border FIELD_BORDER =
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10));

    private AppTheme() { /* prevent instantiation */ }

    /** Apply a dark look-and-feel to common Swing defaults. */
    public static void applyGlobalDefaults() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }

        UIManager.put("Panel.background",            BG_DARK);
        UIManager.put("OptionPane.background",        BG_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont",       FONT_LABEL);
        UIManager.put("OptionPane.buttonFont",        FONT_BUTTON);
        UIManager.put("Button.font",                  FONT_BUTTON);
    }
}
