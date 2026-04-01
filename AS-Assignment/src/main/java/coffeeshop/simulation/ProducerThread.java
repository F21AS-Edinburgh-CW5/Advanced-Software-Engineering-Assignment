package coffeeshop.simulation;

import coffeeshop.api.MenuItemView;
import coffeeshop.loader.OrderLoader;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.MenuItem;
import coffeeshop.model.OrderRecord;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProducerThread extends Thread {
    private final SharedOrderQueue queue;
    private final Map<String, MenuItem> menuMap;
    private final String ordersFilePath;
    private final long intervalMs;

    public ProducerThread(SharedOrderQueue queue,
                          Map<String, MenuItem> menuMap,
                          String ordersFilePath,
                          long intervalMs) {
        this.queue = queue;
        this.menuMap = menuMap;
        this.ordersFilePath = ordersFilePath;
        this.intervalMs = intervalMs;
    }

    @Override
    public void run() {
        OrderLoader loader = new OrderLoader();
        List<OrderRecord> records = loader.load(ordersFilePath);
        if (records == null || records.isEmpty()) {
            System.out.println("[PRODUCER] No orders found in file.");
            queue.markProducerDone();
            return;
        }

        Map<String, List<OrderRecord>> grouped = new HashMap<>();
        for (OrderRecord rec : records) {
            grouped.computeIfAbsent(rec.getCustomerId(), k -> new ArrayList<>()).add(rec);
        }

        List<CustomerOrder> orders = new ArrayList<>();
        for (Map.Entry<String, List<OrderRecord>> entry : grouped.entrySet()) {
            String customerId = entry.getKey();
            List<OrderRecord> recs = entry.getValue();

            List<MenuItemView> items = new ArrayList<>();
            for (OrderRecord rec : recs) {
                String itemId = rec.getItemId();
                MenuItem mi = menuMap.get(itemId);
                if (mi == null) {
                    System.out.println("[PRODUCER] Unknown item ID: " + itemId + " – skipping for customer " + customerId);
                    continue;
                }
                items.add(new MenuItemView(
                    mi.getId(),
                    mi.getDescription(),
                    mi.getCategory(),
                    mi.getCost()
                ));
            }
            if (!items.isEmpty()) {
                CustomerOrder order = new CustomerOrder(customerId, items);
                orders.add(order);
                System.out.println("[PRODUCER] Created order for " + customerId + " with " + items.size() + " items.");
            } else {
                System.out.println("[PRODUCER] No valid items for customer " + customerId + " – order skipped.");
            }
        }

        for (CustomerOrder order : orders) {
            try {
                long adjusted = (long)(intervalMs / SimulationConfig.getSpeedMultiplier());
                TimeUnit.MILLISECONDS.sleep(adjusted);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[PRODUCER] Interrupted while waiting.");
                break;
            }
            queue.add(order);
            System.out.println("[PRODUCER] Added order for " + order.getCustomerId());
        }

        queue.markProducerDone();
        System.out.println("[PRODUCER] Finished. No more orders.");
    }
}
