package seedu.bob.exception;

/**
 * Indicates that a loan cannot be recorded because its date or requested stock is invalid.
 */
public class LoanException extends CommandException {

    /**
     * Creates a loan error with a user-facing explanation.
     *
     * @param message Explanation of the rejected loan.
     */
    public LoanException(String message) {
        super(message);
    }
}
