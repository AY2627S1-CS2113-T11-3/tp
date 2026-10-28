//@@author DaynaG3
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

    /**
     * Creates a loan error without losing the original parsing failure.
     *
     * @param message Explanation shown to the user.
     * @param cause Original failure, retained for debugging.
     */
    public LoanException(String message, Throwable cause) {
        super(message, cause);
    }
}
