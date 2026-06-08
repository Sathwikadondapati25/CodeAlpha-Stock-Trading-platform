import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Theme {
    private static boolean isDarkMode = true;

    // Modern Professional Slate Dark Theme Colors
    public static final Color BG_DARK = new Color(15, 23, 42);        // Slate 900
    public static final Color CARD_BG = new Color(30, 41, 59);        // Slate 800
    public static final Color SIDEBAR_BG = new Color(9, 15, 28);      // Deeper Dark Slate
    public static final Color ACCENT = new Color(59, 130, 246);       // Blue 500
    public static final Color ACCENT_HOVER = new Color(37, 99, 235);  // Blue 600
    public static final Color GAIN = new Color(16, 185, 129);         // Emerald 500
    public static final Color LOSS = new Color(244, 63, 94);           // Rose 500

    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);   // Slate 50
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // Slate 400
    public static final Color TEXT_MUTED = new Color(100, 116, 139);      // Slate 500
    public static final Color BORDER_COLOR = new Color(51, 65, 85);       // Slate 700

    // Light Mode Colors
    public static final Color BG_LIGHT = new Color(248, 250, 252);      // Slate 50
    public static final Color CARD_BG_LIGHT = new Color(255, 255, 255);   // White
    public static final Color SIDEBAR_BG_LIGHT = new Color(241, 245, 249); // Slate 100
    public static final Color TEXT_PRIMARY_LIGHT = new Color(15, 23, 42);  // Slate 900
    public static final Color TEXT_SECONDARY_LIGHT = new Color(71, 85, 105); // Slate 600
    public static final Color TEXT_MUTED_LIGHT = new Color(148, 163, 184);    // Slate 400
    public static final Color BORDER_COLOR_LIGHT = new Color(226, 232, 240);  // Slate 200

    // Typography
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    // Theme Management
    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }

    public static void toggleTheme() {
        isDarkMode = !isDarkMode;
    }

    // Dynamic Color Getters
    public static Color getBackground() {
        return isDarkMode ? BG_DARK : BG_LIGHT;
    }

    public static Color getCardBackground() {
        return isDarkMode ? CARD_BG : CARD_BG_LIGHT;
    }

    public static Color getSidebarBackground() {
        return isDarkMode ? SIDEBAR_BG : SIDEBAR_BG_LIGHT;
    }

    public static Color getTextPrimary() {
        return isDarkMode ? TEXT_PRIMARY : TEXT_PRIMARY_LIGHT;
    }

    public static Color getTextSecondary() {
        return isDarkMode ? TEXT_SECONDARY : TEXT_SECONDARY_LIGHT;
    }

    public static Color getTextMuted() {
        return isDarkMode ? TEXT_MUTED : TEXT_MUTED_LIGHT;
    }

    public static Color getBorderColor() {
        return isDarkMode ? BORDER_COLOR : BORDER_COLOR_LIGHT;
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }
}
