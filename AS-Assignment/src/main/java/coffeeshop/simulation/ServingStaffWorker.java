package coffeeshop.simulation;

import coffeeshop.logging.EventLogger;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.ServingStaff;
import coffeeshop.model.StaffStatus;

/**
 * Consumer thread: takes orders from SharedOrderQueue and simulates processing.
 * Loop: WAITING → take() → PROCESSING → sleep → IDLE → repeat.
 * Returns when take() yields null (producer done + queue empty).
 *
 * @author Lin Yi (Member D)
 */
public class ServingStaffWorker extends Thread {

    private final ServingStaff staff;
    private final SharedOrderQueue queue;
    private final EventLogger logger;

    public ServingStaffWorker(ServingStaff staff, SharedOrderQueue queue) {
        if (staff == null) throw new IllegalArgumentException("staff must not be null");
        if (queue == null) throw new IllegalArgumentException("queue must not be null");
        this.staff = staff;
        this.queue = queue;
        this.logger = EventLogger.getInstance();
    }

    @Override
    public void run() {
        String tag = "[SERVER-" + staff.getStaffId() + "]";
        logger.log(tag + " Ready and waiting for orders.");

        while (true) {
            staff.setStatus(StaffStatus.WAITING);

            CustomerOrder order = queue.take();

            // null means no more orders — exit cleanly
            if (order == null) {
                staff.setStatus(StaffStatus.IDLE);
                staff.setCurrentOrder(null);
                logger.log(tag + " No more orders. Exiting. Total processed: "
                        + staff.getProcessedCount());
                break;
            }

            // Process the order
            staff.setStatus(StaffStatus.PROCESSING);
            staff.setCurrentOrder(order);
            logger.log(tag + " Started processing order for "
                    + order.getCustomerId()
                    + " (" + order.getItems().size() + " item(s))");

            try {
                Thread.sleep(order.getProcessingTimeMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(tag + " Interrupted. Exiting.");
                break;
            }

            // Done
            staff.incrementProcessedCount();
            staff.addProcessingTime(order.getProcessingTimeMs());
            staff.setCurrentOrder(null);
            staff.setStatus(StaffStatus.IDLE);
            logger.log(tag + " Finished order for " + order.getCustomerId());
        }
    }

    public ServingStaff getStaff() {
        return staff;
    }
}
