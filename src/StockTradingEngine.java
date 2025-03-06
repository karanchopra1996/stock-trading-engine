import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a single order in the stock trading system.
 */
class Order {
    String orderType; // Type of order: "Buy" or "Sell"
    int tickerSymbol; // Identifier for the stock (0 to 1023)
    int quantity;     // Number of shares to buy/sell
    int price;        // Price per share

    // Constructor to initialize an order
    Order(String orderType, int tickerSymbol, int quantity, int price) {
        this.orderType = orderType;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.price = price;
    }
}

/**
 * Represents the order book for a single ticker (stock).
 * Manages buy and sell orders and provides functionality to match them.
 */
class OrderBook {
    // Atomic references to store buy and sell orders
    private final AtomicReference<List<Order>> buyOrders = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<List<Order>> sellOrders = new AtomicReference<>(new ArrayList<>());

    /**
     * Adds a buy order to the order book in a thread-safe manner.
     * Uses CAS (Compare-And-Swap) to ensure lock-free updates.
     *
     * @param order The buy order to add.
     */
    public void addBuyOrder(Order order) {
        while (true) {
            List<Order> currentBuyOrders = buyOrders.get(); // Get current buy orders
            List<Order> newBuyOrders = new ArrayList<>(currentBuyOrders); // Create a copy
            newBuyOrders.add(order); // Add the new order
            // Sort buy orders by price in descending order (highest first)
            newBuyOrders.sort((o1, o2) -> Integer.compare(o2.price, o1.price));
            // Attempt to update the order book atomically
            if (buyOrders.compareAndSet(currentBuyOrders, newBuyOrders)) {
                break; // Exit loop if CAS succeeds
            }
            // If CAS fails, retry with the latest state
        }
    }

    /**
     * Adds a sell order to the order book in a thread-safe manner.
     * Uses CAS (Compare-And-Swap) to ensure lock-free updates.
     *
     * @param order The sell order to add.
     */
    public void addSellOrder(Order order) {
        while (true) {
            List<Order> currentSellOrders = sellOrders.get(); // Get current sell orders
            List<Order> newSellOrders = new ArrayList<>(currentSellOrders); // Create a copy
            newSellOrders.add(order); // Add the new order
            // Sort sell orders by price in ascending order (lowest first)
            newSellOrders.sort((o1, o2) -> Integer.compare(o1.price, o2.price));
            // Attempt to update the order book atomically
            if (sellOrders.compareAndSet(currentSellOrders, newSellOrders)) {
                break; // Exit loop if CAS succeeds
            }
            // If CAS fails, retry with the latest state
        }
    }

    /**
     * Matches buy and sell orders based on price and quantity.
     * Executes trades when a buy price is greater than or equal to a sell price.
     */
    public void matchOrders() {
        while (true) {
            List<Order> currentBuyOrders = buyOrders.get(); // Get current buy orders
            List<Order> currentSellOrders = sellOrders.get(); // Get current sell orders

            // If either list is empty, no matching is possible
            if (currentBuyOrders.isEmpty() || currentSellOrders.isEmpty()) {
                break;
            }

            // Get the best buy and sell orders (first in the sorted lists)
            Order bestBuy = currentBuyOrders.get(0);
            Order bestSell = currentSellOrders.get(0);

            // Check if the buy price is greater than or equal to the sell price
            if (bestBuy.price >= bestSell.price) {
                // Determine the matched quantity (minimum of the two)
                int matchedQuantity = Math.min(bestBuy.quantity, bestSell.quantity);
                System.out.println("Matched: Buy " + bestBuy.price + " @ " + matchedQuantity +
                        " with Sell " + bestSell.price + " @ " + matchedQuantity);

                // Create new lists to update the order book
                List<Order> newBuyOrders = new ArrayList<>(currentBuyOrders);
                List<Order> newSellOrders = new ArrayList<>(currentSellOrders);

                // Update or remove the buy order
                if (bestBuy.quantity == matchedQuantity) {
                    newBuyOrders.remove(0); // Remove if fully matched
                } else {
                    // Reduce the quantity if partially matched
                    newBuyOrders.set(0, new Order(bestBuy.orderType, bestBuy.tickerSymbol, bestBuy.quantity - matchedQuantity, bestBuy.price));
                }

                // Update or remove the sell order
                if (bestSell.quantity == matchedQuantity) {
                    newSellOrders.remove(0); // Remove if fully matched
                } else {
                    // Reduce the quantity if partially matched
                    newSellOrders.set(0, new Order(bestSell.orderType, bestSell.tickerSymbol, bestSell.quantity - matchedQuantity, bestSell.price));
                }

                // Attempt to update the order books atomically
                if (buyOrders.compareAndSet(currentBuyOrders, newBuyOrders) &&
                        sellOrders.compareAndSet(currentSellOrders, newSellOrders)) {
                    // Successfully updated order books
                } else {
                    // Retry if CAS fails
                    continue;
                }
            } else {
                // No more matches possible
                break;
            }
        }
    }
}

/**
 * Main class for the stock trading engine.
 * Simulates a real-time stock trading system with multiple tickers.
 */
public class StockTradingEngine {
    private static final int NUM_TICKERS = 1024; // Number of tickers (stocks)
    private static final OrderBook[] orderBooks = new OrderBook[NUM_TICKERS]; // Order books for each ticker

    // Initialize order books for all tickers
    static {
        for (int i = 0; i < NUM_TICKERS; i++) {
            orderBooks[i] = new OrderBook();
        }
    }

    /**
     * Adds an order to the appropriate order book.
     *
     * @param orderType    Type of order: "Buy" or "Sell"
     * @param tickerSymbol Identifier for the stock (0 to 1023)
     * @param quantity     Number of shares to buy/sell
     * @param price        Price per share
     */
    public static void addOrder(String orderType, int tickerSymbol, int quantity, int price) {
        // Validate the ticker symbol
        if (tickerSymbol < 0 || tickerSymbol >= NUM_TICKERS) {
            System.out.println("Invalid ticker symbol");
            return;
        }

        // Create a new order
        Order order = new Order(orderType, tickerSymbol, quantity, price);

        // Add the order to the appropriate order book
        if (orderType.equals("Buy")) {
            orderBooks[tickerSymbol].addBuyOrder(order);
        } else if (orderType.equals("Sell")) {
            orderBooks[tickerSymbol].addSellOrder(order);
        } else {
            System.out.println("Invalid order type");
            return;
        }

        // Attempt to match orders after adding a new one
        orderBooks[tickerSymbol].matchOrders();
    }

    /**
     * Simulates random buy and sell orders for different tickers.
     * Runs in a separate thread to simulate real-time trading.
     */
    public static void simulateOrders() {
        Random random = new Random();
        while (true) {
            // Generate random order parameters
            String orderType = random.nextBoolean() ? "Buy" : "Sell";
            int tickerSymbol = random.nextInt(NUM_TICKERS);
            int quantity = random.nextInt(100) + 1;
            int price = random.nextInt(1000) + 1;

            // Add the order to the system
            addOrder(orderType, tickerSymbol, quantity, price);

            // Simulate a delay between orders
            try {
                Thread.sleep(100); // 100ms delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Main method to start the stock trading engine.
     */
    public static void main(String[] args) {
        // Start a thread to simulate orders
        Thread simulationThread = new Thread(StockTradingEngine::simulateOrders);
        simulationThread.start();

        // Keep the main thread alive
        while (true) {
            try {
                Thread.sleep(1000); // Sleep for 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}