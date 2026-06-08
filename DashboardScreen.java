import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DashboardScreen extends JPanel implements StockManager.StockUpdateListener {
    private final AuthManager authManager;
    private final StockManager stockManager;
    private final StorageManager storageManager;

    private JLabel valTotal;
    private JLabel valCash;
    private JLabel valHoldings;
    private JLabel valProfit;
    private JLabel valTotalTrades;
    private JLabel valWatchlistCount;
    private JLabel valBestPerformer;
    private JLabel valMarketStatus;

    private JTable transTable;
    private DefaultTableModel transModel;

    private JTable watchlistTable;
    private DefaultTableModel watchlistModel;

    private JTable newsTable;
    private DefaultTableModel newsModel;

    public DashboardScreen(AuthManager authManager, StockManager stockManager, StorageManager storageManager) {
        this.authManager = authManager;
        this.stockManager = stockManager;
        this.storageManager = storageManager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- KPI CARDS PANEL ---
        JPanel cardsPanel = new JPanel(new GridLayout(2, 4, 16, 16));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createKPICard("Total Portfolio Value", valTotal = new JLabel("₹0.00")));
        cardsPanel.add(createKPICard("Cash Balance", valCash = new JLabel("₹0.00")));
        cardsPanel.add(createKPICard("Holdings Value", valHoldings = new JLabel("₹0.00")));
        cardsPanel.add(createKPICard("Total Profit/Loss", valProfit = new JLabel("₹0.00 (0.00%)")));
        cardsPanel.add(createKPICard("Total Trades", valTotalTrades = new JLabel("0")));
        cardsPanel.add(createKPICard("Watchlist Count", valWatchlistCount = new JLabel("0")));
        cardsPanel.add(createKPICard("Best Performer", valBestPerformer = new JLabel("N/A")));
        cardsPanel.add(createKPICard("Market Status", valMarketStatus = new JLabel("Open")));

        // --- BOTTOM CONTENT SPLIT ---
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        splitPanel.setOpaque(false);

        // Recent Transactions Section
        JPanel transPanel = new JPanel(new BorderLayout(8, 8));
        transPanel.setBackground(Theme.CARD_BG);
        transPanel.setBorder(Theme.createCardBorder());

        JLabel transTitle = new JLabel("Recent Transactions");
        transTitle.setFont(Theme.FONT_SUBHEADER);
        transTitle.setForeground(Theme.TEXT_PRIMARY);
        transPanel.add(transTitle, BorderLayout.NORTH);

        String[] transCols = {"Time", "Type", "Symbol", "Qty", "Price", "Total"};
        transModel = new DefaultTableModel(transCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        transTable = new JTable(transModel);
        styleTable(transTable);
        transTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        JScrollPane transScroll = new JScrollPane(transTable);
        transScroll.getViewport().setBackground(Theme.CARD_BG);
        transScroll.setBorder(null);
        transPanel.add(transScroll, BorderLayout.CENTER);

        // Watchlist Section
        JPanel watchlistPanel = new JPanel(new BorderLayout(8, 8));
        watchlistPanel.setBackground(Theme.CARD_BG);
        watchlistPanel.setBorder(Theme.createCardBorder());

        JLabel watchlistTitle = new JLabel("Live Market Overview");
        watchlistTitle.setFont(Theme.FONT_SUBHEADER);
        watchlistTitle.setForeground(Theme.TEXT_PRIMARY);
        watchlistPanel.add(watchlistTitle, BorderLayout.NORTH);

        String[] watchCols = {"Stock Symbol", "Current Price", "Day Change"};
        watchlistModel = new DefaultTableModel(watchCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        watchlistTable = new JTable(watchlistModel);
        styleTable(watchlistTable);
        
        // Custom renderer for Day Change color
        watchlistTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean sel, boolean focus, int r, int c) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, val, sel, focus, r, c);
                String strVal = (String) val;
                if (strVal != null) {
                    if (strVal.startsWith("+")) {
                        label.setForeground(Theme.GAIN);
                    } else if (strVal.startsWith("-")) {
                        label.setForeground(Theme.LOSS);
                    } else {
                        label.setForeground(Theme.TEXT_PRIMARY);
                    }
                }
                return label;
            }
        });

        JScrollPane watchScroll = new JScrollPane(watchlistTable);
        watchScroll.getViewport().setBackground(Theme.CARD_BG);
        watchScroll.setBorder(null);
        watchlistPanel.add(watchScroll, BorderLayout.CENTER);

        splitPanel.add(transPanel);
        splitPanel.add(watchlistPanel);

        // Assemble Main Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(splitPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Market News Section (Bottom)
        JPanel newsPanel = new JPanel(new BorderLayout(8, 8));
        newsPanel.setBackground(Theme.CARD_BG);
        newsPanel.setBorder(Theme.createCardBorder());
        newsPanel.setPreferredSize(new Dimension(0, 150));

        JLabel newsTitle = new JLabel("Market News");
        newsTitle.setFont(Theme.FONT_SUBHEADER);
        newsTitle.setForeground(Theme.TEXT_PRIMARY);
        newsPanel.add(newsTitle, BorderLayout.NORTH);

        String[] newsCols = {"Time", "Headline"};
        newsModel = new DefaultTableModel(newsCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        newsTable = new JTable(newsModel);
        styleTable(newsTable);
        newsTable.setRowHeight(25);
        newsTable.setPreferredScrollableViewportSize(new Dimension(-1, 5 * 25));

        JScrollPane newsScroll = new JScrollPane(newsTable);
        newsScroll.getViewport().setBackground(Theme.CARD_BG);
        newsScroll.setBorder(null);
        newsPanel.add(newsScroll, BorderLayout.CENTER);

        add(newsPanel, BorderLayout.SOUTH);
    }

    private JPanel createKPICard(String title, JLabel valueLabel) {
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
        
        // Center text cell rendering
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals("Stock Symbol") || table.getColumnName(i).equals("Time")) {
                DefaultTableCellRenderer left = new DefaultTableCellRenderer();
                left.setHorizontalAlignment(JLabel.LEFT);
                table.getColumnModel().getColumn(i).setCellRenderer(left);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    public synchronized void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        User user = authManager.getCurrentUser();

        // Refresh Cash Balance (always matches current user instance balance)
        valCash.setText(String.format("₹%.2f", user.getBalance()));

        // Load recent transactions (last 5)
        List<Transaction> transactions = storageManager.loadTransactions(user.getUsername());
        transModel.setRowCount(0);
        int limit = Math.min(transactions.size(), 5);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (int i = 0; i < limit; i++) {
            Transaction t = transactions.get(i);
            transModel.addRow(new Object[]{
                t.getTimestamp().format(timeFormatter),
                t.getType().name(),
                t.getSymbol(),
                t.getQuantity(),
                String.format("₹%.2f", t.getPrice()),
                String.format("₹%.2f", t.getTotal())
            });
        }

        // Load market news (simulated)
        loadMarketNews();

        // Trigger updates calculations manually once
        onStocksUpdated(stockManager.getAllStocks());
    }

    private void loadMarketNews() {
        newsModel.setRowCount(0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Simulated market news headlines
        String[] headlines = {
            "TCS announces quarterly results, stock up 2%",
            "Infosys wins major contract with European client",
            "Reliance Industries expands renewable energy portfolio",
            "HDFC Bank reports strong growth in retail lending",
            "Market sentiment positive ahead of RBI policy meeting",
            "IT sector shows resilience amid global uncertainty",
            "Auto stocks rally on strong sales numbers",
            "Pharma sector gains on regulatory approval news",
            "Wipro launches AI-powered enterprise solutions",
            "Reliance shares gain after investment announcement",
            "Infosys expands cloud services partnership",
            "TCS opens new technology innovation center",
            "HDFC Bank digital transactions hit record levels",
            "Wipro secures multi-year IT transformation deal",
            "Banking sector shows strong quarterly performance"
        };

        java.util.Random random = new java.util.Random();
        int count = Math.min(headlines.length, 10);

        for (int i = 0; i < count; i++) {
            int minutesAgo = random.nextInt(60);
            String time = LocalDateTime.now().minusMinutes(minutesAgo).format(timeFormatter);
            newsModel.addRow(new Object[]{time, headlines[i]});
        }
    }

    @Override
    public void onStocksUpdated(Collection<Stock> updatedStocks) {
        if (!authManager.isLoggedIn()) return;
        User user = authManager.getCurrentUser();

        // Calculate Portfolio values
        List<Holding> holdings = storageManager.loadPortfolio(user.getUsername());
        double cash = user.getBalance();
        double holdingsVal = 0.0;
        double investedVal = 0.0;

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                holdingsVal += h.getCurrentValue(s.getCurrentPrice());
                investedVal += h.getInvestedValue();
            }
        }

        double totalVal = cash + holdingsVal;
        double profitLoss = holdingsVal - investedVal;
        double profitLossPct = investedVal == 0 ? 0.0 : (profitLoss / investedVal) * 100.0;

        // Calculate best performer
        String bestPerformer = "N/A";
        double bestPerfPct = Double.MIN_VALUE;
        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                double plPct = (h.getCurrentValue(s.getCurrentPrice()) - h.getInvestedValue()) / h.getInvestedValue() * 100.0;
                if (plPct > bestPerfPct) {
                    bestPerfPct = plPct;
                    bestPerformer = h.getSymbol();
                }
            }
        }

        // Get total trades count
        List<Transaction> transactions = storageManager.loadTransactions(user.getUsername());
        int totalTrades = transactions.size();

        // Get watchlist count
        List<String> watchlist = storageManager.loadWatchlist(user.getUsername());
        int watchlistCount = watchlist.size();

        // Market status (simulated - based on time)
        int hour = java.time.LocalDateTime.now().getHour();
        String marketStatus = (hour >= 9 && hour < 16) ? "Open" : "Closed";

        final double finalTotalVal = totalVal;
        final double finalHoldingsVal = holdingsVal;
        final double finalProfitLoss = profitLoss;
        final double finalProfitLossPct = profitLossPct;
        final String finalBestPerformer = bestPerformer;
        final double finalBestPerfPct = bestPerfPct;
        final int finalTotalTrades = totalTrades;
        final int finalWatchlistCount = watchlistCount;
        final String finalMarketStatus = marketStatus;

        // Run UI updates in Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            valTotal.setText(String.format("₹%.2f", finalTotalVal));
            valHoldings.setText(String.format("₹%.2f", finalHoldingsVal));

            // Style Profit/Loss card
            if (finalProfitLoss > 0) {
                valProfit.setText(String.format("+₹%.2f (+%.2f%%)", finalProfitLoss, finalProfitLossPct));
                valProfit.setForeground(Theme.GAIN);
            } else if (finalProfitLoss < 0) {
                valProfit.setText(String.format("-₹%.2f (%.2f%%)", Math.abs(finalProfitLoss), finalProfitLossPct));
                valProfit.setForeground(Theme.LOSS);
            } else {
                valProfit.setText("₹0.00 (0.00%)");
                valProfit.setForeground(Theme.TEXT_PRIMARY);
            }

            // Update new cards
            valTotalTrades.setText(String.valueOf(finalTotalTrades));
            valWatchlistCount.setText(String.valueOf(finalWatchlistCount));
            valMarketStatus.setText(finalMarketStatus);
            valMarketStatus.setForeground(finalMarketStatus.equals("Open") ? Theme.GAIN : Theme.LOSS);

            if (finalBestPerformer.equals("N/A")) {
                valBestPerformer.setText("N/A");
                valBestPerformer.setForeground(Theme.TEXT_PRIMARY);
            } else {
                valBestPerformer.setText(String.format("%s (%+.2f%%)", finalBestPerformer, finalBestPerfPct));
                valBestPerformer.setForeground(Theme.GAIN);
            }

            // Update watch list table
            watchlistModel.setRowCount(0);
            for (Stock s : updatedStocks) {
                double change = s.getChangePercentage();
                String changeStr = String.format("%s%.2f%%", change >= 0 ? "+" : "", change);
                watchlistModel.addRow(new Object[]{
                    s.getSymbol(),
                    String.format("₹%.2f", s.getCurrentPrice()),
                    changeStr
                });
            }
        });
    }
}
