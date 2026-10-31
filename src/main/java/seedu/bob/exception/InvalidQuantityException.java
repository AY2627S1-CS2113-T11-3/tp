//@@author DaynaG3
package seedu.bob.exception;

/**
 * Indicates that a quantity is not a positive integer.
 */
public class InvalidQuantityException extends CommandException {
    //@@author

    //@@author Lokwx
    /** Message for a parsed quantity that is zero or negative. */
    public static final String NON_POSITIVE_MESSAGE = "Quantity must be greater than zero.";

    //@@author DaynaG3
    private static final String MESSAGE = "Quantity must be a positive integer.";

    /**
     * Creates an invalid-quantity exception with the standard malformed-value explanation.
     */
    public InvalidQuantityException() {
        super(MESSAGE);
    }
    //@@author

    //@@author Lokwx
    /**
     * Creates an invalid-quantity exception with a specific explanation.
     *
     * @param message Explanation shown to the user.
     */
    public InvalidQuantityException(String message) {
        super(message);
    }
}
