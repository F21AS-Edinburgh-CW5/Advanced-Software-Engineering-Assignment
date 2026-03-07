package coffeeshop.api;

import coffeeshop.api.MenuItemView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountCalculatorTest {

    private final DiscountCalculator calculator = new DiscountCalculator();

    @Test
    public void testRule1_20PercentDiscount() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("BEV-001", "Latte", "beverage", 5.0),
                new MenuItemView("FOD-001", "Sandwich", "food", 10.0),
                new MenuItemView("FOD-002", "Cake", "food", 10.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(5.00, result.getDiscountAmount(), 0.001),
                () -> assertEquals("20% off: 1 beverage + 2 food items", result.getRuleApplied())
        );
    }

    @Test
    public void testRule2_15PercentDiscount() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("BEV-001", "Tea", "beverage", 4.0),
                new MenuItemView("BEV-002", "Coffee", "beverage", 4.0),
                new MenuItemView("BEV-003", "Juice", "beverage", 4.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(1.80, result.getDiscountAmount(), 0.001),
                () -> assertEquals("15% off: 3 or more beverages", result.getRuleApplied())
        );
    }

    @Test
    public void testRule3_10PercentDiscount() {
        List<MenuItemView> items = List.of(
                new MenuItemView("OTH-001", "Mug", "other", 30.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(3.00, result.getDiscountAmount(), 0.001),
                () -> assertEquals("10% off: subtotal over £25", result.getRuleApplied())
        );
    }

    @Test
    public void testNoDiscount() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("FOD-001", "Sandwich", "food", 10.0),
                new MenuItemView("OTH-001", "Sticker", "other", 2.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(0.0, result.getDiscountAmount(), 0.001),
                () -> assertEquals("No discount", result.getRuleApplied())
        );
    }

    @Test
    public void testBoundaryExactly25_No10PercentDiscount() {
        List<MenuItemView> items = List.of(
                new MenuItemView("OTH-001", "Cup", "other", 25.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(0.0, result.getDiscountAmount(), 0.001),
                () -> assertEquals("No discount", result.getRuleApplied())
        );
    }

    @Test
    public void testRulePriority_Rule1OverridesRule3() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("BEV-001", "Latte", "beverage", 8.0),
                new MenuItemView("FOD-001", "Sandwich", "food", 14.0),
                new MenuItemView("FOD-002", "Cake", "food", 14.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(7.20, result.getDiscountAmount(), 0.001),
                () -> assertEquals("20% off: 1 beverage + 2 food items", result.getRuleApplied())
        );
    }

    @Test
    public void testDrinkCategoryAlsoRecognisedAsBeverage() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("DRINK-001", "Tea", "drink", 4.0),
                new MenuItemView("DRINK-002", "Coffee", "drink", 4.0),
                new MenuItemView("DRINK-003", "Juice", "drink", 4.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(1.80, result.getDiscountAmount(), 0.001),
                () -> assertEquals("15% off: 3 or more beverages", result.getRuleApplied())
        );
    }

    @Test
    public void testNullOrderList() {
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(null);

        assertAll(
                () -> assertEquals(0.0, result.getDiscountAmount(), 0.001),
                () -> assertEquals("No discount", result.getRuleApplied())
        );
    }

    @Test
    public void testEmptyOrderList() {
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(new ArrayList<>());

        assertAll(
                () -> assertEquals(0.0, result.getDiscountAmount(), 0.001),
                () -> assertEquals("No discount", result.getRuleApplied())
        );
    }

    @Test
    public void testNullItemInsideListIsIgnored() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("BEV-001", "Tea", "beverage", 4.0),
                null,
                new MenuItemView("BEV-002", "Coffee", "beverage", 4.0),
                new MenuItemView("BEV-003", "Juice", "beverage", 4.0)
        );

        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertAll(
                () -> assertEquals(1.80, result.getDiscountAmount(), 0.001),
                () -> assertEquals("15% off: 3 or more beverages", result.getRuleApplied())
        );
    }

    @Test
    public void testNegativePriceThrowsException() {
        List<MenuItemView> items = List.of(
                new MenuItemView("BEV-001", "Broken data", "beverage", -1.0)
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateDiscount(items)
        );

        assertTrue(ex.getMessage().contains("negative"));
    }
}
