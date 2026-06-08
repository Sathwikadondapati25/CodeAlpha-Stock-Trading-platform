import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class GoalsScreen extends JPanel {
    private final StorageManager storageManager;
    private final StockManager stockManager;
    private final AuthManager authManager;

    private JTable goalsTable;
    private DefaultTableModel goalsModel;
    private JTextField goalNameField;
    private JTextField targetAmountField;
    private JTextField targetDateField;

    public GoalsScreen(StorageManager storageManager, StockManager stockManager, AuthManager authManager) {
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

        JLabel titleLabel = new JLabel("Investment Goals Tracker");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.getTextPrimary());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // --- ADD GOAL PANEL ---
        JPanel addPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        addPanel.setOpaque(false);
        addPanel.setBackground(Theme.getCardBackground());
        addPanel.setBorder(Theme.createCardBorder());
        addPanel.setPreferredSize(new Dimension(0, 80));

        goalNameField = new JTextField("Goal Name");
        goalNameField.setBackground(Theme.getBackground());
        goalNameField.setForeground(Theme.TEXT_PRIMARY);
        goalNameField.setCaretColor(Theme.TEXT_PRIMARY);
        goalNameField.setFont(Theme.FONT_BODY);
        goalNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        addPanel.add(goalNameField);

        targetAmountField = new JTextField("Target Amount (₹)");
        targetAmountField.setBackground(Theme.getBackground());
        targetAmountField.setForeground(Theme.TEXT_PRIMARY);
        targetAmountField.setCaretColor(Theme.TEXT_PRIMARY);
        targetAmountField.setFont(Theme.FONT_BODY);
        targetAmountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        addPanel.add(targetAmountField);

        targetDateField = new JTextField("Target Date (YYYY-MM-DD)");
        targetDateField.setBackground(Theme.getBackground());
        targetDateField.setForeground(Theme.TEXT_PRIMARY);
        targetDateField.setCaretColor(Theme.TEXT_PRIMARY);
        targetDateField.setFont(Theme.FONT_BODY);
        targetDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        addPanel.add(targetDateField);

        JButton btnAddGoal = new JButton("Add Goal");
        btnAddGoal.setFont(Theme.FONT_BODY_BOLD);
        btnAddGoal.setBackground(Theme.ACCENT);
        btnAddGoal.setForeground(Color.WHITE);
        btnAddGoal.setFocusPainted(false);
        btnAddGoal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddGoal.addActionListener(e -> addGoal());
        addPanel.add(btnAddGoal);

        add(addPanel, BorderLayout.NORTH);

        // --- GOALS TABLE ---
        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Theme.getCardBackground());
        tablePanel.setBorder(Theme.createCardBorder());

        String[] columns = {"Goal Name", "Target Amount", "Current Value", "Progress", "Target Date", "Status", "Action"};
        goalsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        goalsTable = new JTable(goalsModel);
        styleTable(goalsTable);

        JScrollPane scrollPane = new JScrollPane(goalsTable);
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
        table.setRowHeight(40);
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

        loadGoals();
    }

    private void loadGoals() {
        goalsModel.setRowCount(0);

        String username = authManager.getCurrentUser().getUsername();
        List<Holding> holdings = storageManager.loadPortfolio(username);
        double totalPortfolioValue = 0.0;

        for (Holding h : holdings) {
            Stock s = stockManager.getStock(h.getSymbol());
            if (s != null) {
                totalPortfolioValue += h.getCurrentValue(s.getCurrentPrice());
            }
        }

        // Load goals from storage
        Properties props = storageManager.loadUserProfile(username);
        int goalCount = 1;
        while (props.containsKey("goal" + goalCount + "_name")) {
            String name = props.getProperty("goal" + goalCount + "_name");
            double targetAmount = Double.parseDouble(props.getProperty("goal" + goalCount + "_target", "0"));
            String targetDate = props.getProperty("goal" + goalCount + "_date", "");

            double progress = (totalPortfolioValue / targetAmount) * 100;
            String status = progress >= 100 ? "Achieved" : (progress >= 50 ? "On Track" : "Behind");
            String progressStr = String.format("%.1f%%", progress);

            goalsModel.addRow(new Object[]{
                name,
                String.format("₹%,.2f", targetAmount),
                String.format("₹%,.2f", totalPortfolioValue),
                progressStr,
                targetDate,
                status,
                "Delete"
            });

            goalCount++;
        }
    }

    private void addGoal() {
        String name = goalNameField.getText().trim();
        String targetStr = targetAmountField.getText().trim();
        String date = targetDateField.getText().trim();

        if (name.isEmpty() || targetStr.isEmpty()) {
            NotificationManager.getInstance().showError("Please fill in goal name and target amount");
            return;
        }

        try {
            double targetAmount = Double.parseDouble(targetStr.replace("₹", "").replace(",", "").trim());

            String username = authManager.getCurrentUser().getUsername();
            Properties props = storageManager.loadUserProfile(username);

            int goalCount = 1;
            while (props.containsKey("goal" + goalCount + "_name")) {
                goalCount++;
            }

            props.setProperty("goal" + goalCount + "_name", name);
            props.setProperty("goal" + goalCount + "_target", String.valueOf(targetAmount));
            props.setProperty("goal" + goalCount + "_date", date);

            storageManager.saveUserProfile(username, props);

            NotificationManager.getInstance().showSuccess("Goal added successfully");

            goalNameField.setText("");
            targetAmountField.setText("");
            targetDateField.setText("");

            loadGoals();
        } catch (NumberFormatException e) {
            NotificationManager.getInstance().showError("Invalid target amount");
        }
    }
}
