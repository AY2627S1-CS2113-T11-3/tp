package seedu.duke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private static final int INDEX_OFFSET = 1;

    private final HashMap<String, ArrayList<InventoryItem>> items = new HashMap<>();

    public void addItem(InventoryItem item) {
        String category = item.getCategory();

        if (!items.containsKey(category)) {
            items.put(category, new ArrayList<>());
        }

        ArrayList<InventoryItem> itemsInCategory = items.get(category);
        itemsInCategory.add(item);

        System.out.printf("Successfully added: %dx %s to the inventory\n",item.getQuantity(), item.getName());
    }

    public void removeItem(String category, int userIndex, int quantity) {
        ArrayList<InventoryItem> itemsInCategory = items.get(category);

        int index = userIndex - INDEX_OFFSET;
        InventoryItem item = itemsInCategory.get(index);

        int remainingQuantity = item.getQuantity() - quantity;
        if (remainingQuantity <= 0) {
            //TODO split the < 0 case as error handling
            itemsInCategory.remove(index);
        }
        else {
            item.setQuantity(remainingQuantity);
        }

        System.out.printf("Successfully removed: %dx %s to the inventory\n",item.getQuantity(), item.getName());
    }

    public void listItems() {
        System.out.println("Inventory");
        System.out.println("--------------------------------------------------------------------");
        for (String category : items.keySet()) {
            ArrayList<InventoryItem> itemsInCategory = items.get(category);

            System.out.println(category);

            for (int i = 0; i < itemsInCategory.size(); i++) {
                InventoryItem item = itemsInCategory.get(i);
                System.out.printf("%d: %s (Qty: %s)\n",i+1 ,item.getName(), item.getQuantity());
            }
        }
        System.out.println("--------------------------------------------------------------------");
    }
}
