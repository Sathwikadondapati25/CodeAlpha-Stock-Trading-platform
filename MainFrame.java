import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MainFrame extends JFrame {
    private final AuthManager authManager;
    private final StockManager stockManager;
    private final TradingManager tradingManager;
    private final StorageManager storageManager;

    // Layout panels
    private JPanel sidebarPanel;
    private JPanel contentCardPanel;
    private CardLayout cardLayout;
    
    // Sidebar components
    private JLabel lblUserProfile;
    private List<JButton> navButtons;

    // View Panels
    private LoginScreen loginScreen;
    private DashboardScreen dashboardScreen;
    private MarketsScreen marketsScreen;
    private PortfolioScreen portfolioScreen;
    private TransactionsScreen transactionsScreen;
    private WatchlistScreen watchlistScreen;
    private ProfileScreen profileScreen;
    private AdminScreen adminScreen;
    private SettingsScreen settingsScreen;
    private LeaderboardScreen leaderboardScreen;
    private DiversificationScreen diversificationScreen;
    private ComparisonScreen comparisonScreen;
    private TechnicalIndicatorsScreen technicalIndicatorsScreen;
    private GoalsScreen goalsScreen;
    private ScreenerScreen screenerScreen;
    private DividendScreen dividendScreen;
    private BenchmarkScreen benchmarkScreen;
    private RiskMetricsScreen riskMetricsScreen;

    public MainFrame(AuthManager authManager, StockManager stockManager, TradingManager tradingManager, StorageManager storageManager) {
        this.authManager = authManager;
        this.stockManager = stockManager;
        this.tradingManager = tradingManager;
        this.storageManager = storageManager;

        setTitle("StockPilot - Stock Trading Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null); // Center window

        // Initialize notification manager
        NotificationManager.getInstance().setParentFrame(this);

        initUI();
    }

    private void initUI() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(Theme.BG_DARK);

        // 1. CONTENT CARD PANEL (Switchable screens)
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setOpaque(false);

        // Instantiate screens
        loginScreen = new LoginScreen(authManager, this::onLoginSuccess);
        dashboardScreen = new DashboardScreen(authManager, stockManager, storageManager);
        marketsScreen = new MarketsScreen(authManager, stockManager, tradingManager, storageManager);
        portfolioScreen = new PortfolioScreen(authManager, stockManager, tradingManager, storageManager);
        transactionsScreen = new TransactionsScreen(authManager, storageManager);
        watchlistScreen = new WatchlistScreen(authManager, stockManager, storageManager);
        profileScreen = new ProfileScreen(authManager, storageManager, stockManager);
        adminScreen = new AdminScreen(storageManager, stockManager, authManager);
        settingsScreen = new SettingsScreen(authManager, storageManager, this);
        leaderboardScreen = new LeaderboardScreen(storageManager, stockManager, authManager);
        diversificationScreen = new DiversificationScreen(storageManager, stockManager, authManager);
        comparisonScreen = new ComparisonScreen(stockManager, authManager);
        technicalIndicatorsScreen = new TechnicalIndicatorsScreen(stockManager, authManager);
        goalsScreen = new GoalsScreen(storageManager, stockManager, authManager);
        screenerScreen = new ScreenerScreen(stockManager, authManager);
        dividendScreen = new DividendScreen(storageManager, stockManager, authManager);
        benchmarkScreen = new BenchmarkScreen(storageManager, stockManager, authManager);
        riskMetricsScreen = new RiskMetricsScreen(storageManager, stockManager, authManager);

        // Add to CardLayout
        contentCardPanel.add(loginScreen, "LOGIN");
        contentCardPanel.add(dashboardScreen, "DASHBOARD");
        contentCardPanel.add(marketsScreen, "MARKETS");
        contentCardPanel.add(portfolioScreen, "PORTFOLIO");
        contentCardPanel.add(transactionsScreen, "TRANSACTIONS");
        contentCardPanel.add(watchlistScreen, "WATCHLIST");
        contentCardPanel.add(profileScreen, "PROFILE");
        contentCardPanel.add(adminScreen, "ADMIN");
        contentCardPanel.add(settingsScreen, "SETTINGS");
        contentCardPanel.add(leaderboardScreen, "LEADERBOARD");
        contentCardPanel.add(diversificationScreen, "DIVERSIFICATION");
        contentCardPanel.add(comparisonScreen, "COMPARISON");
        contentCardPanel.add(technicalIndicatorsScreen, "TECHNICAL");
        contentCardPanel.add(goalsScreen, "GOALS");
        contentCardPanel.add(screenerScreen, "SCREENER");
        contentCardPanel.add(dividendScreen, "DIVIDEND");
        contentCardPanel.add(benchmarkScreen, "BENCHMARK");
        contentCardPanel.add(riskMetricsScreen, "RISK");

        container.add(contentCardPanel, BorderLayout.CENTER);

        // 2. SIDEBAR PANEL (Initially hidden)
        initSidebar();
        container.add(sidebarPanel, BorderLayout.WEST);
        sidebarPanel.setVisible(false);

        // Show Login initially
        sidebarPanel.setVisible(false);
        cardLayout.show(contentCardPanel, "LOGIN");
    }

    private void initSidebar() {
        sidebarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(2, 11, 29), 0, getHeight(), new Color(7, 21, 47));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(28, 45, 74)));

        // Header / Logo
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        JLabel lblLogo = new JLabel("STOCKPILOT");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(new Color(61, 139, 255));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(lblLogo, gbc);

        lblUserProfile = new JLabel("Guest User", SwingConstants.CENTER);
        lblUserProfile.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserProfile.setForeground(Color.WHITE);
        lblUserProfile.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        logoPanel.add(lblUserProfile, gbc);

        JLabel lblSession = new JLabel("Active Session", SwingConstants.CENTER);
        lblSession.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSession.setForeground(new Color(140, 160, 194));
        logoPanel.add(lblSession, gbc);

        sidebarPanel.add(logoPanel, BorderLayout.NORTH);

        // Navigation Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        navButtons = new ArrayList<>();

        // Dashboard (standalone)
        JButton dashboardBtn = createNavButton("Dashboard", "DASHBOARD", "🏠");
        navButtons.add(dashboardBtn);
        menuPanel.add(dashboardBtn);
        menuPanel.add(Box.createVerticalStrut(4));

        // Trading Section
        menuPanel.add(createSectionHeader("TRADING"));
        JButton marketsBtn = createNavButton("Markets", "MARKETS", "📈");
        navButtons.add(marketsBtn);
        menuPanel.add(marketsBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton watchlistBtn = createNavButton("Watchlist", "WATCHLIST", "⭐");
        navButtons.add(watchlistBtn);
        menuPanel.add(watchlistBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton transactionsBtn = createNavButton("Transactions", "TRANSACTIONS", "📄");
        navButtons.add(transactionsBtn);
        menuPanel.add(transactionsBtn);
        menuPanel.add(Box.createVerticalStrut(4));

        // Portfolio Section
        menuPanel.add(createSectionHeader("PORTFOLIO"));
        JButton portfolioBtn = createNavButton("Portfolio", "PORTFOLIO", "💼");
        navButtons.add(portfolioBtn);
        menuPanel.add(portfolioBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton diversificationBtn = createNavButton("Diversification", "DIVERSIFICATION", "◔");
        navButtons.add(diversificationBtn);
        menuPanel.add(diversificationBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton benchmarkBtn = createNavButton("Benchmark", "BENCHMARK", "📉");
        navButtons.add(benchmarkBtn);
        menuPanel.add(benchmarkBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton riskBtn = createNavButton("Risk Metrics", "RISK", "⚠");
        navButtons.add(riskBtn);
        menuPanel.add(riskBtn);
        menuPanel.add(Box.createVerticalStrut(4));

        // Analysis Section
        menuPanel.add(createSectionHeader("ANALYSIS"));
        JButton comparisonBtn = createNavButton("Comparison", "COMPARISON", "🔍");
        navButtons.add(comparisonBtn);
        menuPanel.add(comparisonBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton technicalBtn = createNavButton("Technical", "TECHNICAL", "📊");
        navButtons.add(technicalBtn);
        menuPanel.add(technicalBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton screenerBtn = createNavButton("Screener", "SCREENER", "🔽");
        navButtons.add(screenerBtn);
        menuPanel.add(screenerBtn);
        menuPanel.add(Box.createVerticalStrut(4));

        // Account Section
        menuPanel.add(createSectionHeader("ACCOUNT"));
        JButton profileBtn = createNavButton("Profile", "PROFILE", "👤");
        navButtons.add(profileBtn);
        menuPanel.add(profileBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton settingsBtn = createNavButton("Settings", "SETTINGS", "⚙");
        navButtons.add(settingsBtn);
        menuPanel.add(settingsBtn);
        menuPanel.add(Box.createVerticalStrut(4));
        JButton leaderboardBtn = createNavButton("Leaderboard", "LEADERBOARD", "🏆");
        navButtons.add(leaderboardBtn);
        menuPanel.add(leaderboardBtn);
        menuPanel.add(Box.createVerticalStrut(4));

        // Administration Section
        menuPanel.add(createSectionHeader("ADMINISTRATION"));
        menuPanel.add(Box.createVerticalStrut(8));
        JButton adminBtn = createNavButton("Admin", "ADMIN", "🛡");
        navButtons.add(adminBtn);
        menuPanel.add(adminBtn);
        menuPanel.add(Box.createVerticalStrut(20));

        // Create logout icon programmatically
        ImageIcon logoutIcon = createLogoutIcon();
        
        JButton btnLogout = new JButton("LOG OUT");
        btnLogout.setIcon(logoutIcon);
        btnLogout.setIconTextGap(8);
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setBackground(new Color(7, 21, 47));
        btnLogout.setForeground(new Color(255, 77, 115));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 77, 115), 1, true),
            BorderFactory.createEmptyBorder(0, 16, 0, 0)
        ));
        btnLogout.setPreferredSize(new Dimension(220, 42));
        btnLogout.setMaximumSize(new Dimension(220, 42));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());
        
        // Add hover effect
        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogout.setBackground(new Color(12, 28, 58));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogout.setBackground(new Color(7, 21, 47));
            }
        });
        
        menuPanel.add(btnLogout);

        // Wrap menuPanel in scroll pane
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setOpaque(false);
        menuScrollPane.getViewport().setOpaque(false);
        menuScrollPane.setBorder(null);
        menuScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Wrap scroll pane in panel to prevent stretching
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(false);
        scrollWrapper.add(menuScrollPane, BorderLayout.NORTH);
        
        sidebarPanel.add(scrollWrapper, BorderLayout.CENTER);
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(102, 116, 143));
        label.setBorder(BorderFactory.createEmptyBorder(24, 16, 6, 0));
        return label;
    }

    private JButton createNavButton(String text, String screenName, String icon) {
       JButton button = new JButton(icon + "  " + text);
        button.setPreferredSize(new Dimension(220, 42));
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        button.setBackground(new Color(2, 11, 29));
        button.setForeground(new Color(215, 220, 230));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> switchScreen(screenName, button));

        // Hover Effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!isActiveButton(button)) {
                    button.setBackground(new Color(17, 38, 74));
                    button.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!isActiveButton(button)) {
                    button.setBackground(new Color(2, 11, 29));
                    button.setForeground(new Color(215, 220, 230));
                }
            }
        });

        return button;
    }

    private boolean isActiveButton(JButton button) {
        return button.getBackground().equals(new Color(17, 38, 74));
    }

    private void switchScreen(String screenName, JButton activeButton) {
        // Highlight active nav button
        for (JButton btn : navButtons) {
            btn.setBackground(new Color(2, 11, 29));
            btn.setForeground(new Color(215, 220, 230));
            btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
            btn.setOpaque(true);
        }
        if (activeButton != null) {
            activeButton.setBackground(new Color(25, 50, 90));
            activeButton.setForeground(Color.WHITE);
            activeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(61, 139, 255)),
                BorderFactory.createEmptyBorder(0, 11, 0, 0)
            ));
        }

        // Refresh view data
        switch (screenName) {
            case "DASHBOARD":
                dashboardScreen.refresh();
                break;
            case "MARKETS":
                marketsScreen.refresh();
                break;
            case "PORTFOLIO":
                portfolioScreen.refresh();
                break;
            case "TRANSACTIONS":
                transactionsScreen.refresh();
                break;
            case "WATCHLIST":
                watchlistScreen.refresh();
                break;
            case "PROFILE":
                profileScreen.refresh();
                break;
            case "ADMIN":
                adminScreen.refresh();
                break;
            case "SETTINGS":
                settingsScreen.refresh();
                break;
            case "LEADERBOARD":
                leaderboardScreen.refresh();
                break;
            case "DIVERSIFICATION":
                diversificationScreen.refresh();
                break;
            case "COMPARISON":
                comparisonScreen.refresh();
                break;
            case "TECHNICAL":
                technicalIndicatorsScreen.refresh();
                break;
            case "GOALS":
                goalsScreen.refresh();
                break;
            case "SCREENER":
                screenerScreen.refresh();
                break;
            case "DIVIDEND":
                dividendScreen.refresh();
                break;
            case "BENCHMARK":
                benchmarkScreen.refresh();
                break;
            case "RISK":
                riskMetricsScreen.refresh();
                break;
        }

        cardLayout.show(contentCardPanel, screenName);
    }

    private String getIconFromButton(JButton button) {
        String text = button.getText();
        int spaceIndex = text.indexOf("  ");
        if (spaceIndex > 0) {
            return text.substring(0, spaceIndex);
        }
        return "";
    }

    private String getTextFromButton(JButton button) {
        String text = button.getText();
        int spaceIndex = text.indexOf("  ");
        if (spaceIndex > 0) {
            return text.substring(spaceIndex + 2).trim();
        }
        return text;
    }

    public void refreshAllScreens() {
        // Update main frame background
        getContentPane().setBackground(Theme.getBackground());
        sidebarPanel.setBackground(new Color(2, 11, 29));

        dashboardScreen.refresh();
        marketsScreen.refresh();
        portfolioScreen.refresh();
        transactionsScreen.refresh();
        watchlistScreen.refresh();
        profileScreen.refresh();
        adminScreen.refresh();
        settingsScreen.refresh();
        leaderboardScreen.refresh();
        diversificationScreen.refresh();
        comparisonScreen.refresh();
        technicalIndicatorsScreen.refresh();
        goalsScreen.refresh();
        screenerScreen.refresh();
        dividendScreen.refresh();
        benchmarkScreen.refresh();
        riskMetricsScreen.refresh();
    }

    private void onLoginSuccess() {
        // Update user profile block
        String username = authManager.getCurrentUser().getUsername();
        lblUserProfile.setText(username.toUpperCase());

        // Register stock update listeners
        stockManager.addListener(dashboardScreen);
        stockManager.addListener(marketsScreen);
        stockManager.addListener(portfolioScreen);
        stockManager.addListener(watchlistScreen);

        // Show sidebar
        sidebarPanel.setVisible(true);
        revalidate();
        repaint();

        // Default screen: Dashboard
        switchScreen("DASHBOARD", navButtons.get(0));
    }

    private ImageIcon createLogoutIcon() {
        int size = 18;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        Color iconColor = new Color(255, 77, 115);
        g2d.setColor(iconColor);
        g2d.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Scale coordinates to fit 18x18
        double scale = size / 24.0;
        
        // Draw bracket (left side) - more rounded
        int bx = (int)(4 * scale);
        int by = (int)(4 * scale);
        int bw = (int)(7 * scale);
        int bh = (int)(16 * scale);
        g2d.drawRoundRect(bx, by, bw, bh, 3, 3);
        
        // Draw arrow pointing right
        // Arrow line
        g2d.drawLine(
            (int)(13 * scale), 
            (int)(12 * scale), 
            (int)(21 * scale), 
            (int)(12 * scale)
        );
        
        // Arrow head
        int[] xPoints = {
            (int)(21 * scale),
            (int)(16 * scale),
            (int)(16 * scale)
        };
        int[] yPoints = {
            (int)(12 * scale),
            (int)(8 * scale),
            (int)(16 * scale)
        };
        g2d.drawPolyline(xPoints, yPoints, 3);
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of your session?",
            "Confirm Log Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Stop stock simulation
            stockManager.stopSimulation();

            // Remove stock update listeners
            stockManager.removeListener(dashboardScreen);
            stockManager.removeListener(marketsScreen);
            stockManager.removeListener(portfolioScreen);
            stockManager.removeListener(watchlistScreen);

            // Clear credentials
            authManager.logout();
            loginScreen.reset();

            // Hide sidebar & navigate
            sidebarPanel.setVisible(false);
            cardLayout.show(contentCardPanel, "LOGIN");
            revalidate();
            repaint();
        }
    }
}
