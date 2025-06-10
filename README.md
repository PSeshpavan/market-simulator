# Order Matching Engine Simulation

This project is a simple multi-threaded order matching engine simulation written in Java. It models a basic stock exchange where multiple traders submit buy and sell orders for various symbols, and a matching engine matches these orders to execute trades.

## Features

- **Order Matching Engine**: Matches buy and sell orders based on price and time priority.
- **Order Book**: Maintains active buy and sell orders for each symbol.
- **Traders**: Simulated traders generate random orders at configurable rates.
- **Trade and Market Data Listeners**: Receive notifications for executed trades and market data updates.
- **Thread-Safe**: Uses concurrent data structures and locks for safe multi-threaded operation.
- **Simulation**: Runs a configurable simulation with statistics output.

## Project Structure

- [`Order.java`](Order.java): Represents a buy or sell order.
- [`Trade.java`](Trade.java): Represents an executed trade.
- [`OrderBook.java`](OrderBook.java): Manages buy/sell orders and performs order matching for a symbol.
- [`OrderBookSnapshot.java`](OrderBookSnapshot.java): Immutable snapshot of the order book for market data.
- [`MatchingEngine.java`](MatchingEngine.java): Core engine that processes orders, matches them, and notifies listeners.
- [`Trader.java`](Trader.java): Simulates a trader submitting random orders.
- [`TradeListener.java`](TradeListener.java): Functional interface for trade event callbacks.
- [`MarketDataListener.java`](MarketDataListener.java): Functional interface for market data event callbacks.
- [`TradingSimulation.java`](TradingSimulation.java): Main class to run the simulation.

## How It Works

1. **Start the Simulation**: Run [`TradingSimulation.java`](TradingSimulation.java). It creates a `MatchingEngine`, registers listeners, and starts multiple `Trader` threads.
2. **Order Submission**: Each `Trader` submits random buy/sell orders for random symbol(stock) at a configurable rate.
3. **Order Matching**: The `MatchingEngine` processes orders, matches compatible buy/sell orders, and executes trades.
4. **Listeners**: Trade and market data listeners print trade executions and market data updates to the console.
5. **Statistics**: After the simulation, statistics such as total trades, volume, and order book states are displayed.

## Example Output

Executed 1 trades for symbol AMZN
MARKET DATA [MSFT]: Best Bid=100.49, Best Ask=102.61, Spread=2.12
MARKET DATA [GOOGL]: Best Bid=100.04, Best Ask=101.17, Spread=1.13
MARKET DATA [AAPL]: Best Bid=100.82, Best Ask=101.01, Spread=0.19
MARKET DATA [TSLA]: Best Bid=98.76, Best Ask=99.92, Spread=1.16
MARKET DATA [AMZN]: Best Bid=97.53, Best Ask=101.04, Spread=3.51
TRADE EXECUTED: Trade{id='f5fd6a02', symbol='AAPL', price=100.82, qty=36, buyer='TRADER02', seller='TRADER07'} (Total trades: 148, Total volume: 41051)
TRADE EXECUTED: Trade{id='811ae976', symbol='AAPL', price=100.75, qty=175, buyer='TRADER01', seller='TRADER07'} (Total trades: 149, Total volume: 41226)
Executed 2 trades for symbol AAPL
TRADE EXECUTED: Trade{id='199ccf4b', symbol='MSFT', price=100.49, qty=165, buyer='TRADER01', seller='TRADER10'} (Total trades: 150, Total volume: 41391)
TRADE EXECUTED: Trade{id='d99612bd', symbol='MSFT', price=100.10, qty=97, buyer='TRADER08', seller='TRADER10'} (Total trades: 151, Total volume: 41488)