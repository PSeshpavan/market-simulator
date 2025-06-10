import java.time.LocalDateTime;
import java.util.UUID;

public class Trade {
    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String buyTraderId;
    private final String sellTraderId;
    private final String symbol;
    private final double price;
    private final int quantity;
    private final LocalDateTime timestamp;
    
    public Trade(String buyOrderId, String sellOrderId, String buyTraderId, 
                 String sellTraderId, String symbol, double price, int quantity) {
        this.tradeId = UUID.randomUUID().toString();
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyTraderId = buyTraderId;
        this.sellTraderId = sellTraderId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }
    

    public String getTradeId() { return tradeId; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public String getBuyTraderId() { return buyTraderId; }
    public String getSellTraderId() { return sellTraderId; }
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Trade{id='%s', symbol='%s', price=%.2f, qty=%d, buyer='%s', seller='%s'}",
                tradeId.substring(0, 8), symbol, price, quantity, buyTraderId, sellTraderId);
    }
}