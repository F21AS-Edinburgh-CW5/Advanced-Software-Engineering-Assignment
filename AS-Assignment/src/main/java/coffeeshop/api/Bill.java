package coffeeshop.api;

import java.util.Collections;
import java.util.List;

public class Bill {
    private final List<LineItem> items;

    //before discount
    private final double subtotal;

    //discount amount (>=0)
    private final double discount;

    //after discount
    private final double total;

    // optional: describe which discount rule applied (for UI display)
    private final String discountRuleApplied;

    public Bill(List<LineItem> items,
                double subtotal,
                double discount,
                double total,
                String discountRuleApplied) {

       this.items = (items == null) ? Collections.emptyList(): List.copyOf(items);
       this.subtotal = subtotal;
       this.discount = discount;
       this.total = total;
       this.discountRuleApplied = discountRuleApplied ;
    }

    public List<LineItem>getItems() { return items;}
    public double getSubtotal() { return subtotal;}
    public double getDiscount() { return discount;}
    public double getTotal() { return total;}
    public String getDiscountRuleApplied() { return discountRuleApplied; }

    //a single line on the bill, suitable for GUI display
    public static class LineItem {
        private final String id;
        private final String description;
        private final double price;

        public LineItem(String id, String description, double price) {
            this.id = id;
            this.description = description;
            this.price = price;
        }
        public String getId() { return id; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
    }

}
