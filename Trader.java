import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Trader implements Runnable {
    private final String traderId;
    private final MatchingEngine engine;
    private final String[] symbols;
    private final Random random;
    private volatile boolean running;
    private final int ordersPerSecond;
    
    public Trader(String traderId, MatchingEngine engine, String[] symbols, int ordersPerSecond) {
        this.traderId = traderId;
        this.engine = engine;
        this.symbols = symbols;
        this.random = ThreadLocalRandom.current();
        this.running = false;
        this.ordersPerSecond = Math.max(1, ordersPerSecond);
    }
    
    public void start() {
        if (!running) {
            running = true;
            new Thread(this, "Trader-" + traderId).start();
        }
    }
    
    public void stop() {
        running = false;
    }
    
    @Override
    public void run() {
        System.out.println("Trader " + traderId + " started trading");
        
        while (running) {
            try {
                // Generate random order
                String symbol = symbols[random.nextInt(symbols.length)];
                Order.Side side = random.nextBoolean() ? Order.Side.BUY : Order.Side.SELL;
                
                // Price around $100 with some variance
                double basePrice = 100.0;
                double priceVariance = random.nextGaussian() * 2.0; 
                double price = Math.max(1.0, basePrice + priceVariance);
                price = Math.round(price * 100.0) / 100.0; 
                
                int quantity = 100 + random.nextInt(900);
                
                Order order = new Order(traderId, symbol, side, price, quantity);
                engine.submitOrder(order);
                
                // Wait before next order
                Thread.sleep(1000 / ordersPerSecond);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in trader " + traderId + ": " + e.getMessage());
            }
        }
        
        System.out.println("Trader " + traderId + " stopped trading");
    }
}