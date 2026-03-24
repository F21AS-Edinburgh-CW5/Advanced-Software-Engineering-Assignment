package coffeeshop.service;

import coffeeshop.api.MenuItemView;
import coffeeshop.model.CustomerOrder;

import java.util.Random;

public class ProcessingTimeService {

    private static final int BEV_MIN_MS = 2000;
    private static final int BEV_MAX_MS = 4000;
    private static final int FOOD_MIN_MS = 6000;
    private static final int FOOD_MAX_MS = 10000;

    private final Random random = new Random();

    public long getProcessingTimeMs(CustomerOrder order) {
        long total = 0;
        for (MenuItemView item : order.getItems()) {
            total += getItemProcessingTimeMs(item);
        }
        return total;
    }

    private long getItemProcessingTimeMs(MenuItemView item) {
        String category = item.getCategory().trim().toLowerCase();
        String id = item.getId().trim().toLowerCase();
        if (isBeverage(category, id)) {
            return randomBetween(BEV_MIN_MS, BEV_MAX_MS);
        } else if (isFood(category, id)) {
            return randomBetween(FOOD_MIN_MS, FOOD_MAX_MS);
        } else {
            return randomBetween(BEV_MIN_MS, BEV_MAX_MS);
        }
    }

    private boolean isBeverage(String category, String id) {
        return category.equals("beverage")
                || category.equals("bev")
                || category.equals("drink")
                || category.equals("drinks")
                || id.startsWith("bev-")
                || id.startsWith("drink-");
    }

    private boolean isFood(String category, String id) {
        return category.equals("food")
                || category.equals("foods")
                || id.startsWith("fod-")
                || id.startsWith("food-");
    }

    private long randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}