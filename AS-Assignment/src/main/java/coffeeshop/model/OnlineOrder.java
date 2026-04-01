package coffeeshop.model;

import coffeeshop.api.MenuItemView;
import java.util.List;

public class OnlineOrder extends CustomerOrder {
    private final int priority; // 数字越小优先级越高

    public OnlineOrder(String customerId, List<MenuItemView> items, int priority) {
        super(customerId, items);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}