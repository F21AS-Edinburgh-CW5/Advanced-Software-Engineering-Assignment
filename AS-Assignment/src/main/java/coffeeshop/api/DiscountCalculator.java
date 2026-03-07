import java.util.List;

public class DiscountCalculator {

    // Packaging class used to simultaneously return discount amount and rule name, perfectly adapted to Bill class of other members
    public static class DiscountResult {
        private final double discountAmount;
        private final String ruleApplied;

        public DiscountResult(double discountAmount, String ruleApplied) {
            this.discountAmount = discountAmount;
            this.ruleApplied = ruleApplied;
        }

        public double getDiscountAmount() { return discountAmount; }
        public String getRuleApplied() { return ruleApplied; }
    }

    public DiscountResult calculateDiscount(List<MenuItemView> items) {
        if (items == null || items.isEmpty()) {
            return new DiscountResult(0.0, "No discount");
        }

        int beverageCount = 0;
        int foodCount = 0;
        double total = 0.0;

        for (MenuItemView item : items) {
            total += item.getPrice();
            // Match the category attribute of other members
            if (item.getCategory().equalsIgnoreCase("beverage")) {
                beverageCount++;
            } else if (item.getCategory().equalsIgnoreCase("food")) {
                foodCount++;
            }
        }

        // Rule 1:1 cup of beverage+2 portions of food=20%
        if (beverageCount >= 1 && foodCount >= 2) {
            return new DiscountResult(total * 0.20, "20% off: 1 Bev + 2 Food");
        }

        // Rule 2: 3 or more drinks=15%
        if (beverageCount >= 3) {
            return new DiscountResult(total * 0.15, "15% off: 3+ Beverages");
        }

        // Rule 3: Total price greater than £ 25=10%
        if (total > 25.0) {
            return new DiscountResult(total * 0.10, "10% off: Over £25");
        }

        return new DiscountResult(0.0, "No discount");
    }

}
