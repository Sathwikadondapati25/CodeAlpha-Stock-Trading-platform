import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TransactionsScreen extends JPanel {
    private final AuthManager authManager;
    private final StorageManager storageManager;

    private JTable transTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JTextField startDateField;
    private JTextField endDateField;

    public TransactionsScreen(AuthManager authManager, StorageManager storageManager) {
        this.authManager = authManager;
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

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnExport = new JButton("Export to CSV");
        btnExport.setFont(Theme.FONT_BODY_BOLD);
        btnExport.setBackground(Theme.ACCENT);
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnExport.addActionListener(e -> exportToCSV());
        headerPanel.add(btnExport, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- FILTER PANEL ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterPanel.setOpaque(false);

        // Symbol Search
        JLabel lblSearch = new JLabel("Symbol Search:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        filterPanel.add(lblSearch);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 32));
        searchField.setBackground(Theme.CARD_BG);
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setCaretColor(Theme.TEXT_PRIMARY);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(searchField);

        // Type Filter Dropdown
        JLabel lblFilter = new JLabel("Type Filter:");
        lblFilter.setFont(Theme.FONT_BODY_BOLD);
        lblFilter.setForeground(Theme.TEXT_PRIMARY);
        filterPanel.add(lblFilter);

        filterCombo = new JComboBox<>(new String[]{"All Types", "BUY", "SELL"});
        filterCombo.setPreferredSize(new Dimension(120, 32));
        filterCombo.setBackground(Theme.CARD_BG);
        filterCombo.setForeground(Theme.TEXT_PRIMARY);
        filterCombo.setFont(Theme.FONT_BODY);
        filterCombo.addActionListener(e -> applyFilters());
        filterPanel.add(filterCombo);

        // Date Range Filter
        JLabel lblStartDate = new JLabel("From:");
        lblStartDate.setFont(Theme.FONT_BODY_BOLD);
        lblStartDate.setForeground(Theme.TEXT_PRIMARY);
        filterPanel.add(lblStartDate);

        startDateField = new JTextField("YYYY-MM-DD");
        startDateField.setPreferredSize(new Dimension(100, 32));
        startDateField.setBackground(Theme.CARD_BG);
        startDateField.setForeground(Theme.TEXT_PRIMARY);
        startDateField.setCaretColor(Theme.TEXT_PRIMARY);
        startDateField.setFont(Theme.FONT_BODY);
        startDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        startDateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(startDateField);

        JLabel lblEndDate = new JLabel("To:");
        lblEndDate.setFont(Theme.FONT_BODY_BOLD);
        lblEndDate.setForeground(Theme.TEXT_PRIMARY);
        filterPanel.add(lblEndDate);

        endDateField = new JTextField("YYYY-MM-DD");
        endDateField.setPreferredSize(new Dimension(100, 32));
        endDateField.setBackground(Theme.CARD_BG);
        endDateField.setForeground(Theme.TEXT_PRIMARY);
        endDateField.setCaretColor(Theme.TEXT_PRIMARY);
        endDateField.setFont(Theme.FONT_BODY);
        endDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        endDateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(endDateField);

        // --- TRANSACTIONS TABLE ---
        String[] cols = {"Timestamp", "Type", "Symbol", "Quantity", "Execution Price", "Total Value"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        transTable = new JTable(tableModel);
        styleTable(transTable);

        rowSorter = new TableRowSorter<>(tableModel);
        transTable.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(transTable);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true));

        // Layout Assembly
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
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
        
        // Custom alignments
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Qty
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);  // Price
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);  // Total

        // Color cell renderer for Buy/Sell types
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, r, c);
                l.setHorizontalAlignment(JLabel.CENTER);
                String strVal = (String) val;
                if (strVal != null) {
                    if (strVal.equalsIgnoreCase("BUY")) {
                        l.setForeground(Theme.GAIN);
                    } else if (strVal.equalsIgnoreCase("SELL")) {
                        l.setForeground(Theme.LOSS);
                    }
                }
                return l;
            }
        });
    }

    private void applyFilters() {
        String text = searchField.getText().trim();
        String typeFilter = (String) filterCombo.getSelectedItem();
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();

        RowFilter<DefaultTableModel, Object> rfSymbol = null;
        RowFilter<DefaultTableModel, Object> rfType = null;
        RowFilter<DefaultTableModel, Object> rfDate = null;

        if (!text.isEmpty()) {
            rfSymbol = RowFilter.regexFilter("(?i)" + text, 2); // column 2 is Symbol
        }
        if (typeFilter != null && !typeFilter.equals("All Types")) {
            rfType = RowFilter.regexFilter(typeFilter.toUpperCase(), 1); // column 1 is Type
        }

        // Date range filter
        if (!startDateStr.isEmpty() || !endDateStr.isEmpty()) {
            rfDate = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String timestamp = (String) entry.getValue(0); // column 0 is Timestamp
                    try {
                        LocalDate txDate = LocalDate.parse(timestamp.split(" ")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        if (!startDateStr.isEmpty()) {
                            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            if (txDate.isBefore(startDate)) {
                                return false;
                            }
                        }

                        if (!endDateStr.isEmpty()) {
                            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            if (txDate.isAfter(endDate)) {
                                return false;
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        return true; // If date parsing fails, include the row
                    }
                }
            };
        }

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        if (rfSymbol != null) filters.add(rfSymbol);
        if (rfType != null) filters.add(rfType);
        if (rfDate != null) filters.add(rfDate);

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

        List<Transaction> transactions = storageManager.loadTransactions(authManager.getCurrentUser().getUsername());
        tableModel.setRowCount(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                t.getTimestamp().format(formatter),
                t.getType().name(),
                t.getSymbol(),
                t.getQuantity(),
                String.format("₹%.2f", t.getPrice()),
                String.format("₹%.2f", t.getTotal())
            });
        }

        // Reset inputs
        searchField.setText("");
        filterCombo.setSelectedIndex(0);
        rowSorter.setRowFilter(null);
    }

    private void exportToCSV() {
        if (!authManager.isLoggedIn()) return;

        List<Transaction> transactions = storageManager.loadTransactions(authManager.getCurrentUser().getUsername());
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions to export.", "Export Failed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transactions to CSV");
        fileChooser.setSelectedFile(new File(authManager.getCurrentUser().getUsername() + "_transactions.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write CSV header
                writer.write("Timestamp,Type,Symbol,Quantity,Price,Total\n");

                // Write transaction data
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (Transaction t : transactions) {
                    writer.write(String.format("%s,%s,%s,%d,%.2f,%.2f\n",
                        t.getTimestamp().format(formatter),
                        t.getType().name(),
                        t.getSymbol(),
                        t.getQuantity(),
                        t.getPrice(),
                        t.getTotal()));
                }

                JOptionPane.showMessageDialog(this,
                    "Successfully exported " + transactions.size() + " transactions to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                NotificationManager.getInstance().showSuccess("CSV exported successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to export transactions: " + ex.getMessage(),
                    "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
