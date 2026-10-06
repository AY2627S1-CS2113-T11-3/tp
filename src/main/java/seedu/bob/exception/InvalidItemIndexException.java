package seedu.bob.exception;

/**
 * Indicates that an item index is not valid for a category.
 */
public class InvalidItemIndexException extends CommandException {

    private static final String NON_POSITIVE_MESSAGE = "Item index must be a positive integer.";

    /**
     * Creates an exception for an index that cannot be parsed as a positive integer.
     */
    public InvalidItemIndexException() {
        super(NON_POSITIVE_MESSAGE);
    }

    /**
     * Creates an exception describing the invalid index and valid category size.
     *
     * @param index Index requested by the user.
     * @param category Category containing the indexed items.
     * @param itemCount Number of items currently in the category.
     */
    public InvalidItemIndexException(int index, String category, int itemCount) {
        super(String.format("Item index %d is invalid. Category \"%s\" contains %d item(s).",
                index, category, itemCount));
    }
}
