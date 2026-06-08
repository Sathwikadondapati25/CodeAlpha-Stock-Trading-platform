import java.awt.*;
import javax.swing.*;

public class SettingsScreen extends JPanel {
    private final AuthManager authManager;
    private final StorageManager storageManager;
    private final MainFrame mainFrame;

    private JCheckBox chkDarkMode;
    private JComboBox<String> cmbRefreshRate;
    private JComboBox<String> cmbCurrencyFormat;
    private JLabel lblStatus;

    public SettingsScreen(AuthManager authManager, StorageManager storageManager, MainFrame mainFrame) {
        this.authManager = authManager;
        this.storageManager = storageManager;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.getBackground());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- SETTINGS PANEL ---
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(Theme.getCardBackground());
        settingsPanel.setBorder(Theme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 20, 10, 20);

        // Appearance Section
        JLabel appearanceTitle = new JLabel("Appearance");
        appearanceTitle.setFont(Theme.FONT_SUBHEADER);
        appearanceTitle.setForeground(Theme.getTextPrimary());
        settingsPanel.add(appearanceTitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 8, 20);

        // Dark Mode Toggle
        chkDarkMode = new JCheckBox("Dark Mode");
        chkDarkMode.setSelected(Theme.isDarkMode());
        chkDarkMode.setFont(Theme.FONT_BODY);
        chkDarkMode.setForeground(Theme.getTextPrimary());
        chkDarkMode.setBackground(Theme.getCardBackground());
        chkDarkMode.addActionListener(e -> toggleDarkMode());
        settingsPanel.add(chkDarkMode, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 20, 8, 20);

        // Trading Section
        JLabel tradingTitle = new JLabel("Trading");
        tradingTitle.setFont(Theme.FONT_SUBHEADER);
        tradingTitle.setForeground(Theme.getTextPrimary());
        settingsPanel.add(tradingTitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 8, 20);

        // Refresh Rate
        JLabel lblRefreshRate = new JLabel("Price Refresh Rate:");
        lblRefreshRate.setFont(Theme.FONT_BODY_BOLD);
        lblRefreshRate.setForeground(Theme.getTextSecondary());
        settingsPanel.add(lblRefreshRate, gbc);

        gbc.gridy++;
        cmbRefreshRate = new JComboBox<>(new String[]{"2 seconds", "3 seconds", "5 seconds"});
        cmbRefreshRate.setSelectedIndex(1);
        cmbRefreshRate.setFont(Theme.FONT_BODY);
        cmbRefreshRate.setBackground(Theme.getBackground());
        cmbRefreshRate.setForeground(Theme.getTextPrimary());
        settingsPanel.add(cmbRefreshRate, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 8, 20);

        // Currency Format
        JLabel lblCurrency = new JLabel("Currency Format:");
        lblCurrency.setFont(Theme.FONT_BODY_BOLD);
        lblCurrency.setForeground(Theme.getTextSecondary());
        settingsPanel.add(lblCurrency, gbc);

        gbc.gridy++;
        cmbCurrencyFormat = new JComboBox<>(new String[]{"₹ (INR)", "$ (USD)", "€ (EUR)", "£ (GBP)"});
        cmbCurrencyFormat.setSelectedIndex(0);
        cmbCurrencyFormat.setFont(Theme.FONT_BODY);
        cmbCurrencyFormat.setBackground(Theme.getBackground());
        cmbCurrencyFormat.setForeground(Theme.getTextPrimary());
        settingsPanel.add(cmbCurrencyFormat, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 20, 8, 20);

        // Save Button
        JButton btnSave = new JButton("Save Settings");
        btnSave.setFont(Theme.FONT_BODY_BOLD);
        btnSave.setBackground(Theme.ACCENT);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnSave.addActionListener(e -> saveSettings());
        settingsPanel.add(btnSave, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 20, 20);

        // Status Label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(Theme.FONT_SMALL);
        lblStatus.setForeground(Theme.TEXT_MUTED);
        settingsPanel.add(lblStatus, gbc);

        add(settingsPanel, BorderLayout.CENTER);
    }

    private void toggleDarkMode() {
        Theme.setDarkMode(chkDarkMode.isSelected());
        NotificationManager.getInstance().showInfo("Theme changed to " + (Theme.isDarkMode() ? "Dark" : "Light") + " Mode");

        // Refresh the entire application
        SwingUtilities.invokeLater(() -> {
            setBackground(Theme.getBackground());
            mainFrame.refreshAllScreens();
        });
    }

    private void saveSettings() {
        // Save settings to storage
        String username = authManager.getCurrentUser().getUsername();
        java.util.Properties props = storageManager.loadUserProfile(username);
        props.setProperty("darkMode", String.valueOf(chkDarkMode.isSelected()));
        props.setProperty("refreshRate", (String) cmbRefreshRate.getSelectedItem());
        props.setProperty("currencyFormat", (String) cmbCurrencyFormat.getSelectedItem());
        storageManager.saveUserProfile(username, props);

        lblStatus.setText("Settings saved successfully!");
        lblStatus.setForeground(Theme.GAIN);
        NotificationManager.getInstance().showSuccess("Settings saved successfully");
    }

    public void refresh() {
        if (!authManager.isLoggedIn()) return;

        String username = authManager.getCurrentUser().getUsername();
        java.util.Properties props = storageManager.loadUserProfile(username);

        // Load saved settings
        chkDarkMode.setSelected(Boolean.parseBoolean(props.getProperty("darkMode", "true")));
        Theme.setDarkMode(chkDarkMode.isSelected());

        String refreshRate = props.getProperty("refreshRate", "3 seconds");
        cmbRefreshRate.setSelectedItem(refreshRate);

        String currency = props.getProperty("currencyFormat", "₹ (INR)");
        cmbCurrencyFormat.setSelectedItem(currency);
    }
}
