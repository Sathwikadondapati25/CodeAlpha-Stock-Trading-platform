import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BenchmarkScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable benchmarkTable;
    private DefaultTableModel benchmarkModel;

    public BenchmarkScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Portfolio Performance Benchmarking");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- BENCHMARK TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Period", "Portfolio Return", "Benchmark Return", "Alpha", "Beta", "Sharpe Ratio"};
        benchmarkModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        benchmarkTable = new JTable(benchmarkModel);
        styleTable(benchmarkTable);

        JScrollPane scrollPane = new JScrollPane(benchmarkTable);
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

        benchmarkModel.setRowCount(0);

        double portfolioValue = 0.0;
        double initialInvestment = 0.0;

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                portfolioValue += h.getCurrentValue(s.getCurrentPrice());
                initialInvestment += h.getInvestedValue();
            }
        }

        double portfolioReturn = initialInvestment > 0 ? ((portfolioValue - initialInvestment) / initialInvestment) * 100 : 0;

        // Simulated benchmark data (NIFTY 50)
        double benchmark1M = 2.5;
        double benchmark3M = 7.2;
        double benchmark6M = 12.8;
        double benchmark1Y = 18.5;

        // Portfolio returns (simulated based on portfolio return)
        double portfolio1M = portfolioReturn * 0.1;
        double portfolio3M = portfolioReturn * 0.3;
        double portfolio6M = portfolioReturn * 0.6;
        double portfolio1Y = portfolioReturn;

        // Add benchmark data
        addBenchmarkRow("1 Month", portfolio1M, benchmark1M);
        addBenchmarkRow("3 Months", portfolio3M, benchmark3M);
        addBenchmarkRow("6 Months", portfolio6M, benchmark6M);
        addBenchmarkRow("1 Year", portfolio1Y, benchmark1Y);

        // KPI values are displayed in the table
    }

    private void addBenchmarkRow(String period, double portfolioReturn, double benchmarkReturn) {
        double alpha = portfolioReturn - benchmarkReturn;
        double beta = 1.0 + (Math.random() * 0.4 - 0.2); // Simulated beta
        double sharpe = (portfolioReturn - 5.0) / (Math.abs(portfolioReturn) * 0.15); // Simulated Sharpe ratio

        benchmarkModel.addRow(new Object[]{
            period,
            String.format("%.2f%%", portfolioReturn),
            String.format("%.2f%%", benchmarkReturn),
            String.format("%s%.2f%%", alpha >= 0 ? "+" : "", alpha),
            String.format("%.2f", beta),
            String.format("%.2f", sharpe)
        });
    }
}
