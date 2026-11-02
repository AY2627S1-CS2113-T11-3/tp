//@@author J-unn-y
package seedu.bob;

/**
 * Represents one quick-lookup result: how many units of an item are in stock and how many are on loan.
 * Each entry is an immutable snapshot, so later stock or loan changes do not alter displayed results.
 */
public class ItemLookupEntry {

    private final String name;
    private final String category;
    private final int availableQuantity;
    private final int loanedQuantity;

    /**
     * Creates a lookup result for one name/category pair.
     *
     * @param name Item name.
     * @param category Item category.
     * @param availableQuantity Units currently in inventory, or zero if every unit is on loan.
     * @param loanedQuantity Units on active loans.
     */
    ItemLookupEntry(String name, String category, int availableQuantity, int loanedQuantity) {
        assert name != null && !name.isBlank() : "Lookup entries require an item name";
        assert category != null && !category.isBlank() : "Lookup entries require a category";
        assert availableQuantity >= 0 && loanedQuantity >= 0 : "Lookup quantities must not be negative";
        assert availableQuantity > 0 || loanedQuantity > 0 : "A lookup entry must describe at least one unit";
        this.name = name;
        this.category = category;
        this.availableQuantity = availableQuantity;
        this.loanedQuantity = loanedQuantity;
    }

    /**
     * Returns the item name.
     *
     * @return Item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the item category.
     *
     * @return Item category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the units in inventory when the search ran.
     *
     * @return Available units, or zero if every unit was on loan.
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Returns the units on active loans when the search ran.
     *
     * @return Loaned units.
     */
    public int getLoanedQuantity() {
        return loanedQuantity;
    }

    /**
     * Returns whether the item can be handed out now, or only after a loan is returned.
     *
     * @return {@code "Available"} if at least one unit is in stock, otherwise {@code "All on loan"}.
     */
    public String getStatus() {
        return availableQuantity > 0 ? "Available" : "All on loan";
    }

    /**
     * Returns the item, its quantities, and its status for one search-result row.
     */
    @Override
    public String toString() {
        return String.format("%s [%s] (Available: %d, On loan: %d) - %s",
                name, category, availableQuantity, loanedQuantity, getStatus());
    }
}
