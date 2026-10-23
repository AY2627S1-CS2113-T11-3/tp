package seedu.bob.exception;

/**
 * Indicates that a loan index is not a valid one-based position in the active loan list.
 */
public class InvalidLoanIndexException extends CommandException {

    /**
     * Creates an error for an index that is not a positive integer.
     */
    public InvalidLoanIndexException() {
        super("Loan index must be a positive integer.");
    }

    /**
     * Creates an invalid-index error while retaining the integer parsing failure.
     *
     * @param cause Original failure, retained for debugging.
     */
    public InvalidLoanIndexException(NumberFormatException cause) {
        super("Loan index must be a positive integer.", cause);
    }

    /**
     * Creates an error for a position outside the active loan list.
     *
     * @param index Requested one-based position.
     * @param loanCount Number of active loans.
     */
    public InvalidLoanIndexException(int index, int loanCount) {
        super(String.format("Loan index %d is invalid. There are %d active loan(s).", index, loanCount));
    }
}
