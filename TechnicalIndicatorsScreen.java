import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TechnicalIndicatorsScreen extends JPanel {
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JComboBox<String> stockSelector;
    private JTable indicatorsTable;
    private DefaultTableModel indicatorsModel;

    public TechnicalIndicatorsScreen(StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Technical Indicators");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Stock Selector
        List<Stock> stocks = new ArrayList<>(stockManager.getAllStocks());
        String[] stockNames = new String[stocks.size()];
        for (int i = 0; i < stocks.size(); i++) {
            stockNames[i] = stocks.get(i).getSymbol() + " - " + stocks.get(i).getName();
        }

        stockSelector = new JComboBox<>(stockNames);
        stockSelector.setFont(Theme.FONT_BODY);
        stockSelector.setBackground(Theme.getCardBackground());
        stockSelector.setForeground(Theme.getTextPrimary());
        stockSelector.addActionListener(e -> updateIndicators());
        headerPanel.add(stockSelector, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- INDICATORS TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Indicator", "Value", "Signal", "Description"};
        indicatorsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        indicatorsTable = new JTable(indicatorsModel);
        styleTable(indicatorsTable);

        JScrollPane scrollPane = new JScrollPane(indicatorsTable);
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
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.getBorderColor()));
        table.setSelectionBackground(Theme.getBorderColor());
        table.setSelectionForeground(Theme.getTextPrimary());
        table.setShowVerticalLines(false);
    }

    public void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        updateIndicators();
    }

    private void updateIndicators() {
        indicatorsModel.setRowCount(0);

        String selected = (String) stockSelector.getSelectedItem();
        if (selected == null) return;

        String symbol = selected.split(" - ")[0];
        Stock stock = stockManager.getStock(symbol);

        if (stock == null) return;

        List<Double> priceHistory = stock.getPriceHistory();

        // Calculate RSI (Relative Strength Index)
        double rsi = calculateRSI(priceHistory, 14);
        String rsiSignal = getRSISignal(rsi);

        // Calculate MACD
        double[] macd = calculateMACD(priceHistory);
        String macdSignal = getMACDSignal(macd[0], macd[1]);

        // Calculate Moving Averages
        double sma20 = calculateSMA(priceHistory, 20);
        double sma50 = calculateSMA(priceHistory, 50);
        double ema12 = calculateEMA(priceHistory, 12);
        double ema26 = calculateEMA(priceHistory, 26);

        String maSignal = getMASignal(stock.getCurrentPrice(), sma20, sma50);

        // Add indicators to table
        addIndicatorRow("RSI (14)", String.format("%.2f", rsi), rsiSignal, getRSIDescription(rsi));
        addIndicatorRow("MACD", String.format("%.2f", macd[0]), macdSignal, "Moving Average Convergence Divergence");
        addIndicatorRow("MACD Signal", String.format("%.2f", macd[1]), "-", "MACD Signal Line");
        addIndicatorRow("SMA (20)", String.format("%.2f", sma20), "-", "Simple Moving Average (20 periods)");
        addIndicatorRow("SMA (50)", String.format("%.2f", sma50), "-", "Simple Moving Average (50 periods)");
        addIndicatorRow("EMA (12)", String.format("%.2f", ema12), "-", "Exponential Moving Average (12 periods)");
        addIndicatorRow("EMA (26)", String.format("%.2f", ema26), "-", "Exponential Moving Average (26 periods)");
        addIndicatorRow("MA Trend", maSignal, maSignal, "Moving Average Trend Signal");
    }

    private void addIndicatorRow(String indicator, String value, String signal, String description) {
        indicatorsModel.addRow(new Object[]{indicator, value, signal, description});
    }

    private double calculateRSI(List<Double> prices, int period) {
        if (prices.size() < period + 1) return 50.0;

        double gains = 0.0;
        double losses = 0.0;

        for (int i = prices.size() - period; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gains += change;
            } else {
                losses -= change;
            }
        }

        double avgGain = gains / period;
        double avgLoss = losses / period;

        if (avgLoss == 0) return 100.0;

        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private String getRSISignal(double rsi) {
        if (rsi >= 70) return "Overbought";
        if (rsi <= 30) return "Oversold";
        if (rsi >= 55) return "Bullish";
        if (rsi <= 45) return "Bearish";
        return "Neutral";
    }

    private String getRSIDescription(double rsi) {
        if (rsi >= 70) return "Price may be too high, potential reversal";
        if (rsi <= 30) return "Price may be too low, potential reversal";
        return "Normal trading range";
    }

    private double[] calculateMACD(List<Double> prices) {
        double ema12 = calculateEMA(prices, 12);
        double ema26 = calculateEMA(prices, 26);
        double macd = ema12 - ema26;
        double signal = macd * 0.8; // Simplified signal line
        return new double[]{macd, signal};
    }

    private String getMACDSignal(double macd, double signal) {
        if (macd > signal) return "Bullish";
        if (macd < signal) return "Bearish";
        return "Neutral";
    }

    private double calculateSMA(List<Double> prices, int period) {
        if (prices.size() < period) return prices.get(prices.size() - 1);

        double sum = 0.0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    private double calculateEMA(List<Double> prices, int period) {
        if (prices.size() < period) return prices.get(prices.size() - 1);

        double multiplier = 2.0 / (period + 1);
        double ema = prices.get(prices.size() - period);

        for (int i = prices.size() - period + 1; i < prices.size(); i++) {
            ema = (prices.get(i) - ema) * multiplier + ema;
        }

        return ema;
    }

    private String getMASignal(double currentPrice, double sma20, double sma50) {
        if (currentPrice > sma20 && sma20 > sma50) return "Strong Bullish";
        if (currentPrice > sma20) return "Bullish";
        if (currentPrice < sma20 && sma20 < sma50) return "Strong Bearish";
        if (currentPrice < sma20) return "Bearish";
        return "Neutral";
    }
}
