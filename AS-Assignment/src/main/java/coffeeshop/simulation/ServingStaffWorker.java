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

    private volatile boolean shouldStop;
    
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
        this.shouldStop = false;
        setName("staff-worker-" + staff.getStaffId());
    }

    @Override
    public void run() {
        String tag = "[SERVER-" + staff.getStaffId() + "]";
        logger.log(tag + " Information: start worker");

        while (true) {
            if (shouldStop) {
                stopCleanly(tag, "Remove the simulation.");
                return;
            }
            updateState(StaffStatus.WAITING, null);

            CustomerOrder order = queue.take(this::isStopRequested);

            // null means no more orders — exit cleanly
            if (order == null) {
                if (shouldStop) {
                    stopCleanly(tag, "Removed from simulation.");
                } else {
                    updateState(StaffStatus.IDLE, null);
                    logger.log(tag + " No more orders. Exiting. Total processed: "
                            + staff.getProcessedCount());
                }
                return;
                
            }

            if (shouldStop) {
                queue.addToFront(order);
                stopCleanly(tag, "Information : Has removed, can start a new order.");
                return;
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
                return;
            }

            // Done
            staff.incrementProcessedCount();
            staff.addProcessingTime(order.getProcessingTimeMs());
            updateState(StaffStatus.IDLE, null);
            logger.log(tag + " Finished order for " + order.getCustomerId());

            if (shouldStop) {
                stopCleanly(tag, "Removed upon completion of the current order.");
                return;
            }
            
        }
    }
    public void requestStop() {
        shouldStop = true;
    }

    public boolean isStopRequested() {
        return shouldStop;
    }

    public boolean isIdleSafeToRemove() {
        StaffStatus status = staff.getStatus();
        return status == StaffStatus.IDLE || status == StaffStatus.WAITING;
    }

    private void stopCleanly(String tag, String message) {
        updateState(StaffStatus.IDLE, null);
        logger.log(tag + " " + message);
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
