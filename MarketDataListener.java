@FunctionalInterface
public interface MarketDataListener {
    void onMarketData(OrderBookSnapshot snapshot);
}