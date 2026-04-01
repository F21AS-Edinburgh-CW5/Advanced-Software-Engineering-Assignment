package coffeeshop.simulation;

import coffeeshop.api.MenuItemView;
import coffeeshop.logging.EventLogger;
import coffeeshop.model.MenuItem;
import coffeeshop.model.OnlineOrder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OnlineOrderProducerThread extends Thread {
    private final SharedOrderQueue queue;
    private final Map<String, MenuItem> menuMap;
    private final String onlineOrdersFilePath;
    private final long intervalMs;
    private final EventLogger logger = EventLogger.getInstance();

    public OnlineOrderProducerThread(SharedOrderQueue queue,
                                     Map<String, MenuItem> menuMap,
                                     String onlineOrdersFilePath,
                                     long intervalMs) {
        this.queue = queue;
        this.menuMap = menuMap;
        this.onlineOrdersFilePath = onlineOrdersFilePath;
        this.intervalMs = intervalMs;
    }

    @Override
    public void run() {
        logger.log("[OnlineProducer] Starting...");
        try (BufferedReader reader = new BufferedReader(new FileReader(onlineOrdersFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    logger.log("[OnlineProducer] Skipped malformed line: " + line);
                    continue;
                }
                // 格式：timestamp,customerId,itemId1,itemId2,...,priority
                // 时间戳可忽略，取顾客ID、商品ID列表（最后一个是优先级）
                String customerId = parts[1];
                int priority = Integer.parseInt(parts[parts.length - 1]);
                List<MenuItemView> items = new ArrayList<>();
                for (int i = 2; i < parts.length - 1; i++) {
                    String itemId = parts[i];
                    MenuItem mi = menuMap.get(itemId);
                    if (mi != null) {
                        items.add(new MenuItemView(mi.getId(), mi.getDescription(),
                                mi.getCategory(), mi.getCost()));
                    } else {
                        logger.log("[OnlineProducer] Unknown item ID: " + itemId);
                    }
                }
                if (!items.isEmpty()) {
                    OnlineOrder order = new OnlineOrder(customerId, items, priority);
                    queue.addOnlineOrder(order);
                    logger.log("[OnlineProducer] Added online order for " + customerId +
                            " priority " + priority + " (" + items.size() + " items)");
                } else {
                    logger.log("[OnlineProducer] No valid items for customer " + customerId);
                }
                // 控制生产速度，受全局速度滑块影响
                long adjusted = (long)(intervalMs / SimulationConfig.getSpeedMultiplier());
                TimeUnit.MILLISECONDS.sleep(adjusted);
            }
        } catch (IOException e) {
            logger.log("[OnlineProducer] Error reading file: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("[OnlineProducer] Interrupted.");
        }
        logger.log("[OnlineProducer] Finished.");
    }
}