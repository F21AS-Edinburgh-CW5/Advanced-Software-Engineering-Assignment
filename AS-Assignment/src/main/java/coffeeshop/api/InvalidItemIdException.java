package coffeeshop.api;

/**
 * Checked exception thrown when a menu item ID does not satisfy
 * the required format rule.
 *
 * Expected format:
 * <CATEGORY>-XXX
 * where CATEGORY is an alphabetic prefix and XXX is a three-digit number.
 */
public class InvalidItemIdException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidItemIdException(String message) {
        super(message);
    }

    public InvalidItemIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
