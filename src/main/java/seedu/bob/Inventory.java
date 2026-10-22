package seedu.bob;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.DuplicateItemException;
import seedu.bob.exception.InsufficientStockException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidQuantityException;

/**
 * Stores inventory items grouped by category and displays inventory changes.
 *
 * @author Lokwx
 */
public class Inventory {

    private static final int INDEX_OFFSET = 1;

    /**
     * Records inventory diagnostics at FINE level so normal console output stays unchanged.
     */
    private static final Logger LOGGER = Logger.getLogger(Inventory.class.getName());

    private final LinkedHashMap<String, ArrayList<InventoryItem>> items = new LinkedHashMap<>();

    /**
     * Adds an item to its category and prints a confirmation.
     *
     * @param item Item to add.
     * @throws NullPointerException If {@code item} is {@code null}.
     * @throws DuplicateItemException If an item with the same name already exists in the category.
     */
    public void addItem(InventoryItem item) throws DuplicateItemException {
        Objects.requireNonNull(item, "Item to add must not be null");
        String category = findCategory(item.getCategory());

        if (category == null) {
            category = item.getCategory();
            items.put(category, new ArrayList<>());
        }

        ArrayList<InventoryItem> itemsInCategory = items.get(category);
        for (InventoryItem existingItem : itemsInCategory) {
            if (existingItem.getName().equalsIgnoreCase(item.getName())) {
                LOGGER.fine(() -> "Rejected duplicate inventory item: " + item.getName()
                        + " in category " + item.getCategory());
                throw new DuplicateItemException(item.getName(), category);
            }
        }
        itemsInCategory.add(item);
        assert hasValidState() : "Inventory state must remain valid after addition";
        LOGGER.fine(() -> "Added inventory item: " + item.getName() + " in category "
                + item.getCategory() + ", quantity=" + item.getQuantity());

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
     * Returns whether all stored categories and items satisfy inventory invariants.
     */
    private boolean hasValidState() {
        for (String category : items.keySet()) {
            ArrayList<InventoryItem> itemsInCategory = items.get(category);
            if (category == null || category.isBlank()
                    || itemsInCategory == null || itemsInCategory.isEmpty()) {
                return false;
            }

            for (int i = 0; i < itemsInCategory.size(); i++) {
                InventoryItem item = itemsInCategory.get(i);
                if (item == null || item.getName() == null || item.getName().isBlank()
                        || item.getCategory() == null || !category.equalsIgnoreCase(item.getCategory())
                        || item.getQuantity() <= 0) {
                    return false;
                }

                for (int j = i + 1; j < itemsInCategory.size(); j++) {
                    InventoryItem otherItem = itemsInCategory.get(j);
                    if (otherItem == null || item.getName().equalsIgnoreCase(otherItem.getName())) {
                        return false;
                    }
                }
            }
        }
        return true;
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
            LOGGER.fine(() -> "Rejected stock removal: " + item.getName() + " in category "
                    + storedCategory + ", requested=" + quantity + ", available=" + item.getQuantity());
            throw new InsufficientStockException(item.getName(), quantity, item.getQuantity());
        }

        int remainingQuantity = item.getQuantity() - quantity;
        assert remainingQuantity >= 0 : "Validated stock removal must not leave negative stock";
        if (remainingQuantity == 0) {
            itemsInCategory.remove(index);
            if (itemsInCategory.isEmpty()) {
                items.remove(storedCategory);
            }
        } else {
            item.removeQuantity(quantity);
        }

        assert hasValidState() : "Inventory state must remain valid after removal";
        LOGGER.fine(() -> "Removed inventory stock: " + item.getName() + " in category "
                + storedCategory + ", removed=" + quantity + ", remaining=" + remainingQuantity);
        System.out.printf("Successfully removed: %dx %s from the inventory\n", quantity, item.getName());
    }

    /**
     * Returns current stock matching a preparation item's name and category.
     * Matching is case-insensitive and does not reserve or remove stock.
     *
     * @param name Item name to look up.
     * @param category Category containing the item.
     * @return Available quantity, or zero if the name/category pair is absent from inventory.
     */
    public int getAvailableQuantity(String name, String category) {
        String storedCategory = findCategory(category);
        if (storedCategory == null) {
            return 0;
        }
        for (InventoryItem item : items.get(storedCategory)) {
            if (item.getName().equalsIgnoreCase(name)) {
                assert item.getQuantity() > 0 : "Stored inventory items must have positive stock";
                return item.getQuantity();
            }
        }
        return 0;
    }

    /**
     * Returns current stock for an item name across all categories.
     * Matching is case-insensitive and does not reserve or remove stock.
     *
     * @param name Item name to look up.
     * @return Total available quantity, or zero if the item is absent from inventory.
     */
    public int getTotalAvailableQuantity(String name) {
        int totalQuantity = 0;
        // Requests name an item but not a category, so stock is combined across every category.
        for (ArrayList<InventoryItem> itemsInCategory : items.values()) {
            for (InventoryItem item : itemsInCategory) {
                if (item.getName().equalsIgnoreCase(name)) {
                    totalQuantity += item.getQuantity();
                }
            }
        }
        return totalQuantity;
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
