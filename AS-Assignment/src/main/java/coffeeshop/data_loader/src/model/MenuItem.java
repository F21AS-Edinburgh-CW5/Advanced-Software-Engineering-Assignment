package model;
import util.IdValidator;
/*
 AI usage instructions:
 Precautions for using constructors.
 Regarding the name of the function that throws an exception
 */
public class MenuItem {

    private final String id;
    private final String description;
    private final double cost;
    private final String category;

    public MenuItem(String id, String description, double cost, String category) {
        String cleanId = checkValidId(id);
        String cleanDesc = checkDes(description, "Error:description empty");
        String cleanCategory = checkDes(category, "Error:category empty");
        checkCost(cost);

        this.id = cleanId;
        this.description = cleanDesc;
        this.cost = cost;
        this.category = cleanCategory;
    }

    private static String checkValidId(String rawId) {
        if (rawId == null) {
            throw new IllegalArgumentException("Error:menuID: null");
        }
        String cleaned = rawId.trim();
        if (!IdValidator.isValidMenuItemId(cleaned)) {
            throw new IllegalArgumentException("Error! menuID invalid : " + rawId);
        }
        return cleaned;
    }

    private static String checkDes(String text, String messageIfBad) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(messageIfBad);
        }
        return text.trim();
    }

    private static void checkCost(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Error!cost negative: " + value);
        }
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getCost() {
        return cost;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return description + "costs" + cost + " (Information:" + category + " " + id + ")";
    }
}