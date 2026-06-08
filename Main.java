import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Setup FlatLaf Dark Theme
        try {
            FlatDarkLaf.setup();
            
            // Customize component design styles
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("Table.showHorizontalLines", true);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf. Using default Look & Feel.");
        }

        // Initialize Managers
        StorageManager storageManager = new StorageManager();
        StockManager stockManager = new StockManager();
        AuthManager authManager = new AuthManager(storageManager);
        TradingManager tradingManager = new TradingManager(storageManager);

        // Start real-time price fluctuation simulator
        stockManager.startSimulation();

        // Launch UI
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(authManager, stockManager, tradingManager, storageManager);
            frame.setVisible(true);
        });

        // Add shutdown hooks to stop simulator thread cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping background simulator thread...");
            stockManager.stopSimulation();
        }));
    }
}
