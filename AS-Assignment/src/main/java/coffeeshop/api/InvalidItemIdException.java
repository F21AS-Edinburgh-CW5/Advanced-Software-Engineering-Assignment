

/**
*The checked exception thrown when the format of the menu item ID does not comply with the rules.
*Rule: The ID must conform to the format of "<CATEGORY>- XXX", where CATEGORY is a predefined enumeration value,
*XXX is a three digit number (allowing leading zeros).
*/
public class InvalidItemIdException extends Exception {
    
    public InvalidItemIdException(String message) {
        super(message);
    }

    public InvalidItemIdException(String message, Throwable cause) {
        super(message, cause);
    }

}
