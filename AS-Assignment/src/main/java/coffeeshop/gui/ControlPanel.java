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
    // Speed slider reserved for Iter 3
    private final JSlider speedSlider;

    public ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        startButton = new JButton("Start Simulation");
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setEnabled(false); // disabled until Iter 3
        speedSlider.setToolTipText("Simulation speed (coming in Iter 3)");

        add(startButton);
        add(new JLabel("Speed:"));
        add(speedSlider);
    }

    /**
     * Registers a listener on the Start button.
     * Called by SimulationController to connect the button to start().
     */
    public void setStartAction(Runnable action) {
        startButton.addActionListener(e -> action.run());
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JSlider getSpeedSlider() {
        return speedSlider;
    }
}