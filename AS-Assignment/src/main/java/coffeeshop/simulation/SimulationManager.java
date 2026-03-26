package coffeeshop.simulation;

import coffeeshop.model.ServingStaff;
import coffeeshop.model.StaffStatus;
import coffeeshop.report.ReportGenerator;

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

    public SimulationManager(SharedOrderQueue queue,
                             ProducerThread producer,
                             int workerCount) {
        this.queue = queue;
        this.producer = producer;
        this.workers = new ArrayList<>();
        this.staffModels = new ArrayList<>();

        for (int i = 0; i < workerCount; i++) {
            String staffId = "Server-" + (i + 1);
            ServingStaff staffModel = new ServingStaff(staffId);
            staffModels.add(staffModel);
            ServingStaffWorker worker = new ServingStaffWorker(queue, staffModel);
            workers.add(worker);
        }
    }

    public void startSimulation() {
        System.out.println("[Manager] Starting simulation...");
        producer.start();
        for (ServingStaffWorker worker : workers) {
            worker.start();
        }
    }

    public void awaitCompletion() throws InterruptedException {
        // 等待生产者结束
        producer.join();

        // 等待所有服务员线程结束
        for (ServingStaffWorker worker : workers) {
            worker.join();
        }
        System.out.println("[Manager] All workers finished.");

        // 生成报告（目前仅打印，可后续集成 ReportGenerator）
        generateSimpleReport();

        // 注：日志写入由 EventLogger 负责，此处不处理
    }

    /**
     * 简单报告（Iteration 1 控制台输出）
     */
    private void generateSimpleReport() {
        System.out.println("\n===== SIMULATION REPORT =====");
        System.out.println("Total orders processed: " + staffModels.stream()
                .mapToInt(ServingStaff::getProcessedCount).sum());
        for (ServingStaff staff : staffModels) {
            System.out.println(staff.getStaffId() + " processed " + staff.getProcessedCount() + " orders.");
        }
        System.out.println("=============================\n");
    }
}