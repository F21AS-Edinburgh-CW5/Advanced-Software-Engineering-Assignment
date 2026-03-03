import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DiscountCalculatorTest {

    @Test
    public void testRule1_20PercentDiscount() {
        // 使用成员B的 MenuItemView (id, description, category, price)
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("BEV-001", "Latte", "beverage", 5.0),
                new MenuItemView("FOD-001", "Sandwich", "food", 10.0),
                new MenuItemView("FOD-002", "Cake", "food", 10.0)
        ); // 总价 £25

        DiscountCalculator calculator = new DiscountCalculator();
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertEquals(5.0, result.getDiscountAmount(), 0.01);
        assertEquals("20% off: 1 Bev + 2 Food", result.getRuleApplied());
    }

    @Test
    public void testRule2_15PercentDiscount() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("BEV-001", "Tea", "beverage", 4.0),
                new MenuItemView("BEV-002", "Coffee", "beverage", 4.0),
                new MenuItemView("BEV-003", "Juice", "beverage", 4.0)
        ); // 总价 £12

        DiscountCalculator calculator = new DiscountCalculator();
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertEquals(1.8, result.getDiscountAmount(), 0.01);
        assertTrue(result.getRuleApplied().contains("15% off"));
    }

    @Test
    public void testRule3_10PercentDiscount() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("OTH-001", "Mug", "other", 30.0)
        );

        DiscountCalculator calculator = new DiscountCalculator();
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertEquals(3.0, result.getDiscountAmount(), 0.01);
        assertTrue(result.getRuleApplied().contains("10% off"));
    }

    @Test
    public void testNoDiscount() {
        List<MenuItemView> items = Arrays.asList(
                new MenuItemView("FOD-001", "Sandwich", "food", 10.0),
                new MenuItemView("FOD-002", "Cake", "food", 10.0)
        );

        DiscountCalculator calculator = new DiscountCalculator();
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(items);

        assertEquals(0.0, result.getDiscountAmount(), 0.01);
        assertEquals("No discount", result.getRuleApplied());
    }

    @Test
    public void testEmptyOrderEdgeCase() {
        List<MenuItemView> emptyItems = new ArrayList<>();
        DiscountCalculator calculator = new DiscountCalculator();
        DiscountCalculator.DiscountResult result = calculator.calculateDiscount(emptyItems);

        assertEquals(0.0, result.getDiscountAmount(), 0.01);
    }
}