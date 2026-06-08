import java.time.LocalDateTime;
import java.util.List;

public class TradingManager {
    private final StorageManager storageManager;

    public TradingManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public synchronized void buyStock(User user, Stock stock, int quantity) throws IllegalArgumentException {
        if (user == null) throw new IllegalArgumentException("User not authenticated.");
        if (stock == null) throw new IllegalArgumentException("Stock not found.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");

        double currentPrice = stock.getCurrentPrice();
        double totalCost = quantity * currentPrice;

        if (user.getBalance() < totalCost) {
            throw new IllegalArgumentException(String.format("Insufficient balance. Required: ₹%.2f, Available: ₹%.2f", totalCost, user.getBalance()));
        }

        // Deduct balance
        double newBalance = user.getBalance() - totalCost;
        user.setBalance(newBalance);
        storageManager.saveUserBalance(user.getUsername(), newBalance);

        // Update Portfolio
        List<Holding> holdings = storageManager.loadPortfolio(user.getUsername());
        Holding existing = null;
        for (Holding h : holdings) {
            if (h.getSymbol().equalsIgnoreCase(stock.getSymbol())) {
                existing = h;
                break;
            }
        }

        if (existing != null) {
            int oldQty = existing.getQuantity();
            double oldAvg = existing.getAveragePrice();
            int newQty = oldQty + quantity;
            double newAvg = ((oldQty * oldAvg) + (quantity * currentPrice)) / newQty;
            existing.setQuantity(newQty);
            existing.setAveragePrice(newAvg);
        } else {
            holdings.add(new Holding(stock.getSymbol(), quantity, currentPrice));
        }
        storageManager.savePortfolio(user.getUsername(), holdings);

        // Record Transaction
        Transaction trans = new Transaction(LocalDateTime.now(), Transaction.Type.BUY, stock.getSymbol(), quantity, currentPrice, totalCost);
        storageManager.saveTransaction(user.getUsername(), trans);
    }

    public synchronized void sellStock(User user, Stock stock, int quantity) throws IllegalArgumentException {
        if (user == null) throw new IllegalArgumentException("User not authenticated.");
        if (stock == null) throw new IllegalArgumentException("Stock not found.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");

        List<Holding> holdings = storageManager.loadPortfolio(user.getUsername());
        Holding existing = null;
        for (Holding h : holdings) {
            if (h.getSymbol().equalsIgnoreCase(stock.getSymbol())) {
                existing = h;
                break;
            }
        }

        if (existing == null || existing.getQuantity() < quantity) {
            int owned = existing != null ? existing.getQuantity() : 0;
            throw new IllegalArgumentException(String.format("Insufficient shares of %s. Required: %d, Owned: %d", stock.getSymbol(), quantity, owned));
        }

        double currentPrice = stock.getCurrentPrice();
        double totalGain = quantity * currentPrice;

        // Add to balance
        double newBalance = user.getBalance() + totalGain;
        user.setBalance(newBalance);
        storageManager.saveUserBalance(user.getUsername(), newBalance);

        // Update Portfolio
        existing.setQuantity(existing.getQuantity() - quantity);
        if (existing.getQuantity() == 0) {
            holdings.remove(existing);
        }
        storageManager.savePortfolio(user.getUsername(), holdings);

        // Record Transaction
        Transaction trans = new Transaction(LocalDateTime.now(), Transaction.Type.SELL, stock.getSymbol(), quantity, currentPrice, totalGain);
        storageManager.saveTransaction(user.getUsername(), trans);
    }
}
