package coffeeshop.gui;

import coffeeshop.simulation.SimulationConfig;
import javax.swing.*;
import java.awt.FlowLayout;

/**
 * Bottom control panel containing simulation controls.
 * Start button triggers SimulationController.start().
 * Speed slider is used to adjust simulation speed.
 */
public class ControlPanel extends JPanel {

    private final JButton startButton;
    private final JButton addStaffButton;
    private final JButton removeStaffButton;
    private final JSlider speedSlider;
    private final JLabel statusLabel;

    public ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        startButton = new JButton("Start Simulation");
        addStaffButton = new JButton("Add Staff");
        removeStaffButton = new JButton("Remove Staff");
        
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setToolTipText("Simulation speed: 0.25x - 4.0x");
        speedSlider.setEnabled(true);
        
        speedSlider.addChangeListener(e -> {
            if (!speedSlider.getValueIsAdjusting()) {
                int val = speedSlider.getValue();
                double speed = 0.25 + (val - 1) * (4.0 - 0.25) / 9.0;
                SimulationConfig.setSpeedMultiplier(speed);
            }
        });
        
        statusLabel = new JLabel("Information：OK！");
        add(startButton);
        add(addStaffButton);
        add(removeStaffButton);
        add(new JLabel("Speed:"));
        add(speedSlider);
        add(statusLabel);
    }

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
