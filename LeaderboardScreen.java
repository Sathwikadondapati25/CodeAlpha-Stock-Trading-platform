import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class LeaderboardScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable leaderboardTable;
    private DefaultTableModel leaderboardModel;

    public LeaderboardScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
        this.storageManager = storageManager;
        this.stockManager = stockManager;
        this.authManager = authManager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.getBackground());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Leaderboard");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- LEADERBOARD TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Rank", "Username", "Portfolio Value", "Total Trades", "Profit/Loss"};
        leaderboardModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        leaderboardTable = new JTable(leaderboardModel);
        styleTable(leaderboardTable);

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.getViewport().setBackground(Theme.getCardBackground());
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.getCardBackground());
        table.setForeground(Theme.getTextPrimary());
        table.setGridColor(Theme.getBorderColor());
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(Theme.getBackground());
        table.getTableHeader().setForeground(Theme.getTextSecondary());
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.getBorderColor()));
        table.setSelectionBackground(Theme.getBorderColor());
        table.setSelectionForeground(Theme.getTextPrimary());
        table.setShowVerticalLines(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i == 2 || i == 3 || i == 4) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    public void refresh() {
        // Update theme colors
        setBackground(Theme.getBackground());

        leaderboardModel.setRowCount(0);

        // Get all users
        Map<String, String> users = storageManager.loadUsers();
        List<UserRanking> rankings = new ArrayList<>();

        for (String username : users.keySet()) {
            double balance = storageManager.loadUserBalance(username);
            List<Holding> holdings = storageManager.loadPortfolio(username);
            List<Transaction> transactions = storageManager.loadTransactions(username);

            double holdingsValue = 0.0;
            double investedValue = 0.0;

            for (Holding h : holdings) {
                Stock s = stockManager.getStock(h.getSymbol());
                if (s != null) {
                    holdingsValue += h.getCurrentValue(s.getCurrentPrice());
                    investedValue += h.getInvestedValue();
                }
            }

            double totalPortfolioValue = balance + holdingsValue;
            double profitLoss = holdingsValue - investedValue;
            int totalTrades = transactions.size();

            rankings.add(new UserRanking(username, totalPortfolioValue, totalTrades, profitLoss));
        }

        // Sort by portfolio value descending
        rankings.sort((a, b) -> Double.compare(b.portfolioValue, a.portfolioValue));

        // Add to table
        int rank = 1;
        for (UserRanking ranking : rankings) {
            String plStr = String.format("%s₹%.2f", ranking.profitLoss >= 0 ? "+" : "", ranking.profitLoss);
            leaderboardModel.addRow(new Object[]{
                rank,
                ranking.username.toUpperCase(),
                String.format("₹%,.2f", ranking.portfolioValue),
                ranking.totalTrades,
                plStr
            });
            rank++;
        }

        // Highlight current user
        String currentUser = authManager.getCurrentUser().getUsername();
        for (int i = 0; i < leaderboardModel.getRowCount(); i++) {
            String username = (String) leaderboardModel.getValueAt(i, 1);
            if (username.equalsIgnoreCase(currentUser)) {
                leaderboardTable.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private static class UserRanking {
        String username;
        double portfolioValue;
        int totalTrades;
        double profitLoss;

        UserRanking(String username, double portfolioValue, int totalTrades, double profitLoss) {
            this.username = username;
            this.portfolioValue = portfolioValue;
            this.totalTrades = totalTrades;
            this.profitLoss = profitLoss;
        }
    }
}
