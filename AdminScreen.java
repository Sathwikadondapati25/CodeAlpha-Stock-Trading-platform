import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JLabel lblTotalUsers;
    private JLabel lblTotalTrades;
    private JLabel lblTotalPortfolioValue;
    private JLabel lblMostTradedStock;

    private JTable usersTable;
    private DefaultTableModel usersTableModel;

    public AdminScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
        this.storageManager = storageManager;
        this.stockManager = stockManager;
        this.authManager = authManager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- STATS CARDS ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCard("Total Users", lblTotalUsers = new JLabel("0")));
        statsPanel.add(createStatCard("Total Trades", lblTotalTrades = new JLabel("0")));
        statsPanel.add(createStatCard("Total Portfolio Value", lblTotalPortfolioValue = new JLabel("₹0")));
        statsPanel.add(createStatCard("Most Traded Stock", lblMostTradedStock = new JLabel("N/A")));

        // --- USERS TABLE ---
        JPanel usersPanel = new JPanel(new BorderLayout(8, 8));
        usersPanel.setBackground(Theme.CARD_BG);
        usersPanel.setBorder(Theme.createCardBorder());

        JLabel usersTitle = new JLabel("User Management");
        usersTitle.setFont(Theme.FONT_SUBHEADER);
        usersTitle.setForeground(Theme.TEXT_PRIMARY);
        usersPanel.add(usersTitle, BorderLayout.NORTH);

        String[] userCols = {"Username", "Balance", "Holdings Count", "Trades Count"};
        usersTableModel = new DefaultTableModel(userCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersTableModel);
        styleTable(usersTable);
        JScrollPane usersScroll = new JScrollPane(usersTable);
        usersScroll.getViewport().setBackground(Theme.CARD_BG);
        usersScroll.setBorder(null);
        usersPanel.add(usersScroll, BorderLayout.CENTER);

        // --- MAIN CONTENT ---
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setOpaque(false);
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(usersPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(Theme.createCardBorder());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(Theme.FONT_SUBHEADER);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER_COLOR);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(Theme.BG_DARK);
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));
        table.setSelectionBackground(Theme.BORDER_COLOR);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public void refresh() {
        // Update theme colors
        setBackground(Theme.getBackground());

        // Calculate total users
        Map<String, String> users = storageManager.loadUsers();
        int totalUsers = users.size();
        lblTotalUsers.setText(String.valueOf(totalUsers));

        // Calculate total trades across all users
        int totalTrades = 0;
        double totalPortfolioValue = 0.0;
        Map<String, Integer> stockTradeCount = new HashMap<>();

        for (String username : users.keySet()) {
            List<Transaction> transactions = storageManager.loadTransactions(username);
            totalTrades += transactions.size();

            for (Transaction t : transactions) {
                stockTradeCount.put(t.getSymbol(), stockTradeCount.getOrDefault(t.getSymbol(), 0) + 1);
            }

            List<Holding> holdings = storageManager.loadPortfolio(username);
            double userBalance = storageManager.loadUserBalance(username);
            double holdingsValue = 0.0;
            for (Holding h : holdings) {
                Stock s = stockManager.getStock(h.getSymbol());
                if (s != null) {
                    holdingsValue += h.getCurrentValue(s.getCurrentPrice());
                }
            }
            totalPortfolioValue += userBalance + holdingsValue;
        }

        lblTotalTrades.setText(String.valueOf(totalTrades));
        lblTotalPortfolioValue.setText(String.format("₹%,.0f", totalPortfolioValue));

        // Find most traded stock
        String mostTraded = "N/A";
        int maxTrades = 0;
        for (Map.Entry<String, Integer> entry : stockTradeCount.entrySet()) {
            if (entry.getValue() > maxTrades) {
                maxTrades = entry.getValue();
                mostTraded = entry.getKey();
            }
        }
        lblMostTradedStock.setText(mostTraded);

        // Populate users table
        usersTableModel.setRowCount(0);
        for (String username : users.keySet()) {
            double balance = storageManager.loadUserBalance(username);
            List<Holding> holdings = storageManager.loadPortfolio(username);
            List<Transaction> transactions = storageManager.loadTransactions(username);

            usersTableModel.addRow(new Object[]{
                username.toUpperCase(),
                String.format("₹%.2f", balance),
                holdings.size(),
                transactions.size()
            });
        }
    }
}
