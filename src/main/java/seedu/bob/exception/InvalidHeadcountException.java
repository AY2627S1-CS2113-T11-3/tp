package seedu.bob.exception;

/**
 * Indicates that a session headcount is not a positive integer.
 */
public class InvalidHeadcountException extends CommandException {

    private static final String MESSAGE = "Headcount must be a positive integer.";

    /**
     * Creates an invalid-headcount exception with the standard explanation.
     */
    public InvalidHeadcountException() {
        super(MESSAGE);
    }
}
