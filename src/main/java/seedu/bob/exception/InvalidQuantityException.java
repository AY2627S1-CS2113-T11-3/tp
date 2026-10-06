package seedu.bob.exception;

/**
 * Indicates that a quantity is not a positive integer.
 */
public class InvalidQuantityException extends CommandException {

    private static final String MESSAGE = "Quantity must be a positive integer.";

    /**
     * Creates an invalid-quantity exception with the standard explanation.
     */
    public InvalidQuantityException() {
        super(MESSAGE);
    }
}
