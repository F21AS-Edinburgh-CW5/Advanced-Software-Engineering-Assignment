package coffeeshop.simulation;

import coffeeshop.model.CustomerOrder;
import coffeeshop.service.SimulationService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
//shared order queue for producer and staff workers(3)

//order queue: used for transmitting orders between producers and employees
public class SharedOrderQueue {

    //the linked list for storing orders
    private final LinkedList<CustomerOrder> orders = new LinkedList<>();

    //check whether there will be no more new orders entering the queue
    private boolean closed = false;

    //used for informing observers after queue has changes
    private SimulationService simulationService = null;

    /**
     * connect queue 
     */
    public synchronized void setSimulationService(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     *add orders to the queue
     */
    public void add(CustomerOrder newOrder) {
        if (newOrder == null) {
            throw new IllegalArgumentException("Error:Orders can not be null.");
        }

        List<CustomerOrder> snapshot;
        boolean finished;
        SimulationService serviceRef;

        synchronized (this) {
            orders.addLast(newOrder);
            snapshot = new ArrayList<>(orders);
            finished = closed;
            serviceRef = simulationService;
            notifyAll();
        }

        publishQueueChanged(serviceRef, snapshot, finished);
    }

    public void addToFront(CustomerOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Error: Orders cannot be null.");
        }

        List<CustomerOrder> snapshot;
        boolean finished;
        SimulationService serviceRef;

        synchronized (this) {
            orders.addFirst(order);
            snapshot = new ArrayList<>(orders);
            finished = closed;
            serviceRef = simulationService;
            notifyAll();
        }

        publishQueueChanged(serviceRef, snapshot, finished);
    }

    /**
     * get a order from the queue
     * @return orders getted / Null
     */
    public CustomerOrder take() {
        return take(() -> false);
    }

    public CustomerOrder take(BooleanSupplier stopRequested) {
        CustomerOrder nextOrder;
        List<CustomerOrder> snapshot;
        boolean finished;
        SimulationService serviceRef;

        synchronized (this) {
            for (;;) {
                if (stopRequested != null && stopRequested.getAsBoolean()) {
                    nextOrder = null;
                    break;
                }

                if (!orders.isEmpty()) {
                    nextOrder = orders.removeFirst();
                    break;
                }

                if (closed) {
                    nextOrder = null;
                    break;
                }

                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    nextOrder = null;
                    break;
                }
            }

            snapshot = new ArrayList<>(orders);
            finished = closed;
            serviceRef = simulationService;
        }

        publishQueueChanged(serviceRef, snapshot, finished);
        return nextOrder;
    }
    
    //notify all waiting threads
    //information:No new orders will be entered from now time.
    public void markProducerDone() {
        List<CustomerOrder> snapshot;
        SimulationService serviceRef;

        synchronized (this) {
            closed = true;
            snapshot = new ArrayList<>(orders);
            serviceRef = simulationService;
            notifyAll();
        }

        publishQueueChanged(serviceRef, snapshot, true);
    }

    public synchronized void signalStateChange() {
        notifyAll();
    }
    
    // after leaving synchronized block,then notify observers
    private void publishQueueChanged(SimulationService serviceRef,
                                     List<CustomerOrder> snapshot,
                                     boolean finished) {
        if (serviceRef != null) {
            serviceRef.setFinished(finished);
            serviceRef.notifyQueueObservers(snapshot);
        }
    }

    //check whether the queue is empty
    public synchronized boolean isEmpty() {
        return orders.isEmpty();
    }

    //get numbers of the orders(queue size)
    public synchronized int size() {
        return orders.size();
    }

    //check producer thread (whether is ended)
    public synchronized boolean isProducerDone() {
        return closed;
    }

    public synchronized List<CustomerOrder> getQueueSnapshot() {
        return new ArrayList<>(orders);
    }
}
