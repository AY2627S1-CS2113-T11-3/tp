package seedu.bob.exception;

/** Indicates that a complete saved snapshot cannot be restored into a valid application state. */
public class InvalidSnapshotException extends Exception {

    /**
     * Creates a validation failure with diagnostic context from the rejected snapshot.
     *
     * @param message Description of the invalid saved data.
     * @param cause Underlying domain or structural validation failure.
     */
    public InvalidSnapshotException(String message, Throwable cause) {
        super(message, cause);
    }
}
