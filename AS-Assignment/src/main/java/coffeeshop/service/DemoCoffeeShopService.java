package coffeeshop.service;

import coffeeshop.api.Bill;
import coffeeshop.api.CoffeeShopService;
import coffeeshop.api.MenuItemView;

import java.util.ArrayList;
import java.util.List;

public class DemoCoffeeShopService implements CoffeeShopService {

    @Override
    public List<MenuItemView> getMenuItems() {
        List<MenuItemView> items = new ArrayList<>();
        items.add(new MenuItemView("1", "Espresso", "Coffee", 3.0));
        items.add(new MenuItemView("2", "Latte", "Coffee", 4.5));
        items.add(new MenuItemView("3", "Green Tea", "Tea", 2.5));
        return items;
    }

    @Override
    public Bill calculateBill(List<String> selectedItemIds) {
        // demo: 简单把选中的 items 价格加起来
        List<MenuItemView> menu = getMenuItems();

        double subtotal = 0.0;
        List<Bill.LineItem> lineItems = new ArrayList<>();

        for (String id : selectedItemIds) {
            for (MenuItemView m : menu) {
                if (m.getId().equals(id)) {
                    subtotal += m.getPrice();
                    lineItems.add(new Bill.LineItem(m.getId(), m.getDescription(), m.getPrice()));
                    break;
                }
            }
        }

        // demo discount：满10减2（你截图里刚好是这个效果）
        double discount = subtotal >= 10.0 ? 2.0 : 0.0;
        double total = subtotal - discount;
        String rule = discount > 0 ? "Demo Discount" : "No Discount";

        return new Bill(lineItems, subtotal, discount, total, rule);
    }

    @Override
    public void generateReport() {
        // 输出到项目根目录下的 reports/report.txt
        java.nio.file.Path dir = java.nio.file.Paths.get("reports");
        java.nio.file.Path file = dir.resolve("report.txt");

        try {
            java.nio.file.Files.createDirectories(dir);

            List<MenuItemView> menu = getMenuItems();

            StringBuilder sb = new StringBuilder();
            sb.append("=== Coffee Shop Report ===\n\n");

            sb.append("1) Menu items\n");
            for (MenuItemView m : menu) {
                sb.append(String.format("- %s | %s | %s | £%.2f%n",
                        m.getId(), m.getCategory(), m.getDescription(), m.getPrice()));
            }
            sb.append("\n");

            sb.append("2) Demo summary\n");
            sb.append("This is a demo service report for Stage 1.\n");
            sb.append("Replace this with real stats when OrderLoader/real service is integrated.\n");

            java.nio.file.Files.writeString(
                    file,
                    sb.toString(),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            System.out.println("[REPORT] Written to: " + file.toAbsolutePath());

        } catch (Exception e) {
            // 不要让程序退出失败：report 写不了就给提示
            System.err.println("[REPORT] Failed to write report: " + e.getMessage());
        }
    }
}