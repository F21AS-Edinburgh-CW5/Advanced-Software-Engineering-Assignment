package coffeeshop.simulation;

import coffeeshop.logging.EventLogger;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.ServingStaff;
import coffeeshop.model.StaffStatus;
import coffeeshop.service.SimulationService;
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
    private final SimulationService simulationService;

    public ServingStaffWorker(ServingStaff staff, SharedOrderQueue queue) {
        this(staff, queue, null);
    }
    
    public ServingStaffWorker(ServingStaff staff, SharedOrderQueue queue ,SimulationService simulationService) {
        if (staff == null) throw new IllegalArgumentException("staff must not be null");
        if (queue == null) throw new IllegalArgumentException("queue must not be null");
        this.staff = staff;
        this.queue = queue;
        this.logger = EventLogger.getInstance();
        this.simulationService = simulationService;

    }

    @Override
    public void run() {
        String tag = "[SERVER-" + staff.getStaffId() + "]";
        updateState(StaffStatus.WAITING, null);
        logger.log(tag + " Ready and waiting for orders.");

        while (true) {
            updateState(StaffStatus.WAITING, null);

            CustomerOrder order = queue.take();

            // null means no more orders — exit cleanly
            if (order == null) {
                updateState(StaffStatus.IDLE, null);
                logger.log(tag + " No more orders. Exiting. Total processed: "
                        + staff.getProcessedCount());
                break;
            }

            // Process the order
            updateState(StaffStatus.PROCESSING, order);
            logger.log(tag + " Started processing order for "
                    + order.getCustomerId()
                    + " (" + order.getItems().size() + " item(s))");

            try {
                Thread.sleep(order.getProcessingTimeMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateState(StaffStatus.IDLE, null);
                logger.log(tag + " Interrupted. Exiting.");
                break;
            }

            // Done
            staff.incrementProcessedCount();
            staff.addProcessingTime(order.getProcessingTimeMs());
            updateState(StaffStatus.IDLE, null);
            logger.log(tag + " Finished order for " + order.getCustomerId());
        }
    }
    private void updateState(StaffStatus newStatus, CustomerOrder currentOrder) {
        staff.setStatus(newStatus);
        staff.setCurrentOrder(currentOrder);

        if (simulationService != null) {
            simulationService.notifyServerObservers(queue.getQueueSnapshot());
        }
    }


    public ServingStaff getStaff() {
        return staff;
    }
}
