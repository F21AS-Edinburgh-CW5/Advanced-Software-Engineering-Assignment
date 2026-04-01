package coffeeshop;

import coffeeshop.loader.MenuLoader;
import coffeeshop.model.MenuItem;
import coffeeshop.simulation.ProducerThread;
import coffeeshop.simulation.SharedOrderQueue;
import coffeeshop.simulation.SimulationManager;

import java.util.Map;

public class SimulationApp {

    public static void main(String[] args) {

        String menuFile = "AS-Assignment/data/menu.csv";
        String ordersFile = "AS-Assignment/data/orders.csv";

        MenuLoader menuLoader = new MenuLoader();
        Map<String, MenuItem> menuMap = menuLoader.load(menuFile);
        if (menuMap.isEmpty()) {
            System.err.println("Failed to load menu. Exiting.");
            return;
        }
        System.out.println("Loaded " + menuMap.size() + " menu items.");


        SharedOrderQueue queue = new SharedOrderQueue();


        long intervalMs = 2000;
        ProducerThread producer = new ProducerThread(queue, menuMap, ordersFile, intervalMs);


        int workerCount = 2;
        SimulationManager manager = new SimulationManager(queue, producer, workerCount);


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
