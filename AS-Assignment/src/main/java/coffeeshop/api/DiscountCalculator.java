package coffeeshop.api;

import coffeeshop.api.MenuItemView;

import java.util.List;

public class DiscountCalculator {

    private static final double RULE_1_RATE = 0.20; // 1 beverage + 2 food
    private static final double RULE_2_RATE = 0.15; // 3+ beverages
    private static final double RULE_3_RATE = 0.10; // subtotal > 25

    public static class DiscountResult {
        private final double discountAmount;
        private final String ruleApplied;

        public DiscountResult(double discountAmount, String ruleApplied) {
            if (discountAmount < 0) {
                throw new IllegalArgumentException("Discount amount cannot be negative.");
            }
            this.discountAmount = roundToTwoDecimals(discountAmount);
            this.ruleApplied = (ruleApplied == null || ruleApplied.isBlank())
                    ? "No discount"
                    : ruleApplied;
        }

        public double getDiscountAmount() {
            return discountAmount;
        }

        public String getRuleApplied() {
            return ruleApplied;
        }
    }

    public DiscountResult calculateDiscount(List<MenuItemView> items) {
        if (items == null || items.isEmpty()) {
            return noDiscount();
        }

        int beverageCount = 0;
        int foodCount = 0;
        double subtotal = 0.0;

        for (MenuItemView item : items) {
            if (item == null) {
                continue; // ignore null entries safely
            }

            double price = item.getPrice();
            if (price < 0) {
                throw new IllegalArgumentException(
                        "Item price cannot be negative: " + item.getId()
                );
            }

            subtotal += price;

            if (isBeverage(item)) {
                beverageCount++;
            }
            if (isFood(item)) {
                foodCount++;
            }
        }

        subtotal = roundToTwoDecimals(subtotal);

        // Priority: Rule 1 > Rule 2 > Rule 3
        if (beverageCount >= 1 && foodCount >= 2) {
            return new DiscountResult(
                    subtotal * RULE_1_RATE,
                    "20% off: 1 beverage + 2 food items"
            );
        }

        if (beverageCount >= 3) {
            return new DiscountResult(
                    subtotal * RULE_2_RATE,
                    "15% off: 3 or more beverages"
            );
        }

        if (subtotal > 25.0) {
            return new DiscountResult(
                    subtotal * RULE_3_RATE,
                    "10% off: subtotal over £25"
            );
        }

        return noDiscount();
    }

    private boolean isBeverage(MenuItemView item) {
        String category = normalize(item.getCategory());
        String id = normalize(item.getId());

        return category.equals("beverage")
                || category.equals("bev")
                || category.equals("drink")
                || category.equals("drinks")
                || id.startsWith("bev-")
                || id.startsWith("drink-");
    }

    private boolean isFood(MenuItemView item) {
        String category = normalize(item.getCategory());
        String id = normalize(item.getId());

        return category.equals("food")
                || category.equals("foods")
                || id.startsWith("fod-")
                || id.startsWith("food-");
    }

    private DiscountResult noDiscount() {
        return new DiscountResult(0.0, "No discount");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
