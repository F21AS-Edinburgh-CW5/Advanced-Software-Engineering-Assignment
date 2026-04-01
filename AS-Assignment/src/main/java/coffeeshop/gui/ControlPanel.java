package coffeeshop.gui;

import javax.swing.*;
import java.awt.FlowLayout;

/**
 * Bottom control panel containing simulation controls.
 * Start button triggers SimulationController.start().
 * Speed slider is reserved for Iteration 3.
 */
public class ControlPanel extends JPanel {

    private final JButton startButton;
    private final JButton addStaffButton;
    private final JButton removeStaffButton;
    // Speed slider reserved for Iter 3
    private final JSlider speedSlider;
    private final JLabel statusLabel;
    public ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        startButton = new JButton("Start Simulation");
        addStaffButton = new JButton("Add Staff");
        removeStaffButton = new JButton("Remove  Staff");
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setEnabled(false); // disabled until Iter 3
        speedSlider.setToolTipText("Simulation speed (coming in Iter 3)");
        statusLabel = new JLabel("Information：OK！");
        add(startButton);
        add(addStaffButton);
        add(removeStaffButton);
        add(new JLabel("Speed:"));
        add(speedSlider);
        add(statusLabel);
    }

    /**
     * Registers a listener on the Start button.
     * Called by SimulationController to connect the button to start().
     */
    public void setStartAction(Runnable action) {
        startButton.addActionListener(e -> action.run());
    }
   public void setAddStaffAction(Runnable action) {
        addStaffButton.addActionListener(e -> action.run());
    }

    public void setRemoveStaffAction(Runnable action) {
        removeStaffButton.addActionListener(e -> action.run());
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message == null || message.isBlank() ? "Ready." : message);
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JSlider getSpeedSlider() {
        return speedSlider;
    }
    public JButton getAddStaffButton() {
        return addStaffButton;
    }
    public JButton getRemoveStaffButton() {
        return removeStaffButton;
    }

}
