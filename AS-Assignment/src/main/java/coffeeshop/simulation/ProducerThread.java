package coffeeshop.simulation;

import coffeeshop.api.MenuItemView;
import coffeeshop.loader.OrderLoader;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.MenuItem;
import coffeeshop.model.OrderRecord;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 生产者线程：从订单文件读取订单，按顾客分组构建 CustomerOrder，
 * 并以固定间隔将订单添加到共享队列中。
 */
public class ProducerThread extends Thread {
    private final SharedOrderQueue queue;
    private final Map<String, MenuItem> menuMap;   // 菜单映射（ID -> MenuItem）
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
        // 1. 读取订单原始记录（使用 load 方法，不是 loadOrders）
        OrderLoader loader = new OrderLoader();
        List<OrderRecord> records = loader.load(ordersFilePath);
        if (records == null || records.isEmpty()) {
            System.out.println("[PRODUCER] No orders found in file.");
            queue.markProducerDone();
            return;
        }

        // 2. 按顾客ID分组
        Map<String, List<OrderRecord>> grouped = new HashMap<>();
        for (OrderRecord rec : records) {
            grouped.computeIfAbsent(rec.getCustomerId(), k -> new ArrayList<>()).add(rec);
        }

        // 3. 为每个顾客构建 CustomerOrder（需要转换为 MenuItemView）
        List<CustomerOrder> orders = new ArrayList<>();
        for (Map.Entry<String, List<OrderRecord>> entry : grouped.entrySet()) {
            String customerId = entry.getKey();
            List<OrderRecord> recs = entry.getValue();

            // 将每个OrderRecord转换为 MenuItemView
            List<MenuItemView> items = new ArrayList<>();
            for (OrderRecord rec : recs) {
                String itemId = rec.getItemId();
                MenuItem mi = menuMap.get(itemId);
                if (mi == null) {
                    System.out.println("[PRODUCER] Unknown item ID: " + itemId + " – skipping for customer " + customerId);
                    continue;
                }
                // 转换为 MenuItemView
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

        // 4. 按时间戳排序（可选，这里简单按构造顺序）
        // 若需按时间，可解析 OrderRecord 的时间字符串并排序
        // orders.sort(Comparator.comparingLong(CustomerOrder::getTimestamp));

        // 5. 逐个添加到队列，间隔 intervalMs
        for (CustomerOrder order : orders) {
            try {
                TimeUnit.MILLISECONDS.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[PRODUCER] Interrupted while waiting.");
                break;
            }
            queue.add(order);
            System.out.println("[PRODUCER] Added order for " + order.getCustomerId());
        }

        // 6. 通知队列不再有更多订单
        queue.markProducerDone();
        System.out.println("[PRODUCER] Finished. No more orders.");
    }
}
