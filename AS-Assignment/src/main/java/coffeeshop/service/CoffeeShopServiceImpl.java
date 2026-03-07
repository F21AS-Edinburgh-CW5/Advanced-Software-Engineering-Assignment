package coffeeshop.service;

import coffeeshop.api.Bill;
import coffeeshop.api.CoffeeShopService;
import coffeeshop.api.DiscountCalculator;
import coffeeshop.api.MenuItemView;
import coffeeshop.loader.MenuLoader;
import coffeeshop.loader.OrderLoader;
import coffeeshop.model.MenuItem;
import coffeeshop.model.OrderRecord;
import coffeeshop.report.ReportGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real implementation of CoffeeShopService that:
 * - loads menu and orders from CSV files via MenuLoader / OrderLoader
 * - converts data_loader models to API-layer DTOs (MenuItemView)
 * - calculates bills using DiscountCalculator
 * - tracks all orders (file-loaded + GUI-placed) for report generation
 * - generates a summary report on exit via ReportGenerator
 *
 * @author Group 5
 */
public class CoffeeShopServiceImpl implements CoffeeShopService {

    private final Map<String, MenuItem> menuData;          // raw loaded menu
    private final List<MenuItemView> menuItemViews;        // converted for API/GUI
    private final List<OrderRecord> allOrderRecords;       // all order records
    private final List<Double> allOrderTotals;             // post-discount total per order
    private final DiscountCalculator discountCalculator;

    /**
     * Constructs the service by loading data from the given CSV files.
     *
     * @param menuCsvPath   path to the menu CSV file
     * @param ordersCsvPath path to the orders CSV file
     */
    public CoffeeShopServiceImpl(String menuCsvPath, String ordersCsvPath) {
        this.discountCalculator = new DiscountCalculator();
        this.allOrderTotals = new ArrayList<>();

        // 1. Load menu
        MenuLoader menuLoader = new MenuLoader();
        this.menuData = menuLoader.load(menuCsvPath);

        // 2. Convert to MenuItemView list for API layer
        this.menuItemViews = new ArrayList<>();
        for (MenuItem item : menuData.values()) {
            menuItemViews.add(new MenuItemView(
                    item.getId(),
                    item.getDescription(),
                    item.getCategory(),
                    item.getCost()
            ));
        }

        // 3. Load existing orders
        OrderLoader orderLoader = new OrderLoader();
        this.allOrderRecords = new ArrayList<>(orderLoader.load(ordersCsvPath));

        // 4. Process existing orders: group by customer, calculate totals
        processExistingOrders();

        System.out.println("[Service] Loaded " + menuData.size() + " menu items, "
                + allOrderRecords.size() + " order records.");
    }

    /**
     * Groups existing order records by customer ID and calculates a bill total
     * for each customer's combined order, so revenue is tracked from startup.
     */
    private void processExistingOrders() {
        // Group order records by customer
        Map<String, List<OrderRecord>> byCustomer = new HashMap<>();
        for (OrderRecord rec : allOrderRecords) {
            byCustomer.computeIfAbsent(rec.getCustomerId(), k -> new ArrayList<>()).add(rec);
        }

        // For each customer, build their item list and calculate the bill
        for (Map.Entry<String, List<OrderRecord>> entry : byCustomer.entrySet()) {
            List<MenuItemView> items = new ArrayList<>();
            for (OrderRecord rec : entry.getValue()) {
                MenuItem mi = menuData.get(rec.getItemId());
                if (mi != null) {
                    items.add(new MenuItemView(
                            mi.getId(), mi.getDescription(),
                            mi.getCategory(), mi.getCost()));
                }
            }
            if (!items.isEmpty()) {
                double subtotal = items.stream().mapToDouble(MenuItemView::getPrice).sum();
                DiscountCalculator.DiscountResult dr = discountCalculator.calculateDiscount(items);
                double total = subtotal - dr.getDiscountAmount();
                allOrderTotals.add(total);
            }
        }
    }

    @Override
    public List<MenuItemView> getMenuItems() {
        return menuItemViews;
    }

    @Override
    public Bill calculateBill(List<String> selectedItemIds) {
        List<Bill.LineItem> lineItems = new ArrayList<>();
        List<MenuItemView> selectedViews = new ArrayList<>();

        for (String id : selectedItemIds) {
            MenuItem mi = menuData.get(id);
            if (mi != null) {
                lineItems.add(new Bill.LineItem(mi.getId(), mi.getDescription(), mi.getCost()));
                selectedViews.add(new MenuItemView(
                        mi.getId(), mi.getDescription(),
                        mi.getCategory(), mi.getCost()));
            }
        }

        double subtotal = selectedViews.stream().mapToDouble(MenuItemView::getPrice).sum();
        DiscountCalculator.DiscountResult dr = discountCalculator.calculateDiscount(selectedViews);
        double discount = dr.getDiscountAmount();
        double total = subtotal - discount;

        // Track this order
        // Create order records for report tracking
        String custId = "GUI-" + System.currentTimeMillis();
        String timestamp = java.time.LocalDateTime.now().toString();
        for (String itemId : selectedItemIds) {
            allOrderRecords.add(new OrderRecord(timestamp, custId, itemId));
        }
        allOrderTotals.add(total);

        return new Bill(lineItems, subtotal, discount, total, dr.getRuleApplied());
    }

    @Override
    public void generateReport() {
        ReportGenerator rg = new ReportGenerator(menuItemViews, allOrderRecords, allOrderTotals);

        // Print to console
        rg.printReportToConsole();

        // Write to file
        Path dir = Paths.get("reports");
        Path file = dir.resolve("report.txt");
        try {
            Files.createDirectories(dir);
            rg.writeReportToFile(file.toString());
            System.out.println("[REPORT] Written to: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[REPORT] Failed to write report: " + e.getMessage());
        }
    }
}
