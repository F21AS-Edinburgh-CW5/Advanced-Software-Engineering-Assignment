package coffeeshop.model;

public class ServingStaff {

    private final String staffId;
    private int processedCount;
    private StaffStatus status;
    private CustomerOrder currentOrder;

    public ServingStaff(String staffId) {
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID cannot be empty");
        }
        this.staffId = staffId.trim();
        this.status = StaffStatus.IDLE;
        this.processedCount = 0;
        this.currentOrder = null;
    }

    public String getStaffId() {
        return staffId;
    }

    public StaffStatus getStatus() {
        return status;
    }

    public void setStatus(StaffStatus status) {
        this.status = status;
    }

    public CustomerOrder getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(CustomerOrder currentOrder) {
        this.currentOrder = currentOrder;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void incrementProcessedCount() {
        this.processedCount++;
    }

    @Override
    public String toString() {
        return "ServingStaff{staffId='" + staffId + "', status=" + status
                + ", processedCount=" + processedCount + "}";
    }
}