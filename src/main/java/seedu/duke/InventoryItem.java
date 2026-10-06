package seedu.duke;

/**
 * Represents a named inventory item with a category and quantity.
 */
public class InventoryItem {
    private String name;
    private String category;
    private int quantity;

    /**
     * Creates an inventory item with the supplied name, category, and quantity.
     */
    public InventoryItem(String name, String category, int quantity) {
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
