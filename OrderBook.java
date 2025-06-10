import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrderBook {
    private final String symbol;
    private final PriorityQueue<Order> buyOrders; // Bids - highest price first
    private final PriorityQueue<Order> sellOrders; // Asks - lowest price first
    private final Map<String, Order> activeOrders;
    private final ReadWriteLock lock;
    
    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyOrders = new PriorityQueue<>();
        this.sellOrders = new PriorityQueue<>();
        this.activeOrders = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }
    
    public void addOrder(Order order) {
        lock.writeLock().lock();
        try {
            if (order.getSide() == Order.Side.BUY) {
                buyOrders.offer(order);
            } else {
                sellOrders.offer(order);
            }
            activeOrders.put(order.getOrderId(), order);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean cancelOrder(String orderId) {
        lock.writeLock().lock();
        try {
            Order order = activeOrders.get(orderId);
            if (order != null && order.getStatus() == Order.Status.PENDING) {
                order.setStatus(Order.Status.CANCELLED);
                activeOrders.remove(orderId);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public List<Trade> matchOrders() {
        lock.writeLock().lock();
        try {
            List<Trade> trades = new ArrayList<>();
            
            while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
                Order bestBuy = buyOrders.peek();
                Order bestSell = sellOrders.peek();
                
                if (bestBuy.getStatus() == Order.Status.CANCELLED) {
                    buyOrders.poll();
                    continue;
                }
                if (bestSell.getStatus() == Order.Status.CANCELLED) {
                    sellOrders.poll();
                    continue;
                }
                
                if (bestBuy.getPrice() >= bestSell.getPrice()) {

                    // Execute trade at the price of the earlier order
                    double tradePrice = bestBuy.getTimestamp().isBefore(bestSell.getTimestamp()) 
                                      ? bestBuy.getPrice() : bestSell.getPrice();
                    
                    int tradeQuantity = Math.min(bestBuy.getRemainingQuantity(), 
                                               bestSell.getRemainingQuantity());
                    
                    Trade trade = new Trade(
                        bestBuy.getOrderId(),
                        bestSell.getOrderId(),
                        bestBuy.getTraderId(),
                        bestSell.getTraderId(),
                        symbol,
                        tradePrice,
                        tradeQuantity
                    );
                    trades.add(trade);
                    
                    // Update order quantities
                    bestBuy.reduceQuantity(tradeQuantity);
                    bestSell.reduceQuantity(tradeQuantity);
                    
                    // Remove filled orders
                    if (bestBuy.getRemainingQuantity() == 0) {
                        buyOrders.poll();
                        activeOrders.remove(bestBuy.getOrderId());
                    }
                    if (bestSell.getRemainingQuantity() == 0) {
                        sellOrders.poll();
                        activeOrders.remove(bestSell.getOrderId());
                    }
                } else {
                    break;
                }
            }
            
            return trades;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public OrderBookSnapshot getSnapshot() {
        lock.readLock().lock();
        try {
            List<Order> bids = new ArrayList<>();
            List<Order> asks = new ArrayList<>();
            
            // Create copies to avoid concurrent modification
            for (Order order : buyOrders) {
                if (order.getStatus() != Order.Status.CANCELLED) {
                    bids.add(order);
                }
            }
            for (Order order : sellOrders) {
                if (order.getStatus() != Order.Status.CANCELLED) {
                    asks.add(order);
                }
            }
            
            return new OrderBookSnapshot(symbol, bids, asks);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public int getActiveOrderCount() {
        lock.readLock().lock();
        try {
            return activeOrders.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
