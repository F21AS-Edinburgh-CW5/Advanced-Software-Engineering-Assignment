package coffeeshop.gui;

import coffeeshop.service.QueueObserver;
import coffeeshop.service.ServerObserver;
import coffeeshop.service.SimulationService;
import coffeeshop.simulation.ProducerThread;
import coffeeshop.simulation.ServingStaffWorker;
import coffeeshop.simulation.SharedOrderQueue;
import coffeeshop.simulation.SimulationManager;

import java.lang.reflect.Field;
import java.util.List;

/**
 * SimulationController is the Controller in MVC.
 * It connects GUI actions to the simulation model and registers GUI observers.
 */
public class SimulationController {

    private final SimulationManager simulationManager;
    private final SimulationService simulationService;

    private final Object lifecycleLock = new Object();
    private volatile boolean running;
    private Thread awaitThread;

    public SimulationController(SimulationManager simulationManager,
                                SimulationService simulationService) {
        if (simulationManager == null) {
            throw new IllegalArgumentException("simulationManager must not be null");
        }
        if (simulationService == null) {
            throw new IllegalArgumentException("simulationService must not be null");
        }

        this.simulationManager = simulationManager;
        this.simulationService = simulationService;
        this.running = false;
    }

    /**
     * Registers GUI observers so the view can receive snapshots from the model.
     */
    public void registerObservers(QueueObserver queueObserver,
                                  ServerObserver serverObserver) {
        if (queueObserver != null) {
            simulationService.addQueueObserver(queueObserver);
        }
        if (serverObserver != null) {
            simulationService.addServerObserver(serverObserver);
        }
    }

    /**
     * Starts the simulation by delegating to SimulationManager.
     */
    public void start() {
        synchronized (lifecycleLock) {
            if (running) {
                return;
            }

            running = true;
            simulationManager.startSimulation();
            startAwaitThread();
        }
    }

    /**
     * Stops the simulation by interrupting active threads and closing the queue.
     */
    public void stop() {
        synchronized (lifecycleLock) {
            if (!running) {
                return;
            }

            running = false;

            if (awaitThread != null && awaitThread.isAlive()) {
                awaitThread.interrupt();
            }

            requestStopByReflection();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void startAwaitThread() {
        if (awaitThread != null && awaitThread.isAlive()) {
            return;
        }

        awaitThread = new Thread(() -> {
            try {
                simulationManager.awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (lifecycleLock) {
                    running = false;
                }
            }
        }, "simulation-await-thread");

        awaitThread.setDaemon(true);
        awaitThread.start();
    }

    /**
     * Uses the current SimulationManager structure to stop the simulation without
     * requiring changes to teammate code in Iteration 2.
     */
    @SuppressWarnings("unchecked")
    private void requestStopByReflection() {
        try {
            SharedOrderQueue queue =
                    (SharedOrderQueue) getFieldValue(simulationManager, "queue");
            ProducerThread producer =
                    (ProducerThread) getFieldValue(simulationManager, "producer");
            List<ServingStaffWorker> workers =
                    (List<ServingStaffWorker>) getFieldValue(simulationManager, "workers");

            if (queue != null) {
                queue.markProducerDone();
            }

            if (producer != null && producer.isAlive()) {
                producer.interrupt();
            }

            if (workers != null) {
                for (ServingStaffWorker worker : workers) {
                    if (worker != null && worker.isAlive()) {
                        worker.interrupt();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop simulation cleanly", e);
        }
    }

    private Object getFieldValue(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
