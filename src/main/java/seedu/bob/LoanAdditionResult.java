//@@author DaynaG3
package seedu.bob;

import java.util.Objects;

/**
 * Captures a successful loan addition and its overdue status at the time it was recorded.
 */
public final class LoanAdditionResult {

    private final Loan loan;
    /** This status is fixed at addition; future listing queries evaluate their own current date. */
    private final boolean isOverdue;

    /**
     * Creates an immutable addition result after the manager has evaluated the due date.
     *
     * @param loan Recorded loan.
     * @param isOverdue Whether the loan was overdue when added.
     * @throws NullPointerException If loan is null.
     */
    LoanAdditionResult(Loan loan, boolean isOverdue) {
        this.loan = Objects.requireNonNull(loan, "Recorded loan must not be null");
        this.isOverdue = isOverdue;
    }

    /**
     * Returns the recorded loan.
     *
     * @return Successfully added loan.
     */
    public Loan getLoan() {
        return loan;
    }

    /**
     * Returns whether the loan was overdue when added, independently of later clock changes.
     *
     * @return Overdue status captured at addition.
     */
    public boolean isOverdue() {
        return isOverdue;
    }
}
