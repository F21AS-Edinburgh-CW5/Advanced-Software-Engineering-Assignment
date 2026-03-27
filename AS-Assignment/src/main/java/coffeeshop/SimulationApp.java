package coffeeshop;

import coffeeshop.loader.MenuLoader;
import coffeeshop.model.MenuItem;
import coffeeshop.simulation.ProducerThread;
import coffeeshop.simulation.SharedOrderQueue;
import coffeeshop.simulation.SimulationManager;

import java.util.Map;

public class SimulationApp {

    public static void main(String[] args) {
        // 1. 加载菜单
        String menuFile = "AS-Assignment/data/menu.csv";
        String ordersFile = "AS-Assignment/data/orders.csv";

        MenuLoader menuLoader = new MenuLoader();
        Map<String, MenuItem> menuMap = menuLoader.load(menuFile);
        if (menuMap.isEmpty()) {
            System.err.println("Failed to load menu. Exiting.");
            return;
        }
        System.out.println("Loaded " + menuMap.size() + " menu items.");

        // 2. 创建共享队列
        SharedOrderQueue queue = new SharedOrderQueue();

        // 3. 创建生产者线程（间隔 2000 ms）
        long intervalMs = 2000;
        ProducerThread producer = new ProducerThread(queue, menuMap, ordersFile, intervalMs);

        // 4. 创建仿真管理器（2个服务员）
        int workerCount = 2;
        SimulationManager manager = new SimulationManager(queue, producer, workerCount);

        // 5. 启动并等待完成
        try {
            manager.startSimulation();
            manager.awaitCompletion();
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted.");
            Thread.currentThread().interrupt();
        }

        System.out.println("Simulation finished.");
    }
}
