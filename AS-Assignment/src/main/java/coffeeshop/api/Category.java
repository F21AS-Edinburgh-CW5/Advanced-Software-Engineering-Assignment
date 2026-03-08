package coffeeshop.api;

public enum Category {
    DRINK,
    FOOD,
    DESSERT;

    public String toViewString() {
        return this.name().toLowerCase();
    }
}
