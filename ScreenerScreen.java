import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ScreenerScreen extends JPanel {
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable screenerTable;
    private DefaultTableModel screenerModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JComboBox<String> sectorCombo;
    private JComboBox<String> changeCombo;

    public ScreenerScreen(StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Stock Screener");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- FILTER PANEL ---
        JPanel filterPanel = new JPanel(new GridLayout(2, 4, 12, 8));
        filterPanel.setOpaque(false);
        filterPanel.setBackground(Theme.getCardBackground());
        filterPanel.setBorder(Theme.createCardBorder());
        filterPanel.setPreferredSize(new Dimension(0, 100));

        // Min Price
        JPanel minPricePanel = new JPanel(new BorderLayout(4, 0));
        minPricePanel.setOpaque(false);
        JLabel lblMinPrice = new JLabel("Min Price:");
        lblMinPrice.setFont(Theme.FONT_BODY_BOLD);
        lblMinPrice.setForeground(Theme.TEXT_PRIMARY);
        minPricePanel.add(lblMinPrice, BorderLayout.WEST);
        minPriceField = new JTextField("0");
        minPriceField.setBackground(Theme.getBackground());
        minPriceField.setForeground(Theme.TEXT_PRIMARY);
        minPriceField.setCaretColor(Theme.TEXT_PRIMARY);
        minPriceField.setFont(Theme.FONT_BODY);
        minPriceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        minPricePanel.add(minPriceField, BorderLayout.CENTER);
        filterPanel.add(minPricePanel);

        // Max Price
        JPanel maxPricePanel = new JPanel(new BorderLayout(4, 0));
        maxPricePanel.setOpaque(false);
        JLabel lblMaxPrice = new JLabel("Max Price:");
        lblMaxPrice.setFont(Theme.FONT_BODY_BOLD);
        lblMaxPrice.setForeground(Theme.TEXT_PRIMARY);
        maxPricePanel.add(lblMaxPrice, BorderLayout.WEST);
        maxPriceField = new JTextField("100000");
        maxPriceField.setBackground(Theme.getBackground());
        maxPriceField.setForeground(Theme.TEXT_PRIMARY);
        maxPriceField.setCaretColor(Theme.TEXT_PRIMARY);
        maxPriceField.setFont(Theme.FONT_BODY);
        maxPriceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        maxPricePanel.add(maxPriceField, BorderLayout.CENTER);
        filterPanel.add(maxPricePanel);

        // Sector Filter
        JPanel sectorPanel = new JPanel(new BorderLayout(4, 0));
        sectorPanel.setOpaque(false);
        JLabel lblSector = new JLabel("Sector:");
        lblSector.setFont(Theme.FONT_BODY_BOLD);
        lblSector.setForeground(Theme.TEXT_PRIMARY);
        sectorPanel.add(lblSector, BorderLayout.WEST);
        sectorCombo = new JComboBox<>(new String[]{"All Sectors", "IT Services", "Financial Services", "Metals & Mining", "Automotive", "Pharmaceuticals", "FMCG", "Others"});
        sectorCombo.setFont(Theme.FONT_BODY);
        sectorCombo.setBackground(Theme.getBackground());
        sectorCombo.setForeground(Theme.TEXT_PRIMARY);
        sectorPanel.add(sectorCombo, BorderLayout.CENTER);
        filterPanel.add(sectorPanel);

        // Change Filter
        JPanel changePanel = new JPanel(new BorderLayout(4, 0));
        changePanel.setOpaque(false);
        JLabel lblChange = new JLabel("Change %:");
        lblChange.setFont(Theme.FONT_BODY_BOLD);
        lblChange.setForeground(Theme.TEXT_PRIMARY);
        changePanel.add(lblChange, BorderLayout.WEST);
        changeCombo = new JComboBox<>(new String[]{"All", "Positive", "Negative", "> 5%", "< -5%"});
        changeCombo.setFont(Theme.FONT_BODY);
        changeCombo.setBackground(Theme.getBackground());
        changeCombo.setForeground(Theme.TEXT_PRIMARY);
        changePanel.add(changeCombo, BorderLayout.CENTER);
        filterPanel.add(changePanel);

        // Apply Button
        JButton btnApply = new JButton("Apply Filters");
        btnApply.setFont(Theme.FONT_BODY_BOLD);
        btnApply.setBackground(Theme.ACCENT);
        btnApply.setForeground(Color.WHITE);
        btnApply.setFocusPainted(false);
        btnApply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApply.addActionListener(e -> applyFilters());
        filterPanel.add(btnApply);

        // Reset Button
        JButton btnReset = new JButton("Reset");
        btnReset.setFont(Theme.FONT_BODY_BOLD);
        btnReset.setBackground(Theme.LOSS);
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetFilters());
        filterPanel.add(btnReset);

        add(filterPanel, BorderLayout.NORTH);

        // --- SCREENER TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Symbol", "Name", "Price", "Change %", "Sector", "Market Cap", "Action"};
        screenerModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        screenerTable = new JTable(screenerModel);
        styleTable(screenerTable);

        rowSorter = new TableRowSorter<>(screenerModel);
        screenerTable.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(screenerTable);
        scrollPane.getViewport().setBackground(Theme.getCardBackground());
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
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

        loadStocks();
    }

    private void loadStocks() {
        screenerModel.setRowCount(0);

        List<Stock> stocks = new ArrayList<>(stockManager.getAllStocks());

        for (Stock stock : stocks) {
            String sector = getSectorForStock(stock.getSymbol());
            double marketCap = stock.getCurrentPrice() * 1000000; // Simulated market cap

            screenerModel.addRow(new Object[]{
                stock.getSymbol(),
                stock.getName(),
                String.format("₹%.2f", stock.getCurrentPrice()),
                String.format("%.2f%%", stock.getChangePercentage()),
                sector,
                String.format("₹%.2f Cr", marketCap / 10000000),
                "View"
            });
        }
    }

    private void applyFilters() {
        String minPriceStr = minPriceField.getText().trim();
        String maxPriceStr = maxPriceField.getText().trim();
        String sectorFilter = (String) sectorCombo.getSelectedItem();
        String changeFilter = (String) changeCombo.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        // Price range filter
        try {
            double minPrice = Double.parseDouble(minPriceStr);
            double maxPrice = Double.parseDouble(maxPriceStr);

            RowFilter<DefaultTableModel, Object> priceFilter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String priceStr = (String) entry.getValue(2);
                    double price = Double.parseDouble(priceStr.replace("₹", "").replace(",", "").trim());
                    return price >= minPrice && price <= maxPrice;
                }
            };
            filters.add(priceFilter);
        } catch (NumberFormatException e) {
            // Invalid price, skip filter
        }

        // Sector filter
        if (!sectorFilter.equals("All Sectors")) {
            RowFilter<DefaultTableModel, Object> sectorRowFilter = RowFilter.regexFilter(sectorFilter, 4);
            filters.add(sectorRowFilter);
        }

        // Change filter
        if (!changeFilter.equals("All")) {
            RowFilter<DefaultTableModel, Object> changeRowFilter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String changeStr = (String) entry.getValue(3);
                    double change = Double.parseDouble(changeStr.replace("%", "").trim());

                    switch (changeFilter) {
                        case "Positive":
                            return change > 0;
                        case "Negative":
                            return change < 0;
                        case "> 5%":
                            return change > 5;
                        case "< -5%":
                            return change < -5;
                        default:
                            return true;
                    }
                }
            };
            filters.add(changeRowFilter);
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void resetFilters() {
        minPriceField.setText("0");
        maxPriceField.setText("100000");
        sectorCombo.setSelectedItem("All Sectors");
        changeCombo.setSelectedItem("All");
        rowSorter.setRowFilter(null);
    }

    private String getSectorForStock(String symbol) {
        if (symbol.equals("TCS") || symbol.equals("INFY") || symbol.equals("WIPRO")) {
            return "IT Services";
        } else if (symbol.equals("RELIANCE") || symbol.equals("HDFC") || symbol.equals("ICICI")) {
            return "Financial Services";
        } else if (symbol.equals("TATASTEEL") || symbol.equals("JSWSTEEL")) {
            return "Metals & Mining";
        } else if (symbol.equals("MARUTI") || symbol.equals("TATAMOTORS")) {
            return "Automotive";
        } else if (symbol.equals("SUNPHARMA") || symbol.equals("DRREDDY")) {
            return "Pharmaceuticals";
        } else if (symbol.equals("HINDUNILVR") || symbol.equals("ITC")) {
            return "FMCG";
        } else {
            return "Others";
        }
    }
}
