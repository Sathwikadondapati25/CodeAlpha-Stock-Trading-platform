import java.awt.*;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class WatchlistScreen extends JPanel implements StockManager.StockUpdateListener {
    private final AuthManager authManager;
    private final StockManager stockManager;
    private final StorageManager storageManager;

    private JTable watchlistTable;
    private DefaultTableModel tableModel;

    public WatchlistScreen(AuthManager authManager, StockManager stockManager, StorageManager storageManager) {
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

        JLabel titleLabel = new JLabel("My Watchlist");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- WATCHLIST TABLE ---
        String[] cols = {"Symbol", "Company Name", "Current Price", "Day Change", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        watchlistTable = new JTable(tableModel);
        styleTable(watchlistTable);

        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));

        add(scrollPane, BorderLayout.CENTER);
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

        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer); // Price
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

        // Action button renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int r, int c) {
                JButton btn = new JButton("Remove");
                btn.setFont(Theme.FONT_SMALL);
                btn.setBackground(Theme.LOSS);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }
        });

        // Mouse listener for remove button clicks
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (column == 4 && row >= 0) {
                    String symbol = (String) tableModel.getValueAt(row, 0);
                    storageManager.removeFromWatchlist(authManager.getCurrentUser().getUsername(), symbol);
                    refresh();
                }
            }
        });
    }

    public synchronized void refresh() {
        if (!authManager.isLoggedIn()) return;

        // Update theme colors
        setBackground(Theme.getBackground());

        onStocksUpdated(stockManager.getAllStocks());
    }

    @Override
    public void onStocksUpdated(Collection<Stock> updatedStocks) {
        if (!authManager.isLoggedIn()) return;

        String username = authManager.getCurrentUser().getUsername();
        List<String> watchlist = storageManager.loadWatchlist(username);

        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (String symbol : watchlist) {
                Stock stock = stockManager.getStock(symbol);
                if (stock != null) {
                    double change = stock.getChangePercentage();
                    String changeStr = String.format("%s%.2f%%", change >= 0 ? "+" : "", change);
                    tableModel.addRow(new Object[]{
                        stock.getSymbol(),
                        stock.getName(),
                        String.format("₹%.2f", stock.getCurrentPrice()),
                        changeStr,
                        "Remove"
                    });
                }
            }
        });
    }
}
