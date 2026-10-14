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
     * @throws IllegalArgumentException If the name or category is blank, or quantity is not positive.
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

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
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
