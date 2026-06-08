import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Stock {
    private final String symbol;
    private final String name;
    private double currentPrice;
    private final double openPrice;
    private final List<Double> priceHistory;
    private final Map<String, List<Double>> timeframeHistory;
    private static final int MAX_HISTORY = 40;

    public Stock(String symbol, String name, double initialPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = initialPrice;
        this.openPrice = initialPrice;
        this.priceHistory = new ArrayList<>();
        this.timeframeHistory = new HashMap<>();

        // Seed initial history for all timeframes
        for (int i = 0; i < 30; i++) {
            priceHistory.add(initialPrice);
        }

        // Initialize timeframe histories
        timeframeHistory.put("1D", new ArrayList<>(priceHistory));
        timeframeHistory.put("1W", generateWeeklyHistory(initialPrice));
        timeframeHistory.put("1M", generateMonthlyHistory(initialPrice));
        timeframeHistory.put("1Y", generateYearlyHistory(initialPrice));
    }

    private List<Double> generateWeeklyHistory(double initialPrice) {
        List<Double> history = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            history.add(initialPrice * (0.95 + Math.random() * 0.1));
        }
        return history;
    }

    private List<Double> generateMonthlyHistory(double initialPrice) {
        List<Double> history = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            history.add(initialPrice * (0.85 + Math.random() * 0.3));
        }
        return history;
    }

    private List<Double> generateYearlyHistory(double initialPrice) {
        List<Double> history = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            history.add(initialPrice * (0.7 + Math.random() * 0.6));
        }
        return history;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    public synchronized void updatePrice(double newPrice) {
        this.currentPrice = newPrice;
        priceHistory.add(newPrice);
        if (priceHistory.size() > MAX_HISTORY) {
            priceHistory.remove(0);
        }
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public synchronized double getChangeAmount() {
        return currentPrice - openPrice;
    }

    public synchronized double getChangePercentage() {
        return (getChangeAmount() / openPrice) * 100.0;
    }

    public synchronized List<Double> getPriceHistory() {
        return new ArrayList<>(priceHistory);
    }

    public synchronized List<Double> getPriceHistory(String timeframe) {
        if (timeframeHistory.containsKey(timeframe)) {
            return new ArrayList<>(timeframeHistory.get(timeframe));
        }
        return new ArrayList<>(priceHistory);
    }

    public synchronized void updateTimeframeHistory(String timeframe, double newPrice) {
        if (timeframeHistory.containsKey(timeframe)) {
            List<Double> history = timeframeHistory.get(timeframe);
            history.add(newPrice);
            if (history.size() > getMaxHistoryForTimeframe(timeframe)) {
                history.remove(0);
            }
        }
    }

    private int getMaxHistoryForTimeframe(String timeframe) {
        switch (timeframe) {
            case "1D": return 40;
            case "1W": return 7;
            case "1M": return 30;
            case "1Y": return 52;
            default: return 40;
        }
    }
}
