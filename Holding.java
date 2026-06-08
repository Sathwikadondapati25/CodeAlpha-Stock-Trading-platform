public class Holding {
    private final String symbol;
    private int quantity;
    private double averagePrice;

    public Holding(String symbol, int quantity, double averagePrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public double getInvestedValue() {
        return quantity * averagePrice;
    }

    public double getCurrentValue(double currentPrice) {
        return quantity * currentPrice;
    }

    public double getProfitLoss(double currentPrice) {
        return getCurrentValue(currentPrice) - getInvestedValue();
    }
}
