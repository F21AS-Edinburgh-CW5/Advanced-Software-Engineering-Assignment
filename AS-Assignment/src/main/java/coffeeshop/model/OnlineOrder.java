package coffeeshop.model;

import coffeeshop.api.MenuItemView;
import java.util.List;

public class OnlineOrder extends CustomerOrder {
    private final int priority; // The smaller the number, the higher the priority.

    public OnlineOrder(String customerId, List<MenuItemView> items, int priority) {
        super(customerId, items);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}