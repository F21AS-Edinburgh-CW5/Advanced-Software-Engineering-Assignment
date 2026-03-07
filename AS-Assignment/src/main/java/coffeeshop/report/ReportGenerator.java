package coffeeshop.report;

import coffeeshop.api.MenuItemView;
import coffeeshop.model.OrderRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class generates the summary report when the app exits
public class ReportGenerator {

    private List<MenuItemView> menuItems;
    private List<OrderRecord> orderRecords;
    private List<Double> orderTotals;

    public ReportGenerator(List<MenuItemView> menuItems,
                           List<OrderRecord> orderRecords,
                           List<Double> orderTotals) {
        if (menuItems == null || orderRecords == null || orderTotals == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        this.menuItems = menuItems;
        this.orderRecords = orderRecords;
        this.orderTotals = orderTotals;
    }

    // Count how many times each item was ordered
    public Map<String, Integer> countItemOrders() {
        Map<String, Integer> counts = new HashMap<>();

        // start all items at 0
        for (MenuItemView item : menuItems) {
            counts.put(item.getId(), 0);
        }

        // go through each order record and add 1
        for (OrderRecord record : orderRecords) {
            String id = record.getItemId();
            if (counts.containsKey(id)) {
                counts.put(id, counts.get(id) + 1);
            }
        }

        return counts;
    }

    // Add up all the order totals to get revenue
    public double calculateTotalRevenue() {
        double total = 0.0;
        for (double t : orderTotals) {
            total += t;
        }
        return total;
    }

    // Build the report as a string
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> itemCounts = countItemOrders();

        sb.append("=============================================================\n");
        sb.append("              COFFEE SHOP SUMMARY REPORT\n");
        sb.append("=============================================================\n\n");

        // Part 1: list all menu items
        sb.append("-------------------------------------------------------------\n");
        sb.append("  MENU ITEMS\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append(String.format("%-12s %-24s %-12s %8s\n", "Item ID", "Description", "Category", "Price"));
        sb.append(String.format("%-12s %-24s %-12s %8s\n", "-------", "-----------", "--------", "-----"));

        for (MenuItemView item : menuItems) {
            sb.append(String.format("%-12s %-24s %-12s £%7.2f\n",
                    item.getId(), item.getDescription(), item.getCategory(), item.getPrice()));
        }
        sb.append("\n");

        // Part 2: how many times each item was ordered
        sb.append("-------------------------------------------------------------\n");
        sb.append("  ORDER SUMMARY\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append(String.format("%-12s %-24s %14s\n", "Item ID", "Description", "Times Ordered"));
        sb.append(String.format("%-12s %-24s %14s\n", "-------", "-----------", "-------------"));

        int totalItemsOrdered = 0;
        for (MenuItemView item : menuItems) {
            int count = itemCounts.getOrDefault(item.getId(), 0);
            sb.append(String.format("%-12s %-24s %14d\n",
                    item.getId(), item.getDescription(), count));
            totalItemsOrdered += count;
        }
        sb.append("\nTotal individual items ordered: " + totalItemsOrdered + "\n");
        sb.append("Total orders processed:         " + orderTotals.size() + "\n\n");

        // Part 3: total revenue
        sb.append("-------------------------------------------------------------\n");
        sb.append("  REVENUE\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append(String.format("Total Revenue (after discounts): £%.2f\n\n", calculateTotalRevenue()));

        sb.append("=============================================================\n");
        sb.append("                    END OF REPORT\n");
        sb.append("=============================================================\n");

        return sb.toString();
    }

    // Print report to console
    public void printReportToConsole() {
        System.out.println(generateReport());
    }

    // Write report to a file
    public void writeReportToFile(String filePath) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath));
        writer.print(generateReport());
        writer.close();
    }
}
