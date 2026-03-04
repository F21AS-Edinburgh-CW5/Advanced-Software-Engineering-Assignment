package coffeeshop.gui;

import coffeeshop.api.CoffeeShopService;
import coffeeshop.api.MenuItemView;
import coffeeshop.api.Bill;

import javax.swing.*;
import java.util.List;

public class App {

    public static void main(String[] args) {

        // temporal Fake Service
        CoffeeShopService fakeService = new CoffeeShopService() {

            @Override
            public List<MenuItemView> getMenuItems() {
                return List.of(
                        new MenuItemView("1", "Espresso", "Coffee", 3.0),
                        new MenuItemView("2", "Latte", "Coffee", 4.5),
                        new MenuItemView("3", "Green Tea", "Tea", 2.5)
                );
            }

            @Override
            public Bill calculateBill(List<String> selectedItemIds) {
                return new Bill(
                        List.of(),
                        10.0,
                        2.0,
                        8.0,
                        "Demo Discount"
                );
            }

            @Override
            public void generateReport() {
                System.out.println("Report generated.");
            }
        };

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(fakeService);
            frame.setVisible(true);
        });
    }
}