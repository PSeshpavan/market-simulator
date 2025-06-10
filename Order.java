import java.time.LocalDateTime;
import java.util.UUID;

public class Order implements Comparable<Order> {
    public enum Side {
        BUY, SELL
    }
    
    public enum Status {
        PENDING, PARTIALLY_FILLED, FILLED, CANCELLED
    }
    
    private final String orderId;
    private final String traderId;
    private final String symbol;
    private final Side side;
    private final double price;
    private final int originalQuantity;
    private int remainingQuantity;
    private final LocalDateTime timestamp;
    private Status status;
    
    public Order(String traderId, String symbol, Side side, double price, int quantity) {
        this.orderId = UUID.randomUUID().toString();
        this.traderId = traderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.status = Status.PENDING;
    }
    
    @Override
    public int compareTo(Order other) {
        // For buy orders: higher price has priority, than earlier timestamp
        // For sell orders: lower price has priority, than earlier timestamp
        if (this.side == Side.BUY) {
            int priceCompare = Double.compare(other.price, this.price);
            return priceCompare != 0 ? priceCompare : this.timestamp.compareTo(other.timestamp);
        } else {
            int priceCompare = Double.compare(this.price, other.price);
            return priceCompare != 0 ? priceCompare : this.timestamp.compareTo(other.timestamp);
        }
    }
    
    public void reduceQuantity(int quantity) {
        this.remainingQuantity -= quantity;
        if (this.remainingQuantity <= 0) {
            this.status = Status.FILLED;
        } else {
            this.status = Status.PARTIALLY_FILLED;
        }
    }
    
    public String getOrderId() { return orderId; }
    public String getTraderId() { return traderId; }
    public String getSymbol() { return symbol; }
    public Side getSide() { return side; }
    public double getPrice() { return price; }
    public int getOriginalQuantity() { return originalQuantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Status getStatus() { return status; }
    
    public void setStatus(Status status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', trader='%s', symbol='%s', side=%s, price=%.2f, qty=%d/%d, status=%s}",
                orderId.substring(0, 8), traderId, symbol, side, price, remainingQuantity, originalQuantity, status);
    }
}