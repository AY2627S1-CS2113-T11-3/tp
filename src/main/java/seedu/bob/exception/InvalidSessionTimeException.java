package seedu.bob.exception;

/**
 * Indicates that a session time or time range is invalid.
 */
public class InvalidSessionTimeException extends CommandException {

    /**
     * Creates an exception with a user-facing time explanation.
     *
     * @param message Explanation shown to the user.
     */
    public InvalidSessionTimeException(String message) {
        super(message);
    }
}
