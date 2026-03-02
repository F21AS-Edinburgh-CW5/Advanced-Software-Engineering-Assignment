package coffeeshop.api;

public class MenuItemView {
    //Use private final here-once created, it can not be modified to keep the data secure
    private final String id; //goods ID, unique identification
    private final String description;//Product descriptions, like“Espresso Latte.”
    private final String category;//Categories such as“Coffee” and “Tea”
    private final double price;//price of drinks

    public MenuItemView(String id, String description, String category, double price){
        this.id = id;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    public String getId() { return id;}
    public String getDescription() {return description;}
    public String getCategory() { return category;}
    public double getPrice() { return price;}

    @Override
    public String toString(){
        return id + " | " + category + " | £" + price + " | " + description;
    }

}


