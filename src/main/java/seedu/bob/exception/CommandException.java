package seedu.bob.exception;

/**
 * Represents an expected error caused by a user's command.
 */
public abstract class CommandException extends Exception {

    /**
     * Creates a command exception with a user-facing explanation.
     *
     * @param message Explanation shown to the user.
     */
    protected CommandException(String message) {
        super(message);
    }
}
