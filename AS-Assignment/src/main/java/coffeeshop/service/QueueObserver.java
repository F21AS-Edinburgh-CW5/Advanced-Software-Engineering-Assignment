package coffeeshop.service;

import coffeeshop.model.SimulationSnapshot;

/**QueueObserver Interface
 * Observer used for queue changes .
 */
public interface QueueObserver {
    void onQueueChanged(SimulationSnapshot snapshot);
}

