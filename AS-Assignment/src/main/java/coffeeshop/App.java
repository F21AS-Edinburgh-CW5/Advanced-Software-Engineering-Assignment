package coffeeshop;

import coffeeshop.api.CoffeeShopService;
import coffeeshop.gui.MainFrame;
import coffeeshop.service.DemoCoffeeShopService;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Temporary demo service
        CoffeeShopService service = new DemoCoffeeShopService();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
