package seedu.bob.exception;

/**
 * Indicates that a student request status is not prepared or fulfilled.
 */
public class InvalidRequestStatusException extends CommandFormatException {

    private static final String MESSAGE = "Status must be \"prepared\" or \"fulfilled\".";

    /**
     * Creates an invalid-request-status exception with the standard explanation.
     */
    public InvalidRequestStatusException() {
        super(MESSAGE);
    }
}
