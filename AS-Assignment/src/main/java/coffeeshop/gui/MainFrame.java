package coffeeshop.gui;

import coffeeshop.api.MenuItemView;
import coffeeshop.api.CoffeeShopService;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

public class MainFrame extends JFrame {

    private final CoffeeShopService service;
    private JList<MenuItemView> menuList;
    private JPanel centerPanel;

    public MainFrame(CoffeeShopService service) {
        this.service = service;



        setTitle("Coffee Shop Ordering System");
        setSize(600, 400);
        setLocationRelativeTo(null); // 居中
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        JButton exitButton = new JButton("Exit");
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

        // display bill (simple dialog)
        String message =
                "Subtotal: £" + bill.getSubtotal() + "\n" +
                        "Discount: £" + bill.getDiscount() + "\n" +
                        "Total: £" + bill.getTotal() + "\n\n" +
                        "Rule: " + bill.getDiscountRuleApplied();

        JOptionPane.showMessageDialog(this,
                message,
                "Bill",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onExit() {
        // trigger report generation
        service.generateReport();

        // close window
        dispose();
    }
}