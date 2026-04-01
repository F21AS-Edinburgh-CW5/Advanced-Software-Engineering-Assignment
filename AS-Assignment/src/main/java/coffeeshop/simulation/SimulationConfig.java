package coffeeshop.simulation;

import coffeeshop.logging.EventLogger;

public class SimulationConfig {
    // volatile  ensures thread-safety in multi-threading.
    private static volatile double speedMultiplier = 1.0;

    public static double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public static void setSpeedMultiplier(double multiplier) {
        if (multiplier < 0.25) multiplier = 0.25;
        if (multiplier > 4.0) multiplier = 4.0;
        speedMultiplier = multiplier;
        EventLogger.getInstance().log("Speed multiplier set to " + multiplier);
    }
}