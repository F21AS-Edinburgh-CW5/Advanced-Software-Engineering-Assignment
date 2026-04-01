package coffeeshop.simulation;

import coffeeshop.model.CustomerOrder;
import coffeeshop.model.OnlineOrder;
import coffeeshop.service.SimulationService;
import java.util.function.BooleanSupplier;
import java.util.*;

public class SharedOrderQueue {

    private final LinkedList<CustomerOrder> orders = new LinkedList<>();
    private final PriorityQueue<OnlineOrder> onlineOrders = new PriorityQueue<>(
        Comparator.comparingInt(OnlineOrder::getPriority)
    );
    private boolean closed = false;
    private SimulationService simulationService = null;

    public synchronized void setSimulationService(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    public void add(CustomerOrder newOrder) {
        if (newOrder == null) {
            throw new IllegalArgumentException("Error: Orders cannot be null.");
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

    public void addOnlineOrder(OnlineOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        List<CustomerOrder> snapshot;
        boolean finished;
        SimulationService serviceRef;
        synchronized (this) {
            onlineOrders.add(order);
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
                if (!onlineOrders.isEmpty()) {
                    nextOrder = onlineOrders.poll();
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

    private void publishQueueChanged(SimulationService serviceRef,
                                     List<CustomerOrder> snapshot,
                                     boolean finished) {
        if (serviceRef != null) {
            serviceRef.setFinished(finished);
            serviceRef.notifyQueueObservers(snapshot);
        }
    }

    public synchronized boolean isEmpty() {
        return orders.isEmpty() && onlineOrders.isEmpty();
    }

    public synchronized int size() {
        return orders.size() + onlineOrders.size();
    }

    public synchronized boolean isProducerDone() {
        return closed;
    }

    public synchronized List<CustomerOrder> getQueueSnapshot() {
        List<CustomerOrder> all = new ArrayList<>();
        all.addAll(onlineOrders);
        all.addAll(orders);
        return all;
    }
}
