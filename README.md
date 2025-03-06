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

## Code Structure
The project consists of the following classes:

**Order**: Represents a single order with attributes like orderType, ticker symbol, quantity, and price.

**OrderBook**: Manages to buy and sell orders for a single ticker and provides methods to add orders and match them.

**StockTradingEngine**: The main class initializes the system and starts the simulation thread.

## Design Decisions
**Lock-Free Data Structures**:
- The system uses AtomicReference and compareAndSet to ensure thread safety without locks.
- This approach minimizes contention and improves performance in a multi-threaded environment.

**Order Matching Logic**:
- Buy orders are sorted in descending order of price.
- Sell orders are sorted in ascending order of price.
- Orders are matched when the best buy price is greater than or equal to the best sell price.

**Simulation**:
- A separate thread generates random buy and sell orders to simulate real-time trading activity.

## Limitations
**Scalability**:
- The current implementation uses lists for storing orders, which may not scale well for extremely high volumes of orders.
- In a production environment, a more efficient data structure (e.g., a priority queue) could be used.

**Persistence**:
- The system does not persist orders or trades. In a real-world scenario, a database would be needed to store this dat.

**Advanced Features**:
- The system does not support advanced features like market orders, stop orders, or order cancellation.

## Sample Output 

<img width="350" alt="image" src="https://github.com/user-attachments/assets/729df16f-3af4-4db2-81f8-9fdff575b421" />

## How to Run

1. Clone the project
2. Open the project in IDE
3. Compile and run the code using the terminal:
   - javac src/StockTradingEngine.java
   - java -cp src StockTradingEngine
