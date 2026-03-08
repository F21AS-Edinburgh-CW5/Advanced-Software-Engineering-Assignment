package coffeeshop.model;

public class OrderRecord {

    private final String time;
    private final String custId;
    private final String itemId;

    public OrderRecord(String time, String custId, String itemId) {
        if (time == null || time.trim().isEmpty()) throw new IllegalArgumentException("Time empty");
        if (custId == null || custId.trim().isEmpty()) throw new IllegalArgumentException("CustId empty");
        if (itemId == null || itemId.trim().isEmpty()) throw new IllegalArgumentException("ItemId empty");
        this.time = time.trim();
        this.custId = custId.trim();
        this.itemId = itemId.trim();
    }

    public String getTimestamp() { return time; }
    public String getCustomerId() { return custId; }
    public String getItemId() { return itemId; }

    @Override
    public String toString() { return time + "-" + custId + "-" + itemId; }
}
