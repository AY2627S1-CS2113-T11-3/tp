package seedu.bob.exception;

/**
 * Indicates that a student request deadline is malformed or in the past.
 */
public class InvalidRequestDeadlineException extends CommandException {

    /**
     * Creates an exception describing the invalid deadline.
     *
     * @param message Explanation shown to the user.
     */
    public InvalidRequestDeadlineException(String message) {
        super(message);
    }
}
