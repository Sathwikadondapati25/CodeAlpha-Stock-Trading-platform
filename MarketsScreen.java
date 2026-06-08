import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MarketsScreen extends JPanel implements StockManager.StockUpdateListener {
    private final AuthManager authManager;
    private final StockManager stockManager;
    private final TradingManager tradingManager;
    private final StorageManager storageManager;

    private JTable stocksTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JTextField minPriceField;
    private JTextField maxPriceField;

    // Detail Panel Components
    private JPanel detailCard;
    private JLabel lblSymbol;
    private JLabel lblName;
    private JLabel lblPrice;
    private JLabel lblChange;
    private StockChartPanel chartPanel;
    private JTextField txtQty;
    private JLabel lblEstimate;
    private JLabel lblAvailableCash;
    private JLabel lblOwnedShares;
    private JLabel lblTradeStatus;
    private JTextField txtAlertPrice;
    private JLabel lblAlertStatus;

    private Stock selectedStock;

    public MarketsScreen(AuthManager authManager, StockManager stockManager, TradingManager tradingManager, StorageManager storageManager) {
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
        // --- LEFT PANEL: SEARCH & LIST ---
        JPanel leftPanel = new JPanel(new BorderLayout(12, 12));
        leftPanel.setOpaque(false);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Search Stocks:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        searchPanel.add(lblSearch, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setBackground(Theme.CARD_BG);
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setCaretColor(Theme.TEXT_PRIMARY);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        // Performance Filter
        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(Theme.FONT_BODY_BOLD);
        lblFilter.setForeground(Theme.TEXT_PRIMARY);
        filterPanel.add(lblFilter);

        filterCombo = new JComboBox<>(new String[]{"All Stocks", "Gainers Only", "Losers Only"});
        filterCombo.setPreferredSize(new Dimension(120, 28));
        filterCombo.setBackground(Theme.CARD_BG);
        filterCombo.setForeground(Theme.TEXT_PRIMARY);
        filterCombo.setFont(Theme.FONT_BODY);
        filterCombo.addActionListener(e -> applyFilters());
        filterPanel.add(filterCombo);

        // Price Range Filter
        JLabel lblMinPrice = new JLabel("Min Price:");
        lblMinPrice.setFont(Theme.FONT_SMALL);
        lblMinPrice.setForeground(Theme.TEXT_SECONDARY);
        filterPanel.add(lblMinPrice);

        minPriceField = new JTextField("0");
        minPriceField.setPreferredSize(new Dimension(70, 28));
        minPriceField.setBackground(Theme.CARD_BG);
        minPriceField.setForeground(Theme.TEXT_PRIMARY);
        minPriceField.setCaretColor(Theme.TEXT_PRIMARY);
        minPriceField.setFont(Theme.FONT_BODY);
        minPriceField.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));
        minPriceField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(minPriceField);

        JLabel lblMaxPrice = new JLabel("Max Price:");
        lblMaxPrice.setFont(Theme.FONT_SMALL);
        lblMaxPrice.setForeground(Theme.TEXT_SECONDARY);
        filterPanel.add(lblMaxPrice);

        maxPriceField = new JTextField("");
        maxPriceField.setPreferredSize(new Dimension(70, 28));
        maxPriceField.setBackground(Theme.CARD_BG);
        maxPriceField.setForeground(Theme.TEXT_PRIMARY);
        maxPriceField.setCaretColor(Theme.TEXT_PRIMARY);
        maxPriceField.setFont(Theme.FONT_BODY);
        maxPriceField.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));
        maxPriceField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(maxPriceField);

        leftPanel.add(filterPanel, BorderLayout.CENTER);

        // Table Container
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);

        // Table
        String[] cols = {"Symbol", "Company Name", "Price", "Change (%)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        stocksTable = new JTable(tableModel);
        styleTable(stocksTable);
        rowSorter = new TableRowSorter<>(tableModel);
        stocksTable.setRowSorter(rowSorter);

        stocksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = stocksTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Convert index in case table is sorted/filtered
                    int modelIndex = stocksTable.convertRowIndexToModel(selectedRow);
                    String symbol = (String) tableModel.getValueAt(modelIndex, 0);
                    selectStock(stockManager.getStock(symbol));
                }
            }
        });

        // Double-click to show stock details popup
        stocksTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = stocksTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int modelIndex = stocksTable.convertRowIndexToModel(selectedRow);
                        String symbol = (String) tableModel.getValueAt(modelIndex, 0);
                        Stock stock = stockManager.getStock(symbol);
                        if (stock != null) {
                            showStockDetailsPopup(stock);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(stocksTable);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(tableContainer, BorderLayout.SOUTH);

        // --- RIGHT PANEL: DETAILED CHART & TRADE CARD ---
        JPanel rightPanel = new JPanel(new BorderLayout(16, 16));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(420, 0));

        detailCard = new JPanel(new GridBagLayout());
        detailCard.setBackground(Theme.CARD_BG);
        detailCard.setBorder(Theme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        // Stock Title & Price Headers
        lblSymbol = new JLabel("Select a Stock");
        lblSymbol.setFont(Theme.FONT_HEADER);
        lblSymbol.setForeground(Theme.TEXT_PRIMARY);
        detailCard.add(lblSymbol, gbc);

        lblName = new JLabel("Choose a ticker from the list to start trading");
        lblName.setFont(Theme.FONT_SMALL);
        lblName.setForeground(Theme.TEXT_SECONDARY);
        gbc.insets = new Insets(0, 0, 12, 0);
        detailCard.add(lblName, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pricePanel.setOpaque(false);
        lblPrice = new JLabel("₹0.00");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPrice.setForeground(Theme.TEXT_PRIMARY);
        lblChange = new JLabel(" (0.00%)");
        lblChange.setFont(Theme.FONT_BODY_BOLD);
        pricePanel.add(lblPrice);
        pricePanel.add(lblChange);
        detailCard.add(pricePanel, gbc);

        // Live Chart Panel
        chartPanel = new StockChartPanel();
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(12, 0, 16, 0);
        detailCard.add(chartPanel, gbc);

        // Trade Panel Section
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_COLOR);
        detailCard.add(sep, gbc);

        // Qty Input and Estimate
        JPanel qtyPanel = new JPanel(new BorderLayout(8, 8));
        qtyPanel.setOpaque(false);
        JLabel lblQty = new JLabel("Trade Quantity:");
        lblQty.setFont(Theme.FONT_BODY_BOLD);
        lblQty.setForeground(Theme.TEXT_PRIMARY);
        qtyPanel.add(lblQty, BorderLayout.WEST);

        txtQty = new JTextField("0");
        txtQty.setBackground(Theme.BG_DARK);
        txtQty.setForeground(Theme.TEXT_PRIMARY);
        txtQty.setCaretColor(Theme.TEXT_PRIMARY);
        txtQty.setFont(Theme.FONT_BODY);
        txtQty.setHorizontalAlignment(JTextField.RIGHT);
        txtQty.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtQty.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateEstimate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateEstimate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateEstimate(); }
        });
        qtyPanel.add(txtQty, BorderLayout.CENTER);
        detailCard.add(qtyPanel, gbc);

        lblEstimate = new JLabel("Estimated Value: ₹0.00");
        lblEstimate.setFont(Theme.FONT_BODY_BOLD);
        lblEstimate.setForeground(Theme.TEXT_SECONDARY);
        detailCard.add(lblEstimate, gbc);

        // Cash / Holding Info
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        statsPanel.setOpaque(false);
        lblAvailableCash = new JLabel("Balance: ₹0.00");
        lblAvailableCash.setFont(Theme.FONT_SMALL);
        lblAvailableCash.setForeground(Theme.TEXT_MUTED);
        lblOwnedShares = new JLabel("Holdings: 0 shares");
        lblOwnedShares.setFont(Theme.FONT_SMALL);
        lblOwnedShares.setForeground(Theme.TEXT_MUTED);
        lblOwnedShares.setHorizontalAlignment(SwingConstants.RIGHT);
        statsPanel.add(lblAvailableCash);
        statsPanel.add(lblOwnedShares);
        detailCard.add(statsPanel, gbc);

        // Action status message
        lblTradeStatus = new JLabel(" ", SwingConstants.CENTER);
        lblTradeStatus.setFont(Theme.FONT_BODY_BOLD);
        lblTradeStatus.setForeground(Theme.GAIN);
        gbc.insets = new Insets(10, 0, 4, 0);
        detailCard.add(lblTradeStatus, gbc);

        // Buttons Grid
        gbc.insets = new Insets(6, 0, 0, 0);
        JPanel buttonGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonGrid.setOpaque(false);

        JButton btnBuy = new JButton("BUY STOCK");
        btnBuy.setFont(Theme.FONT_BODY_BOLD);
        btnBuy.setBackground(Theme.GAIN);
        btnBuy.setForeground(Color.WHITE);
        btnBuy.setFocusPainted(false);
        btnBuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuy.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btnBuy.addActionListener(e -> executeTrade(Transaction.Type.BUY));

        JButton btnSell = new JButton("SELL STOCK");
        btnSell.setFont(Theme.FONT_BODY_BOLD);
        btnSell.setBackground(Theme.LOSS);
        btnSell.setForeground(Color.WHITE);
        btnSell.setFocusPainted(false);
        btnSell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSell.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btnSell.addActionListener(e -> executeTrade(Transaction.Type.SELL));

        buttonGrid.add(btnBuy);
        buttonGrid.add(btnSell);
        detailCard.add(buttonGrid, gbc);

        // Watchlist Button
        gbc.insets = new Insets(12, 0, 0, 0);
        JButton btnWatchlist = new JButton("★ Add to Watchlist");
        btnWatchlist.setFont(Theme.FONT_BODY_BOLD);
        btnWatchlist.setBackground(Theme.ACCENT);
        btnWatchlist.setForeground(Color.WHITE);
        btnWatchlist.setFocusPainted(false);
        btnWatchlist.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnWatchlist.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        btnWatchlist.addActionListener(e -> addToWatchlist());
        detailCard.add(btnWatchlist, gbc);

        // Price Alert Section
        gbc.insets = new Insets(12, 0, 0, 0);
        JPanel alertPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        alertPanel.setOpaque(false);

        JLabel lblAlertLabel = new JLabel("Alert at ₹:");
        lblAlertLabel.setFont(Theme.FONT_BODY);
        lblAlertLabel.setForeground(Theme.TEXT_SECONDARY);
        alertPanel.add(lblAlertLabel);

        txtAlertPrice = new JTextField(8);
        txtAlertPrice.setBackground(Theme.BG_DARK);
        txtAlertPrice.setForeground(Theme.TEXT_PRIMARY);
        txtAlertPrice.setCaretColor(Theme.TEXT_PRIMARY);
        txtAlertPrice.setFont(Theme.FONT_BODY);
        txtAlertPrice.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        alertPanel.add(txtAlertPrice);

        JButton btnSetAlert = new JButton("Set Alert");
        btnSetAlert.setFont(Theme.FONT_BODY_BOLD);
        btnSetAlert.setBackground(Theme.TEXT_SECONDARY);
        btnSetAlert.setForeground(Color.WHITE);
        btnSetAlert.setFocusPainted(false);
        btnSetAlert.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSetAlert.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnSetAlert.addActionListener(e -> setPriceAlert());
        alertPanel.add(btnSetAlert);

        detailCard.add(alertPanel, gbc);

        gbc.insets = new Insets(4, 0, 0, 0);
        lblAlertStatus = new JLabel(" ", SwingConstants.CENTER);
        lblAlertStatus.setFont(Theme.FONT_SMALL);
        lblAlertStatus.setForeground(Theme.TEXT_MUTED);
        detailCard.add(lblAlertStatus, gbc);

        rightPanel.add(detailCard, BorderLayout.CENTER);

        // Assemble splits
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);
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
        
        // Render alignment & color
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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

    private void selectStock(Stock stock) {
        // Preserve quantity if same stock is being refreshed
        boolean isSameStock = (selectedStock != null && selectedStock.getSymbol().equals(stock.getSymbol()));
        String currentQty = isSameStock ? txtQty.getText() : "0";

        this.selectedStock = stock;
        chartPanel.setStock(stock);

        lblSymbol.setText(stock.getSymbol());
        lblName.setText(stock.getName());
        txtQty.setText(currentQty);
        lblTradeStatus.setText(" ");

        // Recalculate estimate with preserved quantity
        if (!currentQty.equals("0")) {
            updateEstimate();
        } else {
            lblEstimate.setText("Estimated Value: ₹0.00");
        }

        updateLiveStockDetails();
    }

    private void updateLiveStockDetails() {
        if (selectedStock == null || !authManager.isLoggedIn()) return;
        
        double currentPrice = selectedStock.getCurrentPrice();
        double change = selectedStock.getChangePercentage();
        String sign = change >= 0 ? "+" : "";

        SwingUtilities.invokeLater(() -> {
            lblPrice.setText(String.format("₹%.2f", currentPrice));
            lblChange.setText(String.format(" (%s%.2f%%)", sign, change));
            lblChange.setForeground(change >= 0 ? Theme.GAIN : Theme.LOSS);
            
            // Update balance and holdings statistics
            User user = authManager.getCurrentUser();
            lblAvailableCash.setText(String.format("Balance: ₹%.2f", user.getBalance()));
            
            int owned = 0;
            java.util.List<Holding> holdings = storageManager.loadPortfolio(user.getUsername());
            for (Holding h : holdings) {
                if (h.getSymbol().equalsIgnoreCase(selectedStock.getSymbol())) {
                    owned = h.getQuantity();
                    break;
                }
            }
            lblOwnedShares.setText(String.format("Holdings: %d shares", owned));
            
            updateEstimate();
        });
    }

    private void updateEstimate() {
        if (selectedStock == null) return;
        try {
            int qty = Integer.parseInt(txtQty.getText().trim());
            if (qty > 0) {
                double price = selectedStock.getCurrentPrice();
                lblEstimate.setText(String.format("Estimated Value: ₹%.2f", qty * price));
            } else {
                lblEstimate.setText("Estimated Value: ₹0.00");
            }
        } catch (NumberFormatException e) {
            lblEstimate.setText("Estimated Value: ₹0.00");
        }
    }

    private void executeTrade(Transaction.Type type) {
        if (selectedStock == null) {
            showTradeError("Please select a stock first.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
            if (qty <= 0) {
                showTradeError("Quantity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showTradeError("Invalid quantity value.");
            return;
        }

        // Show confirmation dialog
        double totalCost = qty * selectedStock.getCurrentPrice();
        String action = type == Transaction.Type.BUY ? "Buy" : "Sell";
        String message = String.format("%s %d shares of %s for ₹%.2f?", action, qty, selectedStock.getSymbol(), totalCost);

        int choice = JOptionPane.showConfirmDialog(
            this,
            message,
            "Confirm Trade",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            User user = authManager.getCurrentUser();
            if (type == Transaction.Type.BUY) {
                tradingManager.buyStock(user, selectedStock, qty);
                showTradeSuccess(String.format("Successfully bought %d shares of %s!", qty, selectedStock.getSymbol()));
                NotificationManager.getInstance().showSuccess(String.format("Bought %d shares of %s", qty, selectedStock.getSymbol()));
            } else {
                tradingManager.sellStock(user, selectedStock, qty);
                showTradeSuccess(String.format("Successfully sold %d shares of %s!", qty, selectedStock.getSymbol()));
                NotificationManager.getInstance().showSuccess(String.format("Sold %d shares of %s", qty, selectedStock.getSymbol()));
            }

            // Clean/Refresh panel states
            txtQty.setText("0");
            updateLiveStockDetails();
        } catch (IllegalArgumentException ex) {
            showTradeError(ex.getMessage());
            NotificationManager.getInstance().showError(ex.getMessage());
        }
    }

    private void showTradeError(String msg) {
        lblTradeStatus.setForeground(Theme.LOSS);
        lblTradeStatus.setText(msg);
    }

    private void showTradeSuccess(String msg) {
        lblTradeStatus.setForeground(Theme.GAIN);
        lblTradeStatus.setText(msg);
    }

    private void addToWatchlist() {
        if (selectedStock == null) {
            showTradeError("Please select a stock first.");
            return;
        }
        storageManager.addToWatchlist(authManager.getCurrentUser().getUsername(), selectedStock.getSymbol());
        showTradeSuccess("Added " + selectedStock.getSymbol() + " to watchlist!");
        NotificationManager.getInstance().showSuccess("Added " + selectedStock.getSymbol() + " to watchlist");
    }

    private void setPriceAlert() {
        if (selectedStock == null) {
            lblAlertStatus.setText("Please select a stock first.");
            lblAlertStatus.setForeground(Theme.LOSS);
            return;
        }

        try {
            double targetPrice = Double.parseDouble(txtAlertPrice.getText().trim());
            if (targetPrice <= 0) {
                lblAlertStatus.setText("Price must be greater than 0.");
                lblAlertStatus.setForeground(Theme.LOSS);
                return;
            }

            storageManager.addAlert(authManager.getCurrentUser().getUsername(), selectedStock.getSymbol(), targetPrice);
            lblAlertStatus.setText("Alert set for " + selectedStock.getSymbol() + " at ₹" + targetPrice);
            lblAlertStatus.setForeground(Theme.GAIN);
            NotificationManager.getInstance().showSuccess("Alert set for " + selectedStock.getSymbol() + " at ₹" + targetPrice);
            txtAlertPrice.setText("");
        } catch (NumberFormatException e) {
            lblAlertStatus.setText("Invalid price.");
            lblAlertStatus.setForeground(Theme.LOSS);
        }
    }

    private void showStockDetailsPopup(Stock stock) {
        // Get user's holdings for this stock
        List<Holding> holdings = storageManager.loadPortfolio(authManager.getCurrentUser().getUsername());
        int sharesOwned = 0;
        double avgBuyPrice = 0.0;
        for (Holding h : holdings) {
            if (h.getSymbol().equals(stock.getSymbol())) {
                sharesOwned = h.getQuantity();
                avgBuyPrice = h.getAveragePrice();
                break;
            }
        }

        // Calculate profit/loss
        double currentValue = sharesOwned * stock.getCurrentPrice();
        double investedValue = sharesOwned * avgBuyPrice;
        double profitLoss = currentValue - investedValue;
        double profitLossPct = investedValue == 0 ? 0 : (profitLoss / investedValue) * 100.0;

        // Create popup dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Stock Details: " + stock.getSymbol(), true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.CARD_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Stock Symbol
        JLabel lblSymbol = new JLabel(stock.getSymbol());
        lblSymbol.setFont(Theme.FONT_HEADER);
        lblSymbol.setForeground(Theme.TEXT_PRIMARY);
        mainPanel.add(lblSymbol, gbc);

        gbc.gridy++;

        // Company Name
        JLabel lblName = new JLabel(stock.getName());
        lblName.setFont(Theme.FONT_SUBHEADER);
        lblName.setForeground(Theme.TEXT_SECONDARY);
        mainPanel.add(lblName, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 0, 8, 0);

        // Current Price
        addDetailRow(mainPanel, gbc, "Current Price:", String.format("₹%.2f", stock.getCurrentPrice()));

        // Day Change
        double changePct = stock.getChangePercentage();
        String changeStr = String.format("%s%.2f%%", changePct >= 0 ? "+" : "", changePct);
        JLabel lblChangeValue = new JLabel(changeStr);
        lblChangeValue.setForeground(changePct >= 0 ? Theme.GAIN : Theme.LOSS);
        addDetailRow(mainPanel, gbc, "Day Change:", lblChangeValue);

        // Holdings
        addDetailRow(mainPanel, gbc, "Shares Owned:", String.valueOf(sharesOwned));

        // Average Buy Price
        addDetailRow(mainPanel, gbc, "Avg Buy Price:", String.format("₹%.2f", avgBuyPrice));

        // Current Value
        addDetailRow(mainPanel, gbc, "Current Value:", String.format("₹%.2f", currentValue));

        // Profit/Loss
        JLabel lblPLValue = new JLabel(String.format("%s₹%.2f (%.2f%%)", profitLoss >= 0 ? "+" : "", profitLoss, profitLossPct));
        lblPLValue.setForeground(profitLoss >= 0 ? Theme.GAIN : Theme.LOSS);
        addDetailRow(mainPanel, gbc, "Profit/Loss:", lblPLValue);

        // Close button
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JButton btnClose = new JButton("Close");
        btnClose.setFont(Theme.FONT_BODY_BOLD);
        btnClose.setBackground(Theme.ACCENT);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnClose.addActionListener(e -> dialog.dispose());
        mainPanel.add(btnClose, gbc);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(Theme.FONT_BODY_BOLD);
        lblLabel.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lblLabel, gbc);

        gbc.gridy++;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(Theme.FONT_BODY);
        lblValue.setForeground(Theme.TEXT_PRIMARY);
        panel.add(lblValue, gbc);

        gbc.gridy++;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, JLabel value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(Theme.FONT_BODY_BOLD);
        lblLabel.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lblLabel, gbc);

        gbc.gridy++;
        panel.add(value, gbc);

        gbc.gridy++;
    }

    private void applyFilters() {
        String searchText = searchField.getText().trim();
        String performanceFilter = (String) filterCombo.getSelectedItem();
        String minPriceStr = minPriceField.getText().trim();
        String maxPriceStr = maxPriceField.getText().trim();

        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();

        // Text search filter
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }

        // Performance filter (gainers/losers)
        if (performanceFilter != null && !performanceFilter.equals("All Stocks")) {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String changeStr = (String) entry.getValue(3); // Change column
                    if (changeStr == null) return false;
                    boolean isGainer = changeStr.startsWith("+");
                    if (performanceFilter.equals("Gainers Only")) {
                        return isGainer;
                    } else if (performanceFilter.equals("Losers Only")) {
                        return !isGainer && changeStr.startsWith("-");
                    }
                    return true;
                }
            });
        }

        // Price range filter
        try {
            double minPrice = minPriceStr.isEmpty() ? 0 : Double.parseDouble(minPriceStr);
            double maxPrice = maxPriceStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceStr);

            if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
                filters.add(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        String priceStr = (String) entry.getValue(2); // Price column
                        if (priceStr == null) return false;
                        // Remove currency symbol and parse
                        double price = Double.parseDouble(priceStr.replace("₹", "").replace(",", "").trim());
                        return price >= minPrice && price <= maxPrice;
                    }
                });
            }
        } catch (NumberFormatException e) {
            // Invalid price input, ignore price filter
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    public synchronized void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        // Reload all stock prices into rows
        updateStockTableRows();

        // Keep detail view fresh if stock is selected
        if (selectedStock != null) {
            selectStock(stockManager.getStock(selectedStock.getSymbol()));
        } else {
            // Default select the first stock in list
            if (tableModel.getRowCount() > 0) {
                String firstSymbol = (String) tableModel.getValueAt(0, 0);
                stocksTable.setRowSelectionInterval(0, 0);
                selectStock(stockManager.getStock(firstSymbol));
            }
        }
    }

    private void updateStockTableRows() {
        Collection<Stock> stocks = stockManager.getAllStocks();
        tableModel.setRowCount(0);
        for (Stock s : stocks) {
            double change = s.getChangePercentage();
            String changeStr = String.format("%s%.2f%%", change >= 0 ? "+" : "", change);
            tableModel.addRow(new Object[]{
                s.getSymbol(),
                s.getName(),
                String.format("₹%.2f", s.getCurrentPrice()),
                changeStr
            });
        }
    }

    @Override
    public void onStocksUpdated(Collection<Stock> updatedStocks) {
        // Refresh Table while saving selection index
        SwingUtilities.invokeLater(() -> {
            int selectedRow = stocksTable.getSelectedRow();
            String prevSelectedSymbol = selectedStock != null ? selectedStock.getSymbol() : null;

            // Re-render rows
            tableModel.setRowCount(0);
            for (Stock s : updatedStocks) {
                double change = s.getChangePercentage();
                String changeStr = String.format("%s%.2f%%", change >= 0 ? "+" : "", change);
                tableModel.addRow(new Object[]{
                    s.getSymbol(),
                    s.getName(),
                    String.format("₹%.2f", s.getCurrentPrice()),
                    changeStr
                });
            }

            // Restore selection
            if (prevSelectedSymbol != null) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(prevSelectedSymbol)) {
                        int viewIndex = stocksTable.convertRowIndexToView(i);
                        stocksTable.setRowSelectionInterval(viewIndex, viewIndex);
                        break;
                    }
                }
            }

            // Dynamic detail changes
            if (selectedStock != null) {
                updateLiveStockDetails();
            }
        });
    }
}
