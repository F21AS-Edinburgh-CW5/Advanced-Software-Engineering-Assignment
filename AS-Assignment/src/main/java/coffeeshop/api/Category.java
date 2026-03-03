

/**
 * 菜单项类别，用于ID验证和业务分类。
 * 与DiscountCalculator中使用的字符串类别保持一致（转换为小写后匹配）。
 */
public enum Category {
    DRINK,      // 对应 "beverage"
    FOOD,       // 对应 "food"
    DESSERT;    // 其他类别可根据需要扩展

    /**
     * 转换为DiscountCalculator期望的字符串形式（小写）。
     */
    public String toViewString() {
        return this.name().toLowerCase();
    }
}