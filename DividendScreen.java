import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DividendScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable dividendTable;
    private DefaultTableModel dividendModel;

    public DividendScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Dividend Tracking");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- DIVIDEND TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Stock", "Shares", "Dividend/Share", "Yield", "Annual Income", "Payout Frequency"};
        dividendModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        dividendTable = new JTable(dividendModel);
        styleTable(dividendTable);

        JScrollPane scrollPane = new JScrollPane(dividendTable);
        scrollPane.getViewport().setBackground(Theme.getCardBackground());
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createKPICard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Theme.getCardBackground());
        card.setOpaque(true);
        card.setBorder(BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true));
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.FONT_HEADER);
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.getCardBackground());
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.getBorderColor());
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(Theme.getBackground());
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.getBorderColor()));
        table.setSelectionBackground(Theme.getBorderColor());
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
    }

    public void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        String username = authManager.getCurrentUser().getUsername();
        List<Holding> holdings = storageManager.loadPortfolio(username);

        dividendModel.setRowCount(0);

        double totalPortfolioValue = 0.0;
        double totalAnnualDividend = 0.0;

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                double currentValue = h.getCurrentValue(s.getCurrentPrice());
                totalPortfolioValue += currentValue;

                double dividendPerShare = getDividendPerShare(h.getSymbol());
                double dividendYield = (dividendPerShare / s.getCurrentPrice()) * 100;
                double annualIncome = dividendPerShare * h.getQuantity();
                String payoutFrequency = getPayoutFrequency(h.getSymbol());

                totalAnnualDividend += annualIncome;

                dividendModel.addRow(new Object[]{
                    h.getSymbol(),
                    h.getQuantity(),
                    String.format("₹%.2f", dividendPerShare),
                    String.format("%.2f%%", dividendYield),
                    String.format("₹%.2f", annualIncome),
                    payoutFrequency
                });
            }
        }

        double totalYield = totalPortfolioValue > 0 ? (totalAnnualDividend / totalPortfolioValue) * 100 : 0;
        // KPI values are displayed in the table
    }

    private double getDividendPerShare(String symbol) {
        // Simulated dividend data
        Map<String, Double> dividends = new HashMap<>();
        dividends.put("HDFC", 45.0);
        dividends.put("ICICI", 32.0);
        dividends.put("RELIANCE", 28.0);
        dividends.put("ITC", 12.0);
        dividends.put("HINDUNILVR", 38.0);
        dividends.put("TCS", 24.0);
        dividends.put("INFY", 20.0);
        dividends.put("SUNPHARMA", 8.0);
        dividends.put("MARUTI", 95.0);
        dividends.put("TATAMOTORS", 4.0);

        return dividends.getOrDefault(symbol, 0.0);
    }

    private String getPayoutFrequency(String symbol) {
        Map<String, String> frequencies = new HashMap<>();
        frequencies.put("HDFC", "Quarterly");
        frequencies.put("ICICI", "Quarterly");
        frequencies.put("RELIANCE", "Annual");
        frequencies.put("ITC", "Quarterly");
        frequencies.put("HINDUNILVR", "Quarterly");
        frequencies.put("TCS", "Quarterly");
        frequencies.put("INFY", "Quarterly");
        frequencies.put("SUNPHARMA", "Annual");
        frequencies.put("MARUTI", "Annual");
        frequencies.put("TATAMOTORS", "Annual");

        return frequencies.getOrDefault(symbol, "Annual");
    }
}
