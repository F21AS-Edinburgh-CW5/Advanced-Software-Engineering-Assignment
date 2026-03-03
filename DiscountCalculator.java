import java.util.List;

public class DiscountCalculator {

    // 新增：用于同时返回折扣金额和规则名称的包装类，完美适配成员B的 Bill 类
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

    // 参数从 Order 改为 List<MenuItemView>
    public DiscountResult calculateDiscount(List<MenuItemView> items) {
        if (items == null || items.isEmpty()) {
            return new DiscountResult(0.0, "No discount");
        }

        int beverageCount = 0;
        int foodCount = 0;
        double total = 0.0;

        for (MenuItemView item : items) {
            total += item.getPrice();
            // 匹配成员B的 category 属性
            if (item.getCategory().equalsIgnoreCase("beverage")) {
                beverageCount++;
            } else if (item.getCategory().equalsIgnoreCase("food")) {
                foodCount++;
            }
        }

        // 规则1：1杯饮品 + 2份食物 = 20%
        if (beverageCount >= 1 && foodCount >= 2) {
            return new DiscountResult(total * 0.20, "20% off: 1 Bev + 2 Food");
        }

        // 规则2：3杯及以上饮品 = 15%
        if (beverageCount >= 3) {
            return new DiscountResult(total * 0.15, "15% off: 3+ Beverages");
        }

        // 规则3：总价大于 £25 = 10%
        if (total > 25.0) {
            return new DiscountResult(total * 0.10, "10% off: Over £25");
        }

        return new DiscountResult(0.0, "No discount");
    }
}