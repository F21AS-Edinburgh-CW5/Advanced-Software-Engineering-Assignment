package coffeeshop.service;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.ServingStaff;
import coffeeshop.model.SimulationSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
//Iteration2 SimulationService

//Function:keep order list,build snapshots,and inform GUI observers.

//Iteration3 Simulation GUI central model.
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

    public void addStaff(ServingStaff staff) {
        if (staff == null) {
            return;
        }
        synchronized (staffList) {
            staffList.add(staff);
        }
    }

    public boolean removeStaffById(String staffId) {
        if (staffId == null) {
            return false;
        }
        synchronized (staffList) {
            Iterator<ServingStaff> iterator = staffList.iterator();
            while (iterator.hasNext()) {
                ServingStaff staff = iterator.next();
                if (staffId.equals(staff.getStaffId())) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public int getStaffCount() {
        synchronized (staffList) {
            return staffList.size();
        }
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

    public void notifyAllObservers(List<CustomerOrder> queueSnapshot) {
        SimulationSnapshot snapshot = createSnapshot(queueSnapshot);

        for (QueueObserver observer : getQueueObserverCopy()) {
            observer.onQueueChanged(snapshot);
        }
        for (ServerObserver observer : getServerObserverCopy()) {
            observer.onServerStateChanged(snapshot);
        }
    }
    
    private SimulationSnapshot createSnapshot(List<CustomerOrder> queueSnapshot) {
        List<CustomerOrder> queueCopy = copyQueue(queueSnapshot);
        List<ServingStaff> staffCopy;
        boolean finishedCopy;

        synchronized (staffList) {
            staffCopy = copyStaffList();
        }
        synchronized (this) {
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
        return new ArrayList<>(queueObservers);
    }

    
    private synchronized List<ServerObserver> getServerObserverCopy() {
        return new ArrayList<>(serverObservers);
    }

    
    private List<ServingStaff> copyStaffList() {
        List<ServingStaff> result = new ArrayList<>();

        for (ServingStaff staff : staffList) {
            result.add(staff.copy());
        }

        return result;
    }
}
