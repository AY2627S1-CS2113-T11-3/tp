package seedu.bob.exception;

/**
 * Indicates that an inventory category does not exist.
 */
public class CategoryNotFoundException extends CommandException {

    /**
     * Creates an exception identifying the missing category.
     *
     * @param category Category requested by the user.
     */
    public CategoryNotFoundException(String category) {
        super(String.format("Category \"%s\" does not exist.", category));
    }
}
