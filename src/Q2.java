import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Q2 {

    private Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();
    private Map<String, Queue<Integer>> waitingList = new ConcurrentHashMap<>();

    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedList<>());
    }

    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return 0;
        return stock.get();
    }

    public String purchaseItem(String productId, int userId) {

        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = stock.get();

            if (currentStock <= 0) {
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Added to waiting list, position #" + queue.size();
            }

            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
        }
    }

    public int getWaitingListSize(String productId) {
        Queue<Integer> queue = waitingList.get(productId);
        return queue.size();
    }

    public static void main(String[] args) {

        Q2 system = new Q2();

        system.addProduct("IPHONE15_256GB", 100);

        System.out.println("Stock: " + system.checkStock("IPHONE15_256GB"));

        System.out.println(system.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(system.purchaseItem("IPHONE15_256GB", 67890));

        for (int i = 0; i < 100; i++) {
            system.purchaseItem("IPHONE15_256GB", 20000 + i);
        }

        System.out.println(system.purchaseItem("IPHONE15_256GB", 99999));
    }
}
