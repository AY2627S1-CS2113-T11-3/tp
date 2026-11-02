//@@author gavinlimsh
package seedu.bob.exception;

/**
 * Indicates that a broken-item record or operation is invalid.
 */
public class BrokenItemException extends CommandException {

    /**
     * Creates an error with an explanation that can be displayed to the user.
     *
     * @param message Explanation of the rejected record or operation.
     */
    public BrokenItemException(String message) {
        super(message);
    }

    /**
     * Creates an error while preserving the original failure for diagnostics.
     *
     * @param message Explanation shown to the user.
     * @param cause Original failure.
     */
    public BrokenItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
