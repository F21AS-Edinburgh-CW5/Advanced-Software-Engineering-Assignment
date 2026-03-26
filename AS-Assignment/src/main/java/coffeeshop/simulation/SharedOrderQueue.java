package coffeeshop.simulation;

import coffeeshop.model.CustomerOrder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//order queue: used for transmitting orders between producers and employees
public class SharedOrderQueue {

    //the linked list for storing orders
    private final LinkedList<CustomerOrder> orders = new LinkedList<>();

    //check whether there will be no more new orders entering the queue
    private boolean closed = false;

    /**
     *add orders to the queue
     */
    public synchronized void add(CustomerOrder newOrder) {
        if (newOrder == null) {
            throw new IllegalArgumentException("Error:Orders can not be null.");
        }

        orders.add(newOrder);
        notifyAll();
    }

    /**
     * get a order from the queue
     * @return orders getted / Null
     */
    public synchronized CustomerOrder take() {
        for (;;) {
            if (!orders.isEmpty()) {
                return orders.removeFirst();
            }

            if (closed) {
                return null;
            }

            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    //notify all waiting threads
    //information:No new orders will be entered from now time.
    public synchronized void markProducerDone() {
        closed = true;
        notifyAll();
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
        List<CustomerOrder> snapshot = new ArrayList<>();
        snapshot.addAll(orders);
        return snapshot;
    }
}