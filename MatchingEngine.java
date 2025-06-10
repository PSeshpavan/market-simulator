import java.util.*;
import java.util.concurrent.*;

public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks;
    private final ExecutorService matchingExecutor;
    private final ExecutorService marketDataExecutor;
    private final BlockingQueue<Order> orderQueue;
    private final List<TradeListener> tradeListeners;
    private final List<MarketDataListener> marketDataListeners;
    private volatile boolean running;
    
    public MatchingEngine() {
        this.orderBooks = new ConcurrentHashMap<>();
        this.matchingExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "MatchingEngine-Thread");
            t.setDaemon(true);
            return t;
        });
        this.marketDataExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "MarketData-Thread");
            t.setDaemon(true);
            return t;
        });
        this.orderQueue = new LinkedBlockingQueue<>();
        this.tradeListeners = new CopyOnWriteArrayList<>();
        this.marketDataListeners = new CopyOnWriteArrayList<>();
        this.running = false;
    }
    
    public void start() {
        if (running) return;
        running = true;
        
        // Start order processing thread
        matchingExecutor.submit(() -> {
            while (running) {
                try {
                    Order order = orderQueue.take();
                    processOrder(order);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error processing order: " + e.getMessage());
                }
            }
        });
        
        // Start market data distribution thread
        marketDataExecutor.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    distributeMarketData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error distributing market data: " + e.getMessage());
                }
            }
        });
        
        System.out.println("Matching Engine started");
    }
    
    public void stop() {
        running = false;
        matchingExecutor.shutdown();
        marketDataExecutor.shutdown();
        
        try {
            if (!matchingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                matchingExecutor.shutdownNow();
            }
            if (!marketDataExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                marketDataExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Matching Engine stopped");
    }
    
    public void submitOrder(Order order) {
        if (!running) {
            throw new IllegalStateException("Matching engine is not running");
        }
        
        orderBooks.computeIfAbsent(order.getSymbol(), OrderBook::new);
        
        try {
            orderQueue.put(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to submit order", e);
        }
    }
    
    private void processOrder(Order order) {
        OrderBook orderBook = orderBooks.get(order.getSymbol());
        if (orderBook == null) return;
        
        orderBook.addOrder(order);
        List<Trade> trades = orderBook.matchOrders();
        
        // Notify trade listeners
        for (Trade trade : trades) {
            notifyTradeListeners(trade);
        }
        
        if (!trades.isEmpty()) {
            System.out.printf("Executed %d trades for symbol %s%n", trades.size(), order.getSymbol());
        }
    }
    
    public boolean cancelOrder(String symbol, String orderId) {
        OrderBook orderBook = orderBooks.get(symbol);
        return orderBook != null && orderBook.cancelOrder(orderId);
    }
    
    public OrderBookSnapshot getOrderBookSnapshot(String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        return orderBook != null ? orderBook.getSnapshot() : null;
    }
    
    private void distributeMarketData() {
        for (OrderBook orderBook : orderBooks.values()) {
            OrderBookSnapshot snapshot = orderBook.getSnapshot();
            notifyMarketDataListeners(snapshot);
        }
    }
    
    private void notifyTradeListeners(Trade trade) {
        for (TradeListener listener : tradeListeners) {
            try {
                listener.onTrade(trade);
            } catch (Exception e) {
                System.err.println("Error notifying trade listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyMarketDataListeners(OrderBookSnapshot snapshot) {
        for (MarketDataListener listener : marketDataListeners) {
            try {
                listener.onMarketData(snapshot);
            } catch (Exception e) {
                System.err.println("Error notifying market data listener: " + e.getMessage());
            }
        }
    }
    
    public void addTradeListener(TradeListener listener) {
        tradeListeners.add(listener);
    }
    
    public void addMarketDataListener(MarketDataListener listener) {
        marketDataListeners.add(listener);
    }
    
    public Map<String, Integer> getOrderBookSizes() {
        Map<String, Integer> sizes = new HashMap<>();
        for (Map.Entry<String, OrderBook> entry : orderBooks.entrySet()) {
            sizes.put(entry.getKey(), entry.getValue().getActiveOrderCount());
        }
        return sizes;
    }
}
