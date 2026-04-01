package coffeeshop.gui;

import coffeeshop.api.Bill;
import coffeeshop.api.DiscountCalculator;
import coffeeshop.api.MenuItemView;
import coffeeshop.model.CustomerOrder;
import coffeeshop.model.ServingStaff;
import coffeeshop.model.SimulationSnapshot;
import coffeeshop.model.StaffStatus;
import coffeeshop.service.ServerObserver;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * StaffPanel displays one column per staff member and updates itself from
 * simulation snapshots received through ServerObserver.
 */
public class StaffPanel extends JPanel implements ServerObserver {

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font STATUS_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font DETAIL_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    private final Map<String, StaffColumn> columnsById = new LinkedHashMap<>();
    private final DiscountCalculator discountCalculator = new DiscountCalculator();

    public StaffPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createTitledBorder("Serving Staff"));
        setPreferredSize(new Dimension(560, 320));
    }

    /**
     * Receives staff updates and dispatches UI work to the Swing EDT.
     */
    @Override
    public void onServerStateChanged(SimulationSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            applySnapshot(snapshot);
        } else {
            SwingUtilities.invokeLater(() -> applySnapshot(snapshot));
        }
    }

    private void applySnapshot(SimulationSnapshot snapshot) {
        List<ServingStaff> staffList = snapshot.getStaffList();
        if (staffList == null) {
            return;
        }

        List<String> visibleStaffIds = new ArrayList<>();
        
        for (ServingStaff staff : staffList) {
            if (staff == null) {
                continue;
            }

            String staffId = safe(staff.getStaffId());
            if (staffId.isEmpty()) {
                continue;
            }
            visibleStaffIds.add(staffId);
            if (!columnsById.containsKey(staffId)) {
                StaffColumn column = new StaffColumn(staffId);
                columnsById.put(staffId, column);
                add(column);
            }
        }
        List<String> idsToRemove = new ArrayList<>();
        for (String existingId : columnsById.keySet()) {
            if (!visibleStaffIds.contains(existingId)) {
                idsToRemove.add(existingId);
            }
        }
        for (String id : idsToRemove) {
            StaffColumn column = columnsById.remove(id);
            if (column != null) {
                remove(column);
            }
        }
    
        for (ServingStaff staff : staffList) {
            if (staff == null) {
                continue;
            }

            StaffColumn column = columnsById.get(safe(staff.getStaffId()));
            if (column != null) {
                column.update(staff);
            }
        }

        revalidate();
        repaint();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String pounds(double value) {
        return "£" + MONEY.format(value);
    }

    private class StaffColumn extends JPanel {

        private final JLabel headerLabel;
        private final JLabel statusLabel;
        private final JTextArea detailsArea;

        StaffColumn(String staffId) {
            setLayout(new BorderLayout(6, 6));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(6, 6, 6, 6),
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY)
            ));
            setPreferredSize(new Dimension(220, 300));

            headerLabel = new JLabel("Server " + staffId);
            headerLabel.setFont(TITLE_FONT);
            add(headerLabel, BorderLayout.NORTH);

            statusLabel = new JLabel();
            statusLabel.setFont(STATUS_FONT);
            statusLabel.setOpaque(true);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            add(statusLabel, BorderLayout.SOUTH);

            detailsArea = new JTextArea();
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setFont(DETAIL_FONT);

            JScrollPane scrollPane = new JScrollPane(detailsArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);
        }

        void update(ServingStaff staff) {
            StaffStatus status = staff.getStatus();
            if (status == null) {
                status = StaffStatus.IDLE;
            }

            statusLabel.setText("Status: " + status.name());

            switch (status) {
                case PROCESSING -> {
                    statusLabel.setBackground(new Color(198, 239, 206));
                    statusLabel.setForeground(Color.DARK_GRAY);
                    detailsArea.setText(buildProcessingDetails(staff.getCurrentOrder()));
                    detailsArea.setCaretPosition(0);
                }
                case WAITING -> {
                    statusLabel.setBackground(new Color(255, 242, 204));
                    statusLabel.setForeground(Color.DARK_GRAY);
                    detailsArea.setText("Waiting for the next order...");
                }
                case IDLE -> {
                    statusLabel.setBackground(new Color(242, 242, 242));
                    statusLabel.setForeground(Color.DARK_GRAY);
                    detailsArea.setText("Idle");
                }
            }
        }

        private String buildProcessingDetails(CustomerOrder order) {
            if (order == null) {
                return "Processing order...\n(Order details unavailable)";
            }

            Bill bill = createBillFromOrder(order);
            StringBuilder builder = new StringBuilder();

            builder.append("Customer: ")
                    .append(order.getCustomerId())
                    .append("\n\nItems:\n");

            List<MenuItemView> items = order.getItems();
            for (int i = 0; i < items.size(); i++) {
                MenuItemView item = items.get(i);
                builder.append(i + 1)
                        .append(". ")
                        .append(item.getDescription())
                        .append("  ")
                        .append(pounds(item.getPrice()))
                        .append("\n");
            }

            builder.append("\nSubtotal: ").append(pounds(bill.getSubtotal())).append("\n");

            if (bill.getDiscount() > 0.000001) {
                builder.append("Discount: -")
                        .append(pounds(bill.getDiscount()))
                        .append("\nRule: ")
                        .append(bill.getDiscountRuleApplied())
                        .append("\n");
            } else {
                builder.append("Discount: None\n");
            }

            builder.append("\nTotal: ").append(pounds(bill.getTotal()));
            return builder.toString();
        }

        private Bill createBillFromOrder(CustomerOrder order) {
            List<MenuItemView> items = order.getItems();

            List<Bill.LineItem> lineItems = items.stream()
                    .map(item -> new Bill.LineItem(
                            item.getId(),
                            item.getDescription(),
                            item.getPrice()))
                    .toList();

            double subtotal = items.stream().mapToDouble(MenuItemView::getPrice).sum();
            DiscountCalculator.DiscountResult result = discountCalculator.calculateDiscount(items);
            double discount = result.getDiscountAmount();
            double total = subtotal - discount;

            return new Bill(lineItems, subtotal, discount, total, result.getRuleApplied());
        }
    }
}
