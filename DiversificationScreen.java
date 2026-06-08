import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DiversificationScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable sectorTable;
    private DefaultTableModel sectorModel;

    public DiversificationScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Portfolio Diversification Analysis");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- SECTOR BREAKDOWN TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        JLabel tableTitle = new JLabel("Sector Allocation");
        tableTitle.setFont(Theme.FONT_SUBHEADER);
        tableTitle.setForeground(Theme.getTextPrimary());
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Sector", "Stocks", "Allocation %", "Risk Level"};
        sectorModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        sectorTable = new JTable(sectorModel);
        styleTable(sectorTable);

        JScrollPane scrollPane = new JScrollPane(sectorTable);
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
    }

    public void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        String username = authManager.getCurrentUser().getUsername();
        List<Holding> holdings = storageManager.loadPortfolio(username);

        if (holdings.isEmpty()) {
            sectorModel.setRowCount(0);
            return;
        }

        // Calculate sector allocation
        Map<String, SectorData> sectorMap = new HashMap<>();
        double totalValue = 0.0;

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                double value = h.getCurrentValue(s.getCurrentPrice());
                totalValue += value;

                String sector = getSectorForStock(s.getSymbol());
                sectorMap.putIfAbsent(sector, new SectorData(sector));
                sectorMap.get(sector).addStock(h.getSymbol(), value);
            }
        }

        // Calculate diversification score
        int sectorCount = sectorMap.size();
        double diversificationScore = calculateDiversificationScore(sectorMap, totalValue);
        String riskLevel = calculateRiskLevel(diversificationScore);

        // Update KPI cards - values are displayed in the table

        // Update sector table
        sectorModel.setRowCount(0);
        for (SectorData data : sectorMap.values()) {
            double allocationPercent = (data.totalValue / totalValue) * 100;
            String sectorRisk = calculateSectorRisk(allocationPercent);

            sectorModel.addRow(new Object[]{
                data.sector,
                data.stockCount,
                String.format("%.1f%%", allocationPercent),
                sectorRisk
            });
        }
    }

    private String getSectorForStock(String symbol) {
        // Simple sector mapping based on stock symbols
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

    private double calculateDiversificationScore(Map<String, SectorData> sectorMap, double totalValue) {
        if (sectorMap.isEmpty()) return 0.0;

        // Score based on:
        // 1. Number of sectors (max 40 points)
        // 2. Even distribution (max 60 points)
        int sectorCount = sectorMap.size();
        double sectorScore = Math.min(sectorCount * 10, 40.0);

        // Calculate concentration (how evenly distributed)
        double maxAllocation = 0.0;
        for (SectorData data : sectorMap.values()) {
            double allocation = (data.totalValue / totalValue) * 100;
            if (allocation > maxAllocation) {
                maxAllocation = allocation;
            }
        }

        // Lower max allocation = better diversification
        double distributionScore = Math.max(0, 60 - (maxAllocation - 20) * 2);

        return sectorScore + distributionScore;
    }

    private String calculateRiskLevel(double diversificationScore) {
        if (diversificationScore >= 70) return "Low";
        if (diversificationScore >= 40) return "Medium";
        return "High";
    }

    private String calculateSectorRisk(double allocationPercent) {
        if (allocationPercent > 40) return "High";
        if (allocationPercent > 20) return "Medium";
        return "Low";
    }

    private static class SectorData {
        String sector;
        int stockCount;
        double totalValue;

        SectorData(String sector) {
            this.sector = sector;
            this.stockCount = 0;
            this.totalValue = 0.0;
        }

        void addStock(String symbol, double value) {
            stockCount++;
            totalValue += value;
        }
    }
}
