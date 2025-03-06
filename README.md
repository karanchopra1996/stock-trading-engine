# Real-Time Stock Trading Engine

This project implements a **real-time stock trading engine** that matches buy and sell orders for 1,024 different tickers (stocks). The system is designed to handle concurrent order additions and matching while ensuring thread safety using lock-free data structures.

## Features

1. **Order Management**:
   - Supports adding buy and sell orders for 1,024 tickers.
   - Orders are stored in separate buy and sell order books for each ticker.

2. **Order Matching**:
   - Matches buy and sell orders based on price and quantity.
   - Executes trades when a buy price is greater than or equal to the lowest sell price.

3. **Thread Safety**:
   - Uses `AtomicReference` and `compareAndSet` for lock-free updates to the order books.
   - Ensures race conditions are handled correctly in a multi-threaded environment.

4. **Simulation**:
   - Includes a simulation thread that generates random buy and sell orders to test the system.

## How to Run

1. Open the project in VS Code.
2. Compile and run the code using the terminal:
   javac src/StockTradingEngine.java
   java -cp src StockTradingEngine