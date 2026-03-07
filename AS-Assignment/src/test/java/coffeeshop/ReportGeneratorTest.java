package coffeeshop;

import coffeeshop.api.MenuItemView;
import coffeeshop.model.OrderRecord;
import coffeeshop.report.ReportGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Tests for ReportGenerator
public class ReportGeneratorTest {

    private List<MenuItemView> menu;
    private List<OrderRecord> orders;
    private List<Double> totals;
    private ReportGenerator rg;

    @BeforeEach
    public void setUp() {
        menu = Arrays.asList(
            new MenuItemView("BEV-001", "Espresso", "Beverage", 2.50),
            new MenuItemView("BEV-002", "Latte", "Beverage", 3.80),
            new MenuItemView("FOOD-001", "Croissant", "Food", 2.50),
            new MenuItemView("FOOD-002", "Muffin", "Food", 3.00),
            new MenuItemView("OTH-001", "Water", "Other", 1.50)
        );

        // CUST001 orders Espresso + Croissant
        // CUST002 orders Latte + Muffin + Croissant (gets 20% discount)
        // CUST003 orders Water
        orders = Arrays.asList(
            new OrderRecord("2026-02-10 08:00", "CUST001", "BEV-001"),
            new OrderRecord("2026-02-10 08:00", "CUST001", "FOOD-001"),
            new OrderRecord("2026-02-10 09:00", "CUST002", "BEV-002"),
            new OrderRecord("2026-02-10 09:00", "CUST002", "FOOD-002"),
            new OrderRecord("2026-02-10 09:00", "CUST002", "FOOD-001"),
            new OrderRecord("2026-02-10 10:00", "CUST003", "OTH-001")
        );

        // order totals after discount
        // CUST001: 2.50+2.50 = 5.00 (no discount)
        // CUST002: 3.80+3.00+2.50 = 9.30, 20% off = 7.44
        // CUST003: 1.50 (no discount)
        totals = Arrays.asList(5.00, 7.44, 1.50);

        rg = new ReportGenerator(menu, orders, totals);
    }

    // test that null input throws exception
    @Test
    public void testNullMenuThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new ReportGenerator(null, orders, totals));
    }

    @Test
    public void testNullOrdersThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new ReportGenerator(menu, null, totals));
    }

    @Test
    public void testNullTotalsThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new ReportGenerator(menu, orders, null));
    }

    // test counting
    @Test
    public void testEspressoCount() {
        assertEquals(1, rg.countItemOrders().get("BEV-001"));
    }

    @Test
    public void testCroissantCount() {
        // croissant ordered by CUST001 and CUST002
        assertEquals(2, rg.countItemOrders().get("FOOD-001"));
    }

    @Test
    public void testLatteCount() {
        assertEquals(1, rg.countItemOrders().get("BEV-002"));
    }

    @Test
    public void testWaterCount() {
        assertEquals(1, rg.countItemOrders().get("OTH-001"));
    }

    @Test
    public void testMuffinCount() {
        assertEquals(1, rg.countItemOrders().get("FOOD-002"));
    }

    @Test
    public void testUnorderedItemIsZero() {
        List<MenuItemView> bigMenu = new ArrayList<>(menu);
        bigMenu.add(new MenuItemView("BEV-099", "Rare Tea", "Beverage", 5.00));
        ReportGenerator rg2 = new ReportGenerator(bigMenu, orders, totals);
        assertEquals(0, rg2.countItemOrders().get("BEV-099"));
    }

    // test revenue
    @Test
    public void testTotalRevenue() {
        // 5.00 + 7.44 + 1.50 = 13.94
        assertEquals(13.94, rg.calculateTotalRevenue(), 0.01);
    }

    @Test
    public void testRevenueNoOrders() {
        ReportGenerator empty = new ReportGenerator(menu, new ArrayList<>(), new ArrayList<>());
        assertEquals(0.0, empty.calculateTotalRevenue(), 0.01);
    }

    // test report content
    @Test
    public void testReportNotEmpty() {
        assertFalse(rg.generateReport().isEmpty());
    }

    @Test
    public void testReportHasMenuSection() {
        assertTrue(rg.generateReport().contains("MENU ITEMS"));
    }

    @Test
    public void testReportHasOrderSection() {
        assertTrue(rg.generateReport().contains("ORDER SUMMARY"));
    }

    @Test
    public void testReportHasRevenue() {
        assertTrue(rg.generateReport().contains("REVENUE"));
    }

    @Test
    public void testReportListsItems() {
        String report = rg.generateReport();
        assertTrue(report.contains("Espresso"));
        assertTrue(report.contains("Croissant"));
        assertTrue(report.contains("Water"));
    }

    // edge case: empty everything
    @Test
    public void testEmptyReport() {
        ReportGenerator empty = new ReportGenerator(
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        assertEquals(0.0, empty.calculateTotalRevenue(), 0.01);
        assertTrue(empty.countItemOrders().isEmpty());
        assertNotNull(empty.generateReport());
    }

    // edge case: single item
    @Test
    public void testSingleOrder() {
        List<MenuItemView> m = List.of(new MenuItemView("BEV-010", "Coffee", "Beverage", 4.00));
        List<OrderRecord> o = List.of(new OrderRecord("2026-02-10 12:00", "C999", "BEV-010"));
        List<Double> t = List.of(4.00);
        ReportGenerator rg2 = new ReportGenerator(m, o, t);
        assertEquals(1, rg2.countItemOrders().get("BEV-010"));
        assertEquals(4.00, rg2.calculateTotalRevenue(), 0.01);
    }
}
