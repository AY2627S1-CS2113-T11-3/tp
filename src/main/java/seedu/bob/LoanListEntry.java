package seedu.bob;

import java.util.Objects;

/**
 * Holds one immutable listing row with its full-list position and overdue status at query time.
 */
public final class LoanListEntry {

    private final int index;
    private final Loan loan;
    /** Fixed for this query snapshot; a later query calculates a new status from the manager's clock. */
    private final boolean isOverdue;

    /**
     * Creates a listing row after the manager has selected its loan and evaluated the due date.
     *
     * @param index One-based position in the full active-loan list when queried.
     * @param loan Selected loan.
     * @param isOverdue Whether the selected loan was overdue at query time.
     * @throws IllegalArgumentException If index is not positive.
     * @throws NullPointerException If loan is null.
     */
    LoanListEntry(int index, Loan loan, boolean isOverdue) {
        if (index <= 0) {
            throw new IllegalArgumentException("Loan listing index must be positive");
        }
        this.index = index;
        this.loan = Objects.requireNonNull(loan, "Listed loan must not be null");
        this.isOverdue = isOverdue;
    }

    /**
     * Returns the original full-list position captured by the query.
     *
     * @return One-based position, which can contain gaps in an overdue-only view.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the selected loan.
     *
     * @return Loan stored in this query row.
     */
    public Loan getLoan() {
        return loan;
    }

    /**
     * Returns the overdue status captured when this row was queried.
     *
     * @return Whether the loan was overdue at query time.
     */
    public boolean isOverdue() {
        return isOverdue;
    }
}
