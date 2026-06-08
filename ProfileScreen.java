import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

public class ProfileScreen extends JPanel {
    private final AuthManager authManager;
    private final StorageManager storageManager;
    private final StockManager stockManager;

    private JLabel lblUsername;
    private JLabel lblAccountCreated;
    private JLabel lblTotalTrades;
    private JLabel lblPortfolioValue;
    private JLabel lblCashBalance;

    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblPasswordStatus;
    private JButton btnToggleTheme;

    public ProfileScreen(AuthManager authManager, StorageManager storageManager, StockManager stockManager) {
        this.authManager = authManager;
        this.storageManager = storageManager;
        this.stockManager = stockManager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.getBackground());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("User Profile");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        mainPanel.setOpaque(false);

        // Left Panel: Profile Info
        JPanel profileInfoPanel = createProfileInfoPanel();
        mainPanel.add(profileInfoPanel);

        // Right Panel: Change Password
        JPanel passwordPanel = createPasswordPanel();
        mainPanel.add(passwordPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.getCardBackground());
        panel.setBorder(Theme.createCardBorder());
        panel.setPreferredSize(new Dimension(350, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 20, 10, 20);

        // Title
        JLabel title = new JLabel("Account Information");
        title.setFont(Theme.FONT_SUBHEADER);
        title.setForeground(Theme.getTextPrimary());
        panel.add(title, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 8, 20);

        // Username
        addInfoField(panel, gbc, "Username:", lblUsername = new JLabel());

        // Account Created
        addInfoField(panel, gbc, "Account Created:", lblAccountCreated = new JLabel());

        // Total Trades
        addInfoField(panel, gbc, "Total Trades:", lblTotalTrades = new JLabel());

        // Portfolio Value
        addInfoField(panel, gbc, "Portfolio Value:", lblPortfolioValue = new JLabel());

        // Cash Balance
        addInfoField(panel, gbc, "Cash Balance:", lblCashBalance = new JLabel());

        return panel;
    }

    private void addInfoField(JPanel panel, GridBagConstraints gbc, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Theme.TEXT_SECONDARY);
        panel.add(label, gbc);

        gbc.gridy++;
        valueLabel.setFont(Theme.FONT_BODY);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        panel.add(valueLabel, gbc);

        gbc.gridy++;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.getCardBackground());
        panel.setBorder(Theme.createCardBorder());
        panel.setPreferredSize(new Dimension(350, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 20, 10, 20);

        // Title
        JLabel title = new JLabel("Change Password");
        title.setFont(Theme.FONT_SUBHEADER);
        title.setForeground(Theme.TEXT_PRIMARY);
        panel.add(title, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 8, 20);

        // Current Password
        JLabel lblCurrent = new JLabel("Current Password:");
        lblCurrent.setFont(Theme.FONT_BODY_BOLD);
        lblCurrent.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lblCurrent, gbc);

        gbc.gridy++;
        txtCurrentPassword = new JPasswordField();
        txtCurrentPassword.setBackground(Theme.getBackground());
        txtCurrentPassword.setForeground(Theme.getTextPrimary());
        txtCurrentPassword.setCaretColor(Theme.getTextPrimary());
        txtCurrentPassword.setFont(Theme.FONT_BODY);
        txtCurrentPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(txtCurrentPassword, gbc);

        gbc.gridy++;

        // New Password
        JLabel lblNew = new JLabel("New Password:");
        lblNew.setFont(Theme.FONT_BODY_BOLD);
        lblNew.setForeground(Theme.getTextSecondary());
        panel.add(lblNew, gbc);

        gbc.gridy++;
        txtNewPassword = new JPasswordField();
        txtNewPassword.setBackground(Theme.getBackground());
        txtNewPassword.setForeground(Theme.getTextPrimary());
        txtNewPassword.setCaretColor(Theme.getTextPrimary());
        txtNewPassword.setFont(Theme.FONT_BODY);
        txtNewPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(txtNewPassword, gbc);

        gbc.gridy++;

        // Confirm Password
        JLabel lblConfirm = new JLabel("Confirm Password:");
        lblConfirm.setFont(Theme.FONT_BODY_BOLD);
        lblConfirm.setForeground(Theme.getTextSecondary());
        panel.add(lblConfirm, gbc);

        gbc.gridy++;
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setBackground(Theme.getBackground());
        txtConfirmPassword.setForeground(Theme.getTextPrimary());
        txtConfirmPassword.setCaretColor(Theme.getTextPrimary());
        txtConfirmPassword.setFont(Theme.FONT_BODY);
        txtConfirmPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(txtConfirmPassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 20, 8, 20);

        // Status Label
        lblPasswordStatus = new JLabel(" ");
        lblPasswordStatus.setFont(Theme.FONT_SMALL);
        lblPasswordStatus.setForeground(Theme.TEXT_MUTED);
        panel.add(lblPasswordStatus, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 20, 20);

        // Change Password Button
        JButton btnChangePassword = new JButton("Change Password");
        btnChangePassword.setFont(Theme.FONT_BODY_BOLD);
        btnChangePassword.setBackground(Theme.ACCENT);
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setFocusPainted(false);
        btnChangePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChangePassword.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnChangePassword.addActionListener(e -> changePassword());
        panel.add(btnChangePassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 20, 20);

        // Theme Toggle Button
        btnToggleTheme = new JButton(Theme.isDarkMode() ? "Switch to Light Mode" : "Switch to Dark Mode");
        btnToggleTheme.setFont(Theme.FONT_BODY_BOLD);
        btnToggleTheme.setBackground(Theme.TEXT_SECONDARY);
        btnToggleTheme.setForeground(Color.WHITE);
        btnToggleTheme.setFocusPainted(false);
        btnToggleTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleTheme.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnToggleTheme.addActionListener(e -> toggleTheme());
        panel.add(btnToggleTheme, gbc);

        return panel;
    }

    private void changePassword() {
        String currentPassword = new String(txtCurrentPassword.getPassword());
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordError("All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("New passwords do not match.");
            return;
        }

        if (newPassword.length() < 6) {
            showPasswordError("Password must be at least 6 characters.");
            return;
        }

        // Verify current password
        if (!authManager.verifyPassword(authManager.getCurrentUser().getUsername(), currentPassword)) {
            showPasswordError("Current password is incorrect.");
            return;
        }

        // Change password
        authManager.changePassword(authManager.getCurrentUser().getUsername(), newPassword);
        showPasswordSuccess("Password changed successfully!");

        // Clear fields
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }

    private void showPasswordError(String message) {
        lblPasswordStatus.setText(message);
        lblPasswordStatus.setForeground(Theme.LOSS);
    }

    private void showPasswordSuccess(String message) {
        lblPasswordStatus.setText(message);
        lblPasswordStatus.setForeground(Theme.GAIN);
    }

    private void toggleTheme() {
        Theme.toggleTheme();
        btnToggleTheme.setText(Theme.isDarkMode() ? "Switch to Light Mode" : "Switch to Dark Mode");
        NotificationManager.getInstance().showInfo("Theme switched to " + (Theme.isDarkMode() ? "Dark" : "Light") + " Mode");

        // Refresh the entire UI
        SwingUtilities.invokeLater(() -> {
            refresh();
        });
    }

    public void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        String username = authManager.getCurrentUser().getUsername();

        // Update profile info
        lblUsername.setText(username.toUpperCase());

        // Get account creation date from profile
        java.util.Properties profile = storageManager.loadUserProfile(username);
        String createdDate = profile.getProperty("createdDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        lblAccountCreated.setText(createdDate);

        // Get total trades
        List<Transaction> transactions = storageManager.loadTransactions(username);
        lblTotalTrades.setText(String.valueOf(transactions.size()));

        // Calculate portfolio value
        List<Holding> holdings = storageManager.loadPortfolio(username);
        double portfolioValue = 0.0;
        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                portfolioValue += h.getCurrentValue(s.getCurrentPrice());
            }
        }
        lblPortfolioValue.setText(String.format("₹%.2f", portfolioValue));

        // Cash balance
        double balance = authManager.getCurrentUser().getBalance();
        lblCashBalance.setText(String.format("₹%.2f", balance));
    }
}
