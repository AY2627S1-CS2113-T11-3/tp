package seedu.bob;

/**
 * Represents an immutable total requirement for a named item in an inventory category.
 */
public class PreparationItem {

    private final String name;
    private final String category;
    private final int quantity;

    /**
     * Creates a requirement after its values have been validated by {@link Preparation}.
     */
    PreparationItem(String name, String category, int quantity) {
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

    @Override
    public String toString() {
        return name + " [" + category + "]";
    }
}
