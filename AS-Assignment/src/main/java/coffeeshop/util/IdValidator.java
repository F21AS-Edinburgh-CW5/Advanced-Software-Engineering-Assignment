package coffeeshop.util;

/**
 * Utility class for validating identifiers used in the coffee shop system.
 * Menu item IDs must follow the pattern: ALPHA-NNN
 * where ALPHA is one or more alphabetic characters and NNN is exactly three digits.
 *
 * Examples of valid IDs:  BEV-001, FOOD-010, OTH-100
 * Examples of invalid IDs: BADID-1, 123-ABC, BEV003, -005
 */
public class IdValidator {

    /**
     * Checks whether a menu item ID matches the required pattern.
     *
     * @param id the ID string to validate
     * @return true if the ID matches [A-Za-z]+-\d{3}, false otherwise
     */
    public static boolean isValidMenuItemId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return id.trim().matches("^[A-Za-z]+-\\d{3}$");
    }
}
