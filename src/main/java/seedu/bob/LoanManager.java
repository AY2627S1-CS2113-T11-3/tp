//@@author DaynaG3
package seedu.bob;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidLoanIndexException;
import seedu.bob.exception.LoanException;

/**
 * Retains every loan record in creation order and coordinates inventory deductions, corrections, and full returns.
 */
public class LoanManager {

    /** Records loan diagnostics without adding messages to the normal console workflow. */
    private static final Logger LOGGER = Logger.getLogger(LoanManager.class.getName());

    /** Shared available stock; each active loan retains an independent item snapshot. */
    private final Inventory inventory;
    /** Supplies the local date at each operation so overdue state is never cached across days. */
    private final Clock clock;
    /** Each addition is a separate loan, even when the item and borrower match an existing entry. */
    private final ArrayList<LoanRecord> records = new ArrayList<>();
    /** Next permanent ID; consumed only after a successful stock deduction. */
    private long nextLoanId = 1;

    /**
     * Creates an empty loan list backed by the application's inventory.
     *
     * @param inventory Inventory from which new loans deduct stock.
     * @throws NullPointerException If inventory is null.
     */
    public LoanManager(Inventory inventory) {
        this(inventory, Clock.systemDefaultZone());
    }

    /**
     * Creates an empty loan list with an explicit clock for deterministic date and rollover testing.
     *
     * @param inventory Inventory from which new loans deduct stock.
     * @param clock Clock whose timezone defines the local calendar date.
     * @throws NullPointerException If inventory or clock is null.
     */
    public LoanManager(Inventory inventory, Clock clock) {
        this.inventory = Objects.requireNonNull(inventory, "Loan inventory must not be null");
        this.clock = Objects.requireNonNull(clock, "Loan clock must not be null");
        assert hasValidState() : "A new loan manager must have valid dependencies and an empty loan list";
        //@@author DaynaG3
    }

    /** Exports all records, including closed ones, without storing derived overdue flags. */
    List<AppSnapshot.LoanRecordEntry> toSnapshot() {
        return records.stream().map(LoanRecord::toSnapshot).toList();
    }

    /** Returns the persisted next ID independently of active-list positions. */
    long getNextLoanId() {
        return nextLoanId;
    }

    /** Restores all lifecycle states and the counter without replaying lending or return operations. */
    void restoreRecords(List<AppSnapshot.LoanRecordEntry> entries, long savedNextId) throws CommandException {
        if (!records.isEmpty()) {
            throw new IllegalStateException("Loans can only be restored into an empty manager");
        }
        Set<Long> ids = new HashSet<>();
        ArrayList<LoanRecord> restored = new ArrayList<>();
        for (AppSnapshot.LoanRecordEntry entry : entries) {
            LoanRecord record = LoanRecord.restore(entry);
            if (!ids.add(record.loanId()) || record.loanId() >= savedNextId) {
                throw new LoanException("Saved loan IDs must be unique and below the next loan ID.");
            }
            restored.add(record);
        }
        if (savedNextId <= 0) {
            throw new LoanException("The next loan ID must be positive.");
        }
        records.addAll(restored);
        nextLoanId = savedNextId;
        assert hasValidState() : "Restored history and counter must remain valid without changing stock";
    }

    /**
     * Deducts stock and records a validated loan without printing a confirmation.
     * Captures whether the accepted loan is already overdue. Rejected requests do not change state.
     *
     * @param loan Validated loan to record.
     * @return Recorded loan and overdue status at addition.
     * @throws CommandException If the item is missing or its available stock is insufficient.
     * @throws NullPointerException If the loan is null.
     */
    public LoanAdditionResult addLoan(Loan loan) throws CommandException {
        Objects.requireNonNull(loan, "Loan to add must not be null");
        assert hasValidState() && loan.hasValidState() : "Adding a loan requires valid records";
        if (nextLoanId == Long.MAX_VALUE) {
            throw new LoanException("No more loan IDs are available.");
        }
        OffsetDateTime recordedAt = OffsetDateTime.now(clock);
        LoanRecord record = new LoanRecord(nextLoanId, loan, LoanRecord.Status.ACTIVE, recordedAt, null);
        LoanAdditionResult result = new LoanAdditionResult(loan, loan.isOverdue(recordedAt.toLocalDate()));
        int previousCount = records.size();
        int previousStock = inventory.getAvailableQuantity(loan.getName(), loan.getCategory());
        inventory.lendItem(loan.getName(), loan.getCategory(), loan.getQuantity());
        assert inventory.getAvailableQuantity(loan.getName(), loan.getCategory())
                == (long) previousStock - loan.getQuantity() : "Lending must deduct the exact loan quantity";
        records.add(record);
        nextLoanId++;
        assert records.size() == previousCount + 1 && records.get(previousCount).loan() == loan
                : "Lending must append exactly one loan";
        assert hasValidState() : "Loan records must remain valid after addition";
        LOGGER.fine(() -> "Recorded loan: " + loan.getName() + " in category " + loan.getCategory()
                + ", quantity=" + loan.getQuantity() + ", due=" + loan.getDueDate()
                + ", overdue=" + result.isOverdue() + ", active=" + getLoans().size());
        return result;
    }

    /**
     * Voids an incorrect loan while retaining its details and restoring all units to available inventory.
     * Restores stock before closing the record so a failed restoration preserves the active loan.
     *
     * @param userIndex One-based loan position from the latest loan list.
     * @return Original details of the voided loan after successful stock restoration.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    public Loan deleteLoan(int userIndex) throws CommandException {
        Loan loan = closeLoanAndRestoreStock(userIndex, LoanRecord.Status.VOIDED);
        LOGGER.fine(() -> "Deleted incorrect loan: index=" + userIndex + ", item=" + loan.getName()
                + ", category=" + loan.getCategory() + ", quantity=" + loan.getQuantity()
                + ", active=" + getLoans().size());
        return loan;
    }

    /**
     * Records a full return by restoring all units and retaining the closed record in history.
     *
     * @param userIndex One-based loan position from the latest loan list.
     * @return Original details of the fully returned loan after successful stock restoration.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    public Loan returnLoan(int userIndex) throws CommandException {
        Loan loan = closeLoanAndRestoreStock(userIndex, LoanRecord.Status.RETURNED);
        LOGGER.fine(() -> "Returned loan: index=" + userIndex + ", item=" + loan.getName()
                + ", category=" + loan.getCategory() + ", quantity=" + loan.getQuantity()
                + ", active=" + getLoans().size());
        return loan;
    }

    /**
     * Restores stock before closing a record so failed returns and corrections preserve the active loan.
     *
     * @param userIndex One-based position in the full active-loan list, including non-overdue loans.
     * @return Captured loan details, for the operation-specific confirmation.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    private Loan closeLoanAndRestoreStock(int userIndex, LoanRecord.Status status) throws CommandException {
        assert hasValidState() : "Closing a loan requires valid manager state";
        List<LoanRecord> active = records.stream()
                .filter(record -> record.status() == LoanRecord.Status.ACTIVE).toList();
        if (userIndex <= 0 || userIndex > active.size()) {
            LOGGER.fine(() -> "Rejected loan removal: index=" + userIndex + ", active=" + active.size());
            throw new InvalidLoanIndexException(userIndex, active.size());
        }
        LoanRecord selected = active.get(userIndex - 1);
        LoanRecord closed = selected.close(status, OffsetDateTime.now(clock));
        Loan loan = selected.loan();
        int previousStock = inventory.getAvailableQuantity(loan.getName(), loan.getCategory());
        inventory.restoreStock(loan.getName(), loan.getCategory(), loan.getQuantity());
        assert inventory.getAvailableQuantity(loan.getName(), loan.getCategory())
                == (long) previousStock + loan.getQuantity() : "Closure must restore the exact loan quantity";
        records.set(records.indexOf(selected), closed);
        assert getLoans().size() == active.size() - 1 : "Closure must remove exactly one loan from the active view";
        assert hasValidState() : "Loan records must remain valid after closure";
        return loan;
    }

    /**
     * Returns whether dependencies and all lifecycle records satisfy the manager's invariants.
     * Duplicate records and past due dates remain valid.
     */
    private boolean hasValidState() {
        if (inventory == null || clock == null || nextLoanId <= 0) {
            return false;
        }
        Set<Long> ids = new HashSet<>();
        for (LoanRecord record : records) {
            if (record == null || !record.loan().hasValidState() || record.loanId() >= nextLoanId
                    || !ids.add(record.loanId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an immutable snapshot of active loans in their displayed order.
     *
     * @return Active loan records.
     */
    public List<Loan> getLoans() {
        assert hasValidState() : "Loan snapshots require valid manager state";
        return records.stream().filter(record -> record.status() == LoanRecord.Status.ACTIVE)
                .map(LoanRecord::loan).toList();
    }

    /** Returns an immutable view of every active, returned, and voided record in creation order. */
    public List<LoanRecord> getLoanHistory() {
        assert hasValidState() : "History queries require valid records";
        return List.copyOf(records);
    }

    /** Returns returned records only, preserving their original creation order. */
    public List<LoanRecord> getReturnedLoans() {
        return records.stream().filter(record -> record.status() == LoanRecord.Status.RETURNED).toList();
    }

    /**
     * Returns all active loans with their positions and overdue status at query time.
     *
     * @return Immutable rows in active-loan insertion order.
     */
    public List<LoanListEntry> getLoanEntries() {
        return buildLoanEntries(false);
    }

    /**
     * Returns overdue loans with their original positions in the full active-loan list.
     *
     * @return Immutable overdue rows, whose positions may contain gaps.
     */
    public List<LoanListEntry> getOverdueLoanEntries() {
        return buildLoanEntries(true);
    }

    /**
     * Selects loan rows against one current local date without printing or mutating feature state.
     *
     * @param isOverdueOnly Whether to exclude current loans while preserving full-list positions.
     * @return Immutable rows whose overdue flags share the same reference date.
     */
    private List<LoanListEntry> buildLoanEntries(boolean isOverdueOnly) {
        assert hasValidState() : "Listing requires valid loan records";
        LocalDate today = LocalDate.now(clock);
        List<Loan> loans = getLoans();
        LOGGER.fine(() -> "Listing loans: overdueOnly=" + isOverdueOnly + ", active=" + getLoans().size()
                + ", date=" + today);
        List<LoanListEntry> entries = new ArrayList<>();
        for (int i = 0; i < loans.size(); i++) {
            Loan loan = loans.get(i);
            boolean isOverdue = loan.isOverdue(today);
            if (isOverdueOnly && !isOverdue) {
                continue;
            }
            entries.add(new LoanListEntry(i + 1, loan, isOverdue));
        }
        return List.copyOf(entries);
    }
}
