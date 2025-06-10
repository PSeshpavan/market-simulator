import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TradingSimulation {
    private static final AtomicInteger tradeCount = new AtomicInteger(0);
    private static final AtomicInteger totalVolume = new AtomicInteger(0);
    
    public static void main(String[] args) {
        System.out.println("Starting Order Matching Engine Simulation...");
        
        MatchingEngine engine = new MatchingEngine();
        
        // trade listener to track statistics
        engine.addTradeListener(trade -> {
            int trades = tradeCount.incrementAndGet();
            int volume = totalVolume.addAndGet(trade.getQuantity());
            
            System.out.printf("TRADE EXECUTED: %s (Total trades: %d, Total volume: %d)%n", 
                            trade, trades, volume);
        });
        
        // market data listener
        engine.addMarketDataListener(snapshot -> {
            if (!snapshot.getBids().isEmpty() || !snapshot.getAsks().isEmpty()) {
                System.out.printf("MARKET DATA [%s]: Best Bid=%.2f, Best Ask=%.2f, Spread=%.2f%n",
                                snapshot.getSymbol(), 
                                snapshot.getBestBidPrice(),
                                snapshot.getBestAskPrice(),
                                snapshot.getSpread());
            }
        });
        
        // Start the engine
        engine.start();
        
        // Create trading symbols
        String[] symbols = {"AAPL", "GOOGL", "MSFT", "TSLA", "AMZN"};
        
        // Create and start multiple traders
        List<Trader> traders = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Trader trader = new Trader("TRADER" + String.format("%02d", i), 
                                     engine, symbols, 2 + (i % 3)); // 2-4 orders per second
            traders.add(trader);
            trader.start();
        }
        
        // Run simulation
        try {
            // Let the simulation run for 30 seconds
            System.out.println("Simulation running for 30 seconds...");
            Thread.sleep(30000);
            
            // Stop all traders
            System.out.println("Stopping traders...");
            for (Trader trader : traders) {
                trader.stop();
            }
            
            // Wait a bit for final trades
            Thread.sleep(2000);
            
            // Print final statistics
            System.out.println("\n=== SIMULATION RESULTS ===");
            System.out.printf("Total Trades Executed: %d%n", tradeCount.get());
            System.out.printf("Total Volume Traded: %d shares%n", totalVolume.get());
            
            // Print order book sizes
            System.out.println("\nActive Orders by Symbol:");
            engine.getOrderBookSizes().forEach((symbol, count) -> 
                System.out.printf("  %s: %d orders%n", symbol, count));
            
            // Show final order book snapshots
            System.out.println("\nFinal Order Book States:");
            for (String symbol : symbols) {
                OrderBookSnapshot snapshot = engine.getOrderBookSnapshot(symbol);
                if (snapshot != null) {
                    System.out.printf("  %s: %d bids, %d asks, Best Bid=%.2f, Best Ask=%.2f%n",
                                    symbol, 
                                    snapshot.getBids().size(),
                                    snapshot.getAsks().size(),
                                    snapshot.getBestBidPrice(),
                                    snapshot.getBestAskPrice());
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Stop the engine
            engine.stop();
            System.out.println("\nSimulation completed!");
        }
    }
}