import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class RiskMetricsScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable riskTable;
    private DefaultTableModel riskModel;

    public RiskMetricsScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Risk Metrics Dashboard");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- RISK TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Stock", "Beta", "Volatility", "Risk Level", "Contribution to Portfolio Risk"};
        riskModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        riskTable = new JTable(riskModel);
        styleTable(riskTable);

        JScrollPane scrollPane = new JScrollPane(riskTable);
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

        riskModel.setRowCount(0);

        double portfolioBeta = 0.0;
        double portfolioVolatility = 0.0;
        double totalValue = 0.0;

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                double currentValue = h.getCurrentValue(s.getCurrentPrice());
                totalValue += currentValue;

                double beta = getStockBeta(h.getSymbol());
                double volatility = getStockVolatility(h.getSymbol());
                String riskLevel = getRiskLevel(volatility);
                double riskContribution = (currentValue / totalValue) * volatility;

                portfolioBeta += beta * (currentValue / totalValue);
                portfolioVolatility += volatility * (currentValue / totalValue);

                riskModel.addRow(new Object[]{
                    h.getSymbol(),
                    String.format("%.2f", beta),
                    String.format("%.2f%%", volatility),
                    riskLevel,
                    String.format("%.2f%%", riskContribution)
                });
            }
        }

        // Calculate Sharpe Ratio (simplified)
        double portfolioReturn = totalValue > 0 ? ((totalValue - getTotalInvested(holdings)) / getTotalInvested(holdings)) * 100 : 0;
        double riskFreeRate = 5.0; // Assumed risk-free rate
        double sharpeRatio = portfolioVolatility > 0 ? (portfolioReturn - riskFreeRate) / portfolioVolatility : 0;

        // Calculate Value at Risk (95% confidence)
        double var = totalValue * 0.05 * Math.sqrt(252); // Simplified VaR calculation

        // KPI values are displayed in the table
    }

    private double getStockBeta(String symbol) {
        // Simulated beta values
        Map<String, Double> betas = new HashMap<>();
        betas.put("HDFC", 1.15);
        betas.put("ICICI", 1.20);
        betas.put("RELIANCE", 0.95);
        betas.put("ITC", 0.65);
        betas.put("HINDUNILVR", 0.70);
        betas.put("TCS", 0.85);
        betas.put("INFY", 0.90);
        betas.put("SUNPHARMA", 0.75);
        betas.put("MARUTI", 1.10);
        betas.put("TATAMOTORS", 1.30);

        return betas.getOrDefault(symbol, 1.0);
    }

    private double getStockVolatility(String symbol) {
        // Simulated volatility (annualized)
        Map<String, Double> volatilities = new HashMap<>();
        volatilities.put("HDFC", 18.5);
        volatilities.put("ICICI", 22.3);
        volatilities.put("RELIANCE", 25.8);
        volatilities.put("ITC", 15.2);
        volatilities.put("HINDUNILVR", 14.8);
        volatilities.put("TCS", 19.7);
        volatilities.put("INFY", 21.5);
        volatilities.put("SUNPHARMA", 23.2);
        volatilities.put("MARUTI", 28.5);
        volatilities.put("TATAMOTORS", 32.1);

        return volatilities.getOrDefault(symbol, 20.0);
    }

    private String getRiskLevel(double volatility) {
        if (volatility < 15) return "Low";
        if (volatility < 25) return "Medium";
        return "High";
    }

    private double getTotalInvested(List<Holding> holdings) {
        double total = 0.0;
        for (Holding h : holdings) {
            total += h.getInvestedValue();
        }
        return total;
    }
}
