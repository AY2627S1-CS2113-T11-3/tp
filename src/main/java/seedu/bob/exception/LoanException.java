package seedu.bob.exception;

/**
 * Indicates that a loan operation fails because its date, requested stock, or restoration is invalid.
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
