package coffeeshop.model;

import coffeeshop.util.IdValidator;

public class MenuItem {

    private final String id;
    private final String description;
    private final double cost;
    private final String category;

    public MenuItem(String id, String description, double cost, String category) {
        if (id == null || !IdValidator.isValidMenuItemId(id.trim())) {
            throw new IllegalArgumentException("Invalid menu item ID: " + id);
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost cannot be negative: " + cost);
        }
        this.id = id.trim();
        this.description = description.trim();
        this.cost = cost;
        this.category = category.trim();
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getCost() { return cost; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return description + " costs " + cost + " (" + category + " " + id + ")";
    }
}
