package coffeeshop.service;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.ServingStaff;
import coffeeshop.model.SimulationSnapshot;
import java.util.ArrayList;
import java.util.List;
//Iteration2 SimulationService

//Function:keep order list,build snapshots,and inform GUI observers.


public class SimulationService {

    private final List<QueueObserver> queueObservers = new ArrayList<>();
    private final List<ServerObserver> serverObservers = new ArrayList<>();

    //current staff list in simulation
    private final List<ServingStaff> staffList;
    //check whether simulation is finished
    private boolean finished = false;

    public SimulationService(List<ServingStaff> staffList) {
        if (staffList == null) {
            this.staffList = new ArrayList<>();
        } else {
            this.staffList = staffList;
        }
    }

    public synchronized void addQueueObserver(QueueObserver observer) {
        if (observer != null) {
            queueObservers.add(observer);
        }
    }

    public synchronized void addServerObserver(ServerObserver observer) {
        if (observer != null) {
            serverObservers.add(observer);
        }
    }

    public synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void notifyQueueObservers(List<CustomerOrder> queueSnapshot) {
        SimulationSnapshot snapshot = createSnapshot(queueSnapshot);
        List<QueueObserver> observers = getQueueObserverCopy();

        for (QueueObserver observer : observers) {
            observer.onQueueChanged(snapshot);
        }
    }

    public void notifyServerObservers(List<CustomerOrder> queueSnapshot) {
        SimulationSnapshot snapshot = createSnapshot(queueSnapshot);
        List<ServerObserver> observers = getServerObserverCopy();

        for (ServerObserver observer : observers) {
            observer.onServerStateChanged(snapshot);
        }
    }

    private SimulationSnapshot createSnapshot(List<CustomerOrder> queueSnapshot) {
        List<CustomerOrder> queueCopy = copyQueue(queueSnapshot);
        List<ServingStaff> staffCopy;
        boolean finishedCopy;

        synchronized (this) {
            staffCopy = copyStaffList();
            finishedCopy = finished;
        }

        return new SimulationSnapshot(queueCopy, staffCopy, finishedCopy);
    }

    private List<CustomerOrder> copyQueue(List<CustomerOrder> queueSnapshot) {
        List<CustomerOrder> queueCopy = new ArrayList<>();

        if (queueSnapshot != null) {
            queueCopy.addAll(queueSnapshot);
        }

        return queueCopy;
    }

    
    private synchronized List<QueueObserver> getQueueObserverCopy() {
        List<QueueObserver> copy = new ArrayList<>();
        copy.addAll(queueObservers);
        return copy;
    }

    
    private synchronized List<ServerObserver> getServerObserverCopy() {
        List<ServerObserver> copy = new ArrayList<>();
        copy.addAll(serverObservers);
        return copy;
    }

    
    private List<ServingStaff> copyStaffList() {
        List<ServingStaff> result = new ArrayList<>();

        for (ServingStaff oldStaff : staffList) {
            ServingStaff newStaff = new ServingStaff(oldStaff.getStaffId());
            newStaff.setStatus(oldStaff.getStatus());
            newStaff.setCurrentOrder(oldStaff.getCurrentOrder());

            for (int i = 0; i < oldStaff.getProcessedCount(); i++) {
                newStaff.incrementProcessedCount();
            }

            result.add(newStaff);
        }

        return result;
    }
}
