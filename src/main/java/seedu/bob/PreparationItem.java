package seedu.bob;

/**
 * Represents an immutable total requirement for a named item in an inventory category.
 */
public class PreparationItem {

    private final String name;
    private final String category;
    private final int quantity;

    /**
     * Creates an item snapshot for calculations performed by {@link Preparation}.
     * Stored requirements have positive totals; a temporary snapshot may contain zero before accumulation.
     *
     * @param name Validated item name.
     * @param category Validated inventory category.
     * @param quantity Total units required, including zero for an initial calculation snapshot.
     */
    PreparationItem(String name, String category, int quantity) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    /**
     * Returns the required item's display name.
     *
     * @return Required item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the required item's inventory category.
     *
     * @return Required item category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the total units required for the session, rather than units per attendee.
     *
     * @return Total required quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the item name and category for preparation confirmations and list rows.
     */
    @Override
    public String toString() {
        return name + " [" + category + "]";
    }
}
