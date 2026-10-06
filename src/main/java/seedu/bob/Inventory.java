package seedu.bob;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.DuplicateItemException;
import seedu.bob.exception.InsufficientStockException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidQuantityException;

/**
 * Stores inventory items grouped by category and displays inventory changes.
 */
public class Inventory {

    private static final int INDEX_OFFSET = 1;

    private final LinkedHashMap<String, ArrayList<InventoryItem>> items = new LinkedHashMap<>();

    /**
     * Adds an item to its category and prints a confirmation.
     *
     * @param item Item to add.
     * @throws DuplicateItemException If an item with the same name already exists in the category.
     */
    public void addItem(InventoryItem item) throws DuplicateItemException {
        String category = findCategory(item.getCategory());

        if (category == null) {
            category = item.getCategory();
            items.put(category, new ArrayList<>());
        }

        ArrayList<InventoryItem> itemsInCategory = items.get(category);
        for (InventoryItem existingItem : itemsInCategory) {
            if (existingItem.getName().equalsIgnoreCase(item.getName())) {
                throw new DuplicateItemException(item.getName(), category);
            }
        }
        itemsInCategory.add(item);

        System.out.printf("Successfully added: %dx %s to the inventory\n", item.getQuantity(), item.getName());
    }

    /**
     * Finds the stored display name for a category without requiring matching capitalization.
     *
     * @param requestedCategory Category name supplied by the user.
     * @return Stored category name, or {@code null} if the category does not exist.
     */
    private String findCategory(String requestedCategory) {
        for (String category : items.keySet()) {
            if (category.equalsIgnoreCase(requestedCategory)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Removes the requested quantity and prints a confirmation. Removes the item
     * and its empty category when no units remain.
     *
     * @param category Existing category containing the item.
     * @param userIndex One-based item position in the category.
     * @param quantity Quantity to subtract.
     * @throws CategoryNotFoundException If the category does not exist.
     * @throws InvalidItemIndexException If the item position is invalid.
     * @throws InvalidQuantityException If the requested quantity is not positive.
     * @throws InsufficientStockException If the requested quantity exceeds available stock.
     */
    public void removeItem(String category, int userIndex, int quantity)
            throws CategoryNotFoundException, InvalidItemIndexException,
            InvalidQuantityException, InsufficientStockException {
        String storedCategory = findCategory(category);
        if (storedCategory == null) {
            throw new CategoryNotFoundException(category);
        }

        ArrayList<InventoryItem> itemsInCategory = items.get(storedCategory);
        if (userIndex <= 0 || userIndex > itemsInCategory.size()) {
            throw new InvalidItemIndexException(userIndex, storedCategory, itemsInCategory.size());
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }

        int index = userIndex - INDEX_OFFSET;
        InventoryItem item = itemsInCategory.get(index);
        if (quantity > item.getQuantity()) {
            throw new InsufficientStockException(item.getName(), quantity, item.getQuantity());
        }

        int remainingQuantity = item.getQuantity() - quantity;
        if (remainingQuantity == 0) {
            itemsInCategory.remove(index);
            if (itemsInCategory.isEmpty()) {
                items.remove(storedCategory);
            }
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
        System.out.println(Ui.DIVIDER);
        System.out.println("Inventory");
        System.out.println(Ui.DIVIDER);
        if (items.isEmpty()) {
            System.out.println("No items in inventory.");
        }
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
