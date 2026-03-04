package coffeeshop;

import coffeeshop.api.CoffeeShopService;
import coffeeshop.gui.MainFrame;
import coffeeshop.service.DemoCoffeeShopService;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // 临时 demo service（下一步我们会创建这个类）
        CoffeeShopService service = new DemoCoffeeShopService();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}