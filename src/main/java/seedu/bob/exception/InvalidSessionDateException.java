package seedu.bob.exception;

/**
 * Indicates that a session date is invalid or is in the past.
 */
public class InvalidSessionDateException extends CommandException {

    /**
     * Creates an exception with a user-facing date explanation.
     *
     * @param message Explanation shown to the user.
     */
    public InvalidSessionDateException(String message) {
        super(message);
    }
}
