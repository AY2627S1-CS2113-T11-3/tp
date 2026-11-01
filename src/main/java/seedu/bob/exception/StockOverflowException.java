//@@author Lokwx
package seedu.bob.exception;

/**
 * Indicates that adding stock would exceed the largest quantity an inventory item can hold.
 */
public class StockOverflowException extends CommandException {

    /**
     * Creates an exception describing the rejected addition and the maximum quantity.
     *
     * @param itemName Name of the inventory item.
     * @param requestedQuantity Units requested for addition.
     * @param maximumQuantity Largest quantity the item can hold.
     */
    public StockOverflowException(String itemName, int requestedQuantity, int maximumQuantity) {
        super(String.format("Cannot add %d %s. Inventory quantity must not exceed %d.",
                requestedQuantity, itemName, maximumQuantity));
    }
}
