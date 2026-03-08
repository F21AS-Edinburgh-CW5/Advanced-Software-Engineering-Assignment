package coffeeshop;

import coffeeshop.api.CoffeeShopService;
import coffeeshop.gui.MainFrame;
import coffeeshop.service.CoffeeShopServiceImpl;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        String menuFile = "AS-Assignment/data/menu.csv";
        String ordersFile = "AS-Assignment/data/orders.csv";

        if (args.length >= 2) {
            menuFile = args[0];
            ordersFile = args[1];
        }

        CoffeeShopService service = new CoffeeShopServiceImpl(menuFile, ordersFile);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
