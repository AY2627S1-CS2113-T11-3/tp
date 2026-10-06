package seedu.bob.exception;

/**
 * Indicates that a removal requests more units than are available.
 */
public class InsufficientStockException extends CommandException {

    /**
     * Creates an exception describing the requested and available quantities.
     *
     * @param itemName Name of the inventory item.
     * @param requestedQuantity Quantity requested for removal.
     * @param availableQuantity Quantity currently available.
     */
    public InsufficientStockException(String itemName, int requestedQuantity, int availableQuantity) {
        super(String.format("Cannot remove %d %s. Only %d are available.",
                requestedQuantity, itemName, availableQuantity));
    }
}
