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

    /**
     * Creates a command exception while retaining the underlying diagnostic cause.
     *
     * @param message Explanation shown to the user.
     * @param cause Original failure, retained for debugging.
     */
    protected CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
