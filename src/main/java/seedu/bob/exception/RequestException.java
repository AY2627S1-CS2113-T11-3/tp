//@@author Lokwx
package seedu.bob.exception;

/**
 * Indicates that a requested student-request change cannot be applied.
 */
public class RequestException extends CommandException {

    /**
     * Creates an exception explaining the invalid request change.
     *
     * @param message Explanation shown to the user.
     */
    public RequestException(String message) {
        super(message);
    }
}
