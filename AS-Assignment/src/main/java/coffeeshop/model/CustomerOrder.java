package coffeeshop.model;

import coffeeshop.service.ProcessingTimeService;
import coffeeshop.api.DiscountCalculator;
import coffeeshop.api.MenuItemView;

import java.util.Collections;
import java.util.List;

public class CustomerOrder {

    private final String customerId;
    private final List<MenuItemView> items;
    private final DiscountCalculator.DiscountResult discountResult;
    private final long processingTimeMs;

    public CustomerOrder(String customerId, List<MenuItemView> items) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.customerId = customerId.trim();
        this.items = Collections.unmodifiableList(items);
        this.discountResult = new DiscountCalculator().calculateDiscount(items);
        this.processingTimeMs = new ProcessingTimeService().getProcessingTimeMs(this);
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<MenuItemView> getItems() {
        return items;
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(MenuItemView::getPrice).sum();
    }

    public double getDiscountAmount() {
        return discountResult.getDiscountAmount();
    }

    public String getDiscountRule() {
        return discountResult.getRuleApplied();
    }

    public double getTotalPrice() {
        return getSubtotal() - getDiscountAmount();
    }

    public int getItemCount() {
        return items.size();
    }


    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    @Override
    public String toString() {
        return "CustomerOrder{customerId='" + customerId + "', items=" + items.size()
                + ", total=£" + String.format("%.2f", getTotalPrice()) + "}";
    }
}