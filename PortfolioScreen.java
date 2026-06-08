import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PortfolioScreen extends JPanel implements StockManager.StockUpdateListener {
    private final AuthManager authManager;
    private final StockManager stockManager;
    private final TradingManager tradingManager;
    private final StorageManager storageManager;

    private JTable portfolioTable;
    private DefaultTableModel tableModel;
    private PortfolioChartPanel chartPanel;

    private JLabel lblTotalInvested;
    private JLabel lblCurrentValue;
    private JLabel lblTotalPL;
    private JLabel lblBestPerformer;
    private JLabel lblWorstPerformer;

    private JButton btnQuickSell;
    private JButton btnExportReport;
    private Holding selectedHolding;

    public PortfolioScreen(AuthManager authManager, StockManager stockManager, TradingManager tradingManager, StorageManager storageManager) {
        this.authManager = authManager;
        this.stockManager = stockManager;
        this.tradingManager = tradingManager;
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

        JLabel titleLabel = new JLabel("My Portfolio Holdings");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- TOP SUMMARY BAR ---
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryPanel.setOpaque(false);

        summaryPanel.add(createSummaryCard("Total Invested", lblTotalInvested = new JLabel("₹0.00")));
        summaryPanel.add(createSummaryCard("Current Asset Value", lblCurrentValue = new JLabel("₹0.00")));
        summaryPanel.add(createSummaryCard("Net Profit / Loss", lblTotalPL = new JLabel("₹0.00 (0.00%)")));

        // --- PERFORMANCE ANALYTICS BAR ---
        JPanel performancePanel = new JPanel(new GridLayout(1, 2, 16, 0));
        performancePanel.setOpaque(false);

        performancePanel.add(createSummaryCard("Best Performer", lblBestPerformer = new JLabel("N/A")));
        performancePanel.add(createSummaryCard("Worst Performer", lblWorstPerformer = new JLabel("N/A")));

        // --- PORTFOLIO CHART ---
        JPanel chartContainerPanel = new JPanel(new BorderLayout());
        chartContainerPanel.setBackground(Theme.CARD_BG);
        chartContainerPanel.setBorder(Theme.createCardBorder());
        chartContainerPanel.setPreferredSize(new Dimension(450, 300));

        JLabel chartTitle = new JLabel("Portfolio Performance Over Time");
        chartTitle.setFont(Theme.FONT_SUBHEADER);
        chartTitle.setForeground(Theme.TEXT_PRIMARY);
        chartContainerPanel.add(chartTitle, BorderLayout.NORTH);

        chartPanel = new PortfolioChartPanel();
        chartContainerPanel.add(chartPanel, BorderLayout.CENTER);

        // --- HOLDINGS TABLE ---
        String[] cols = {"Symbol", "Company Name", "Shares Owned", "Avg Buy Price", "Total Invested", "Current Price", "Current Value", "Profit / Loss"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        portfolioTable = new JTable(tableModel);
        styleTable(portfolioTable);

        portfolioTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = portfolioTable.getSelectedRow();
                if (row != -1 && authManager.isLoggedIn()) {
                    String symbol = (String) tableModel.getValueAt(row, 0);
                    List<Holding> holdings = storageManager.loadPortfolio(authManager.getCurrentUser().getUsername());
                    for (Holding h : holdings) {
                        if (h.getSymbol().equalsIgnoreCase(symbol)) {
                            selectedHolding = h;
                            btnQuickSell.setEnabled(true);
                            break;
                        }
                    }
                } else {
                    selectedHolding = null;
                    btnQuickSell.setEnabled(false);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));

        // --- BOTTOM ACTION PANEL ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        btnExportReport = new JButton("EXPORT PORTFOLIO REPORT");
        btnExportReport.setFont(Theme.FONT_BODY_BOLD);
        btnExportReport.setBackground(Theme.ACCENT);
        btnExportReport.setForeground(Color.WHITE);
        btnExportReport.setFocusPainted(false);
        btnExportReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportReport.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnExportReport.addActionListener(e -> exportPortfolioReport());
        actionPanel.add(btnExportReport);

        JButton btnExportExcel = new JButton("EXPORT TO EXCEL");
        btnExportExcel.setFont(Theme.FONT_BODY_BOLD);
        btnExportExcel.setBackground(Theme.GAIN);
        btnExportExcel.setForeground(Color.WHITE);
        btnExportExcel.setFocusPainted(false);
        btnExportExcel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportExcel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnExportExcel.addActionListener(e -> exportToExcel());
        actionPanel.add(btnExportExcel);

        btnQuickSell = new JButton("QUICK SELL ENTIRE POSITION");
        btnQuickSell.setFont(Theme.FONT_BODY_BOLD);
        btnQuickSell.setBackground(Theme.LOSS);
        btnQuickSell.setForeground(Color.WHITE);
        btnQuickSell.setFocusPainted(false);
        btnQuickSell.setEnabled(false);
        btnQuickSell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuickSell.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnQuickSell.addActionListener(e -> executeQuickSell());
        actionPanel.add(btnQuickSell);

        // Center layouts
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(summaryPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel(new BorderLayout(0, 16));
        middlePanel.setOpaque(false);
        middlePanel.add(performancePanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 16));
        bottomPanel.setOpaque(false);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel chartsPanel = new JPanel(new GridLayout(1, 1, 16, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.add(chartContainerPanel);

        middlePanel.add(chartsPanel, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.add(middlePanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        centerPanel.add(contentPanel, BorderLayout.CENTER);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSummaryCard(String title, JLabel valLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(Theme.createCardBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_SMALL);
        lblTitle.setForeground(Theme.TEXT_SECONDARY);
        card.add(lblTitle, BorderLayout.NORTH);

        valLabel.setFont(Theme.FONT_SUBHEADER);
        valLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(valLabel, BorderLayout.CENTER);

        return card;
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.CARD_BG);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER_COLOR);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(Theme.BG_DARK);
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));
        table.setSelectionBackground(Theme.BORDER_COLOR);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        
        // Right alignment for values
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer); // Shares
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Avg Buy
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Total Invested
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Current Price
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Current Val

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, r, c);
                l.setHorizontalAlignment(JLabel.RIGHT);
                String strVal = (String) val;
                if (strVal != null) {
                    if (strVal.startsWith("+")) {
                        l.setForeground(Theme.GAIN);
                    } else if (strVal.startsWith("-")) {
                        l.setForeground(Theme.LOSS);
                    } else {
                        l.setForeground(Theme.TEXT_PRIMARY);
                    }
                }
                return l;
            }
        });
    }

    private void executeQuickSell() {
        if (selectedHolding == null || !authManager.isLoggedIn()) return;
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to sell ALL %d shares of %s at the market price?", 
                          selectedHolding.getQuantity(), selectedHolding.getSymbol()),
            "Confirm Liquidate",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                Stock stock = stockManager.getStock(selectedHolding.getSymbol());
                tradingManager.sellStock(authManager.getCurrentUser(), stock, selectedHolding.getQuantity());
                
                selectedHolding = null;
                btnQuickSell.setEnabled(false);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Trade Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public synchronized void refresh() {
        // Update theme colors
        setBackground(Theme.getBackground());

        onStocksUpdated(stockManager.getAllStocks());
    }

    @Override
    public void onStocksUpdated(Collection<Stock> updatedStocks) {
        if (!authManager.isLoggedIn()) return;

        String username = authManager.getCurrentUser().getUsername();
        List<Holding> holdings = storageManager.loadPortfolio(username);

        // Capture previous selected row symbol
        String selectedSymbol = selectedHolding != null ? selectedHolding.getSymbol() : null;

        double totalInvested = 0.0;
        double currentTotalVal = 0.0;

        String bestPerformer = "N/A";
        double bestPerfPct = Double.MIN_VALUE;
        String worstPerformer = "N/A";
        double worstPerfPct = Double.MAX_VALUE;

        // Clean model rows
        SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                double invested = h.getInvestedValue();
                double curVal = h.getCurrentValue(s.getCurrentPrice());
                double pl = h.getProfitLoss(s.getCurrentPrice());
                double plPct = h.getAveragePrice() == 0 ? 0.0 : (pl / invested) * 100.0;

                totalInvested += invested;
                currentTotalVal += curVal;

                // Track best and worst performers
                if (plPct > bestPerfPct) {
                    bestPerfPct = plPct;
                    bestPerformer = h.getSymbol();
                }
                if (plPct < worstPerfPct) {
                    worstPerfPct = plPct;
                    worstPerformer = h.getSymbol();
                }

                String plStr = String.format("%s₹%.2f (%.2f%%)", pl >= 0 ? "+" : "", pl, plPct);

                final double finalInvested = invested;
                final double finalCurVal = curVal;
                final double finalPL = pl;

                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[]{
                        h.getSymbol(),
                        s.getName(),
                        h.getQuantity(),
                        String.format("₹%.2f", h.getAveragePrice()),
                        String.format("₹%.2f", finalInvested),
                        String.format("₹%.2f", s.getCurrentPrice()),
                        String.format("₹%.2f", finalCurVal),
                        plStr
                    });
                });
            }
        }

        double finalTotalInvested = totalInvested;
        double finalCurrentTotalVal = currentTotalVal;
        double totalPL = currentTotalVal - totalInvested;
        double totalPLPct = totalInvested == 0 ? 0.0 : (totalPL / totalInvested) * 100.0;

        final String finalBestPerformer = bestPerformer;
        final double finalBestPerfPct = bestPerfPct;
        final String finalWorstPerformer = worstPerformer;
        final double finalWorstPerfPct = worstPerfPct;

        SwingUtilities.invokeLater(() -> {
            lblTotalInvested.setText(String.format("₹%.2f", finalTotalInvested));
            lblCurrentValue.setText(String.format("₹%.2f", finalCurrentTotalVal));

            if (totalPL > 0) {
                lblTotalPL.setText(String.format("+₹%.2f (+%.2f%%)", totalPL, totalPLPct));
                lblTotalPL.setForeground(Theme.GAIN);
            } else if (totalPL < 0) {
                lblTotalPL.setText(String.format("-₹%.2f (%.2f%%)", Math.abs(totalPL), totalPLPct));
                lblTotalPL.setForeground(Theme.LOSS);
            } else {
                lblTotalPL.setText("₹0.00 (0.00%)");
                lblTotalPL.setForeground(Theme.TEXT_PRIMARY);
            }

            // Update best performer
            if (finalBestPerformer.equals("N/A")) {
                lblBestPerformer.setText("N/A");
                lblBestPerformer.setForeground(Theme.TEXT_PRIMARY);
            } else {
                lblBestPerformer.setText(String.format("%s (%+.2f%%)", finalBestPerformer, finalBestPerfPct));
                lblBestPerformer.setForeground(Theme.GAIN);
            }

            // Update worst performer
            if (finalWorstPerformer.equals("N/A")) {
                lblWorstPerformer.setText("N/A");
                lblWorstPerformer.setForeground(Theme.TEXT_PRIMARY);
            } else {
                lblWorstPerformer.setText(String.format("%s (%.2f%%)", finalWorstPerformer, finalWorstPerfPct));
                lblWorstPerformer.setForeground(Theme.LOSS);
            }

            // Update portfolio chart
            double totalPortfolioValue = finalCurrentTotalVal + authManager.getCurrentUser().getBalance();
            chartPanel.updatePortfolioValue(totalPortfolioValue);

            // Restore selection index
            if (selectedSymbol != null) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(selectedSymbol)) {
                        portfolioTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
        });
    }

    private void exportPortfolioReport() {
        if (!authManager.isLoggedIn()) return;

        String username = authManager.getCurrentUser().getUsername();
        List<Holding> holdings = storageManager.loadPortfolio(username);
        List<Transaction> transactions = storageManager.loadTransactions(username);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Portfolio Report");
        fileChooser.setSelectedFile(new File(username + "_portfolio_report.txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                // Header
                writer.write("========================================\n");
                writer.write("       STOCKPILOT PORTFOLIO REPORT      \n");
                writer.write("========================================\n\n");
                writer.write("Generated: " + now.format(formatter) + "\n");
                writer.write("User: " + username.toUpperCase() + "\n\n");

                // Portfolio Summary
                writer.write("----------------------------------------\n");
                writer.write("           PORTFOLIO SUMMARY            \n");
                writer.write("----------------------------------------\n");

                double cash = authManager.getCurrentUser().getBalance();
                double holdingsValue = 0.0;
                double investedValue = 0.0;

                for (Holding h : holdings) {
                    Stock s = stockManager.getStock(h.getSymbol());
                    if (s != null) {
                        holdingsValue += h.getCurrentValue(s.getCurrentPrice());
                        investedValue += h.getInvestedValue();
                    }
                }

                double totalValue = cash + holdingsValue;
                double profitLoss = holdingsValue - investedValue;
                double profitLossPct = investedValue == 0 ? 0 : (profitLoss / investedValue) * 100.0;

                writer.write(String.format("Cash Balance:          ₹%,.2f\n", cash));
                writer.write(String.format("Holdings Value:        ₹%,.2f\n", holdingsValue));
                writer.write(String.format("Total Portfolio Value: ₹%,.2f\n", totalValue));
                writer.write(String.format("Total Invested:        ₹%,.2f\n", investedValue));
                writer.write(String.format("Total Profit/Loss:     ₹%,.2f (%.2f%%)\n\n", profitLoss, profitLossPct));

                // Holdings Detail
                writer.write("----------------------------------------\n");
                writer.write("              HOLDINGS                 \n");
                writer.write("----------------------------------------\n");
                writer.write(String.format("%-10s %-25s %-10s %-12s %-12s %-12s\n",
                    "Symbol", "Company", "Shares", "Avg Price", "Cur Price", "P/L"));
                writer.write(String.format("%-10s %-25s %-10s %-12s %-12s %-12s\n",
                    "------", "-------", "------", "---------", "---------", "------"));

                for (Holding h : holdings) {
                    Stock s = stockManager.getStock(h.getSymbol());
                    if (s != null) {
                        double currentPrice = s.getCurrentPrice();
                        double pl = h.getCurrentValue(currentPrice) - h.getInvestedValue();
                        double plPct = (pl / h.getInvestedValue()) * 100.0;
                        writer.write(String.format("%-10s %-25s %-10d ₹%-11.2f ₹%-11.2f ₹%-11.2f\n",
                            h.getSymbol(), s.getName(), h.getQuantity(),
                            h.getAveragePrice(), currentPrice, pl));
                    }
                }
                writer.write("\n");

                // Transaction History (Last 20)
                writer.write("----------------------------------------\n");
                writer.write("          RECENT TRANSACTIONS           \n");
                writer.write("----------------------------------------\n");
                writer.write(String.format("%-20s %-8s %-8s %-8s %-12s %-12s\n",
                    "Date", "Type", "Symbol", "Qty", "Price", "Total"));
                writer.write(String.format("%-20s %-8s %-8s %-8s %-12s %-12s\n",
                    "----", "----", "------", "---", "-----", "-----"));

                int limit = Math.min(transactions.size(), 20);
                for (int i = 0; i < limit; i++) {
                    Transaction t = transactions.get(i);
                    writer.write(String.format("%-20s %-8s %-8s %-8d ₹%-11.2f ₹%-11.2f\n",
                        t.getTimestamp().format(formatter), t.getType().name(),
                        t.getSymbol(), t.getQuantity(), t.getPrice(), t.getTotal()));
                }
                writer.write("\n");

                // Footer
                writer.write("----------------------------------------\n");
                writer.write("         END OF REPORT                  \n");
                writer.write("========================================\n");

                JOptionPane.showMessageDialog(this,
                    "Portfolio report exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                NotificationManager.getInstance().showSuccess("Portfolio report exported successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to export report: " + ex.getMessage(),
                    "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToExcel() {
        if (!authManager.isLoggedIn()) return;

        String username = authManager.getCurrentUser().getUsername();
        List<Holding> holdings = storageManager.loadPortfolio(username);

        try {
            java.io.File file = new java.io.File("portfolio_export.csv");
            java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file));

            // Write CSV header
            writer.write("Symbol,Company,Shares,Average Price,Current Price,Invested Value,Current Value,Profit/Loss,Profit/Loss %\n");

            double totalInvested = 0.0;
            double totalCurrentValue = 0.0;

            for (Holding h : holdings) {
                Stock s = stockManager.getStock(h.getSymbol());
                if (s != null) {
                    double currentPrice = s.getCurrentPrice();
                    double investedValue = h.getInvestedValue();
                    double currentValue = h.getCurrentValue(currentPrice);
                    double profitLoss = currentValue - investedValue;
                    double profitLossPct = (profitLoss / investedValue) * 100.0;

                    totalInvested += investedValue;
                    totalCurrentValue += currentValue;

                    writer.write(String.format("%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        h.getSymbol(), s.getName(), h.getQuantity(),
                        h.getAveragePrice(), currentPrice, investedValue, currentValue,
                        profitLoss, profitLossPct));
                }
            }

            writer.close();
            NotificationManager.getInstance().showSuccess("Portfolio exported to Excel (CSV format)");
        } catch (Exception e) {
            NotificationManager.getInstance().showError("Failed to export portfolio: " + e.getMessage());
        }
    }
}
