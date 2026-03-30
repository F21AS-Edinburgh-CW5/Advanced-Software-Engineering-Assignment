package coffeeshop.gui;

import coffeeshop.logging.EventLogger;
import coffeeshop.model.SimulationSnapshot;
import coffeeshop.service.QueueObserver;
import coffeeshop.service.ServerObserver;
import coffeeshop.service.SimulationService;
import coffeeshop.simulation.SimulationManager;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * Main simulation window for Stage 2.
 * Layout: QueuePanel (left) + StaffPanel (right) + ControlPanel (bottom).
 * Implements QueueObserver and ServerObserver to receive simulation updates.
 *
 * Stage 1 ordering GUI has been superseded by the Stage 2 simulation GUI.
 */
public class MainFrame extends JFrame implements QueueObserver, ServerObserver {

    private final SimulationService simulationService;
    private final ControlPanel controlPanel;


    private final QueuePanel queuePanel;


    private final StaffPanel staffPanel;

    public MainFrame(SimulationService simulationService, SimulationManager simulationManager) {
        this.simulationService = simulationService;

        setTitle("Coffee Shop Simulation");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onExit();
            }
        });

        queuePanel = new QueuePanel();

        staffPanel = new StaffPanel();

        controlPanel = new ControlPanel();

        initUI();
        SimulationController controller = new SimulationController(simulationManager, simulationService);
        controller.registerObservers(this, staffPanel);
        controlPanel.setStartAction(controller::start);


    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(queuePanel);
        centerPanel.add(staffPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    @Override
    public void onQueueChanged(SimulationSnapshot snapshot) {
        queuePanel.onQueueChanged(snapshot);
    }

    @Override
    public void onServerStateChanged(SimulationSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            // TODO: delegate to StaffPanel when ready
            staffPanel.removeAll();
            staffPanel.revalidate();
            staffPanel.repaint();
        });
    }

    private void onExit() {
        try {
            EventLogger.getInstance().writeToFile("simulation_log.txt");
        } catch (Exception ex) {
            System.out.println("[MainFrame] Failed to write log: " + ex.getMessage());
        }
        dispose();
        System.exit(0);
    }
}


/*
package coffeeshop.gui;

import coffeeshop.api.MenuItemView;
import coffeeshop.api.CoffeeShopService;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {

    private final CoffeeShopService service;
    private JList<MenuItemView> menuList;
    private JPanel centerPanel;

    public MainFrame(CoffeeShopService service) {
        this.service = service;

        setTitle("Coffee Shop Ordering System");
        setSize(600, 400);
        setLocationRelativeTo(null);
        // Use DO_NOTHING so we can generate report before closing
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        initUI();
    }

    private void initUI() {

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Coffee Shop Menu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        // ===== Menu list (multi-select) =====
        DefaultListModel<MenuItemView> listModel = new DefaultListModel<>();
        for (MenuItemView item : service.getMenuItems()) {
            listModel.addElement(item);
        }

        menuList = new JList<>(listModel);
        menuList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(menuList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton generateButton = new JButton("Generate Bill");
        JButton exitButton = new JButton("Exit & Generate Report");
        generateButton.addActionListener(e -> onGenerateBill());
        exitButton.addActionListener(e -> onExit());

        buttonPanel.add(generateButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onGenerateBill() {

        List<MenuItemView> selected = menuList.getSelectedValuesList();

        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one item.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // extract IDs
        java.util.List<String> selectedIds = new java.util.ArrayList<>();
        for (MenuItemView item : selected) {
            selectedIds.add(item.getId());
        }

        // call service
        coffeeshop.api.Bill bill = service.calculateBill(selectedIds);

        // display bill with item details
        StringBuilder message = new StringBuilder();
        message.append("Items:\n");
        for (coffeeshop.api.Bill.LineItem li : bill.getItems()) {
            message.append(String.format("  %s - %s  £%.2f\n",
                    li.getId(), li.getDescription(), li.getPrice()));
        }
        message.append(String.format("\nSubtotal: £%.2f\n", bill.getSubtotal()));
        message.append(String.format("Discount: £%.2f\n", bill.getDiscount()));
        message.append(String.format("Total:    £%.2f\n\n", bill.getTotal()));
        message.append("Rule: ").append(bill.getDiscountRuleApplied());

        JOptionPane.showMessageDialog(this,
                message.toString(),
                "Bill",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onExit() {
        // trigger report generation
        service.generateReport();

        JOptionPane.showMessageDialog(this,
                "Report generated. See reports/report.txt",
                "Report",
                JOptionPane.INFORMATION_MESSAGE);

        // close and exit
        dispose();
        System.exit(0);
    }
}
*/
