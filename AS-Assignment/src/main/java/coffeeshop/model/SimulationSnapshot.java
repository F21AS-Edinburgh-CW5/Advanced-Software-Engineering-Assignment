package coffeeshop.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationSnapshot {

    private final List<CustomerOrder> queue;
    private final List<ServingStaff> staffList;
    private final boolean finished;

    public SimulationSnapshot(List<CustomerOrder> queue, List<ServingStaff> staffList, boolean finished) {
        this.queue = Collections.unmodifiableList(new ArrayList<>(queue));
        this.staffList = Collections.unmodifiableList(new ArrayList<>(staffList));
        this.finished = finished;
    }

    public List<CustomerOrder> getQueue() {
        return queue;
    }

    public List<ServingStaff> getStaffList() {
        return staffList;
    }

    public boolean isFinished() {
        return finished;
    }
}