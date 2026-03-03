

import coffeeshop.exception.InvalidItemIdException;
import coffeeshop.api.MenuItemView;  // 现有API视图类

/**
 * 菜单项实体类，包含ID、描述、价格和类别。
 * ID必须符合 "<CATEGORY>-XXX" 格式，例如 "DRINK-001"、"FOOD-123"。
 */
public class MenuItem {
    private String itemId;
    private String description;
    private double price;
    private Category category;

    /**
     * 构造一个MenuItem，并进行全面的参数校验。
     *
     * @param itemId      项目ID，必须符合格式要求
     * @param description 项目描述，不能为空
     * @param price       项目价格，不能为负数
     * @param category    项目类别，不能为null
     * @throws InvalidItemIdException 如果itemId格式不正确
     * @throws IllegalArgumentException 如果description为空、price为负或category为null
     */
    public MenuItem(String itemId, String description, double price, Category category)
            throws InvalidItemIdException {
        // 基础参数校验
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        // ID格式校验
        if (!isValidItemId(itemId, category)) {
            throw new InvalidItemIdException(
                String.format("Invalid ID format: '%s'. Expected pattern: %s-XXX (e.g., %s-001)",
                itemId, category.name(), category.name())
            );
        }
        this.itemId = itemId;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    /**
     * 验证ID是否符合指定类别的格式。
     * 格式：<CATEGORY>-XXX，其中XXX是三位数字。
     *
     * @param id       待验证的ID
     * @param category 预期的类别
     * @return true 如果格式正确，否则false
     */
    private boolean isValidItemId(String id, Category category) {
        String prefix = category.name() + "-";
        if (!id.startsWith(prefix)) {
            return false;
        }
        String suffix = id.substring(prefix.length());
        return suffix.matches("\\d{3}");
    }

    // --- getters ---
    public String getItemId() { return itemId; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Category getCategory() { return category; }

    /**
     * 修改ID（需重新验证格式）。
     *
     * @param itemId 新的ID
     * @throws InvalidItemIdException 如果新ID格式不正确
     */
    public void setItemId(String itemId) throws InvalidItemIdException {
        if (!isValidItemId(itemId, this.category)) {
            throw new InvalidItemIdException("Invalid ID format for category " + this.category);
        }
        this.itemId = itemId;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description = description;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("MenuItem{id='%s', description='%s', price=%.2f, category=%s}",
                itemId, description, price, category);
    }

    /**
     * 转换为API层使用的视图对象 MenuItemView。
     * 类别名称转为小写，以匹配DiscountCalculator中预期的 "beverage"/"food" 等。
     *
     * @return MenuItemView 实例
     */
    public MenuItemView toView() {
        return new MenuItemView(
            this.itemId,
            this.description,
            this.category.toViewString(),  // 例如 "drink", "food"
            this.price
        );
    }
}