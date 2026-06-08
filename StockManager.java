import java.util.*;

public class StockManager {
    private final Map<String, Stock> stocks;
    private final List<StockUpdateListener> listeners;
    private Thread simulationThread;
    private boolean running = false;
    private final Random random = new Random();

    public interface StockUpdateListener {
        void onStocksUpdated(Collection<Stock> updatedStocks);
    }

    public StockManager() {
        stocks = new LinkedHashMap<>();
        listeners = new ArrayList<>();
        
        // Initialize 5 predefined stocks as requested
        stocks.put("TCS", new Stock("TCS", "Tata Consultancy Services", 3850.00));
        stocks.put("Infosys", new Stock("Infosys", "Infosys Limited", 1480.00));
        stocks.put("Wipro", new Stock("Wipro", "Wipro Limited", 490.00));
        stocks.put("Reliance", new Stock("Reliance", "Reliance Industries Ltd.", 2920.00));
        stocks.put("HDFC", new Stock("HDFC", "HDFC Bank Limited", 1630.00));
    }

    public synchronized void addListener(StockUpdateListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(StockUpdateListener listener) {
        listeners.remove(listener);
    }

    public Collection<Stock> getAllStocks() {
        return stocks.values();
    }

    public Stock getStock(String symbol) {
        if (symbol == null) return null;
        // Case insensitive lookup
        for (String key : stocks.keySet()) {
            if (key.equalsIgnoreCase(symbol)) {
                return stocks.get(key);
            }
        }
        return null;
    }

    public synchronized void startSimulation() {
        if (running) return;
        running = true;
        simulationThread = new Thread(this::runSimulation, "Stock-Simulation-Thread");
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    public synchronized void stopSimulation() {
        running = false;
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
    }

    private void runSimulation() {
        while (running) {
            try {
                Thread.sleep(2000);
                
                // Fluctuate each stock price by a small percentage (-1.2% to +1.2%)
                for (Stock stock : stocks.values()) {
                    double current = stock.getCurrentPrice();
                    double percentChange = (random.nextDouble() * 2.4 - 1.2) / 100.0;
                    double newPrice = current * (1.0 + percentChange);
                    
                    // Keep price realistic (at least 5 rupees)
                    if (newPrice < 5.0) {
                        newPrice = 5.0;
                    }
                    stock.updatePrice(newPrice);
                }
                
                // Notify listeners
                notifyListeners();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void notifyListeners() {
        List<StockUpdateListener> targets;
        synchronized (this) {
            targets = new ArrayList<>(listeners);
        }
        for (StockUpdateListener listener : targets) {
            listener.onStocksUpdated(stocks.values());
        }
    }
}
