package seedu.bob;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores inventory items grouped by category and displays inventory changes.
 */
public class Inventory {

    private static final int INDEX_OFFSET = 1;

    private final HashMap<String, ArrayList<InventoryItem>> items = new HashMap<>();

    /**
     * Adds an item to its category and prints a confirmation.
     *
     * @param item Item to add.
     */
    public void addItem(InventoryItem item) {
        String category = item.getCategory();

        if (!items.containsKey(category)) {
            items.put(category, new ArrayList<>());
        }

        ArrayList<InventoryItem> itemsInCategory = items.get(category);
        itemsInCategory.add(item);

        System.out.printf("Successfully added: %dx %s to the inventory\n", item.getQuantity(), item.getName());
    }

    /**
     * Removes the requested quantity and prints a confirmation, removing the
     * item when none remain. Prints an error without changing the inventory if
     * the quantity exceeds the available stock.
     *
     * @param category Existing category containing the item.
     * @param userIndex One-based item position in the category.
     * @param quantity Quantity to subtract.
     * @throws NullPointerException If the category does not exist.
     * @throws IndexOutOfBoundsException If the item position is invalid.
     */
    public void removeItem(String category, int userIndex, int quantity) {
        ArrayList<InventoryItem> itemsInCategory = items.get(category);

        int index = userIndex - INDEX_OFFSET;
        InventoryItem item = itemsInCategory.get(index);

        int remainingQuantity = item.getQuantity() - quantity;
        if (remainingQuantity < 0) {
            // TODO: Handle insufficient stock separately.
            System.out.println("Invalid quantity!");
            return;
        } else if (remainingQuantity == 0) {
            itemsInCategory.remove(index);
        } else {
            item.setQuantity(remainingQuantity);
        }

        System.out.printf("Successfully removed: %dx %s from the inventory\n", quantity, item.getName());
    }

    /**
     * Prints inventory items by category with one-based positions and
     * quantities.
     */
    public void listItems() {
        System.out.println("\n" + Ui.DIVIDER);
        System.out.println("Inventory");
        System.out.println(Ui.DIVIDER);
        for (String category : items.keySet()) {
            ArrayList<InventoryItem> itemsInCategory = items.get(category);

            System.out.println(category);

            for (int i = 0; i < itemsInCategory.size(); i++) {
                InventoryItem item = itemsInCategory.get(i);
                System.out.printf("%d: %s (Qty: %s)\n", i + 1, item.getName(), item.getQuantity());
            }
        }
        System.out.println(Ui.DIVIDER);
    }
}
