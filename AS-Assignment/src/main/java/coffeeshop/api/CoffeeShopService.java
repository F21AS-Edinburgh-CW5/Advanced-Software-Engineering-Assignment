package coffeeshop.api;

import java.util.List;

public interface CoffeeShopService{

    //return all available menu items
    List<MenuItemView> getMenuItems();

    //calculate bill based on selected menu item IDs
    Bill calculateBill(List<String>selectedItemIds);

    //generate final report when application exits
    void generateReport();


}