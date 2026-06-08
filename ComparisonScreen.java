import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ComparisonScreen extends JPanel {
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JComboBox<String> stockSelector1;
    private JComboBox<String> stockSelector2;
    private JTable comparisonTable;
    private DefaultTableModel comparisonModel;

    public ComparisonScreen(StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Stock Comparison Tool");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- STOCK SELECTION PANEL ---
        JPanel selectionPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        selectionPanel.setOpaque(false);

        selectionPanel.add(createStockSelectorPanel("Stock 1", stockSelector1));
        selectionPanel.add(createStockSelectorPanel("Stock 2", stockSelector2));

        add(selectionPanel, BorderLayout.NORTH);

        // --- COMPARISON TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Metric", "Stock 1", "Stock 2", "Difference"};
        comparisonModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        comparisonTable = new JTable(comparisonModel);
        styleTable(comparisonTable);

        JScrollPane scrollPane = new JScrollPane(comparisonTable);
        scrollPane.getViewport().setBackground(Theme.getCardBackground());
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createStockSelectorPanel(String title, JComboBox<String> selector) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.getCardBackground());
        panel.setBorder(Theme.createCardBorder());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_BODY_BOLD);
        titleLabel.setForeground(Theme.getTextPrimary());
        panel.add(titleLabel, BorderLayout.NORTH);

        List<Stock> stocks = new ArrayList<>(stockManager.getAllStocks());
        String[] stockNames = new String[stocks.size()];
        for (int i = 0; i < stocks.size(); i++) {
            stockNames[i] = stocks.get(i).getSymbol() + " - " + stocks.get(i).getName();
        }

        selector = new JComboBox<>(stockNames);
        selector.setFont(Theme.FONT_BODY);
        selector.setBackground(Theme.getBackground());
        selector.setForeground(Theme.getTextPrimary());
        selector.addActionListener(e -> updateComparison());

        if (title.equals("Stock 1")) {
            stockSelector1 = selector;
        } else {
            stockSelector2 = selector;
        }

        panel.add(selector, BorderLayout.CENTER);

        return panel;
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        updateComparison();
    }

    private void updateComparison() {
        comparisonModel.setRowCount(0);

        String selected1 = (String) stockSelector1.getSelectedItem();
        String selected2 = (String) stockSelector2.getSelectedItem();

        if (selected1 == null || selected2 == null) return;

        String symbol1 = selected1.split(" - ")[0];
        String symbol2 = selected2.split(" - ")[0];

        Stock stock1 = stockManager.getStock(symbol1);
        Stock stock2 = stockManager.getStock(symbol2);

        if (stock1 == null || stock2 == null) return;

        // Add comparison rows
        addComparisonRow("Symbol", stock1.getSymbol(), stock2.getSymbol());
        addComparisonRow("Name", stock1.getName(), stock2.getName());
        addComparisonRow("Current Price", stock1.getCurrentPrice(), stock2.getCurrentPrice());
        addComparisonRow("Open Price", stock1.getOpenPrice(), stock2.getOpenPrice());
        addComparisonRow("Change Amount", stock1.getChangeAmount(), stock2.getChangeAmount());
        addComparisonRow("Change %", stock1.getChangePercentage(), stock2.getChangePercentage());
        addComparisonRow("52W High", stock1.getCurrentPrice() * 1.2, stock2.getCurrentPrice() * 1.2);
        addComparisonRow("52W Low", stock1.getCurrentPrice() * 0.8, stock2.getCurrentPrice() * 0.8);
    }

    private void addComparisonRow(String metric, double value1, double value2) {
        String formatted1 = String.format("₹%.2f", value1);
        String formatted2 = String.format("₹%.2f", value2);
        double diff = value1 - value2;
        String diffStr = String.format("%s₹%.2f", diff >= 0 ? "+" : "", diff);

        comparisonModel.addRow(new Object[]{metric, formatted1, formatted2, diffStr});
    }

    private void addComparisonRow(String metric, String value1, String value2) {
        String diffStr = value1.equals(value2) ? "Same" : "Different";
        comparisonModel.addRow(new Object[]{metric, value1, value2, diffStr});
    }
}
