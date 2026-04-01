package coffeeshop;

import coffeeshop.gui.MainFrame;
import coffeeshop.loader.MenuLoader;
import coffeeshop.model.MenuItem;
import coffeeshop.model.ServingStaff;
import coffeeshop.service.SimulationService;
import coffeeshop.simulation.ProducerThread;
import coffeeshop.simulation.SharedOrderQueue;
import coffeeshop.simulation.SimulationManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        String menuFile = "AS-Assignment/data/menu.csv";
        String ordersFile = "AS-Assignment/data/orders.csv";

        if (args.length >= 2) {
            menuFile = args[0];
            ordersFile = args[1];
        }

        // Load menu
        MenuLoader menuLoader = new MenuLoader();
        Map<String, MenuItem> menuMap = menuLoader.load(menuFile);

        // Build staff list
        List<ServingStaff> staffList = new ArrayList<>();
        staffList.add(new ServingStaff("Server-1"));
        staffList.add(new ServingStaff("Server-2"));

        // Build simulation components
        SharedOrderQueue queue = new SharedOrderQueue();
        ProducerThread producer = new ProducerThread(queue, menuMap, ordersFile, 2000);
        SimulationService simulationService = new SimulationService(staffList);
        queue.setSimulationService(simulationService);

        // Pass menuMap to SimulationManager
        SimulationManager manager =
                new SimulationManager(queue, producer, staffList, simulationService, menuMap);

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(simulationService, manager);
            frame.setVisible(true);
        });
    }
}
