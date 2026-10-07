package seedu.bob.exception;

/**
 * Indicates that a requested preparation change cannot be applied.
 */
public class PreparationException extends CommandException {

    /**
     * Creates an exception explaining the invalid preparation change.
     *
     * @param message Explanation shown to the user.
     */
    public PreparationException(String message) {
        super(message);
    }
}
