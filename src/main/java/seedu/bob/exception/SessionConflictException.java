package seedu.bob.exception;

/**
 * Indicates that two sessions overlap at the same date and location.
 */
public class SessionConflictException extends CommandException {

    /**
     * Creates an exception describing the existing conflicting session.
     *
     * @param message Description of the conflict shown to the user.
     */
    public SessionConflictException(String message) {
        super(message);
    }
}
