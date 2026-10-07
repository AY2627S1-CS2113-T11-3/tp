package seedu.bob.exception;

/**
 * Indicates that an item already exists in its category.
 */
public class DuplicateItemException extends CommandException {

    /**
     * Creates an exception identifying the duplicate item and category.
     *
     * @param itemName Name supplied for the duplicate item.
     * @param category Existing category's display name.
     */
    public DuplicateItemException(String itemName, String category) {
        super(String.format("Item \"%s\" already exists in category \"%s\".", itemName, category));
    }
}
