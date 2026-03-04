package model;

/*
 AI usage instructions:
 Precautions for using constructors.
 Regarding the name of the function that throws an exception
 */
public class OrderRecord {

    private final String time;
    private final String custId;
    private final String itemId;

    public OrderRecord(String time, String custId, String itemId) {
        this.time = requireText(time, "Error!time empty");
        this.custId = requireText(custId, "Error!custId empty");
        this.itemId = requireText(itemId, "Error!itemId empty");
    }

    private static String requireText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value.trim();
    }

    public String getTimestamp() {
        return time;
    }

    public String getCustomerId() {
        return custId;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        return time + "-" + custId + "-" + itemId;
    }
}