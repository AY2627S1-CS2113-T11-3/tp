//@@author DaynaG3
package seedu.bob;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.LoanException;

/**
 * Immutable bookkeeping entry whose identity and captured loan details survive a return or correction.
 * A missing recording timestamp represents an imported loan whose original recording time is unknown.
 */
public record LoanRecord(long loanId, Loan loan, Status status, OffsetDateTime recordedAt,
        OffsetDateTime closedAt) {

    /** Active records can be closed exactly once as either a genuine return or a voided incorrect entry. */
    public enum Status {
        ACTIVE, RETURNED, VOIDED
    }

    /**
     * Validates identity and lifecycle fields without changing inventory.
     *
     * @throws IllegalArgumentException If the ID or closure fields are inconsistent.
     * @throws NullPointerException If the loan or status is absent.
     */
    public LoanRecord {
        Objects.requireNonNull(loan, "Recorded loan must not be null");
        Objects.requireNonNull(status, "Loan status must not be null");
        if (loanId <= 0) {
            throw new IllegalArgumentException("Loan ID must be positive.");
        }
        if ((status == Status.ACTIVE) != (closedAt == null)) {
            throw new IllegalArgumentException("Only active loans may have no closure timestamp.");
        }
    }

    /** Copies identity, lifecycle metadata, and original loan values into the storage boundary. */
    AppSnapshot.LoanRecordEntry toSnapshot() {
        AppSnapshot.LoanEntry details = new AppSnapshot.LoanEntry(loan.getName(), loan.getCategory(),
                loan.getQuantity(), loan.getBorrowerName(), loan.getBorrowerPhone(), loan.getDueDate().toString());
        return new AppSnapshot.LoanRecordEntry(loanId, details, status.name(),
                recordedAt == null ? null : recordedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                closedAt == null ? null : closedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    /** Restores one validated record without changing inventory or inventing recording timestamps. */
    static LoanRecord restore(AppSnapshot.LoanRecordEntry entry) throws CommandException {
        AppSnapshot.LoanEntry details = Objects.requireNonNull(entry.loan(), "Saved loan details must not be null");
        Loan loan = new Loan(details.name(), details.category(), details.quantity(),
                new Contact(details.borrowerName(), details.borrowerPhone(), null, null),
                SnapshotFormat.toCommandDate(details.dueDate()));
        try {
            return new LoanRecord(entry.loanId(), loan, Status.valueOf(entry.status()),
                    entry.recordedAt() == null ? null : OffsetDateTime.parse(entry.recordedAt()),
                    entry.closedAt() == null ? null : OffsetDateTime.parse(entry.closedAt()));
        } catch (DateTimeException | IllegalArgumentException e) {
            throw new LoanException("Saved loan lifecycle fields are invalid.", e);
        }
    }

    /**
     * Creates a closed replacement while retaining the original identity, details, and recording time.
     *
     * @param terminalStatus RETURNED for a full return, or VOIDED for an incorrect entry.
     * @param timestamp Time the closure is recorded in Bob.
     * @return Closed immutable replacement.
     * @throws IllegalStateException If this record is already closed.
     * @throws IllegalArgumentException If the requested status is ACTIVE.
     * @throws NullPointerException If the closure time or status is absent.
     */
    public LoanRecord close(Status terminalStatus, OffsetDateTime timestamp) {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("A closed loan cannot be closed again.");
        }
        Objects.requireNonNull(terminalStatus, "Closure status must not be null");
        if (terminalStatus == Status.ACTIVE) {
            throw new IllegalArgumentException("A closure must be returned or voided.");
        }
        return new LoanRecord(loanId, loan, terminalStatus, recordedAt,
                Objects.requireNonNull(timestamp, "Closure timestamp must not be null"));
    }
}
