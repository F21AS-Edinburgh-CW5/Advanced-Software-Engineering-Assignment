package coffeeshop.service;
import coffeeshop.model.SimulationSnapshot;

/**ServerObserver Interface
 * observer used for staff to state changes.
 */
public interface ServerObserver {
    void onServerStateChanged(SimulationSnapshot snapshot);
}
