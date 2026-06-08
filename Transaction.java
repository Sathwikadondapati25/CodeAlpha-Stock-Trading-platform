import java.time.LocalDateTime;

public class Transaction {
    public enum Type {
        BUY, SELL
    }

    private final LocalDateTime timestamp;
    private final Type type;
    private final String symbol;
    private final int quantity;
    private final double price;
    private final double total;

    public Transaction(LocalDateTime timestamp, Type type, String symbol, int quantity, double price, double total) {
        this.timestamp = timestamp;
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getTotal() {
        return total;
    }
}
