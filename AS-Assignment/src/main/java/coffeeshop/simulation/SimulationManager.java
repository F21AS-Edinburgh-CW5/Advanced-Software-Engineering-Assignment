package coffeeshop.simulation;

import coffeeshop.model.ServingStaff;
import coffeeshop.model.StaffStatus;
import coffeeshop.report.ReportGenerator;
import coffeeshop.logging.EventLogger;
import coffeeshop.service.SimulationService;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟管理器：启动所有线程，等待仿真结束，生成报告并清理日志。
 */
public class SimulationManager {
    private final SharedOrderQueue queue;
    private final ProducerThread producer;
    private final List<ServingStaffWorker> workers;
    private final List<ServingStaff> staffModels;   // 用于报告统计
    private final List<ServingStaff> reportStaffModels;
    private final SimulationService simulationService;

    private final Object workerLock = new Object();
    private int nextStaffNumber;
    private volatile boolean simulationStarted;
    private volatile boolean simulationFinished;
    
    public SimulationManager(SharedOrderQueue queue,
                             ProducerThread producer,
                             int workerCount) {
        this.queue = queue;
        this.producer = producer;
        this.workers = new ArrayList<>();
        this.staffModels = new ArrayList<>();
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
    }

    //overwrite for iteration 2
    public SimulationManager(SharedOrderQueue queue,
                             ProducerThread producer,
                             List<ServingStaff> staffModels,
                             SimulationService simulationService) {
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
        
        for (ServingStaff staffModel : this.staffModels) {
            ServingStaffWorker worker =
                    new ServingStaffWorker(staffModel, queue, simulationService);
            workers.add(worker);
        }
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
        // 等待生产者结束
        producer.join();
        while (true) {
        List<ServingStaffWorker> snapshot;
            synchronized (workerLock) {
                snapshot = new ArrayList<>(workers);
            }
        // 等待所有服务员线程结束
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
        
        // 生成报告（目前仅打印，可后续集成 ReportGenerator）
        generateSimpleReport();

        // 注：日志写入由 EventLogger 负责，此处不处理
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

        EventLogger.getInstance().log("Information:need to removal a staff member: " + removedStaffId);
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

    /**
     * 简单报告（Iteration 1 控制台输出）
     */
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

        // 写入EventLogger日志文件
        try {
            EventLogger.getInstance().writeToFile("simulation_log.txt");
        } catch (Exception e) {
            System.out.println("[Manager] Failed to write log: " + e.getMessage());
        }
    }
}
