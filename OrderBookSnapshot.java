import java.util.List;

public class OrderBookSnapshot {
    private final String symbol;
    private final List<Order> bids;
    private final List<Order> asks;
    
    public OrderBookSnapshot(String symbol, List<Order> bids, List<Order> asks) {
        this.symbol = symbol;
        this.bids = bids;
        this.asks = asks;
    }
    
    public String getSymbol() { return symbol; }
    public List<Order> getBids() { return bids; }
    public List<Order> getAsks() { return asks; }
    
    public double getBestBidPrice() {
        return bids.isEmpty() ? 0.0 : bids.get(0).getPrice();
    }
    
    public double getBestAskPrice() {
        return asks.isEmpty() ? 0.0 : asks.get(0).getPrice();
    }
    
    public double getSpread() {
        if (bids.isEmpty() || asks.isEmpty()) return 0.0;
        return getBestAskPrice() - getBestBidPrice();
    }
}