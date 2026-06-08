import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LoginScreen extends JPanel {
    private final AuthManager authManager;
    private final LoginListener loginListener;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton actionButton;
    private JButton toggleButton;
    private JLabel statusLabel;
    private JLabel lblForgotPassword;
    private JCheckBox chkRememberMe;
    private JButton btnShowPassword;

    private boolean isLoginMode = true;

    public interface LoginListener {
        void onLoginSuccess();
    }

    public LoginScreen(AuthManager authManager, LoginListener loginListener) {
        this.authManager = authManager;
        this.loginListener = loginListener;

        setLayout(new GridBagLayout());
        setBackground(Theme.BG_DARK);

        initUI();
    }

    private void initUI() {
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Theme.CARD_BG);
        cardPanel.setBorder(Theme.createCardBorder());
        cardPanel.setPreferredSize(new Dimension(380, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Title
        JLabel titleLabel = new JLabel("STOCKPILOT", SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.ACCENT);
        cardPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Stock Trading Terminal", SwingConstants.CENTER);
        subtitleLabel.setFont(Theme.FONT_SMALL);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        gbc.insets = new Insets(0, 0, 24, 0);
        cardPanel.add(subtitleLabel, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);

        // Username Label & Field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(Theme.FONT_BODY_BOLD);
        userLabel.setForeground(Theme.TEXT_PRIMARY);
        cardPanel.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setBackground(Theme.BG_DARK);
        usernameField.setForeground(Theme.TEXT_PRIMARY);
        usernameField.setCaretColor(Theme.TEXT_PRIMARY);
        usernameField.setFont(Theme.FONT_BODY);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cardPanel.add(usernameField, gbc);

        // Password Label & Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_BODY_BOLD);
        passLabel.setForeground(Theme.TEXT_PRIMARY);
        cardPanel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setBackground(Theme.BG_DARK);
        passwordField.setForeground(Theme.TEXT_PRIMARY);
        passwordField.setCaretColor(Theme.TEXT_PRIMARY);
        passwordField.setFont(Theme.FONT_BODY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cardPanel.add(passwordField, gbc);

        // Password Options Panel (Remember Me + Show Password)
        JPanel passwordOptionsPanel = new JPanel(new BorderLayout());
        passwordOptionsPanel.setOpaque(false);
        gbc.insets = new Insets(4, 0, 4, 0);
        cardPanel.add(passwordOptionsPanel, gbc);

        // Remember Me Checkbox
        chkRememberMe = new JCheckBox("Remember Me");
        chkRememberMe.setFont(Theme.FONT_SMALL);
        chkRememberMe.setForeground(Theme.TEXT_SECONDARY);
        chkRememberMe.setBackground(Theme.CARD_BG);
        chkRememberMe.setSelected(false);
        passwordOptionsPanel.add(chkRememberMe, BorderLayout.WEST);

        // Show Password Button
        btnShowPassword = new JButton("Show");
        btnShowPassword.setFont(Theme.FONT_SMALL);
        btnShowPassword.setForeground(Theme.ACCENT);
        btnShowPassword.setBackground(Theme.CARD_BG);
        btnShowPassword.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        btnShowPassword.setContentAreaFilled(false);
        btnShowPassword.setFocusPainted(false);
        btnShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowPassword.addActionListener(e -> togglePasswordVisibility());
        passwordOptionsPanel.add(btnShowPassword, BorderLayout.EAST);

        // Status Label (For error/success messages)
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.LOSS);
        gbc.insets = new Insets(12, 0, 4, 0);
        cardPanel.add(statusLabel, gbc);

        // Action Button (Login / Register)
        actionButton = new JButton("Log In");
        actionButton.setFont(Theme.FONT_BODY_BOLD);
        actionButton.setBackground(Theme.ACCENT);
        actionButton.setForeground(Color.WHITE);
        actionButton.setFocusPainted(false);
        actionButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButton.addActionListener(e -> handleAuth());
        gbc.insets = new Insets(8, 0, 8, 0);
        cardPanel.add(actionButton, gbc);

        // Toggle Mode Button
        toggleButton = new JButton("Don't have an account? Register");
        toggleButton.setFont(Theme.FONT_SMALL);
        toggleButton.setForeground(Theme.TEXT_SECONDARY);
        toggleButton.setBorder(null);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(e -> toggleMode());
        gbc.insets = new Insets(8, 0, 0, 0);
        cardPanel.add(toggleButton, gbc);

        // Forgot Password Link (only in login mode)
        lblForgotPassword = new JLabel("Forgot Password?");
        lblForgotPassword.setFont(Theme.FONT_SMALL);
        lblForgotPassword.setForeground(Theme.ACCENT);
        lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleForgotPassword();
            }
        });
        gbc.insets = new Insets(4, 0, 0, 0);
        cardPanel.add(lblForgotPassword, gbc);

        add(cardPanel);

        // Support Enter key submit
        ActionListener enterSubmit = e -> handleAuth();
        usernameField.addActionListener(enterSubmit);
        passwordField.addActionListener(enterSubmit);
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            actionButton.setText("Log In");
            toggleButton.setText("Don't have an account? Register");
            lblForgotPassword.setVisible(true);
        } else {
            actionButton.setText("Register Account");
            toggleButton.setText("Already have an account? Log In");
            lblForgotPassword.setVisible(false);
        }
        statusLabel.setText(" ");
        usernameField.setText("");
        passwordField.setText("");
    }

    private void handleForgotPassword() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Please enter your username first.");
            return;
        }

        // For this simulation, we'll just show a message
        // In a real app, you would send a password reset email
        JOptionPane.showMessageDialog(this,
            "Password reset link has been sent to your email.\n\n(This is a simulation - no actual email was sent)",
            "Password Reset",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void togglePasswordVisibility() {
        if (passwordField.getEchoChar() == '\u0000') {
            passwordField.setEchoChar('•');
            btnShowPassword.setText("Show");
        } else {
            passwordField.setEchoChar('\u0000');
            btnShowPassword.setText("Hide");
        }
    }

    private void handleAuth() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Fields cannot be empty.");
            return;
        }

        if (isLoginMode) {
            boolean success = authManager.login(username, password);
            if (success) {
                loginListener.onLoginSuccess();
            } else {
                showError("Invalid username or password.");
            }
        } else {
            boolean success = authManager.register(username, password);
            if (success) {
                statusLabel.setForeground(Theme.GAIN);
                statusLabel.setText("Registration successful! Logging in...");
                Timer timer = new Timer(1000, e -> {
                    authManager.login(username, password);
                    loginListener.onLoginSuccess();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                showError("Username already exists.");
            }
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(Theme.LOSS);
        statusLabel.setText(msg);
    }

    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
        if (!isLoginMode) {
            toggleMode();
        }
    }
}
