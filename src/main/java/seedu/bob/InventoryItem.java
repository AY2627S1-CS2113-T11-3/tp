package seedu.bob;

/**
 * Represents a named inventory item with a category and quantity.
 */
public class InventoryItem {

    private final String name;
    private final String category;
    private int quantity;

    /**
     * Creates an inventory item with the supplied name, category, and quantity.
     *
     * @param name Nonblank item name.
     * @param category Nonblank inventory category.
     * @param quantity Positive available quantity or independent snapshot quantity.
     * @throws IllegalArgumentException If the name or category is null or blank, or quantity is not positive.
     */
    public InventoryItem(String name, String category, int quantity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    /**
     * Returns the item's display name.
     *
     * @return Item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the item's inventory category.
     *
     * @return Category name.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the item's positive quantity.
     *
     * @return Units represented by this item.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Restores positive units without overflowing the inventory quantity.
     *
     * @param quantityToAdd Units to restore.
     * @throws IllegalArgumentException If the addition is non-positive or exceeds the integer limit.
     */
    void addQuantity(int quantityToAdd) {
        if (quantityToAdd <= 0 || quantityToAdd > Integer.MAX_VALUE - quantity) {
            throw new IllegalArgumentException("Stock addition must be positive and fit within the integer limit");
        }
        quantity += quantityToAdd;
    }

    /**
     * Removes units while preserving the positive-stock invariant of a stored item.
     * Complete removal is handled by {@link Inventory}, which removes the item itself.
     *
     * @param quantityToRemove Number of units to remove.
     * @throws IllegalArgumentException If the removal is non-positive or would leave no stock.
     */
    void removeQuantity(int quantityToRemove) {
        if (quantityToRemove <= 0 || quantityToRemove >= quantity) {
            throw new IllegalArgumentException("Partial stock removal must leave a positive quantity");
        }
        quantity -= quantityToRemove;
    }
}
