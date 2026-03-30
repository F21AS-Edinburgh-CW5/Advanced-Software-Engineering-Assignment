package coffeeshop.gui;

import coffeeshop.model.CustomerOrder;
import coffeeshop.model.SimulationSnapshot;
import coffeeshop.service.QueueObserver;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Panel that displays the current queue of waiting orders.
 * Implements QueueObserver to receive simulation snapshots and updates the UI
 * on the Event Dispatch Thread.
 */
public class QueuePanel extends JPanel implements QueueObserver {

    private final DefaultListModel<String> listModel;
    private final JList<String> orderList;
    private final JScrollPane scrollPane;

    public QueuePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Queue (waiting)"));

        listModel = new DefaultListModel<>();
        orderList = new JList<>(listModel);
        orderList.setFont(orderList.getFont().deriveFont(12f));

        scrollPane = new JScrollPane(orderList);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void onQueueChanged(SimulationSnapshot snapshot) {
        // Ensure UI updates happen on the EDT
        SwingUtilities.invokeLater(() -> refreshQueue(snapshot));
    }

    private void refreshQueue(SimulationSnapshot snapshot) {
        listModel.clear();

        List<CustomerOrder> queue = snapshot.getQueue();
        if (queue == null || queue.isEmpty()) {
            listModel.addElement("(No orders waiting)");
        } else {
            for (CustomerOrder order : queue) {
                String line = String.format("%s (%d item%s)",
                        order.getCustomerId(),
                        order.getItemCount(),
                        order.getItemCount() == 1 ? "" : "s");
                listModel.addElement(line);
            }
        }

        // Scroll to top when new orders arrive
        if (queue != null && !queue.isEmpty()) {
            orderList.ensureIndexIsVisible(0);
        }
    }
}