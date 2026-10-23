package seedu.bob;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.DuplicateItemException;
import seedu.bob.exception.InsufficientStockException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.LoanException;

/**
 * Stores inventory items grouped by category and displays inventory changes.
 */
public class Inventory {

    private static final int INDEX_OFFSET = 1;

    /**
     * Records inventory diagnostics at FINE level so normal console output stays unchanged.
     */
    private static final Logger LOGGER = Logger.getLogger(Inventory.class.getName());

    /** Preserves category and item insertion order for display and category-specific deletion indices. */
    private final LinkedHashMap<String, ArrayList<InventoryItem>> items = new LinkedHashMap<>();

    /**
     * Creates an empty inventory with categories and items kept in insertion order.
     */
    public Inventory() {
    }

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
        deductQuantity(storedCategory, index, quantity);

        assert hasValidState() : "Inventory state must remain valid after removal";
        LOGGER.fine(() -> "Removed inventory stock: " + item.getName() + " in category "
                + storedCategory + ", removed=" + quantity + ", remaining=" + remainingQuantity);
        System.out.printf("Successfully removed: %dx %s from the inventory\n", quantity, item.getName());
    }

    /**
     * Deducts a validated quantity without displaying an inventory-deletion confirmation.
     * Both manual deletion and lending use this path to remove depleted items and categories consistently.
     *
     * @param storedCategory Existing category's stored display name.
     * @param index Zero-based position within the category.
     * @param quantity Positive units to deduct, not exceeding the item's available stock.
     */
    private void deductQuantity(String storedCategory, int index, int quantity) {
        assert hasValidState() : "Stock deduction requires valid inventory";
        ArrayList<InventoryItem> itemsInCategory = items.get(storedCategory);
        assert itemsInCategory != null && index >= 0 && index < itemsInCategory.size()
                : "Stock deduction requires an existing category and valid item position";
        InventoryItem item = itemsInCategory.get(index);
        assert quantity > 0 && quantity <= item.getQuantity()
                : "Stock deduction requires a validated positive quantity within available stock";
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

        assert hasValidState() : "Inventory state must remain valid after deduction";
    }

    /**
     * Deducts loaned units by case-insensitive item name and category without printing a confirmation.
     * All validation completes before stock changes. Missing items cannot be loaned.
     *
     * @param name Item name to locate.
     * @param category Category containing the item.
     * @param quantity Positive total units to lend.
     * @throws CommandException If the quantity is invalid, the item is absent, or stock is insufficient.
     */
    void lendItem(String name, String category, int quantity) throws CommandException {
        validateLoanItemDetails(name, category);
        if (quantity <= 0) {
            LOGGER.fine(() -> "Rejected lending: quantity must be positive, requested=" + quantity);
            throw new InvalidQuantityException();
        }
        String storedCategory = findCategory(category);
        if (storedCategory == null) {
            LOGGER.fine(() -> "Rejected lending: missing category=" + category + ", item=" + name);
            throw new CategoryNotFoundException(category);
        }
        ArrayList<InventoryItem> itemsInCategory = items.get(storedCategory);
        for (int i = 0; i < itemsInCategory.size(); i++) {
            InventoryItem item = itemsInCategory.get(i);
            if (!item.getName().equalsIgnoreCase(name)) {
                continue;
            }
            if (quantity > item.getQuantity()) {
                LOGGER.fine(() -> "Rejected lending: " + item.getName() + " in category " + storedCategory
                        + ", requested=" + quantity + ", available=" + item.getQuantity());
                throw new LoanException(String.format("Cannot loan %d %s. Only %d are available.",
                        quantity, item.getName(), item.getQuantity()));
            }
            int remainingQuantity = item.getQuantity() - quantity;
            deductQuantity(storedCategory, i, quantity);
            LOGGER.fine(() -> "Lent inventory stock: " + item.getName() + " in category " + storedCategory
                    + ", quantity=" + quantity + ", remaining=" + remainingQuantity);
            return;
        }
        LOGGER.fine(() -> "Rejected lending: missing item=" + name + " in category " + storedCategory);
        throw new LoanException(String.format("Item \"%s\" does not exist in category \"%s\".", name, storedCategory));
    }

    /**
     * Restores a loan's stock without printing an inventory-addition confirmation.
     * Matches existing names and categories ignoring case, or appends missing entries.
     *
     * @param name Item name from the loan.
     * @param category Category from the loan.
     * @param quantity Positive number of units to restore.
     * @throws CommandException If the quantity is invalid or restoration would overflow existing stock.
     */
    void restoreStock(String name, String category, int quantity) throws CommandException {
        validateLoanItemDetails(name, category);
        if (quantity <= 0) {
            LOGGER.fine(() -> "Rejected loan stock restoration: quantity must be positive, requested=" + quantity);
            throw new InvalidQuantityException();
        }
        String storedCategory = findCategory(category);
        if (storedCategory == null) {
            InventoryItem restoredItem = new InventoryItem(name, category, quantity);
            ArrayList<InventoryItem> restoredItems = new ArrayList<>();
            restoredItems.add(restoredItem);
            items.put(category, restoredItems);
        } else {
            ArrayList<InventoryItem> itemsInCategory = items.get(storedCategory);
            for (InventoryItem item : itemsInCategory) {
                if (!item.getName().equalsIgnoreCase(name)) {
                    continue;
                }
                if (quantity > Integer.MAX_VALUE - item.getQuantity()) {
                    LOGGER.fine(() -> "Rejected loan stock restoration: " + item.getName()
                            + " in category " + storedCategory + ", requested=" + quantity
                            + ", available=" + item.getQuantity() + ", maximum=" + Integer.MAX_VALUE);
                    throw new LoanException(String.format(
                            "Cannot restore %d %s. Inventory quantity must not exceed %d.",
                            quantity, item.getName(), Integer.MAX_VALUE));
                }
                item.addQuantity(quantity);
                assert hasValidState() : "Inventory state must remain valid after restoration";
                LOGGER.fine(() -> "Restored loan stock: " + item.getName() + " in category " + storedCategory
                        + ", quantity=" + quantity + ", available=" + item.getQuantity());
                return;
            }
            itemsInCategory.add(new InventoryItem(name, storedCategory, quantity));
        }
        assert hasValidState() : "Inventory state must remain valid after restoration";
        LOGGER.fine(() -> "Recreated loan stock: " + name + " in category " + category
                + ", quantity=" + quantity);
    }

    /**
     * Validates loan item identifiers before either stock path can modify inventory.
     * Direct domain calls receive the same checked errors as loan construction.
     *
     * @param name Loan item name.
     * @param category Loan item category.
     * @throws CommandFormatException If the name or category is null or blank.
     */
    private void validateLoanItemDetails(String name, String category) throws CommandFormatException {
        if (name == null || name.isBlank()) {
            LOGGER.fine("Rejected loan stock operation: item name is blank");
            throw new CommandFormatException("Item name cannot be blank.");
        }
        if (category == null || category.isBlank()) {
            LOGGER.fine("Rejected loan stock operation: category is blank");
            throw new CommandFormatException("Category cannot be blank.");
        }
    }

    /**
     * Returns current stock matching a preparation or loan item's name and category.
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
