//used for mutable model(one serving staff member)
package coffeeshop.model;

public class ServingStaff {

    private final String staffId;
    private int processedCount;
    private StaffStatus status;
    private CustomerOrder currentOrder;
    private long totalProcessingTimeMs;

    public ServingStaff(String staffId) {
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID cannot be empty");
        }
        this.staffId = staffId.trim();
        this.status = StaffStatus.IDLE;
        this.processedCount = 0;
        this.currentOrder = null;
        this.totalProcessingTimeMs = 0L;
    }

    public synchronized void addProcessingTime(long ms) {
        this.totalProcessingTimeMs += ms;
    }

    public synchronized long getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }

    public String getStaffId() {
        return staffId;
    }

    public synchronized StaffStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(StaffStatus status) {
        this.status = status;
    }

    public synchronized CustomerOrder getCurrentOrder() {
        return currentOrder;
    }

    public synchronized void setCurrentOrder(CustomerOrder currentOrder) {
        this.currentOrder = currentOrder;
    }

    public synchronized int getProcessedCount() {
        return processedCount;
    }

    public synchronized void incrementProcessedCount() {
        this.processedCount++;
    }

    public synchronized boolean isIdle() {
        return status == StaffStatus.IDLE;
    }
    
    public synchronized ServingStaff copy() {
        ServingStaff copy = new ServingStaff(staffId);
        copy.processedCount = this.processedCount;
        copy.status = this.status;
        copy.currentOrder = this.currentOrder;
        copy.totalProcessingTimeMs = this.totalProcessingTimeMs;
        return copy;
    }

    @Override
    public synchronized String toString() {
        return "ServingStaff{staffId='" + staffId + "', status=" + status
                + ", processedCount=" + processedCount + "}";
    }
}
