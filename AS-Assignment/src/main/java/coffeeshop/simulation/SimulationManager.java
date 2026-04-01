package coffeeshop.simulation;

import coffeeshop.model.MenuItem;
import coffeeshop.model.ServingStaff;
import coffeeshop.model.StaffStatus;
import coffeeshop.report.ReportGenerator;
import coffeeshop.logging.EventLogger;
import coffeeshop.service.SimulationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulationManager {
    private final SharedOrderQueue queue;
    private final ProducerThread producer;
    private final List<ServingStaffWorker> workers;
    private final List<ServingStaff> staffModels;
    private final List<ServingStaff> reportStaffModels;
    private final SimulationService simulationService;
    private OnlineOrderProducerThread onlineProducer;  // 新增

    private final Object workerLock = new Object();
    private int nextStaffNumber;
    private volatile boolean simulationStarted;
    private volatile boolean simulationFinished;

    // Console version constructor (without enabling online orders)
    public SimulationManager(SharedOrderQueue queue,
            ProducerThread producer,
            int workerCount) {
this.queue = queue;
this.producer = producer;
this.workers = new ArrayList<>();
this.staffModels = new ArrayList<>();
this.reportStaffModels = new ArrayList<>();   // Add this line to resolve the "final" variable not initialized error
this.simulationService = null;

for (int i = 0; i < workerCount; i++) {
String staffId = "Server-" + (i + 1);
ServingStaff staffModel = new ServingStaff(staffId);
staffModels.add(staffModel);
reportStaffModels.add(staffModel);
ServingStaffWorker worker = new ServingStaffWorker(staffModel, queue);
workers.add(worker);
}
this.nextStaffNumber = workerCount + 1;
this.simulationStarted = false;
this.simulationFinished = false;
this.onlineProducer = null;
}

    // GUI version constructor (enabling online orders)
    public SimulationManager(SharedOrderQueue queue,
                             ProducerThread producer,
                             List<ServingStaff> staffModels,
                             SimulationService simulationService,
                             Map<String, MenuItem> menuMap) {
        this.queue = queue;
        this.producer = producer;
        this.workers = new ArrayList<>();
        this.simulationService = simulationService;
        this.simulationStarted = false;
        this.simulationFinished = false;
        
        if (staffModels == null) {
            this.staffModels = new ArrayList<>();
        } else {
            this.staffModels = staffModels;
        }
        this.reportStaffModels = new ArrayList<>(this.staffModels);

        this.nextStaffNumber = determineNextStaffNumber(this.staffModels);
        
        for (ServingStaff staffModel : this.staffModels) {
            ServingStaffWorker worker =
                    new ServingStaffWorker(staffModel, queue, simulationService);
            workers.add(worker);
        }

        // Initialize the online order producer (file path can be configured)
        String onlineOrdersFile = "AS-Assignment/data/online_orders.csv";
        long onlineInterval = 3000; // One online order every 3 seconds
        this.onlineProducer = new OnlineOrderProducerThread(queue, menuMap, onlineOrdersFile, onlineInterval);
    }

    public void startSimulation() {
        synchronized (workerLock) {
            if (simulationStarted) {
                return;
            }
            simulationStarted = true;
        }

        System.out.println("[Manager] Starting simulation...");
        producer.start();
        if (onlineProducer != null) {
            onlineProducer.start();
        }

        List<ServingStaffWorker> snapshot;
        synchronized (workerLock) {
            snapshot = new ArrayList<>(workers);
        }
        
        for (ServingStaffWorker worker : snapshot) {
            worker.start();
        }
        publishStaffChange();
    }

    public void awaitCompletion() throws InterruptedException {
        producer.join();
        if (onlineProducer != null) {
            onlineProducer.join();
        }
        while (true) {
            List<ServingStaffWorker> snapshot;
            synchronized (workerLock) {
                snapshot = new ArrayList<>(workers);
            }
            for (ServingStaffWorker worker : snapshot) {
                worker.join();
            }
            synchronized (workerLock) {
                boolean allJoined = true;
                for (ServingStaffWorker worker : workers) {
                    if (worker.isAlive()) {
                        allJoined = false;
                        break;
                    }
                }
                if (allJoined) {
                    break;
                }
            }
        }
        simulationFinished = true;
        System.out.println("[Manager] All workers finished.");

        if (simulationService != null) {
            simulationService.setFinished(true);
            simulationService.notifyAllObservers(queue.getQueueSnapshot());
        }
        
        generateSimpleReport();
    }
    
    public String addServingStaff() {
        ServingStaff newStaff;
        ServingStaffWorker newWorker;

        synchronized (workerLock) {
            if (simulationFinished) {
                return null;
            }

            String staffId = "Server-" + nextStaffNumber++;
            newStaff = new ServingStaff(staffId);
            synchronized (staffModels) {
                staffModels.add(newStaff);
            }
            synchronized (reportStaffModels) {
                reportStaffModels.add(newStaff);
            }

            newWorker = new ServingStaffWorker(newStaff, queue, simulationService);
            workers.add(newWorker);

            if (simulationStarted) {
                newWorker.start();
            }
        }

        EventLogger.getInstance().log("Information: add a staff member  " + newStaff.getStaffId());
        publishStaffChange();
        return newStaff.getStaffId();
    }
    
    public String removeOneIdleStaff() {
        ServingStaffWorker candidate = null;
        String removedStaffId = null;

        synchronized (workerLock) {
            if (simulationFinished || countActiveStaffLocked() <= 1) {
                return null;
            }

            for (int i = workers.size() - 1; i >= 0; i--) {
                ServingStaffWorker worker = workers.get(i);
                if (worker == null || worker.isStopRequested()) {
                    continue;
                }
                if (!simulationStarted && !worker.isAlive()) {
                    candidate = worker;
                    break;
                }
                if (worker.isAlive() && worker.isIdleSafeToRemove()) {
                    candidate = worker;
                    break;
                }
            }

            if (candidate == null) {
                return null;
            }

            removedStaffId = candidate.getStaff().getStaffId();
            candidate.requestStop();

            if (!simulationStarted && !candidate.isAlive()) {
                workers.remove(candidate);
            }
        }

        synchronized (staffModels) {
            removeStaffModelByIdLocked(removedStaffId);
        }

        EventLogger.getInstance().log("Information: need to removal a staff member: " + removedStaffId);
        queue.signalStateChange();
        publishStaffChange();
        cleanupWorkerWhenStopped(candidate);
        return removedStaffId;
    }
    
    private void cleanupWorkerWhenStopped(ServingStaffWorker worker) {
        if (worker == null || !worker.isAlive()) {
            return;
        }

        Thread cleaner = new Thread(() -> {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (workerLock) {
                workers.remove(worker);
            }
        }, "staff-cleaner-" + worker.getStaff().getStaffId());

        cleaner.setDaemon(true);
        cleaner.start();
    }
    
    private int countActiveStaffLocked() {
        int count = 0;
        for (ServingStaffWorker worker : workers) {
            if (worker != null && !worker.isStopRequested()) {
                count++;
            }
        }
        return count;
    }
    
    private void removeStaffModelByIdLocked(String staffId) {
        for (int i = 0; i < staffModels.size(); i++) {
            if (staffId.equals(staffModels.get(i).getStaffId())) {
                staffModels.remove(i);
                return;
            }
        }
    }
    
    private void publishStaffChange() {
        if (simulationService != null) {
            simulationService.notifyServerObservers(queue.getQueueSnapshot());
        }
    }
    
    private int determineNextStaffNumber(List<ServingStaff> initialStaff) {
        int maxNumber = 0;
        for (ServingStaff staff : initialStaff) {
            String staffId = staff.getStaffId();
            int dashIndex = staffId.lastIndexOf('-');
            if (dashIndex >= 0 && dashIndex < staffId.length() - 1) {
                try {
                    int number = Integer.parseInt(staffId.substring(dashIndex + 1));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return maxNumber + 1;
    }

    private void generateSimpleReport() {
        System.out.println("\n===== SIMULATION REPORT =====");
        System.out.println("Total orders processed: " + staffModels.stream()
                .mapToInt(ServingStaff::getProcessedCount).sum());
        for (ServingStaff staff : staffModels) {
            System.out.println(staff.getStaffId() + " processed "
                    + staff.getProcessedCount() + " orders, total time: "
                    + staff.getTotalProcessingTimeMs() / 1000 + "s");
        }
        System.out.println("=============================\n");

        try {
            EventLogger.getInstance().writeToFile("simulation_log.txt");
        } catch (Exception e) {
            System.out.println("[Manager] Failed to write log: " + e.getMessage());
        }
    }
}
