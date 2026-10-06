package seedu.bob;

/**
 * Represents a named inventory item with a category and quantity.
 */
public class InventoryItem {

    private String name;
    private String category;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
