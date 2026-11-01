//@@author Lokwx
package seedu.bob.exception;

/**
 * Indicates that an inventory category has no item with the requested name.
 */
public class InventoryItemNotFoundException extends CommandException {

    /**
     * Creates an exception naming the missing item and category, and the command that creates entries.
     *
     * @param itemName Item name that was not found.
     * @param category Existing category that was searched.
     */
    public InventoryItemNotFoundException(String itemName, String category) {
        super(String.format("Item \"%s\" does not exist in category \"%s\". Use add-i to add a new item.",
                itemName, category));
    }
}
